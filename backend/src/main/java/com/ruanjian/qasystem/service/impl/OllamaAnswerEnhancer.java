package com.ruanjian.qasystem.service.impl;

import com.ruanjian.qasystem.common.QaIntent;
import com.ruanjian.qasystem.model.vo.QaFact;
import com.ruanjian.qasystem.service.LlmAnswerEnhancer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;

@Slf4j
@Service
public class OllamaAnswerEnhancer implements LlmAnswerEnhancer {

    @Value("${qa.llm.enabled:false}")
    private boolean enabled;

    @Value("${qa.llm.base-url:http://localhost:11434}")
    private String baseUrl;

    @Value("${qa.llm.model:deepseek-r1:7b}")
    private String model;

    @Value("${qa.llm.timeout-seconds:20}")
    private long timeoutSeconds;

    @Override
    public Optional<String> enhance(String question, QaIntent intent, String keyword, QaFact fact, String baseAnswer) {
        if (!enabled || fact == null || fact.getValue() == null || fact.getValue().isBlank()) {
            return Optional.empty();
        }

        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(timeoutSeconds))
                    .build();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/generate"))
                    .timeout(Duration.ofSeconds(timeoutSeconds))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(buildRequestBody(question, intent, keyword, fact, baseAnswer)))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                log.warn("Ollama 答案增强失败，status={}", response.statusCode());
                return Optional.empty();
            }
            return sanitizeAnswer(extractJsonString(response.body(), "response"));
        } catch (Exception e) {
            log.warn("Ollama 答案增强异常，将回退模板回答，question={}", question, e);
            return Optional.empty();
        }
    }

    private String buildRequestBody(String question, QaIntent intent, String keyword, QaFact fact, String baseAnswer) {
        String prompt = """
                # Role
                你是一名专业的线上博物馆智能讲解助手，名称为“博物馆AI导览员”。

                你的职责：
                1. 回答用户关于馆藏文物、历史背景、朝代信息、艺术风格的问题
                2. 根据用户兴趣推荐展品
                3. 提供导览服务
                4. 用通俗、易理解的方式解释专业知识
                5. 激发用户探索兴趣

                # Knowledge Source
                你必须优先使用系统提供的知识库内容回答。
                知识来源包括：文物信息数据库、展览信息数据库、博物馆知识图谱、馆藏历史资料、系统提供的上下文内容。
                禁止自行编造不存在的信息。
                如果知识库中没有相关信息，只能回复：目前馆藏资料中没有找到相关信息。

                # Response Rules
                回答时遵循：
                1. 面向普通游客时，使用简单语言解释，避免大量专业术语。
                2. 面向学生时，可适当增加背景知识。
                3. 面向专业研究人员时，可以提供更详细信息。
                4. 回答结构应包含：①直接回答问题 ②补充背景知识 ③提供延伸推荐。

                # Recommendation Rules
                当用户询问“还有类似的吗”“推荐看看”“相关文物”时，优先推荐同朝代、同类别、同作者或同主题展品。
                推荐格式：
                【推荐展品】
                1. 名称：
                2. 朝代：
                3. 推荐原因：
                如果上下文没有朝代或推荐原因，不要编造，说明“当前馆藏资料未记录”。

                # Guide Rules
                如果用户要求导览，根据时间、兴趣、热门程度生成路线。
                只有在上下文提供展厅或展品信息时才生成具体路线；资料不足时说明无法生成完整路线。

                # Safety Rules
                禁止编造文物信息、虚构历史事件、输出不确定但伪装成确定的信息。
                若不确定，请明确说明：该信息存在争议。

                # Output Style
                风格自然、富有故事感，像真人讲解员。
                普通问题控制在 100 到 300 字；深度问题控制在 300 到 800 字。
                不要输出思考过程，不要输出 Markdown 代码块。
                回答末尾必须标注数据来源，例如：数据来源：知识图谱。
                如果有详情链接，末尾补充：详情链接：链接地址。

                # Context
                用户问题：%s
                系统识别意图：%s
                命中关键词：%s
                知识库事实：%s
                基础回答：%s
                数据来源：%s
                详情链接：%s

                回答：
                """.formatted(
                question,
                intent.name(),
                keyword,
                fact.getValue(),
                baseAnswer,
                blankToDefault(fact.getSourceName(), "知识图谱"),
                blankToDefault(fact.getSourceUrl(), "无")
        );

        return """
                {"model":"%s","prompt":"%s","stream":false}
                """.formatted(escapeJson(model), escapeJson(prompt));
    }

    private Optional<String> sanitizeAnswer(String answer) {
        if (answer == null || answer.isBlank()) {
            return Optional.empty();
        }

        String cleaned = answer
                .replaceAll("(?s)<think>.*?</think>", "")
                .replace("```", "")
                .trim();
        if (cleaned.isBlank()) {
            return Optional.empty();
        }
        return Optional.of(cleaned);
    }

    private String extractJsonString(String json, String fieldName) {
        String pattern = "\"" + fieldName + "\"";
        int fieldIndex = json.indexOf(pattern);
        if (fieldIndex < 0) {
            return "";
        }
        int colonIndex = json.indexOf(':', fieldIndex + pattern.length());
        int startQuote = json.indexOf('"', colonIndex + 1);
        if (colonIndex < 0 || startQuote < 0) {
            return "";
        }

        StringBuilder value = new StringBuilder();
        boolean escaping = false;
        for (int i = startQuote + 1; i < json.length(); i++) {
            char c = json.charAt(i);
            if (escaping) {
                appendEscaped(value, json, i, c);
                if (c == 'u') {
                    i += 4;
                }
                escaping = false;
                continue;
            }
            if (c == '\\') {
                escaping = true;
                continue;
            }
            if (c == '"') {
                return value.toString();
            }
            value.append(c);
        }
        return "";
    }

    private void appendEscaped(StringBuilder value, String json, int index, char c) {
        switch (c) {
            case 'n' -> value.append('\n');
            case 'r' -> value.append('\r');
            case 't' -> value.append('\t');
            case 'b' -> value.append('\b');
            case 'f' -> value.append('\f');
            case '"', '\\', '/' -> value.append(c);
            case 'u' -> appendUnicode(value, json, index);
            default -> value.append(c);
        }
    }

    private void appendUnicode(StringBuilder value, String json, int index) {
        if (index + 4 >= json.length()) {
            return;
        }
        try {
            int codePoint = Integer.parseInt(json.substring(index + 1, index + 5), 16);
            value.append((char) codePoint);
        } catch (NumberFormatException e) {
            value.append("\\u");
        }
    }

    private String blankToDefault(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value;
    }

    private String escapeJson(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n");
    }
}
