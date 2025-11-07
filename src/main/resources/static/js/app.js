// Core JS for clean index page
// Handles: pagination, fetching, table rendering, mobile view, filters, comparison

let currentPage = 0;
let pageSize = 5;
let sortBy = "field_1";
let sortDir = "desc";
let search = "";
let totalPages = 1;

// ==========================
// INITIALIZATION
// ==========================
document.addEventListener("DOMContentLoaded", () => {
    loadPage();
    initializeMobileScrolling();
    loadFilters();
});

// ==========================
// FETCH PAGE DATA
// ==========================
async function loadPage() {
    showLoading(true);

    const url = `/api/interest-rates/paginated?page=${currentPage}&size=${pageSize}&sortBy=${sortBy}&sortDir=${sortDir}&search=${search}`;
    const res = await fetch(url);
    const data = await res.json();

    totalPages = data.totalPages;

    renderTable(data);
    renderPagination("paginationTop");
    renderPagination("paginationBottom");

    showLoading(false);
}

// ==========================
// TABLE RENDERING
// ==========================
function renderTable(data) {
    const tableContainer = document.getElementById("tableContainer");

    if (!data.content.length) {
        tableContainer.innerHTML = `<div class="empty-state">No results found</div>`;
        return;
    }

    const headers = Object.keys(data.rateFieldValuesMap[data.content[0].id] || {});

    let html = `
        <table class="table table-sm">
            <thead><tr>`;

    headers.forEach(h => {
        html += `<th>${h}</th>`;
    });

    html += `</tr></thead><tbody>`;

    data.content.forEach(rate => {
        html += `<tr>`;
        const row = data.rateFieldValuesMap[rate.id];
        headers.forEach(h => html += `<td>${row[h] || "-"}</td>`);
        html += `</tr>`;
    });

    html += `</tbody></table>`;

    tableContainer.innerHTML = html;
}

// ==========================
// PAGINATION
// ==========================
function renderPagination(containerId) {
    const el = document.getElementById(containerId);
    if (!el) return;

    let pagesHtml = "";
    for (let i = 0; i < totalPages; i++) {
        pagesHtml += `<button class="page-btn ${i === currentPage ? "active" : ""}" onclick="goToPage(${i})">${i + 1}</button>`;
    }

    el.innerHTML = `
        <div class="pagination-controls">
            <button class="page-btn" onclick="goToPage(0)">⏮</button>
            <button class="page-btn" onclick="goToPage(${currentPage - 1})">‹</button>
            ${pagesHtml}
            <button class="page-btn" onclick="goToPage(${currentPage + 1})">›</button>
            <button class="page-btn" onclick="goToPage(${totalPages - 1})">⏭</button>
        </div>`;
}

function goToPage(page) {
    if (page < 0 || page >= totalPages) return;
    currentPage = page;
    loadPage();
}

function changePageSize(size) {
    pageSize = parseInt(size);
    currentPage = 0;
    loadPage();
}

// ==========================
// LOADING
// ==========================
function showLoading(state) {
    document.getElementById("loadingOverlay").style.display = state ? "flex" : "none";
}

// ==========================
// FILTER SYSTEM
// ==========================
async function loadFilters() {
    const res = await fetch("/filters/available");
    const filters = await res.json();

    const filterSection = document.getElementById("filterSection");
    filterSection.innerHTML = filters.map(f => `<div>${f.label}</div>`).join("");
}

// Placeholder for full filter apply logic
function applyFilters() { alert("Filters applied (placeholder)"); }

// ==========================
// MOBILE VIEW TOGGLE
// ==========================
function initializeMobileScrolling() {
    const mobile = window.matchMedia("(max-width: 768px)");
    toggleMobileFunctions(mobile.matches);

    mobile.addEventListener("change", e => toggleMobileFunctions(e.matches));

    document.getElementById("stackedView").onclick = () => enableCardView();
    document.getElementById("scrollView").onclick = () => enableTableView();
}

function toggleMobileFunctions(isMobile) {
    if (!isMobile) enableTableView();
}

function enableCardView() {
    document.getElementById("tableContainer").classList.add("mobile-card-view");
}

function enableTableView() {
    document.getElementById("tableContainer").classList.remove("mobile-card-view");
}

// ==========================
// COMPARISON PLACEHOLDER
// ==========================
function quickStartComparison() {
    alert("Comparison feature placeholder");
}
