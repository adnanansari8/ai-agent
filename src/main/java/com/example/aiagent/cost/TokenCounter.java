package com.example.aiagent.cost;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TokenCounter {

    // Rough estimation: 1 token ≈ 4 characters for English
    // For production, use tiktoken or similar library
    public int estimateTokens(String text) {
        if (text == null || text.isEmpty()) return 0;
        return text.length() / 4;
    }

    // More accurate counting using regex for words
    public int estimateTokensV2(String text) {
        if (text == null || text.isEmpty()) return 0;

        // Split by whitespace and punctuation
        String[] words = text.split("\\s+|[.,;:!?]");
        int count = 0;
        for (String word : words) {
            if (word.length() <= 4) count += 1;
            else if (word.length() <= 8) count += 2;
            else count += 3;
        }
        return count;
    }
}
