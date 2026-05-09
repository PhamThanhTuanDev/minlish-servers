package com.minlish.service.impl;

import com.minlish.dto.VocabularySetDTO;
import com.minlish.entity.User;
import com.minlish.entity.VocabularySet;
import com.minlish.repository.StudyHistoryRepository;
import com.minlish.repository.VocabularyRepository;
import com.minlish.repository.VocabularySetRepository;
import com.minlish.service.VocabularySetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VocabularySetServiceImpl implements VocabularySetService {

    private final VocabularySetRepository vocabularySetRepository;
    private final VocabularyRepository vocabularyRepository;
    private final StudyHistoryRepository studyHistoryRepository;

    @Override
    public VocabularySet createSet(User user, VocabularySetDTO dto) {
        VocabularySet set = new VocabularySet();
        set.setUser(user);
        set.setName(dto.getName());
        set.setDescription(dto.getDescription());
        set.setTags(dto.getTags());
        return vocabularySetRepository.save(set);
    }

    @Override
    public List<VocabularySet> getUserSets(User user) {
        return vocabularySetRepository.findByUser(user);
    }

    @Override
    public VocabularySet getSetById(Long setId, User user) {
        VocabularySet set = vocabularySetRepository.findById(setId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bộ từ vựng không tồn tại"));
        if (!set.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bạn không có quyền truy cập bộ từ vựng này");
        }
        return set;
    }

    @Override
    public VocabularySet updateSet(Long setId, User user, VocabularySetDTO dto) {
        VocabularySet set = getSetById(setId, user);
        set.setName(dto.getName());
        set.setDescription(dto.getDescription());
        set.setTags(dto.getTags());
        return vocabularySetRepository.save(set);
    }

    @Override
    public void deleteSet(Long setId, User user) {
        VocabularySet set = getSetById(setId, user);

        // Fetch all vocabulary ids for the set
        List<Long> allIds = vocabularyRepository.findIdsByVocabularySetId(setId);
        final int CHUNK = 200;
        for (int i = 0; i < allIds.size(); i += CHUNK) {
            int toIndex = Math.min(i + CHUNK, allIds.size());
            List<Long> chunk = allIds.subList(i, toIndex);
            // Delete study history in a separate transaction per chunk
            deleteStudyHistoriesChunk(chunk);
            // Delete vocabularies in a separate transaction per chunk
            deleteVocabulariesChunk(chunk);
        }

        // Finally delete the set
        vocabularySetRepository.delete(set);
    }

    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    protected void deleteStudyHistoriesChunk(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return;
        studyHistoryRepository.deleteByVocabularyIds(ids);
    }

    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    protected void deleteVocabulariesChunk(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return;
        vocabularyRepository.deleteByIdIn(ids);
    }
}
