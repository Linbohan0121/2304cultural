package com.ruanjian.qasystem.service.impl;

import com.ruanjian.qasystem.common.QaIntent;
import com.ruanjian.qasystem.model.vo.LlmParseResult;
import com.ruanjian.qasystem.service.LlmIntentParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class OllamaIntentParser implements LlmIntentParser {

    @Value("${qa.llm.enabled:false}")
    private boolean enabled;

    @Value("${qa.llm.base-url:http://localhost:11434}")
    private String baseUrl;

    @Value("${qa.llm.model:deepseek-r1:7b}")
    private String model;

    @Value("${qa.llm.timeout-seconds:20}")
    private long timeoutSeconds;

    @Override
    public Optional<LlmParseResult> parse(String question) {
        if (!enabled || question == null || question.isBlank()) {
            return Optional.empty();
        }

        try {
            String requestBody = buildRequestBody(question);

            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(timeoutSeconds))
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/generate"))
                    .timeout(Duration.ofSeconds(timeoutSeconds))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                log.warn("Ollama 意图解析失败，status={}", response.statusCode());
                return Optional.empty();
            }

            String rawText = extractJsonStringField(response.body(), "response");
            return parseJsonText(rawText);
        } catch (Exception e) {
            log.warn("Ollama 意图解析异常，将回退规则解析，question={}", question, e);
            return Optional.empty();
        }
    }

    private Optional<LlmParseResult> parseJsonText(String text) {
        String json = extractJsonObject(text);
        if (json.isBlank()) {
            return Optional.empty();
        }

        try {
            String intentText = extractJsonStringField(json, "intent");
            String keyword = extractJsonStringField(json, "keyword").trim();
            if (intentText.isBlank()) {
                intentText = extractJsonStringField(json, ".intent");
            }
            if (keyword.isBlank()) {
                keyword = extractJsonStringField(json, ".keyword").trim();
            }

            QaIntent intent = QaIntent.valueOf(intentText);
            if (intent == QaIntent.UNKNOWN || keyword.isBlank()) {
                return Optional.empty();
            }

            return Optional.of(new LlmParseResult(intent, keyword));
        } catch (Exception e) {
            log.warn("Ollama 返回内容不是合法意图 JSON，text={}", text);
            return Optional.empty();
        }
    }

    private String extractJsonObject(String text) {
        if (text == null) {
            return "";
        }
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start < 0 || end <= start) {
            return "";
        }
        return text.substring(start, end + 1);
    }

    private String buildRequestBody(String question) {
        return """
                {"model":"%s","prompt":"%s","stream":false,"format":"json"}
                """.formatted(escapeJson(model), escapeJson(buildPrompt(question))).trim();
    }

    private String extractJsonStringField(String json, String fieldName) {
        if (json == null || json.isBlank()) {
            return "";
        }

        Pattern pattern = Pattern.compile("\"" + Pattern.quote(fieldName) + "\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\"");
        Matcher matcher = pattern.matcher(json);
        if (!matcher.find()) {
            return "";
        }
        return unescapeJson(matcher.group(1));
    }

    private String escapeJson(String value) {
        return value == null ? "" : value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n")
                .replace("\t", "\\t");
    }

    private String unescapeJson(String value) {
        StringBuilder builder = new StringBuilder();
        boolean escaping = false;
        for (int i = 0; i < value.length(); i++) {
            char current = value.charAt(i);
            if (!escaping) {
                if (current == '\\') {
                    escaping = true;
                } else {
                    builder.append(current);
                }
                continue;
            }

            switch (current) {
                case '"' -> builder.append('"');
                case '\\' -> builder.append('\\');
                case '/' -> builder.append('/');
                case 'b' -> builder.append('\b');
                case 'f' -> builder.append('\f');
                case 'n' -> builder.append('\n');
                case 'r' -> builder.append('\r');
                case 't' -> builder.append('\t');
                case 'u' -> {
                    if (i + 4 < value.length()) {
                        String hex = value.substring(i + 1, i + 5);
                        try {
                            builder.append((char) Integer.parseInt(hex, 16));
                            i += 4;
                        } catch (NumberFormatException e) {
                            builder.append("\\u").append(hex);
                            i += 4;
                        }
                    } else {
                        builder.append("\\u");
                    }
                }
                default -> builder.append(current);
            }
            escaping = false;
        }
        return builder.toString();
    }

    private String buildPrompt(String question) {
        return """
                你是文物知识问答系统的意图解析器。
                只输出一个 JSON 对象，不要解释，不要 Markdown，不要输出思考过程。

                JSON 格式：
                {"intent":"ARTIFACT_MUSEUM","keyword":"青花瓷瓶"}

                可选 intent：
                ARTIFACT_MUSEUM：询问文物收藏在哪个博物馆
                ARTIFACT_DYNASTY：询问文物朝代/年代
                ARTIFACT_MATERIAL：询问文物材质
                ARTIFACT_TYPE：询问文物类型
                ARTIFACT_DESCRIPTION：询问文物介绍
                ARTIFACT_ARTIST：询问文物作者
                ARTIST_BIOGRAPHY：询问作者生平
                ARTIST_WORKS：询问作者有哪些作品
                DYNASTY_ARTIFACTS：询问某朝代有哪些文物
                RELATED_ARTIFACTS：询问相关或相似文物
                MUSEUM_ARTIFACT_COUNT：询问某博物馆收藏多少件文物
                MUSEUM_ARTIFACTS：询问某博物馆收藏哪些文物
                TYPE_ARTIFACTS：询问某类型有哪些文物
                MATERIAL_ARTIFACTS：询问某材质有哪些文物
                UNKNOWN：无法识别

                规则：
                1. keyword 只保留核心实体名，例如文物名、作者名、朝代名、博物馆名、类型名或材质名。
                2. 如果 intent 是 UNKNOWN，keyword 为空字符串。
                3. 不要编造实体。

                用户问题：%s
                """.formatted(question);
    }
}
