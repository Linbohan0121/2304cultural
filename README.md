# 海外藏中国文物知识问答系统

本项目是一个面向海外藏中国文物的知识管理与问答服务平台。当前版本包含后端问答服务、Neo4j 知识图谱查询、MySQL 问答历史、用户端页面和管理端页面。

## 模块结构

- `backend`：Spring Boot 后端服务，提供问答、历史、反馈、热门问题和管理接口。
- `web-frontend`：用户端问答页面。
- `admin-frontend`：后台管理页面。
- `database/mysql`：MySQL 相关脚本目录。
- `database/neo4j`：Neo4j 相关脚本目录。
- `docs`：项目文档。

## 环境要求

- JDK 17
- Maven
- Node.js 18 或以上
- MySQL 8
- Neo4j Desktop 或 Neo4j Server

## 数据库配置

后端默认配置文件：

```properties
spring.neo4j.uri=bolt://localhost:7687
spring.neo4j.authentication.username=neo4j
spring.neo4j.authentication.password=12345678

spring.datasource.url=jdbc:mysql://localhost:3306/relic_qa?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
spring.datasource.username=root
spring.datasource.password=123456
```

如本机密码不同，修改 `backend/src/main/resources/application.properties`。

## 启动顺序

1. 启动 MySQL，并确认存在 `relic_qa` 数据库和 `qa_message` 表。
2. 启动 Neo4j，并确认 Bolt 端口为 `7687`。
3. 启动后端：

```powershell
cd D:\desktop\2304\2304Main\backend
mvn spring-boot:run
```

4. 启动用户端：

```powershell
cd D:\desktop\2304\2304Main\web-frontend
npm run dev
```

默认访问：`http://localhost:5173`。如果端口被占用，可使用：

```powershell
$env:PORT=5175; node server.mjs
```

5. 启动管理端：

```powershell
cd D:\desktop\2304\2304Main\admin-frontend
npm run dev
```

默认访问：`http://localhost:5176`。

## Mock 图谱模式

如果暂时没有 Neo4j 数据，可以使用 mock 图谱服务启动后端：

```powershell
cd D:\desktop\2304\2304Main\backend
mvn spring-boot:run "-Dspring-boot.run.profiles=mock-graph"
```

## Ollama 大模型意图解析

系统支持使用本地 Ollama 增强意图识别和实体抽取。默认关闭，不影响规则解析。

确认 Ollama 可用：

```powershell
ollama list
Invoke-RestMethod http://localhost:11434/api/tags
```

当前推荐模型：

```text
deepseek-r1:7b
```

开启方式：修改 `backend/src/main/resources/application.properties`：

```properties
qa.llm.enabled=true
qa.llm.provider=ollama
qa.llm.base-url=http://localhost:11434
qa.llm.model=deepseek-r1:7b
qa.llm.timeout-seconds=20
```

开启后流程为：

```text
用户问题 -> Ollama 解析 intent/keyword -> Neo4j 查询事实 -> 后端生成答案
```

如果 Ollama 未启动、超时或返回格式不合法，系统会自动回退到原有规则解析。

## 测试

后端测试：

```powershell
cd D:\desktop\2304\2304Main\backend
mvn test
```

前端语法检查：

```powershell
cd D:\desktop\2304\2304Main\web-frontend
node --check app.js
node --check server.mjs

cd D:\desktop\2304\2304Main\admin-frontend
node --check app.js
node --check server.mjs
```

## 常用页面

- 用户端：`http://localhost:5175`
- 管理端：`http://localhost:5176`
- 后端接口根路径：`http://localhost:8080`
