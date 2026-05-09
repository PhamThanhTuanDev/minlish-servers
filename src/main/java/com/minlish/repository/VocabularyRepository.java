package com.minlish.repository;

import com.minlish.entity.Vocabulary;
import com.minlish.entity.VocabularySet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
/**
 * Created by: IntelliJ IDEA
 * User      : dutv
 * Date      : 29/03/2026
 * Time      : 14:42
 * File      : VocabularyRepository
 */
public interface VocabularyRepository extends JpaRepository<Vocabulary, Long> {
    List<Vocabulary> findByVocabularySet(VocabularySet vocabularySet);
    List<Vocabulary> findByVocabularySetId(Long vocabularySetId);
    List<Vocabulary> findByIdIn(List<Long> ids);
    void deleteByVocabularySetId(Long vocabularySetId);

    @Query("SELECT v.id FROM Vocabulary v WHERE v.vocabularySet.id = :setId")
    List<Long> findIdsByVocabularySetId(@Param("setId") Long setId);

    @Modifying
    @Query("DELETE FROM Vocabulary v WHERE v.id IN :ids")
    void deleteByIdIn(@Param("ids") List<Long> ids);
}
