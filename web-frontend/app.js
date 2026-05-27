const state = {
  currentMessageId: null,
  lastAnswer: null
};

const quickQuestions = [
  "青花瓷瓶收藏在哪个博物馆？",
  "青花瓷瓶属于哪个朝代？",
  "青花瓷瓶是什么材质？",
  "张大千还有哪些作品？",
  "大英博物馆收藏了多少件中国文物？",
  "瓷器有哪些文物？",
  "陶瓷有哪些文物？",
  "推荐一些和青花瓷瓶相关的文物"
];

const elements = {
  userId: document.querySelector("#userId"),
  questionInput: document.querySelector("#questionInput"),
  askButton: document.querySelector("#askButton"),
  connectionState: document.querySelector("#connectionState"),
  quickQuestions: document.querySelector("#quickQuestions"),
  answerTitle: document.querySelector("#answerTitle"),
  answerText: document.querySelector("#answerText"),
  qaStatus: document.querySelector("#qaStatus"),
  intentValue: document.querySelector("#intentValue"),
  questionTypeValue: document.querySelector("#questionTypeValue"),
  keywordValue: document.querySelector("#keywordValue"),
  sourceValue: document.querySelector("#sourceValue"),
  parseSourceValue: document.querySelector("#parseSourceValue"),
  answerSourceValue: document.querySelector("#answerSourceValue"),
  helpfulButton: document.querySelector("#helpfulButton"),
  inaccurateButton: document.querySelector("#inaccurateButton"),
  feedbackState: document.querySelector("#feedbackState"),
  historyList: document.querySelector("#historyList"),
  hotList: document.querySelector("#hotList"),
  refreshHistoryButton: document.querySelector("#refreshHistoryButton"),
  refreshHotButton: document.querySelector("#refreshHotButton")
};

init();

function init() {
  renderQuickQuestions();
  bindEvents();
  refreshHistory();
  refreshHot();
}

function bindEvents() {
  elements.askButton.addEventListener("click", askQuestion);
  elements.questionInput.addEventListener("keydown", (event) => {
    if (event.key === "Enter" && (event.ctrlKey || event.metaKey)) {
      askQuestion();
    }
  });
  elements.refreshHistoryButton.addEventListener("click", refreshHistory);
  elements.refreshHotButton.addEventListener("click", refreshHot);
  elements.helpfulButton.addEventListener("click", () => submitFeedback("helpful"));
  elements.inaccurateButton.addEventListener("click", () => submitFeedback("inaccurate"));
}

function renderQuickQuestions() {
  elements.quickQuestions.innerHTML = "";
  quickQuestions.forEach((question) => {
    const button = document.createElement("button");
    button.className = "quick-chip";
    button.type = "button";
    button.textContent = question;
    button.addEventListener("click", () => {
      elements.questionInput.value = question;
      askQuestion();
    });
    elements.quickQuestions.appendChild(button);
  });
}

async function askQuestion() {
  const userId = Number(elements.userId.value || 1);
  const question = elements.questionInput.value.trim();

  if (!question) {
    setConnectionState("请输入问题", "error");
    elements.questionInput.focus();
    return;
  }

  setLoading(true);
  setConnectionState("正在查询知识图谱...", "loading");
  elements.feedbackState.textContent = "";

  try {
    const result = await requestJson("/api/qa/ask", {
      method: "POST",
      body: JSON.stringify({ userId, question })
    });

    if (result.code !== 200) {
      throw new Error(result.message || "请求失败");
    }

    state.lastAnswer = result.data;
    renderAnswer(result.data);
    await refreshHistory();
    syncCurrentMessageFromHistory(result.data);
    setConnectionState("查询完成", "success");
  } catch (error) {
    renderError(error);
    setConnectionState(error.message, "error");
  } finally {
    setLoading(false);
  }
}

async function refreshHistory() {
  const userId = Number(elements.userId.value || 1);
  try {
    const result = await requestJson(`/api/qa/history?userId=${encodeURIComponent(userId)}&page=1&pageSize=20`);
    if (result.code !== 200) {
      throw new Error(result.message || "历史记录加载失败");
    }
    renderHistory(result.data?.records || []);
    return result.data?.records || [];
  } catch (error) {
    elements.historyList.innerHTML = `<div class="empty-state">${escapeHtml(error.message)}</div>`;
    return [];
  }
}

async function refreshHot() {
  try {
    const result = await requestJson("/api/qa/hot?limit=8");
    if (result.code !== 200) {
      throw new Error(result.message || "热门问题加载失败");
    }
    renderHot(result.data || []);
  } catch (error) {
    elements.hotList.innerHTML = `<div class="empty-state">${escapeHtml(error.message)}</div>`;
  }
}

async function submitFeedback(feedbackType) {
  if (!state.currentMessageId) {
    elements.feedbackState.textContent = "请先从历史记录中选择一条问答";
    return;
  }

  elements.feedbackState.textContent = "正在提交反馈...";
  try {
    const result = await requestJson(`/api/qa/messages/${state.currentMessageId}/feedback`, {
      method: "PATCH",
      body: JSON.stringify({ feedbackType })
    });
    if (result.code !== 200) {
      throw new Error(result.message || "反馈提交失败");
    }
    elements.feedbackState.textContent = feedbackType === "helpful" ? "已标记为有用" : "已标记为不准确";
    await refreshHistory();
  } catch (error) {
    elements.feedbackState.textContent = error.message;
  }
}

async function requestJson(url, options = {}) {
  const response = await fetch(url, {
    headers: {
      "Content-Type": "application/json",
      ...(options.headers || {})
    },
    ...options
  });
  return response.json();
}

function renderAnswer(data) {
  elements.answerTitle.textContent = data.question || "问答结果";
  elements.answerText.textContent = data.answer || "暂无回答";
  elements.intentValue.textContent = data.intent || "-";
  elements.questionTypeValue.textContent = data.questionType || "-";
  elements.keywordValue.textContent = data.keyword || "-";
  elements.sourceValue.textContent = data.sourceName || "-";
  elements.parseSourceValue.textContent = formatParseSource(data.parseSource);
  elements.answerSourceValue.textContent = formatAnswerSource(data.answerSource);
  renderStatus(data.qaStatus);
  setFeedbackEnabled(Boolean(data.matched));
}

function renderError(error) {
  elements.answerTitle.textContent = "请求失败";
  elements.answerText.textContent = error.message || "无法连接服务";
  elements.intentValue.textContent = "-";
  elements.questionTypeValue.textContent = "-";
  elements.keywordValue.textContent = "-";
  elements.sourceValue.textContent = "-";
  elements.parseSourceValue.textContent = "-";
  elements.answerSourceValue.textContent = "-";
  renderStatus("error");
  setFeedbackEnabled(false);
}

function renderStatus(status) {
  elements.qaStatus.textContent = status || "unknown";
  elements.qaStatus.className = "pill";
  if (status === "success") {
    elements.qaStatus.classList.add("success");
  } else if (status === "error") {
    elements.qaStatus.classList.add("error");
  } else if (status === "no_data") {
    elements.qaStatus.classList.add("no-data");
  } else {
    elements.qaStatus.classList.add("neutral");
  }
}

function renderHistory(records) {
  if (!records.length) {
    elements.historyList.innerHTML = `<div class="empty-state">暂无历史记录</div>`;
    return;
  }

  elements.historyList.innerHTML = "";
  records.forEach((record) => {
    const button = document.createElement("button");
    button.className = "record-item";
    button.type = "button";
    button.innerHTML = `
      <p class="record-question">${escapeHtml(record.question || "")}</p>
      <p class="record-answer">${escapeHtml(record.answer || "")}</p>
      <p class="record-meta">${escapeHtml(record.intent || "-")} · ${escapeHtml(record.qaStatus || "-")}</p>
    `;
    button.addEventListener("click", () => selectHistoryRecord(record));
    elements.historyList.appendChild(button);
  });
}

function renderHot(records) {
  if (!records.length) {
    elements.hotList.innerHTML = `<div class="empty-state">暂无热门问题</div>`;
    return;
  }

  elements.hotList.innerHTML = "";
  records.forEach((record) => {
    const question = record.question || record.keyword || "";
    const button = document.createElement("button");
    button.className = "record-item";
    button.type = "button";
    button.innerHTML = `
      <p class="record-question">${escapeHtml(question)}</p>
      <p class="record-meta">提问 ${escapeHtml(String(record.count ?? record.questionCount ?? "-"))} 次</p>
    `;
    button.addEventListener("click", () => {
      elements.questionInput.value = question;
      askQuestion();
    });
    elements.hotList.appendChild(button);
  });
}

function selectHistoryRecord(record) {
  state.currentMessageId = record.id;
  elements.questionInput.value = record.question || "";
  renderAnswer({
    question: record.question,
    answer: record.answer,
    matched: record.matched,
    qaStatus: record.qaStatus,
    intent: record.intent,
    questionType: record.questionType,
    keyword: record.keywordText,
    sourceName: record.sourceName,
    parseSource: record.parseSource,
    answerSource: record.answerSource
  });
  setFeedbackEnabled(Boolean(record.id));
  elements.feedbackState.textContent = record.feedbackType ? `已反馈：${record.feedbackType}` : "";
}

function syncCurrentMessageFromHistory(answer) {
  const first = elements.historyList.querySelector(".record-item");
  if (!first || !answer) {
    state.currentMessageId = null;
    return;
  }

  refreshHistory().then((records) => {
    const matched = records.find((record) =>
      record.question === answer.question && record.answer === answer.answer
    );
    state.currentMessageId = matched?.id || null;
    setFeedbackEnabled(Boolean(state.currentMessageId && answer.matched));
  });
}

function setFeedbackEnabled(enabled) {
  elements.helpfulButton.disabled = !enabled;
  elements.inaccurateButton.disabled = !enabled;
}

function setLoading(loading) {
  elements.askButton.disabled = loading;
  elements.askButton.textContent = loading ? "查询中..." : "发送问题";
}

function setConnectionState(message, type) {
  elements.connectionState.textContent = message;
  elements.connectionState.style.color = type === "error"
    ? "var(--danger)"
    : type === "success"
      ? "var(--success)"
      : "var(--muted)";
}

function formatParseSource(value) {
  if (value === "llm") {
    return "Ollama";
  }
  if (value === "rule") {
    return "规则";
  }
  return "-";
}

function formatAnswerSource(value) {
  if (value === "llm") {
    return "Ollama";
  }
  if (value === "template") {
    return "模板";
  }
  return "-";
}

function escapeHtml(value) {
  return String(value)
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#039;");
}
