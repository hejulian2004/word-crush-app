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
import com.wordcrush.server.module.learning.api.LearningWordAdminFacade;
import com.wordcrush.server.module.user.api.UserAdminFacade;
import com.wordcrush.server.security.api.AuthenticatedUserContext;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserAdminFacade userAdminFacade;
    private final LearningWordAdminFacade learningWordAdminFacade;

    public AdminMeResponse me() {
        UserAdminFacade.AdminUserView admin = userAdminFacade.currentAdmin(operatorId());
        return new AdminMeResponse(admin.id(), admin.username(), admin.role());
    }

    public AdminOverviewResponse overview() {
        Long operatorId = operatorId();
        UserAdminFacade.AdminSummary users = userAdminFacade.summary(operatorId);
        LearningWordAdminFacade.WordSummary words = learningWordAdminFacade.summary(operatorId);
        return new AdminOverviewResponse(
                users.totalUsers(), users.activeUsers(), words.totalWords(), words.activeWords());
    }

    public AdminPageResponse<AdminUserResponse> listUsers(
            String query,
            Integer status,
            Integer page,
            Integer size
    ) {
        UserAdminFacade.AdminUserPage result = userAdminFacade.listUsers(
                operatorId(), query, status, page, size);
        return new AdminPageResponse<>(
                result.items().stream().map(this::toUserResponse).toList(),
                result.page(), result.size(), result.total());
    }

    public AdminUserResponse updateUserStatus(Long userId, UpdateUserStatusRequest request) {
        return toUserResponse(userAdminFacade.updateUserStatus(operatorId(), userId, request.status()));
    }

    public void resetPassword(Long userId, ResetPasswordRequest request) {
        userAdminFacade.resetPassword(operatorId(), userId, request.password());
    }

    public AdminPageResponse<AdminWordResponse> listWords(
            String query,
            Integer status,
            Integer page,
            Integer size
    ) {
        LearningWordAdminFacade.WordPage result = learningWordAdminFacade.listWords(
                operatorId(), query, status, page, size);
        return new AdminPageResponse<>(
                result.items().stream().map(this::toWordResponse).toList(),
                result.page(), result.size(), result.total());
    }

    public AdminWordResponse createWord(CreateWordRequest request) {
        return toWordResponse(learningWordAdminFacade.createWord(
                operatorId(), request.id(), request.english(), request.pronunciation(), request.chinese()));
    }

    public AdminWordResponse updateWord(Integer wordId, UpdateWordRequest request) {
        return toWordResponse(learningWordAdminFacade.updateWord(
                operatorId(), wordId, request.english(), request.pronunciation(), request.chinese(), request.status()));
    }

    public WordImportResponse importWords(MultipartFile file, boolean replace) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ApiCode.BAD_REQUEST, "csv file must not be empty");
        }
        CsvParseResult parsed = parseCsv(file);
        List<LearningWordAdminFacade.WordImportRow> rows = parsed.words().stream()
                .map(row -> new LearningWordAdminFacade.WordImportRow(
                        row.id(), row.english(), row.pronunciation(), row.chinese()))
                .toList();
        LearningWordAdminFacade.WordImportResult result = learningWordAdminFacade.importWords(
                operatorId(), rows, replace, parsed.skipped());
        return new WordImportResponse(
                result.added(), result.updated(), result.disabled(), result.total(), result.skipped());
    }

    public byte[] exportWords() {
        return learningWordAdminFacade.exportWords(operatorId());
    }

    private Long operatorId() {
        return AuthenticatedUserContext.requireCurrentSession().userId();
    }

    private AdminUserResponse toUserResponse(UserAdminFacade.AdminUserView user) {
        return new AdminUserResponse(
                user.id(), user.username(), user.role(), user.status(), user.createdAt(), user.updatedAt());
    }

    private AdminWordResponse toWordResponse(LearningWordAdminFacade.WordView word) {
        return new AdminWordResponse(
                word.id(), word.english(), word.pronunciation(), word.chinese(),
                word.contentVersion(), word.status(), word.createdAt(), word.updatedAt());
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

    private record CsvWord(Integer id, String english, String pronunciation, String chinese) {
    }

    private record CsvParseResult(List<CsvWord> words, int skipped) {
    }
}
