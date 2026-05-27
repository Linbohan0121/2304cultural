# 系统演示与验收脚本

## 一、启动检查

1. MySQL 已启动，数据库为 `relic_qa`。
2. Neo4j 已启动，Bolt 地址为 `bolt://localhost:7687`。
3. 后端启动：

```powershell
cd D:\desktop\2304\2304Main\backend
mvn spring-boot:run
```

4. 用户端启动：

```powershell
cd D:\desktop\2304\2304Main\web-frontend
$env:PORT=5175; node server.mjs
```

5. 管理端启动：

```powershell
cd D:\desktop\2304\2304Main\admin-frontend
npm run dev
```

## 二、用户端演示

打开用户端：

```text
http://localhost:5175
```

按顺序测试以下问题：

```json
{"userId":1,"question":"青花瓷瓶收藏在哪个博物馆？"}
{"userId":1,"question":"青花瓷瓶属于哪个朝代？"}
{"userId":1,"question":"青花瓷瓶是什么材质？"}
{"userId":1,"question":"青花瓷瓶属于什么类型？"}
{"userId":1,"question":"青花瓷瓶的作者是谁？"}
{"userId":1,"question":"张大千还有哪些作品？"}
{"userId":1,"question":"张大千的生平经历是怎样的？"}
{"userId":1,"question":"唐代有哪些代表性文物？"}
{"userId":1,"question":"大英博物馆收藏了哪些文物？"}
{"userId":1,"question":"大英博物馆收藏了多少件中国文物？"}
{"userId":1,"question":"瓷器有哪些文物？"}
{"userId":1,"question":"陶瓷有哪些文物？"}
{"userId":1,"question":"推荐一些和青花瓷瓶相关的文物"}
```

重点观察：

- 页面是否返回自然语言答案。
- `intent` 是否符合问题类型。
- `keyword` 是否抽取正确。
- `qaStatus` 是否为 `success`。
- 来源字段是否展示知识图谱来源。

## 三、反馈演示

1. 在用户端提交一个问题。
2. 点击“有用”或“不准确”。
3. 刷新管理端，观察反馈统计和记录反馈字段是否变化。

## 四、管理端演示

打开管理端：

```text
http://localhost:5176
```

演示内容：

1. 查看总问答数、命中数、未命中数、异常数、反馈数。
2. 使用状态筛选：
   - `success`
   - `no_data`
   - `error`
3. 使用反馈筛选：
   - `helpful`
   - `inaccurate`
4. 使用关键词搜索，例如：
   - `青花瓷瓶`
   - `张大千`
   - `大英博物馆`
5. 点击记录行，查看完整问题、答案、关键词、来源和反馈备注。

## 五、数据库验证

MySQL 中查看问答记录：

```sql
SELECT id, user_id, question, answer, intent, question_type, keyword_text, matched, qa_status, feedback_type, created_at
FROM qa_message
ORDER BY created_at DESC
LIMIT 20;
```

Neo4j 中查看图谱关系：

```cypher
MATCH (a:Artifact)-[r]->(n)
RETURN a.name_zh AS artifact, type(r) AS relation, labels(n) AS targetLabels, coalesce(n.name_zh, n.name) AS target
ORDER BY artifact, relation;
```

## 六、答辩讲解顺序

1. 介绍系统目标：面向海外藏中国文物的知识管理与问答服务。
2. 介绍技术架构：Spring Boot + MySQL + Neo4j + 原生前端。
3. 展示 Neo4j 图谱数据和实体关系。
4. 在用户端提问，展示知识图谱问答。
5. 展示问答历史和反馈。
6. 打开管理端，展示统计、筛选和记录详情。
7. 说明测试覆盖：意图识别、实体抽取、答案生成、接口返回、管理接口。
8. 说明后续扩展：接入大模型做意图识别、实体抽取和答案润色。
