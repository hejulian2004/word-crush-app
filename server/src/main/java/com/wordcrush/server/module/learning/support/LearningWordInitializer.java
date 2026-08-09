package com.wordcrush.server.module.learning.support;

import com.wordcrush.server.module.learning.entity.LearningWord;
import com.wordcrush.server.module.learning.repository.LearningWordRepository;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LearningWordInitializer implements ApplicationRunner {

    private final LearningWordRepository learningWordRepository;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        List<LearningWord> words = parse(new ClassPathResource("wordbook.csv").getInputStream());
        if (words.isEmpty()) {
            return;
        }

        Set<Integer> existingIds = new HashSet<>(learningWordRepository.findAllById(
                words.stream().map(LearningWord::getId).toList()
        ).stream().map(LearningWord::getId).toList());
        List<LearningWord> missingWords = words.stream()
                .filter(word -> !existingIds.contains(word.getId()))
                .toList();
        if (!missingWords.isEmpty()) {
            learningWordRepository.saveAll(missingWords);
            log.info("Learning word catalog initialized: {} missing words", missingWords.size());
        }
    }

    private List<LearningWord> parse(InputStream inputStream) throws IOException {
        String content;
        try (inputStream) {
            content = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }

        List<LearningWord> words = new ArrayList<>();
        for (List<String> row : parseRows(content)) {
            if (row.size() < 4) {
                continue;
            }
            Integer id = parseInteger(row.get(0));
            if (id == null || row.get(1).isBlank()) {
                continue;
            }
            LearningWord word = new LearningWord();
            word.setId(id);
            word.setEnglish(row.get(1).trim());
            word.setPronunciation(row.get(2).trim());
            word.setChinese(row.get(3).trim());
            word.setContentVersion(1L);
            word.setStatus(1);
            words.add(word);
        }
        return words;
    }

    private Integer parseInteger(String value) {
        try {
            return Integer.valueOf(value.replace("\uFEFF", "").trim());
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private List<List<String>> parseRows(String content) {
        List<List<String>> rows = new ArrayList<>();
        List<String> row = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean inQuotes = false;
        for (int index = 0; index < content.length(); index++) {
            char current = content.charAt(index);
            if (current == '"') {
                if (inQuotes && index + 1 < content.length() && content.charAt(index + 1) == '"') {
                    field.append('"');
                    index++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (current == ',' && !inQuotes) {
                row.add(field.toString());
                field.setLength(0);
            } else if (current == '\n' && !inQuotes) {
                row.add(field.toString());
                field.setLength(0);
                if (!row.isEmpty() && row.stream().anyMatch(value -> !value.isBlank())) {
                    rows.add(row);
                }
                row = new ArrayList<>();
            } else if (current != '\r' || inQuotes) {
                field.append(current);
            }
        }
        if (field.length() > 0 || !row.isEmpty()) {
            row.add(field.toString());
            if (row.stream().anyMatch(value -> !value.isBlank())) {
                rows.add(row);
            }
        }
        return rows;
    }
}
