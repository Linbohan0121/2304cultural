const state = {
  page: 1,
  pageSize: 20,
  total: 0,
  records: []
};

const elements = {
  totalCount: document.querySelector("#totalCount"),
  matchedCount: document.querySelector("#matchedCount"),
  noDataCount: document.querySelector("#noDataCount"),
  errorCount: document.querySelector("#errorCount"),
  helpfulCount: document.querySelector("#helpfulCount"),
  inaccurateCount: document.querySelector("#inaccurateCount"),
  keywordInput: document.querySelector("#keywordInput"),
  statusSelect: document.querySelector("#statusSelect"),
  feedbackSelect: document.querySelector("#feedbackSelect"),
  searchButton: document.querySelector("#searchButton"),
  messageTable: document.querySelector("#messageTable"),
  prevButton: document.querySelector("#prevButton"),
  nextButton: document.querySelector("#nextButton"),
  pageInfo: document.querySelector("#pageInfo"),
  detailTitle: document.querySelector("#detailTitle"),
  detailKeyword: document.querySelector("#detailKeyword"),
  detailQuestionType: document.querySelector("#detailQuestionType"),
  detailSource: document.querySelector("#detailSource"),
  detailRemark: document.querySelector("#detailRemark"),
  detailAnswer: document.querySelector("#detailAnswer")
};

init();

function init() {
  bindEvents();
  refreshAll();
}

function bindEvents() {
  elements.searchButton.addEventListener("click", () => {
    state.page = 1;
    loadMessages();
  });
  elements.keywordInput.addEventListener("keydown", (event) => {
    if (event.key === "Enter") {
      state.page = 1;
      loadMessages();
    }
  });
  elements.prevButton.addEventListener("click", () => {
    if (state.page > 1) {
      state.page -= 1;
      loadMessages();
    }
  });
  elements.nextButton.addEventListener("click", () => {
    if (state.page * state.pageSize < state.total) {
      state.page += 1;
      loadMessages();
    }
  });
}

async function refreshAll() {
  await Promise.all([loadStatistics(), loadMessages()]);
}

async function loadStatistics() {
  try {
    const result = await requestJson("/api/admin/qa/statistics");
    if (result.code !== 200) {
      throw new Error(result.message || "统计加载失败");
    }

    const data = result.data || {};
    elements.totalCount.textContent = data.totalCount ?? 0;
    elements.matchedCount.textContent = data.matchedCount ?? 0;
    elements.noDataCount.textContent = data.noDataCount ?? 0;
    elements.errorCount.textContent = data.errorCount ?? 0;
    elements.helpfulCount.textContent = data.helpfulCount ?? 0;
    elements.inaccurateCount.textContent = data.inaccurateCount ?? 0;
  } catch (error) {
    elements.totalCount.textContent = "!";
    elements.matchedCount.textContent = "!";
    elements.noDataCount.textContent = "!";
    elements.errorCount.textContent = "!";
    elements.helpfulCount.textContent = "!";
    elements.inaccurateCount.textContent = "!";
  }
}

async function loadMessages() {
  const params = new URLSearchParams({
    page: String(state.page),
    pageSize: String(state.pageSize)
  });

  const keyword = elements.keywordInput.value.trim();
  const qaStatus = elements.statusSelect.value;
  const feedbackType = elements.feedbackSelect.value;

  if (keyword) params.set("keyword", keyword);
  if (qaStatus) params.set("qaStatus", qaStatus);
  if (feedbackType) params.set("feedbackType", feedbackType);

  try {
    const result = await requestJson(`/api/admin/qa/messages?${params.toString()}`);
    if (result.code !== 200) {
      throw new Error(result.message || "记录加载失败");
    }

    state.records = result.data?.records || [];
    state.total = result.data?.total || 0;
    state.page = result.data?.page || state.page;
    state.pageSize = result.data?.pageSize || state.pageSize;

    renderTable();
    renderPager();
  } catch (error) {
    elements.messageTable.innerHTML = `<tr><td class="empty-row" colspan="6">${escapeHtml(error.message)}</td></tr>`;
  }
}

async function requestJson(url) {
  const response = await fetch(url);
  return response.json();
}

function renderTable() {
  if (!state.records.length) {
    elements.messageTable.innerHTML = `<tr><td class="empty-row" colspan="6">暂无记录</td></tr>`;
    return;
  }

  elements.messageTable.innerHTML = "";
  state.records.forEach((record) => {
    const row = document.createElement("tr");
    row.innerHTML = `
      <td><div class="cell-clip">${escapeHtml(record.question || "")}</div></td>
      <td><div class="cell-clip">${escapeHtml(record.answer || "")}</div></td>
      <td><span class="tag">${escapeHtml(record.intent || "-")}</span></td>
      <td><span class="tag ${statusClass(record.qaStatus)}">${escapeHtml(record.qaStatus || "-")}</span></td>
      <td>${escapeHtml(record.feedbackType || "-")}</td>
      <td>${escapeHtml(formatTime(record.createdAt))}</td>
    `;
    row.addEventListener("click", () => renderDetail(record));
    elements.messageTable.appendChild(row);
  });
}

function renderPager() {
  const totalPages = Math.max(1, Math.ceil(state.total / state.pageSize));
  elements.pageInfo.textContent = `第 ${state.page} / ${totalPages} 页，共 ${state.total} 条`;
  elements.prevButton.disabled = state.page <= 1;
  elements.nextButton.disabled = state.page >= totalPages;
}

function renderDetail(record) {
  elements.detailTitle.textContent = record.question || "记录详情";
  elements.detailKeyword.textContent = record.keyword || "-";
  elements.detailQuestionType.textContent = record.questionType || "-";
  elements.detailSource.textContent = record.sourceName || "-";
  elements.detailRemark.textContent = record.feedbackRemark || "-";
  elements.detailAnswer.textContent = record.answer || "暂无回答";
}

function statusClass(status) {
  if (status === "success") return "success";
  if (status === "no_data") return "no-data";
  if (status === "error") return "error";
  return "";
}

function formatTime(value) {
  if (!value) return "-";
  return String(value).replace("T", " ").slice(0, 19);
}

function escapeHtml(value) {
  return String(value)
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#039;");
}
