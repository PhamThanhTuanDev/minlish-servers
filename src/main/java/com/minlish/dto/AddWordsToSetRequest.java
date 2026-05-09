package com.minlish.dto;

import lombok.Data;
import java.util.List;

/**
 * Request DTO để bulk add từ vào bộ từ mới
 */
@Data
public class AddWordsToSetRequest {
    private List<Long> wordIds;
}
