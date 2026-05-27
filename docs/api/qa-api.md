# 问答系统接口说明

统一返回格式：

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

## 1. 提问

`POST /api/qa/ask`

请求体：

```json
{
  "userId": 1,
  "question": "青花瓷瓶收藏在哪个博物馆？"
}
```

成功返回示例：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "question": "青花瓷瓶收藏在哪个博物馆？",
    "answer": "“青花瓷瓶”现收藏于大英博物馆。",
    "matched": true,
    "qaStatus": "success",
    "intent": "ARTIFACT_MUSEUM",
    "questionType": "attribute",
    "sourceName": "大英博物馆",
    "sourceUrl": "https://example.com",
    "sources": [
      {
        "type": "knowledge_graph",
        "id": null,
        "name": "大英博物馆",
        "url": "https://example.com"
      }
    ],
    "keyword": "青花瓷瓶"
  }
}
```

`qaStatus` 含义：

- `success`：知识图谱查询成功并命中数据。
- `no_data`：问题类型可识别，但知识图谱暂无数据。
- `error`：知识图谱或后端查询异常。

## 2. 用户问答历史

`GET /api/qa/history?userId=1&page=1&pageSize=20`

返回分页字段：

- `records`：记录列表
- `total`：总数
- `page`：当前页
- `pageSize`：每页数量

## 3. 问答详情

`GET /api/qa/messages/{id}`

示例：

```http
GET /api/qa/messages/1
```

## 4. 反馈

`PATCH /api/qa/messages/{id}/feedback`

请求体：

```json
{
  "feedbackType": "helpful",
  "feedbackRemark": "回答准确"
}
```

`feedbackType` 建议值：

- `helpful`
- `inaccurate`

## 5. 热门问题

`GET /api/qa/hot?limit=10`

返回示例：

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "question": "青花瓷瓶收藏在哪个博物馆？",
      "count": 5
    }
  ]
}
```

## 6. 管理端问答记录

`GET /api/admin/qa/messages`

查询参数：

- `page`：页码，默认 `1`
- `pageSize`：每页数量，默认 `20`
- `qaStatus`：可选，`success`、`no_data`、`error`
- `feedbackType`：可选，`helpful`、`inaccurate`
- `keyword`：可选，按问题、回答、关键词模糊查询

示例：

```http
GET /api/admin/qa/messages?page=1&pageSize=20&qaStatus=success&feedbackType=helpful&keyword=青花瓷瓶
```

## 7. 管理端统计

`GET /api/admin/qa/statistics`

返回示例：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "totalCount": 10,
    "matchedCount": 7,
    "noDataCount": 2,
    "errorCount": 1,
    "helpfulCount": 5,
    "inaccurateCount": 3
  }
}
```
