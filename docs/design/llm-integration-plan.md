# Ollama 大模型接入方案

## 接入目标

当前系统已经具备规则意图识别、实体抽取、Neo4j 查询和答案生成能力。大模型接入目标不是替代知识图谱，而是增强自然语言理解能力。

大模型负责：

- 判断用户问题意图 `intent`
- 抽取核心实体 `keyword`

大模型不负责：

- 直接生成事实答案
- 编造文物、作者、博物馆或关系

## 当前架构

```text
用户问题
  -> OllamaIntentParser
  -> 成功：返回 intent + keyword
  -> 失败：回退 IntentResolver + EntityExtractor
  -> GraphQueryService 查询 Neo4j
  -> AnswerBuilder 生成答案
  -> 保存问答历史
```

## 配置项

位于 `backend/src/main/resources/application.properties`：

```properties
qa.llm.enabled=false
qa.llm.provider=ollama
qa.llm.base-url=http://localhost:11434
qa.llm.model=deepseek-r1:7b
qa.llm.timeout-seconds=20
```

默认 `qa.llm.enabled=false`，保证未安装或未启动 Ollama 时系统仍能运行。

## Ollama 调用接口

接口：

```http
POST http://localhost:11434/api/generate
```

请求体：

```json
{
  "model": "deepseek-r1:7b",
  "prompt": "请解析用户问题...",
  "stream": false,
  "format": "json"
}
```

模型应返回：

```json
{
  "intent": "ARTIFACT_MUSEUM",
  "keyword": "青花瓷瓶"
}
```

后端会从 Ollama 的 `response` 字段中提取 JSON，并校验：

- `intent` 必须属于 `QaIntent`
- `keyword` 不能为空
- `UNKNOWN` 会视为解析失败并回退规则

## 异常回退

以下情况都会回退到规则解析：

- `qa.llm.enabled=false`
- Ollama 服务未启动
- 请求超时
- HTTP 状态码非 2xx
- 返回内容不是合法 JSON
- `intent` 不在枚举中
- `keyword` 为空

## 测试策略

自动化测试不真实调用 Ollama。测试中通过 mock `LlmIntentParser` 验证：

- LLM 成功时优先使用 LLM 的 `intent/keyword`
- LLM 返回空时回退原规则
- 默认关闭时不影响原有问答流程

## 后续扩展

下一阶段可以增加答案润色，但必须遵守：

```text
只允许基于 Neo4j 查询到的事实润色，不允许模型自由补充事实。
```

可扩展流程：

```text
Neo4j 查询结果 -> 大模型润色 -> 返回更自然的答案
```
