package com.wordcrush.server.module.admin.service;

import com.wordcrush.api.ApiCode;
import com.wordcrush.server.common.exception.BusinessException;
import com.wordcrush.server.module.admin.dto.CreateWordRequest;
import com.wordcrush.server.module.admin.dto.ResetPasswordRequest;
import com.wordcrush.server.module.admin.dto.UpdateUserStatusRequest;
import com.wordcrush.server.module.admin.dto.UpdateWordRequest;
import com.wordcrush.server.module.admin.response.AdminMeResponse;
import com.wordcrush.server.module.admin.response.AdminOverviewResponse;
import com.wordcrush.server.module.admin.response.AdminPageResponse;
import com.wordcrush.server.module.admin.response.AdminUserResponse;
import com.wordcrush.server.module.admin.response.AdminWordResponse;
import com.wordcrush.server.module.admin.response.WordImportResponse;
import com.wordcrush.server.module.learning.entity.LearningWord;
import com.wordcrush.server.module.learning.repository.LearningWordRepository;
import com.wordcrush.server.module.user.account.entity.UserAccount;
import com.wordcrush.server.module.user.account.repository.UserAccountRepository;
import com.wordcrush.server.security.AuthenticatedUserContext;
import com.wordcrush.server.security.TokenSession;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class AdminService {

    private static final int ACTIVE_STATUS = 1;

    private final UserAccountRepository userAccountRepository;
    private final LearningWordRepository learningWordRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public AdminMeResponse me() {
        UserAccount admin = requireAdmin();
        return new AdminMeResponse(admin.getId(), admin.getUsername(), admin.getRole());
    }

    @Transactional(readOnly = true)
    public AdminOverviewResponse overview() {
        requireAdmin();
        List<UserAccount> users = userAccountRepository.findAll();
        List<LearningWord> words = learningWordRepository.findAll();
        return new AdminOverviewResponse(
                users.size(),
                users.stream().filter(this::isActive).count(),
                words.size(),
                words.stream().filter(this::isActive).count()
        );
    }

    @Transactional(readOnly = true)
    public AdminPageResponse<AdminUserResponse> listUsers(String query, Integer status, Integer page, Integer size) {
        requireAdmin();
        String normalizedQuery = normalize(query);
        List<AdminUserResponse> filtered = userAccountRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"))
                .stream()
                .filter(user -> normalizedQuery.isBlank()
                        || user.getUsername().toLowerCase().contains(normalizedQuery))
                .filter(user -> status == null || status.equals(user.getStatus()))
                .map(this::toUserResponse)
                .toList();
        return page(filtered, page, size);
    }

    @Transactional
    public AdminUserResponse updateUserStatus(Long userId, UpdateUserStatusRequest request) {
        UserAccount admin = requireAdmin();
        UserAccount user = findUser(userId);
        int nextStatus = normalizeStatus(request.status());
        if (admin.getId().equals(user.getId()) && nextStatus == 0) {
            throw new BusinessException(ApiCode.BAD_REQUEST, "cannot disable the current admin account");
        }
        if (nextStatus == 0 && UserAccount.ROLE_ADMIN.equals(user.getRole())
                && activeAdminCount() <= 1) {
            throw new BusinessException(ApiCode.BAD_REQUEST, "at least one active admin is required");
        }
        user.setStatus(nextStatus);
        return toUserResponse(userAccountRepository.save(user));
    }

    @Transactional
    public void resetPassword(Long userId, ResetPasswordRequest request) {
        requireAdmin();
        UserAccount user = findUser(userId);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        userAccountRepository.save(user);
    }

    @Transactional(readOnly = true)
    public AdminPageResponse<AdminWordResponse> listWords(String query, Integer status, Integer page, Integer size) {
        requireAdmin();
        String normalizedQuery = normalize(query);
        List<AdminWordResponse> filtered = learningWordRepository.findAll(Sort.by(Sort.Direction.ASC, "id"))
                .stream()
                .filter(word -> normalizedQuery.isBlank()
                        || word.getEnglish().toLowerCase().contains(normalizedQuery)
                        || word.getChinese().toLowerCase().contains(normalizedQuery)
                        || word.getPronunciation().toLowerCase().contains(normalizedQuery))
                .filter(word -> status == null || status.equals(word.getStatus()))
                .map(this::toWordResponse)
                .toList();
        return page(filtered, page, size);
    }

    @Transactional
    public AdminWordResponse createWord(CreateWordRequest request) {
        requireAdmin();
        if (request.id() <= 0 || learningWordRepository.existsById(request.id())) {
            throw new BusinessException(ApiCode.CONFLICT, "word id already exists or is invalid");
        }
        LearningWord word = new LearningWord();
        word.setId(request.id());
        word.setEnglish(request.english().trim());
        word.setPronunciation(request.pronunciation().trim());
        word.setChinese(request.chinese().trim());
        word.setContentVersion(1L);
        word.setStatus(ACTIVE_STATUS);
        return toWordResponse(learningWordRepository.save(word));
    }

    @Transactional
    public AdminWordResponse updateWord(Integer wordId, UpdateWordRequest request) {
        requireAdmin();
        LearningWord word = findWord(wordId);
        int nextStatus = normalizeStatus(request.status());
        String english = request.english().trim();
        String pronunciation = request.pronunciation().trim();
        String chinese = request.chinese().trim();
        boolean contentChanged = !english.equals(word.getEnglish())
                || !pronunciation.equals(word.getPronunciation())
                || !chinese.equals(word.getChinese());
        word.setEnglish(english);
        word.setPronunciation(pronunciation);
        word.setChinese(chinese);
        word.setStatus(nextStatus);
        if (contentChanged) {
            word.setContentVersion((word.getContentVersion() == null ? 0L : word.getContentVersion()) + 1);
        }
        return toWordResponse(learningWordRepository.save(word));
    }

    @Transactional
    public WordImportResponse importWords(MultipartFile file, boolean replace) {
        requireAdmin();
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ApiCode.BAD_REQUEST, "csv file must not be empty");
        }
        CsvParseResult parsed = parseCsv(file);
        List<CsvWord> incoming = parsed.words();
        Map<Integer, LearningWord> existing = learningWordRepository.findAll().stream()
                .collect(Collectors.toMap(LearningWord::getId, word -> word));
        Set<Integer> incomingIds = incoming.stream().map(CsvWord::id).collect(Collectors.toSet());
        int added = 0;
        int updated = 0;
        int disabled = 0;

        for (CsvWord row : incoming) {
            LearningWord word = existing.get(row.id());
            if (word == null) {
                word = new LearningWord();
                word.setId(row.id());
                word.setContentVersion(1L);
                added++;
            } else if (!row.english().equals(word.getEnglish())
                    || !row.pronunciation().equals(word.getPronunciation())
                    || !row.chinese().equals(word.getChinese())
                    || !Integer.valueOf(ACTIVE_STATUS).equals(word.getStatus())) {
                word.setContentVersion((word.getContentVersion() == null ? 0L : word.getContentVersion()) + 1);
                updated++;
            }
            word.setEnglish(row.english());
            word.setPronunciation(row.pronunciation());
            word.setChinese(row.chinese());
            word.setStatus(ACTIVE_STATUS);
            existing.put(row.id(), word);
        }

        if (replace) {
            for (LearningWord word : existing.values()) {
                if (!incomingIds.contains(word.getId()) && isActive(word)) {
                    word.setStatus(0);
                    disabled++;
                }
            }
        }
        learningWordRepository.saveAll(existing.values());
        return new WordImportResponse(added, updated, disabled, incoming.size(), parsed.skipped());
    }

    @Transactional(readOnly = true)
    public byte[] exportWords() {
        requireAdmin();
        StringBuilder csv = new StringBuilder("id,english,pronunciation,chinese\n");
        learningWordRepository.findAll(Sort.by(Sort.Direction.ASC, "id")).stream()
                .filter(this::isActive)
                .forEach(word -> csv.append(word.getId())
                        .append(',').append(csvField(word.getEnglish()))
                        .append(',').append(csvField(word.getPronunciation()))
                        .append(',').append(csvField(word.getChinese()))
                        .append('\n'));
        return ("\uFEFF" + csv).getBytes(StandardCharsets.UTF_8);
    }

    private CsvParseResult parseCsv(MultipartFile file) {
        String content;
        try {
            content = new String(file.getBytes(), StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new BusinessException(ApiCode.BAD_REQUEST, "unable to read csv file");
        }
        List<String> errors = new ArrayList<>();
        List<CsvWord> words = new ArrayList<>();
        Set<Integer> ids = new HashSet<>();
        int skipped = 0;
        List<List<String>> rows = parseRows(content);
        for (int index = 0; index < rows.size(); index++) {
            List<String> row = rows.get(index);
            int line = index + 1;
            if (isHeaderRow(row)) {
                skipped++;
                continue;
            }
            if (row.size() < 4) {
                errors.add("line " + line + ": expected 4 columns");
                continue;
            }
            Integer id = parseInteger(row.get(0));
            String english = row.get(1).trim();
            String pronunciation = row.get(2).trim();
            String chinese = row.get(3).trim();
            if (id == null || id <= 0 || english.isBlank() || pronunciation.isBlank() || chinese.isBlank()) {
                errors.add("line " + line + ": id, english, pronunciation and chinese are required");
                continue;
            }
            if (english.length() > 128 || pronunciation.length() > 255 || chinese.length() > 1024) {
                errors.add("line " + line + ": one or more fields exceed the length limit");
                continue;
            }
            if (!ids.add(id)) {
                errors.add("line " + line + ": duplicate id " + id);
                continue;
            }
            words.add(new CsvWord(id, english, pronunciation, chinese));
        }
        if (!errors.isEmpty()) {
            String message = errors.stream().limit(5).collect(Collectors.joining("; "));
            if (errors.size() > 5) {
                message += "; ...";
            }
            throw new BusinessException(ApiCode.BAD_REQUEST, message);
        }
        if (words.isEmpty()) {
            throw new BusinessException(ApiCode.BAD_REQUEST, "csv file contains no words");
        }
        return new CsvParseResult(words, skipped);
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
                if (row.stream().anyMatch(value -> !value.isBlank())) {
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

    private UserAccount requireAdmin() {
        TokenSession session = AuthenticatedUserContext.requireCurrentSession();
        UserAccount user = userAccountRepository.findById(session.userId())
                .orElseThrow(() -> new BusinessException(ApiCode.UNAUTHORIZED, "invalid token"));
        if (!isActive(user) || !UserAccount.ROLE_ADMIN.equals(user.getRole())) {
            throw new BusinessException(ApiCode.FORBIDDEN, "admin access required");
        }
        return user;
    }

    private UserAccount findUser(Long userId) {
        return userAccountRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ApiCode.NOT_FOUND, "user not found"));
    }

    private LearningWord findWord(Integer wordId) {
        return learningWordRepository.findById(wordId)
                .orElseThrow(() -> new BusinessException(ApiCode.NOT_FOUND, "word not found"));
    }

    private long activeAdminCount() {
        return userAccountRepository.findAll().stream()
                .filter(user -> UserAccount.ROLE_ADMIN.equals(user.getRole()) && isActive(user))
                .count();
    }

    private int normalizeStatus(Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException(ApiCode.BAD_REQUEST, "status must be 0 or 1");
        }
        return status;
    }

    private boolean isActive(UserAccount user) {
        return Integer.valueOf(ACTIVE_STATUS).equals(user.getStatus());
    }

    private boolean isActive(LearningWord word) {
        return Integer.valueOf(ACTIVE_STATUS).equals(word.getStatus());
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }

    private AdminUserResponse toUserResponse(UserAccount user) {
        return new AdminUserResponse(
                user.getId(), user.getUsername(), user.getRole(), user.getStatus(),
                user.getCreatedAt(), user.getUpdatedAt());
    }

    private AdminWordResponse toWordResponse(LearningWord word) {
        return new AdminWordResponse(
                word.getId(), word.getEnglish(), word.getPronunciation(), word.getChinese(),
                word.getContentVersion(), word.getStatus(), word.getCreatedAt(), word.getUpdatedAt());
    }

    private <T> AdminPageResponse<T> page(List<T> items, Integer page, Integer size) {
        int normalizedPage = Math.max(page == null ? 0 : page, 0);
        int normalizedSize = Math.min(Math.max(size == null ? 20 : size, 1), 100);
        int from = Math.min(normalizedPage * normalizedSize, items.size());
        int to = Math.min(from + normalizedSize, items.size());
        return new AdminPageResponse<>(items.subList(from, to), normalizedPage, normalizedSize, items.size());
    }

    private Integer parseInteger(String value) {
        try {
            return Integer.valueOf(value.replace("\uFEFF", "").trim());
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private boolean isInteger(String value) {
        return parseInteger(value) != null;
    }

    private boolean isHeaderRow(List<String> row) {
        if (row.isEmpty()) {
            return true;
        }
        String first = row.get(0).replace("\uFEFF", "").trim().toLowerCase();
        String second = row.size() > 1 ? row.get(1).trim().toLowerCase() : "";
        return first.equals("id")
                || first.equals("序号")
                || first.equals("编号")
                || (second.equals("单词") && !isInteger(first));
    }

    private String csvField(String value) {
        if (value == null) {
            return "";
        }
        String escaped = value.replace("\"", "\"\"");
        return escaped.contains(",") || escaped.contains("\n") || escaped.contains("\r")
                ? "\"" + escaped + "\""
                : escaped;
    }

    private record CsvWord(Integer id, String english, String pronunciation, String chinese) {
    }

    private record CsvParseResult(List<CsvWord> words, int skipped) {
    }
}
