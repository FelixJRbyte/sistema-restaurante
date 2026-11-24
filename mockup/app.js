(() => {
  "use strict";

  const ADMIN_PIN = "1234";
  const VAT_RATE = 0.19; // RN-003

  const ROLE_NAMES = {
    ADMIN: "Administrator",
    WAITER: "Waiter",
    CASHIER: "Cashier",
    KITCHEN: "Kitchen Staff"
  };

  const ORDER_FLOW = ["PENDING", "IN_PREPARATION", "READY", "DELIVERED", "INVOICED"];

  const STATUS_TRANSITIONS = {
    PENDING: ["IN_PREPARATION", "CANCELLED"],
    IN_PREPARATION: ["READY", "CANCELLED"],
    READY: ["DELIVERED", "CANCELLED"],
    DELIVERED: ["CANCELLED"],
    INVOICED: [],
    CANCELLED: []
  };

  // ==================== SEED DATA (aligned with database/restaurante_db.sql) ====================
  function createInitialState() {
    return {
      session: { role: "ADMIN", name: "Carlos Admin", loggedIn: false },
      currentView: "dashboard",
      detailOrderId: null,
      detailInvoiceId: null,
      filters: {
        tableStatus: "ALL",
        tableMinCap: 0,
        menuCategory: "ALL",
        menuSearch: "",
        orderStatus: "ALL",
        orderTab: "LIST", // LIST | KDS
        invoiceStatus: "ALL",
        customerSearch: "",
        reportOrderStatus: "IN_PREPARATION"
      },
      categories: [
        { id: 1, name: "Starters" },
        { id: 2, name: "Soups" },
        { id: 3, name: "Main Courses" },
        { id: 4, name: "Pasta & Rice" },
        { id: 5, name: "Beverages" },
        { id: 6, name: "Desserts" }
      ],
      tables: [
        { id: 1, number: 1, capacity: 2, status: "FREE", location: "Window" },
        { id: 2, number: 2, capacity: 2, status: "FREE", location: "Window" },
        { id: 3, number: 3, capacity: 4, status: "OCCUPIED", location: "Center" },
        { id: 4, number: 4, capacity: 4, status: "OCCUPIED", location: "Center" },
        { id: 5, number: 5, capacity: 4, status: "OCCUPIED", location: "Center" },
        { id: 6, number: 6, capacity: 6, status: "OCCUPIED", location: "Terrace" },
        { id: 7, number: 7, capacity: 6, status: "RESERVED", location: "Terrace" },
        { id: 8, number: 8, capacity: 8, status: "OCCUPIED", location: "VIP Room" },
        { id: 9, number: 9, capacity: 4, status: "FREE", location: "Bar" },
        { id: 10, number: 10, capacity: 2, status: "FREE", location: "Bar" },
        { id: 11, number: 11, capacity: 10, status: "FREE", location: "Private Salon" },
        { id: 12, number: 12, capacity: 4, status: "MAINTENANCE", location: "Terrace" }
      ],
      dishes: [
        { id: 1, categoryId: 1, name: "Classic Ceviche", description: "Fresh white fish cured in lime juice", price: 28000, prepTime: 15, available: true },
        { id: 2, categoryId: 1, name: "Beef Empanadas", description: "4 crispy turnovers with chili dip", price: 16000, prepTime: 10, available: true },
        { id: 3, categoryId: 1, name: "Garlic Shrimp", description: "Sauteed shrimp in olive oil and garlic", price: 34000, prepTime: 15, available: true },
        { id: 4, categoryId: 1, name: "Burrata Salad", description: "Fresh burrata, tomato, arugula & balsamic", price: 26000, prepTime: 10, available: true },
        { id: 5, categoryId: 2, name: "Ajiaco Santafereño", description: "Traditional three-potato chicken soup", price: 29000, prepTime: 20, available: true },
        { id: 6, categoryId: 2, name: "Sancocho de Gallina", description: "Hearty broth with plantain and cassava", price: 32000, prepTime: 25, available: true },
        { id: 7, categoryId: 3, name: "Grilled Ribeye Steak", description: "350g prime cut with chimichurri & fries", price: 54000, prepTime: 25, available: true },
        { id: 8, categoryId: 3, name: "Bandeja Paisa", description: "Beans, rice, pork belly, chorizo & avocado", price: 42000, prepTime: 25, available: true },
        { id: 9, categoryId: 3, name: "Grilled Salmon", description: "Atlantic salmon with lemon butter & veggies", price: 48000, prepTime: 20, available: true },
        { id: 10, categoryId: 3, name: "Baby Beef", description: "Tender beef loin with baked potato", price: 46000, prepTime: 22, available: true },
        { id: 11, categoryId: 4, name: "Seafood Rice", description: "Creamy rice with shrimp, calamari & mussels", price: 45000, prepTime: 25, available: true },
        { id: 12, categoryId: 4, name: "Fettuccine Alfredo", description: "Pasta in parmesan cream sauce with chicken", price: 31000, prepTime: 18, available: true },
        { id: 13, categoryId: 5, name: "Coconut Lemonade", description: "Refreshing blended coconut & lime drink", price: 11000, prepTime: 5, available: true },
        { id: 14, categoryId: 5, name: "Natural Passion Fruit Juice", description: "Fresh maracuya juice in water or milk", price: 9000, prepTime: 5, available: true },
        { id: 15, categoryId: 5, name: "Craft Beer", description: "330ml local golden ale", price: 14000, prepTime: 2, available: true },
        { id: 16, categoryId: 6, name: "Tres Leches Cake", description: "Sponge cake soaked in three milks", price: 15000, prepTime: 5, available: true },
        { id: 17, categoryId: 6, name: "Chocolate Volcano", description: "Warm fondant with vanilla ice cream", price: 18000, prepTime: 12, available: false }
      ],
      customers: [
        { id: 1, name: "Juan Pérez", phone: "3104567890", email: "juan.perez@email.com", address: "Calle 10 # 5-20, Neiva", active: true },
        { id: 2, name: "María Gómez", phone: "3159876543", email: "maria.gomez@email.com", address: "Carrera 7 # 14-35, Neiva", active: true },
        { id: 3, name: "Carlos Rodríguez", phone: "3201122334", email: "carlos.rod@email.com", address: "Av. Circunvalar # 8-12", active: true },
        { id: 4, name: "Laura Martínez", phone: "3005544332", email: "laura.m@email.com", address: "Calle 21 # 3-44", active: true },
        { id: 5, name: "Andrés López", phone: "3127788990", email: "andres.lopez@email.com", address: "Carrera 15 # 22-10", active: true }
      ],
      orders: [
        {
          id: 1, tableId: 1, customerId: 1, createdAt: "2026-04-06 12:15", status: "INVOICED",
          notes: "No onions in ceviche",
          items: [
            { dishId: 1, quantity: 1, unitPrice: 28000 },
            { dishId: 7, quantity: 1, unitPrice: 54000 },
            { dishId: 13, quantity: 2, unitPrice: 11000 }
          ]
        },
        {
          id: 2, tableId: 3, customerId: 2, createdAt: "2026-04-06 12:40", status: "INVOICED",
          notes: "Split bill requested",
          items: [
            { dishId: 8, quantity: 1, unitPrice: 42000 },
            { dishId: 15, quantity: 1, unitPrice: 14000 }
          ]
        },
        {
          id: 3, tableId: 4, customerId: 3, createdAt: "2026-04-06 13:05", status: "DELIVERED",
          notes: "Medium-rare steak",
          items: [
            { dishId: 7, quantity: 1, unitPrice: 54000 },
            { dishId: 9, quantity: 1, unitPrice: 48000 },
            { dishId: 13, quantity: 2, unitPrice: 11000 }
          ]
        },
        {
          id: 4, tableId: 5, customerId: 4, createdAt: "2026-04-06 13:20", status: "READY",
          notes: "Extra avocado",
          items: [
            { dishId: 5, quantity: 2, unitPrice: 29000 },
            { dishId: 14, quantity: 2, unitPrice: 9000 }
          ]
        },
        {
          id: 5, tableId: 6, customerId: 5, createdAt: "2026-04-06 13:35", status: "IN_PREPARATION",
          notes: "Serve starters first",
          items: [
            { dishId: 2, quantity: 2, unitPrice: 16000 },
            { dishId: 11, quantity: 2, unitPrice: 45000 },
            { dishId: 16, quantity: 2, unitPrice: 15000 }
          ]
        },
        {
          id: 6, tableId: 8, customerId: null, createdAt: "2026-04-06 13:45", status: "PENDING",
          notes: "VIP celebration table",
          items: [
            { dishId: 3, quantity: 2, unitPrice: 34000 },
            { dishId: 10, quantity: 2, unitPrice: 46000 }
          ]
        }
      ],
      invoices: [
        {
          id: 1, orderId: 1, number: "INV-20260406-001", issueDate: "2026-04-06 13:00",
          subtotal: 104000, taxes: 19760, total: 123760, paymentMethod: "CASH",
          paymentStatus: "PAID", notes: "Paid in cash at table",
          payments: [
            { id: 1, amount: 123760, method: "CASH", reference: "CASH-001", date: "2026-04-06 13:00" }
          ]
        },
        {
          id: 2, orderId: 2, number: "INV-20260406-002", issueDate: "2026-04-06 13:25",
          subtotal: 56000, taxes: 10640, total: 66640, paymentMethod: "MIXED",
          paymentStatus: "PARTIAL", notes: "First partial payment received",
          payments: [
            { id: 2, amount: 30000, method: "CREDIT_CARD", reference: "AUTH-88412", date: "2026-04-06 13:27" }
          ]
        }
      ],
      nextIds: { table: 13, dish: 18, customer: 6, order: 7, invoice: 3, payment: 3 }
    };
  }

  let state = createInitialState();

  // ==================== HELPERS ====================
  const $ = (sel) => document.querySelector(sel);

  function esc(val) {
    return String(val ?? "")
      .replace(/&/g, "&amp;")
      .replace(/</g, "&lt;")
      .replace(/>/g, "&gt;")
      .replace(/"/g, "&quot;")
      .replace(/'/g, "&#39;");
  }

  function money(n) {
    return "$" + Number(n || 0).toLocaleString("en-US", { minimumFractionDigits: 2, maximumFractionDigits: 2 });
  }

  function nowStamp() {
    const d = new Date();
    const pad = (x) => String(x).padStart(2, "0");
    return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`;
  }

  function getCategoryName(catId) {
    const c = state.categories.find((x) => x.id === catId);
    return c ? c.name : "N/A";
  }

  function getTable(tableId) {
    return state.tables.find((t) => t.id === tableId);
  }

  function getCustomer(custId) {
    return state.customers.find((c) => c.id === custId);
  }

  function getDish(dishId) {
    return state.dishes.find((d) => d.id === dishId);
  }

  function calcOrderTotal(order) {
    return order.items.reduce((sum, it) => sum + it.quantity * it.unitPrice, 0);
  }

  function calcInvoicePaid(inv) {
    return inv.payments.reduce((sum, p) => sum + p.amount, 0);
  }

  function hasActiveOrdersOrUnpaidInvoices(tableId) {
    const activeOrder = state.orders.some(
      (o) => o.tableId === tableId && ["PENDING", "IN_PREPARATION", "READY", "DELIVERED"].includes(o.status)
    );
    const unpaidInv = state.invoices.some((inv) => {
      const ord = state.orders.find((o) => o.id === inv.orderId);
      return ord && ord.tableId === tableId && ["PENDING", "PARTIAL"].includes(inv.paymentStatus);
    });
    return activeOrder || unpaidInv;
  }

  function tryReleaseTable(tableId) {
    const tbl = getTable(tableId);
    if (!tbl) return false;
    if (!hasActiveOrdersOrUnpaidInvoices(tableId) && tbl.status === "OCCUPIED") {
      tbl.status = "FREE";
      return true;
    }
    return false;
  }

  function badgeHtml(status) {
    const map = {
      FREE: "badge-success",
      PAID: "badge-success",
      DELIVERED: "badge-success",
      READY: "badge-info",
      INVOICED: "badge-info",
      IN_PREPARATION: "badge-warning",
      RESERVED: "badge-warning",
      PARTIAL: "badge-warning",
      PENDING: "badge-warning",
      OCCUPIED: "badge-danger",
      CANCELLED: "badge-danger",
      VOIDED: "badge-danger",
      MAINTENANCE: "badge-neutral"
    };
    return `<span class="badge ${map[status] || "badge-neutral"}">${esc(status)}</span>`;
  }

  // ==================== TOASTS & MODALS ====================
  function showToast(message, type = "info") {
    const stack = $("#toastStack");
    const el = document.createElement("div");
    el.className = `toast toast-${type}`;
    el.textContent = message;
    stack.appendChild(el);
    setTimeout(() => {
      el.remove();
    }, 3600);
  }

  function openModal({ title, bodyHtml, footerHtml, large = false }) {
    const box = $("#modalBox");
    box.classList.toggle("modal-lg", large);
    $("#modalTitle").textContent = title;
    $("#modalBody").innerHTML = bodyHtml;
    $("#modalFooter").innerHTML = footerHtml;
    $("#modalBackdrop").classList.remove("hidden");
  }

  function closeModal() {
    $("#modalBackdrop").classList.add("hidden");
  }

  function openConfirm({ title, message, confirmLabel = "Confirm", danger = true, onConfirm }) {
    openModal({
      title,
      bodyHtml: `<p style="font-size:0.95rem;">${esc(message)}</p>`,
      footerHtml: `
        <button class="btn btn-secondary" id="confirmCancelBtn">Cancel</button>
        <button class="btn ${danger ? "btn-danger" : "btn-primary"}" id="confirmOkBtn">${esc(confirmLabel)}</button>
      `
    });
    $("#confirmCancelBtn").onclick = closeModal;
    $("#confirmOkBtn").onclick = () => {
      closeModal();
      onConfirm();
    };
  }

  function openAdminPinDialog(actionDescription, onAuthorized) {
    openModal({
      title: "Administrator Authorization Required (RN-002)",
      bodyHtml: `
        <p style="margin-bottom:12px; font-size:0.9rem;">${esc(actionDescription)}</p>
        <div class="form-field">
          <label for="pinInput">Administrator PIN</label>
          <input id="pinInput" class="input" type="password" placeholder="Enter PIN (default: 1234)" autocomplete="off" />
        </div>
        <div id="pinError" class="form-error hidden"></div>
      `,
      footerHtml: `
        <button class="btn btn-secondary" id="pinCancelBtn">Cancel</button>
        <button class="btn btn-danger" id="pinVerifyBtn">Authorize &amp; Execute</button>
      `
    });
    $("#pinCancelBtn").onclick = closeModal;
    $("#pinVerifyBtn").onclick = () => {
      const val = $("#pinInput").value.trim();
      if (val !== ADMIN_PIN) {
        const err = $("#pinError");
        err.textContent = "Authorization denied: invalid Administrator PIN (RN-002).";
        err.classList.remove("hidden");
        showToast("Invalid Administrator PIN", "error");
        return;
      }
      closeModal();
      onAuthorized();
    };
    setTimeout(() => $("#pinInput")?.focus(), 30);
  }

  // ==================== LOGIN / SESSION ====================
  let selectedRole = "ADMIN";

  function bindLoginEvents() {
    $("#roleGrid").addEventListener("click", (e) => {
      const btn = e.target.closest(".role-option");
      if (!btn) return;
      selectedRole = btn.dataset.role;
      document.querySelectorAll(".role-option").forEach((b) => b.classList.toggle("selected", b === btn));
      const names = {
        ADMIN: "Carlos Admin",
        WAITER: "Sofia Waiter",
        CASHIER: "Miguel Cashier",
        KITCHEN: "Chef Andrea"
      };
      $("#loginName").value = names[selectedRole];
      $("#loginPinWrap").classList.toggle("hidden", selectedRole !== "ADMIN");
      $("#loginError").classList.add("hidden");
    });

    $("#loginForm").addEventListener("submit", (e) => {
      e.preventDefault();
      const name = $("#loginName").value.trim();
      const pin = $("#loginPin").value.trim();
      const err = $("#loginError");

      if (!name) {
        err.textContent = "Please enter an operator name.";
        err.classList.remove("hidden");
        return;
      }
      if (selectedRole === "ADMIN" && pin !== ADMIN_PIN) {
        err.textContent = "Invalid Administrator PIN. Use 1234 for the demo.";
        err.classList.remove("hidden");
        return;
      }

      err.classList.add("hidden");
      state.session = { role: selectedRole, name, loggedIn: true };
      $("#loginScreen").classList.add("hidden");
      $("#appShell").classList.remove("hidden");
      $("#sidebarUserName").textContent = name;
      $("#sidebarUserRole").textContent = `Role: ${ROLE_NAMES[selectedRole]}`;

      const startView = selectedRole === "KITCHEN" ? "orders" : "dashboard";
      if (selectedRole === "KITCHEN") state.filters.orderTab = "KDS";
      navigate(startView);
      showToast(`Signed in as ${name} (${ROLE_NAMES[selectedRole]})`, "success");
    });

    $("#btnResetDemo").addEventListener("click", () => {
      state = createInitialState();
      showToast("Sample dataset restored to initial seed state.", "info");
    });

    $("#btnLogout").addEventListener("click", () => {
      state.session.loggedIn = false;
      $("#appShell").classList.add("hidden");
      $("#loginScreen").classList.remove("hidden");
    });

    $("#modalCloseBtn").addEventListener("click", closeModal);
    $("#modalBackdrop").addEventListener("click", (e) => {
      if (e.target.id === "modalBackdrop") closeModal();
    });

    $("#navList").addEventListener("click", (e) => {
      const btn = e.target.closest(".nav-btn");
      if (!btn) return;
      navigate(btn.dataset.view);
    });
  }

  // ==================== NAVIGATION & COUNTS ====================
  function updateSidebarCounts() {
    const freeTables = state.tables.filter((t) => t.status === "FREE").length;
    const availDishes = state.dishes.filter((d) => d.available).length;
    const activeOrders = state.orders.filter((o) => !["INVOICED", "CANCELLED"].includes(o.status)).length;
    const pendingInv = state.invoices.filter((i) => ["PENDING", "PARTIAL"].includes(i.paymentStatus)).length;
    const activeCust = state.customers.filter((c) => c.active).length;

    $("#navCountTables").textContent = `${freeTables}/${state.tables.length}`;
    $("#navCountMenu").textContent = availDishes;
    $("#navCountOrders").textContent = activeOrders;
    $("#navCountInvoices").textContent = pendingInv;
    $("#navCountCustomers").textContent = activeCust;
  }

  function navigate(view, params = {}) {
    state.currentView = view;
    if (params.orderId !== undefined) state.detailOrderId = params.orderId;
    if (params.invoiceId !== undefined) state.detailInvoiceId = params.invoiceId;

    const activeNav = view === "order-detail" ? "orders" : view === "invoice-detail" ? "invoices" : view;
    document.querySelectorAll(".nav-btn").forEach((b) => b.classList.toggle("active", b.dataset.view === activeNav));

    render();
  }

  function render() {
    updateSidebarCounts();
    switch (state.currentView) {
      case "dashboard": renderDashboard(); break;
      case "tables": renderTables(); break;
      case "menu": renderMenu(); break;
      case "orders": renderOrders(); break;
      case "order-detail": renderOrderDetail(); break;
      case "invoices": renderInvoices(); break;
      case "invoice-detail": renderInvoiceDetail(); break;
      case "customers": renderCustomers(); break;
      case "reports": renderReports(); break;
      default: renderDashboard();
    }
  }

  // ==================== 1. DASHBOARD ====================
  function renderDashboard() {
    $("#breadcrumb").textContent = "Home / Dashboard";
    $("#pageTitle").textContent = "Operational Dashboard";
    $("#topbarActions").innerHTML = `
      <button class="btn btn-primary" id="dashNewOrder">+ New Order</button>
      <button class="btn btn-secondary" id="dashReports">View Reports</button>
    `;

    const freeTables = state.tables.filter((t) => t.status === "FREE").length;
    const occupiedTables = state.tables.filter((t) => t.status === "OCCUPIED").length;
    const kitchenCount = state.orders.filter((o) => ["PENDING", "IN_PREPARATION"].includes(o.status)).length;
    const pendingInvoices = state.invoices.filter((i) => ["PENDING", "PARTIAL"].includes(i.paymentStatus));
    const paidTotal = state.invoices
      .filter((i) => i.paymentStatus === "PAID")
      .reduce((s, i) => s + i.total, 0);

    const activeOrders = state.orders.filter((o) => !["INVOICED", "CANCELLED"].includes(o.status));

    $("#viewContainer").innerHTML = `
      <section class="kpi-grid">
        <div class="kpi-card">
          <div class="kpi-label">Available Tables</div>
          <div class="kpi-value">${freeTables} <span style="font-size:1rem;color:var(--text-muted)">/ ${state.tables.length}</span></div>
          <div class="kpi-sub">${occupiedTables} currently occupied (RF-001)</div>
        </div>
        <div class="kpi-card">
          <div class="kpi-label">Kitchen Queue</div>
          <div class="kpi-value">${kitchenCount}</div>
          <div class="kpi-sub">Orders PENDING or IN_PREPARATION (RF-004)</div>
        </div>
        <div class="kpi-card">
          <div class="kpi-label">Pending Invoices</div>
          <div class="kpi-value">${pendingInvoices.length}</div>
          <div class="kpi-sub">Awaiting payment settlement (RF-006)</div>
        </div>
        <div class="kpi-card">
          <div class="kpi-label">Paid Sales Today</div>
          <div class="kpi-value">${money(paidTotal)}</div>
          <div class="kpi-sub">Includes 19% VAT (RN-003, RF-009)</div>
        </div>
      </section>

      <section class="panel">
        <div class="panel-header">
          <h2>Quick Actions</h2>
        </div>
        <div class="quick-actions">
          <button class="quick-card" data-qa="new-order">
            <strong>+ Create Order</strong>
            <span>Assign free table &amp; dishes (HU-06)</span>
          </button>
          <button class="quick-card" data-qa="kitchen">
            <strong>Kitchen Display (KDS)</strong>
            <span>Advance prep &amp; ready status (HU-08)</span>
          </button>
          <button class="quick-card" data-qa="invoices">
            <strong>Billing &amp; Payments</strong>
            <span>19% VAT &amp; table release (HU-09/10)</span>
          </button>
          <button class="quick-card" data-qa="menu">
            <strong>Manage Menu</strong>
            <span>Dishes, prices &amp; availability (HU-03)</span>
          </button>
          <button class="quick-card" data-qa="reports">
            <strong>Daily Sales &amp; Top 10</strong>
            <span>Revenue &amp; best sellers (HU-13/14)</span>
          </button>
        </div>
      </section>

      <div class="two-col">
        <section class="panel">
          <div class="panel-header">
            <h2>Active Orders</h2>
            <button class="btn btn-secondary btn-sm" id="btnGoOrders">View All</button>
          </div>
          ${activeOrders.length === 0 ? `<div class="empty-state">No active orders.</div>` : `
            <div class="table-wrap">
              <table>
                <thead><tr><th>Order</th><th>Table</th><th>Status</th><th>Total</th><th></th></tr></thead>
                <tbody>
                  ${activeOrders.map((o) => {
                    const tbl = getTable(o.tableId);
                    return `<tr>
                      <td><strong>#${o.id}</strong></td>
                      <td>Table ${tbl ? tbl.number : o.tableId}</td>
                      <td>${badgeHtml(o.status)}</td>
                      <td>${money(calcOrderTotal(o))}</td>
                      <td><button class="btn btn-secondary btn-sm" data-open-order="${o.id}">Inspect</button></td>
                    </tr>`;
                  }).join("")}
                </tbody>
              </table>
            </div>
          `}
        </section>

        <section class="panel">
          <div class="panel-header">
            <h2>Pending / Partial Invoices</h2>
            <button class="btn btn-secondary btn-sm" id="btnGoInvoices">View All</button>
          </div>
          ${pendingInvoices.length === 0 ? `<div class="empty-state">All invoices are settled.</div>` : `
            <div class="table-wrap">
              <table>
                <thead><tr><th>Invoice</th><th>Status</th><th>Balance</th><th></th></tr></thead>
                <tbody>
                  ${pendingInvoices.map((inv) => {
                    const bal = Math.max(0, inv.total - calcInvoicePaid(inv));
                    return `<tr>
                      <td><strong>${esc(inv.number)}</strong></td>
                      <td>${badgeHtml(inv.paymentStatus)}</td>
                      <td>${money(bal)}</td>
                      <td><button class="btn btn-primary btn-sm" data-open-invoice="${inv.id}">Pay / View</button></td>
                    </tr>`;
                  }).join("")}
                </tbody>
              </table>
            </div>
          `}
        </section>
      </div>
    `;

    $("#dashNewOrder").onclick = () => openOrderFormModal();
    $("#dashReports").onclick = () => navigate("reports");
    $("#btnGoOrders").onclick = () => navigate("orders");
    $("#btnGoInvoices").onclick = () => navigate("invoices");

    document.querySelectorAll("[data-qa]").forEach((btn) => {
      btn.onclick = () => {
        const qa = btn.dataset.qa;
        if (qa === "new-order") openOrderFormModal();
        else if (qa === "kitchen") { state.filters.orderTab = "KDS"; navigate("orders"); }
        else navigate(qa);
      };
    });
    document.querySelectorAll("[data-open-order]").forEach((btn) => {
      btn.onclick = () => navigate("order-detail", { orderId: Number(btn.dataset.openOrder) });
    });
    document.querySelectorAll("[data-open-invoice]").forEach((btn) => {
      btn.onclick = () => navigate("invoice-detail", { invoiceId: Number(btn.dataset.openInvoice) });
    });
  }

  // ==================== 2. TABLES & FLOOR MAP ====================
  function renderTables() {
    $("#breadcrumb").textContent = "Home / Tables & Floor Map";
    $("#pageTitle").textContent = "Table Management (RF-001 / HU-01, HU-02)";
    $("#topbarActions").innerHTML = `<button class="btn btn-primary" id="btnAddTable">+ Register Table</button>`;

    const filtered = state.tables.filter((t) => {
      if (state.filters.tableStatus !== "ALL" && t.status !== state.filters.tableStatus) return false;
      if (state.filters.tableMinCap > 0 && t.capacity < state.filters.tableMinCap) return false;
      return true;
    });

    $("#viewContainer").innerHTML = `
      <section class="panel">
        <div class="toolbar">
          <div class="filter-group">
            <label for="fTableStatus">Status:</label>
            <select id="fTableStatus">
              <option value="ALL">All Statuses</option>
              <option value="FREE">FREE</option>
              <option value="OCCUPIED">OCCUPIED</option>
              <option value="RESERVED">RESERVED</option>
              <option value="MAINTENANCE">MAINTENANCE</option>
            </select>
            <label for="fTableCap">Min Capacity:</label>
            <input id="fTableCap" class="input" type="number" min="0" max="20" style="width:90px;" value="${state.filters.tableMinCap}" />
          </div>
          <div style="font-size:0.84rem;color:var(--text-muted);">Showing ${filtered.length} of ${state.tables.length} tables</div>
        </div>

        <div class="table-grid">
          ${filtered.map((t) => `
            <div class="table-card status-${t.status}">
              <div class="table-card-top">
                <h3>Table #${t.number}</h3>
                ${badgeHtml(t.status)}
              </div>
              <div class="table-meta">Capacity: <strong>${t.capacity} guests</strong> &bull; ${esc(t.location)}</div>
              <div class="table-actions">
                ${t.status === "FREE" ? `<button class="btn btn-primary btn-sm" data-t-order="${t.id}">+ Order</button>` : ""}
                <button class="btn btn-secondary btn-sm" data-t-status="${t.id}">Status</button>
                <button class="btn btn-secondary btn-sm" data-t-edit="${t.id}">Edit</button>
              </div>
            </div>
          `).join("")}
        </div>
      </section>
    `;

    $("#fTableStatus").value = state.filters.tableStatus;
    $("#fTableStatus").onchange = (e) => { state.filters.tableStatus = e.target.value; renderTables(); };
    $("#fTableCap").onchange = (e) => { state.filters.tableMinCap = Math.max(0, Number(e.target.value) || 0); renderTables(); };
    $("#btnAddTable").onclick = () => openTableFormModal(null);

    document.querySelectorAll("[data-t-order]").forEach((b) => {
      b.onclick = () => openOrderFormModal(Number(b.dataset.tOrder));
    });
    document.querySelectorAll("[data-t-edit]").forEach((b) => {
      b.onclick = () => openTableFormModal(getTable(Number(b.dataset.tEdit)));
    });
    document.querySelectorAll("[data-t-status]").forEach((b) => {
      b.onclick = () => openTableStatusModal(getTable(Number(b.dataset.tStatus)));
    });
  }

  function openTableFormModal(table) {
    const isEdit = Boolean(table);
    openModal({
      title: isEdit ? `Edit Table #${table.number}` : "Register New Table (HU-01)",
      bodyHtml: `
        <div id="tableFormErr" class="form-error hidden"></div>
        <div class="form-grid">
          <div class="form-field">
            <label for="tfNumber">Table Number</label>
            <input id="tfNumber" class="input" type="number" min="1" value="${isEdit ? table.number : state.tables.length + 1}" />
          </div>
          <div class="form-field">
            <label for="tfCap">Capacity (1 - 20)</label>
            <input id="tfCap" class="input" type="number" min="1" max="20" value="${isEdit ? table.capacity : 4}" />
          </div>
          <div class="form-field full">
            <label for="tfLoc">Location</label>
            <input id="tfLoc" class="input" type="text" placeholder="e.g., Window, Terrace, VIP Room" value="${isEdit ? esc(table.location) : ""}" />
          </div>
        </div>
      `,
      footerHtml: `
        <button class="btn btn-secondary" id="tfCancel">Cancel</button>
        <button class="btn btn-primary" id="tfSave">${isEdit ? "Save Changes" : "Register Table"}</button>
      `
    });
    $("#tfCancel").onclick = closeModal;
    $("#tfSave").onclick = () => {
      const number = Number($("#tfNumber").value);
      const capacity = Number($("#tfCap").value);
      const location = $("#tfLoc").value.trim();
      const err = $("#tableFormErr");

      if (!Number.isInteger(number) || number <= 0) {
        err.textContent = "Table number must be a positive integer.";
        err.classList.remove("hidden"); return;
      }
      if (state.tables.some((t) => t.number === number && (!isEdit || t.id !== table.id))) {
        err.textContent = `Table #${number} already exists.`;
        err.classList.remove("hidden"); return;
      }
      if (!Number.isInteger(capacity) || capacity < 1 || capacity > 20) {
        err.textContent = "Capacity must be between 1 and 20 guests (RN-004).";
        err.classList.remove("hidden"); return;
      }
      if (!location) {
        err.textContent = "Please provide a table location.";
        err.classList.remove("hidden"); return;
      }

      if (isEdit) {
        table.number = number;
        table.capacity = capacity;
        table.location = location;
        showToast(`Table #${number} updated.`, "success");
      } else {
        state.tables.push({ id: state.nextIds.table++, number, capacity, status: "FREE", location });
        showToast(`Table #${number} registered as FREE.`, "success");
      }
      closeModal();
      render();
    };
  }

  function openTableStatusModal(table) {
    openModal({
      title: `Change Status — Table #${table.number} (HU-02)`,
      bodyHtml: `
        <div id="tsErr" class="form-error hidden"></div>
        <p style="margin-bottom:12px;font-size:0.9rem;">Current status: ${badgeHtml(table.status)}</p>
        <div class="form-field">
          <label for="tsSelect">New Status</label>
          <select id="tsSelect">
            <option value="FREE">FREE</option>
            <option value="OCCUPIED">OCCUPIED</option>
            <option value="RESERVED">RESERVED</option>
            <option value="MAINTENANCE">MAINTENANCE</option>
          </select>
        </div>
      `,
      footerHtml: `
        <button class="btn btn-secondary" id="tsCancel">Cancel</button>
        <button class="btn btn-primary" id="tsSave">Update Status</button>
      `
    });
    $("#tsSelect").value = table.status;
    $("#tsCancel").onclick = closeModal;
    $("#tsSave").onclick = () => {
      const nextStatus = $("#tsSelect").value;
      const err = $("#tsErr");
      if (table.status === "OCCUPIED" && nextStatus !== "OCCUPIED" && hasActiveOrdersOrUnpaidInvoices(table.id)) {
        err.textContent = "Blocked by RN-001: Cannot change an OCCUPIED table that still has active orders or unpaid invoices.";
        err.classList.remove("hidden");
        showToast("Table has active orders or unpaid invoices.", "error");
        return;
      }
      table.status = nextStatus;
      closeModal();
      showToast(`Table #${table.number} status set to ${nextStatus}.`, "success");
      render();
    };
  }

  // ==================== 3. MENU CATALOG ====================
  function renderMenu() {
    $("#breadcrumb").textContent = "Home / Menu Catalog";
    $("#pageTitle").textContent = "Menu & Dishes Management (RF-002 / HU-03, HU-04, HU-05)";
    $("#topbarActions").innerHTML = `<button class="btn btn-primary" id="btnAddDish">+ Register Dish</button>`;

    const q = state.filters.menuSearch.toLowerCase();
    const filtered = state.dishes.filter((d) => {
      if (state.filters.menuCategory !== "ALL" && d.categoryId !== Number(state.filters.menuCategory)) return false;
      if (q && !d.name.toLowerCase().includes(q) && !d.description.toLowerCase().includes(q)) return false;
      return true;
    });

    $("#viewContainer").innerHTML = `
      <section class="panel">
        <div class="toolbar">
          <div class="filter-group">
            <select id="fMenuCat">
              <option value="ALL">All Categories</option>
              ${state.categories.map((c) => `<option value="${c.id}">${esc(c.name)}</option>`).join("")}
            </select>
            <input id="fMenuSearch" class="input" type="search" placeholder="Search dish by name..." value="${esc(state.filters.menuSearch)}" style="min-width:230px;" />
          </div>
          <div style="font-size:0.84rem;color:var(--text-muted);">${filtered.length} dishes found</div>
        </div>

        ${filtered.length === 0 ? `<div class="empty-state">No dishes match your filter.</div>` : `
          <div class="table-wrap">
            <table>
              <thead>
                <tr><th>ID</th><th>Dish</th><th>Category</th><th>Price</th><th>Prep Time</th><th>Status</th><th>Actions</th></tr>
              </thead>
              <tbody>
                ${filtered.map((d) => `
                  <tr>
                    <td>#${d.id}</td>
                    <td><strong>${esc(d.name)}</strong><div style="font-size:0.8rem;color:var(--text-muted);">${esc(d.description)}</div></td>
                    <td>${esc(getCategoryName(d.categoryId))}</td>
                    <td><strong>${money(d.price)}</strong></td>
                    <td>${d.prepTime} min</td>
                    <td>${d.available ? `<span class="badge badge-success">AVAILABLE</span>` : `<span class="badge badge-neutral">UNAVAILABLE</span>`}</td>
                    <td style="white-space:nowrap;">
                      <button class="btn btn-secondary btn-sm" data-d-toggle="${d.id}">${d.available ? "Disable" : "Enable"}</button>
                      <button class="btn btn-secondary btn-sm" data-d-edit="${d.id}">Edit</button>
                      <button class="btn btn-danger btn-sm" data-d-del="${d.id}">Deactivate</button>
                    </td>
                  </tr>
                `).join("")}
              </tbody>
            </table>
          </div>
        `}
      </section>
    `;

    $("#fMenuCat").value = state.filters.menuCategory;
    $("#fMenuCat").onchange = (e) => { state.filters.menuCategory = e.target.value; renderMenu(); };
    $("#fMenuSearch").oninput = (e) => { state.filters.menuSearch = e.target.value; renderMenu(); };
    $("#btnAddDish").onclick = () => openDishFormModal(null);

    document.querySelectorAll("[data-d-toggle]").forEach((b) => {
      b.onclick = () => {
        const d = getDish(Number(b.dataset.dToggle));
        d.available = !d.available;
        showToast(`"${d.name}" marked as ${d.available ? "AVAILABLE" : "UNAVAILABLE"}.`, "info");
        renderMenu();
      };
    });
    document.querySelectorAll("[data-d-edit]").forEach((b) => {
      b.onclick = () => openDishFormModal(getDish(Number(b.dataset.dEdit)));
    });
    document.querySelectorAll("[data-d-del]").forEach((b) => {
      b.onclick = () => {
        const d = getDish(Number(b.dataset.dDel));
        openConfirm({
          title: "Deactivate Dish (Soft Delete)",
          message: `Are you sure you want to deactivate "${d.name}"? It will no longer be selectable for new orders.`,
          confirmLabel: "Deactivate",
          onConfirm: () => {
            d.available = false;
            showToast(`"${d.name}" deactivated.`, "warning");
            render();
          }
        });
      };
    });
  }

  function openDishFormModal(dish) {
    const isEdit = Boolean(dish);
    openModal({
      title: isEdit ? `Edit Dish: ${dish.name}` : "Register New Dish (HU-03)",
      bodyHtml: `
        <div id="dishFormErr" class="form-error hidden"></div>
        <div class="form-grid">
          <div class="form-field full">
            <label for="dfName">Dish Name</label>
            <input id="dfName" class="input" type="text" value="${isEdit ? esc(dish.name) : ""}" />
          </div>
          <div class="form-field">
            <label for="dfCat">Category</label>
            <select id="dfCat">
              ${state.categories.map((c) => `<option value="${c.id}">${esc(c.name)}</option>`).join("")}
            </select>
          </div>
          <div class="form-field">
            <label for="dfPrice">Price (COP, &gt; 0)</label>
            <input id="dfPrice" class="input" type="number" min="1" step="500" value="${isEdit ? dish.price : ""}" />
          </div>
          <div class="form-field">
            <label for="dfPrep">Prep Time (minutes)</label>
            <input id="dfPrep" class="input" type="number" min="1" value="${isEdit ? dish.prepTime : 15}" />
          </div>
          <div class="form-field full">
            <label for="dfDesc">Description</label>
            <textarea id="dfDesc" rows="2">${isEdit ? esc(dish.description) : ""}</textarea>
          </div>
        </div>
      `,
      footerHtml: `
        <button class="btn btn-secondary" id="dfCancel">Cancel</button>
        <button class="btn btn-primary" id="dfSave">${isEdit ? "Save Changes" : "Register Dish"}</button>
      `
    });
    if (isEdit) $("#dfCat").value = dish.categoryId;
    $("#dfCancel").onclick = closeModal;
    $("#dfSave").onclick = () => {
      const name = $("#dfName").value.trim();
      const categoryId = Number($("#dfCat").value);
      const price = Number($("#dfPrice").value);
      const prepTime = Number($("#dfPrep").value);
      const description = $("#dfDesc").value.trim();
      const err = $("#dishFormErr");

      if (!name) { err.textContent = "Dish name is required."; err.classList.remove("hidden"); return; }
      if (!(price > 0)) { err.textContent = "Price must be strictly greater than 0 (RN-004)."; err.classList.remove("hidden"); return; }
      if (!Number.isInteger(prepTime) || prepTime <= 0) { err.textContent = "Preparation time must be a positive integer."; err.classList.remove("hidden"); return; }

      if (isEdit) {
        Object.assign(dish, { name, categoryId, price, prepTime, description });
        showToast(`Dish "${name}" updated.`, "success");
      } else {
        state.dishes.push({ id: state.nextIds.dish++, categoryId, name, description, price, prepTime, available: true });
        showToast(`Dish "${name}" added to menu.`, "success");
      }
      closeModal();
      render();
    };
  }

  // ==================== 4. ORDERS & KITCHEN KDS ====================
  function renderOrders() {
    $("#breadcrumb").textContent = "Home / Orders & Kitchen";
    $("#pageTitle").textContent = "Order Management & Kitchen Display (RF-003, RF-004)";
    $("#topbarActions").innerHTML = `
      <button class="btn ${state.filters.orderTab === "LIST" ? "btn-primary" : "btn-secondary"}" id="tabOrderList">All Orders</button>
      <button class="btn ${state.filters.orderTab === "KDS" ? "btn-primary" : "btn-secondary"}" id="tabOrderKds">Kitchen View (KDS)</button>
      <button class="btn btn-success" id="btnNewOrder">+ New Order</button>
    `;

    if (state.filters.orderTab === "KDS") {
      const kitchenOrders = state.orders.filter((o) => ["PENDING", "IN_PREPARATION"].includes(o.status));
      $("#viewContainer").innerHTML = `
        <section class="panel">
          <div class="panel-header">
            <h2>Live Kitchen Queue (<code>v_kitchen_orders</code>)</h2>
            <span class="badge badge-info">${kitchenOrders.length} active</span>
          </div>
          ${kitchenOrders.length === 0 ? `<div class="empty-state">Kitchen queue is clear!</div>` : `
            <div class="kds-grid">
              ${kitchenOrders.map((o) => {
                const tbl = getTable(o.tableId);
                const nextStatus = o.status === "PENDING" ? "IN_PREPARATION" : "READY";
                return `
                  <div class="kds-card">
                    <div style="display:flex;justify-content:space-between;align-items:center;">
                      <strong>Order #${o.id} &bull; Table ${tbl ? tbl.number : o.tableId}</strong>
                      ${badgeHtml(o.status)}
                    </div>
                    <div style="font-size:0.8rem;color:var(--text-muted);">${esc(o.createdAt)} ${o.notes ? "&bull; Note: " + esc(o.notes) : ""}</div>
                    <ul class="kds-items">
                      ${o.items.map((it) => {
                        const d = getDish(it.dishId);
                        return `<li><span><strong>${it.quantity}x</strong> ${esc(d ? d.name : "Dish #" + it.dishId)}</span><span>${d ? d.prepTime + "m" : ""}</span></li>`;
                      }).join("")}
                    </ul>
                    <div style="display:flex;gap:8px;">
                      <button class="btn btn-primary btn-sm" style="flex:1;" data-kds-next="${o.id}" data-next="${nextStatus}">Mark ${nextStatus}</button>
                      <button class="btn btn-secondary btn-sm" data-o-detail="${o.id}">Detail</button>
                    </div>
                  </div>
                `;
              }).join("")}
            </div>
          `}
        </section>
      `;
    } else {
      const filtered = state.orders.filter((o) => state.filters.orderStatus === "ALL" || o.status === state.filters.orderStatus);
      $("#viewContainer").innerHTML = `
        <section class="panel">
          <div class="toolbar">
            <div class="filter-group">
              <label for="fOrdStatus">Filter Status:</label>
              <select id="fOrdStatus">
                <option value="ALL">All Statuses</option>
                <option value="PENDING">PENDING</option>
                <option value="IN_PREPARATION">IN_PREPARATION</option>
                <option value="READY">READY</option>
                <option value="DELIVERED">DELIVERED</option>
                <option value="INVOICED">INVOICED</option>
                <option value="CANCELLED">CANCELLED</option>
              </select>
            </div>
            <div style="font-size:0.84rem;color:var(--text-muted);">Showing ${filtered.length} orders</div>
          </div>

          <div class="table-wrap">
            <table>
              <thead><tr><th>Order</th><th>Table</th><th>Customer</th><th>Created</th><th>Items</th><th>Total</th><th>Status</th><th>Action</th></tr></thead>
              <tbody>
                ${filtered.map((o) => {
                  const tbl = getTable(o.tableId);
                  const cust = getCustomer(o.customerId);
                  return `<tr>
                    <td><strong>#${o.id}</strong></td>
                    <td>Table #${tbl ? tbl.number : o.tableId}</td>
                    <td>${cust ? esc(cust.name) : "<em>Walk-in</em>"}</td>
                    <td>${esc(o.createdAt)}</td>
                    <td>${o.items.reduce((s, i) => s + i.quantity, 0)}</td>
                    <td><strong>${money(calcOrderTotal(o))}</strong></td>
                    <td>${badgeHtml(o.status)}</td>
                    <td><button class="btn btn-primary btn-sm" data-o-detail="${o.id}">Open Detail</button></td>
                  </tr>`;
                }).join("")}
              </tbody>
            </table>
          </div>
        </section>
      `;
      $("#fOrdStatus").value = state.filters.orderStatus;
      $("#fOrdStatus").onchange = (e) => { state.filters.orderStatus = e.target.value; renderOrders(); };
    }

    $("#tabOrderList").onclick = () => { state.filters.orderTab = "LIST"; renderOrders(); };
    $("#tabOrderKds").onclick = () => { state.filters.orderTab = "KDS"; renderOrders(); };
    $("#btnNewOrder").onclick = () => openOrderFormModal();

    document.querySelectorAll("[data-o-detail]").forEach((b) => {
      b.onclick = () => navigate("order-detail", { orderId: Number(b.dataset.oDetail) });
    });
    document.querySelectorAll("[data-kds-next]").forEach((b) => {
      b.onclick = () => {
        const o = state.orders.find((x) => x.id === Number(b.dataset.kdsNext));
        o.status = b.dataset.next;
        showToast(`Order #${o.id} updated to ${o.status}.`, "success");
        render();
      };
    });
  }

  function openOrderFormModal(preselectedTableId = null) {
    const freeTables = state.tables.filter((t) => t.status === "FREE");
    if (freeTables.length === 0) {
      showToast("No FREE tables available to create a new order (RN-001).", "error");
      return;
    }
    const availDishes = state.dishes.filter((d) => d.available);
    openModal({
      title: "Create New Order (HU-06 / sp_create_order)",
      bodyHtml: `
        <div id="ofErr" class="form-error hidden"></div>
        <div class="form-grid">
          <div class="form-field">
            <label for="ofTable">Available Table</label>
            <select id="ofTable">
              ${freeTables.map((t) => `<option value="${t.id}" ${t.id === preselectedTableId ? "selected" : ""}>Table #${t.number} (${t.capacity} guests - ${esc(t.location)})</option>`).join("")}
            </select>
          </div>
          <div class="form-field">
            <label for="ofCust">Customer (Optional)</label>
            <select id="ofCust">
              <option value="">-- Walk-in Guest --</option>
              ${state.customers.filter((c) => c.active).map((c) => `<option value="${c.id}">${esc(c.name)}</option>`).join("")}
            </select>
          </div>
          <div class="form-field">
            <label for="ofDish">Initial Dish</label>
            <select id="ofDish">
              ${availDishes.map((d) => `<option value="${d.id}">${esc(d.name)} — ${money(d.price)}</option>`).join("")}
            </select>
          </div>
          <div class="form-field">
            <label for="ofQty">Quantity (&gt; 0)</label>
            <input id="ofQty" class="input" type="number" min="1" value="1" />
          </div>
          <div class="form-field full">
            <label for="ofNotes">Kitchen Notes</label>
            <input id="ofNotes" class="input" type="text" placeholder="Allergies, cooking preferences..." />
          </div>
        </div>
      `,
      footerHtml: `
        <button class="btn btn-secondary" id="ofCancel">Cancel</button>
        <button class="btn btn-primary" id="ofCreate">Create Order</button>
      `
    });
    $("#ofCancel").onclick = closeModal;
    $("#ofCreate").onclick = () => {
      const tableId = Number($("#ofTable").value);
      const customerId = $("#ofCust").value ? Number($("#ofCust").value) : null;
      const dishId = Number($("#ofDish").value);
      const qty = Number($("#ofQty").value);
      const notes = $("#ofNotes").value.trim();
      const err = $("#ofErr");

      if (!Number.isInteger(qty) || qty <= 0) {
        err.textContent = "Quantity must be a positive integer (RN-004).";
        err.classList.remove("hidden"); return;
      }

      const tbl = getTable(tableId);
      const dish = getDish(dishId);
      const newOrder = {
        id: state.nextIds.order++,
        tableId,
        customerId,
        createdAt: nowStamp(),
        status: "IN_PREPARATION",
        notes,
        items: [{ dishId, quantity: qty, unitPrice: dish.price }]
      };
      tbl.status = "OCCUPIED";
      state.orders.unshift(newOrder);
      closeModal();
      showToast(`Order #${newOrder.id} created. Table #${tbl.number} is now OCCUPIED.`, "success");
      navigate("order-detail", { orderId: newOrder.id });
    };
  }

  // ==================== 5. ORDER DETAIL SCREEN ====================
  function renderOrderDetail() {
    const order = state.orders.find((o) => o.id === state.detailOrderId);
    if (!order) { navigate("orders"); return; }

    const tbl = getTable(order.tableId);
    const cust = getCustomer(order.customerId);
    const total = calcOrderTotal(order);
    const linkedInv = state.invoices.find((i) => i.orderId === order.id);
    const canModifyItems = ["PENDING", "IN_PREPARATION"].includes(order.status);
    const allowedNext = STATUS_TRANSITIONS[order.status] || [];
    const canInvoice = ["READY", "DELIVERED"].includes(order.status) && !linkedInv && total > 0;

    $("#breadcrumb").textContent = `Home / Orders / Order #${order.id}`;
    $("#pageTitle").textContent = `Order #${order.id} — Table #${tbl ? tbl.number : order.tableId}`;
    $("#topbarActions").innerHTML = `
      <button class="btn btn-secondary" id="odBack">&larr; Back to Orders</button>
      ${canInvoice ? `<button class="btn btn-success" id="odInvoice">Generate Invoice (19% VAT)</button>` : ""}
      ${linkedInv ? `<button class="btn btn-primary" id="odOpenInv">View Invoice ${esc(linkedInv.number)}</button>` : ""}
    `;

    const curIdx = ORDER_FLOW.indexOf(order.status);

    $("#viewContainer").innerHTML = `
      <section class="panel">
        <div class="panel-header">
          <div>
            <h2>Order Lifecycle &amp; Metadata</h2>
            <div style="font-size:0.86rem;color:var(--text-muted);">
              Created: ${esc(order.createdAt)} &bull; Customer: <strong>${cust ? esc(cust.name) : "Walk-in"}</strong>
              ${order.notes ? ` &bull; Notes: <em>"${esc(order.notes)}"</em>` : ""}
            </div>
          </div>
          <div>${badgeHtml(order.status)}</div>
        </div>

        <div class="stepper">
          ${order.status === "CANCELLED"
            ? `<span class="step cancelled">CANCELLED</span>`
            : ORDER_FLOW.map((st, idx) => `<span class="step ${idx < curIdx ? "done" : idx === curIdx ? "current" : ""}">${st}</span>`).join("<span>&rarr;</span>")}
        </div>

        <div style="display:flex;gap:8px;flex-wrap:wrap;">
          ${allowedNext.filter((s) => s !== "CANCELLED").map((st) => `
            <button class="btn btn-primary btn-sm" data-od-transition="${st}">Advance to ${st}</button>
          `).join("")}
          ${allowedNext.includes("CANCELLED") ? `<button class="btn btn-danger btn-sm" id="odCancelBtn">Cancel Order</button>` : ""}
          ${canModifyItems ? `<button class="btn btn-secondary btn-sm" id="odAddDishBtn">+ Add Dish to Order</button>` : ""}
        </div>
      </section>

      <div class="two-col">
        <section class="panel">
          <div class="panel-header">
            <h2>Order Items (${order.items.length})</h2>
            <span style="font-size:0.8rem;color:var(--text-muted);">Item removal requires Admin PIN (RN-002)</span>
          </div>
          ${order.items.length === 0 ? `<div class="empty-state">No dishes in this order yet.</div>` : `
            <div class="table-wrap">
              <table>
                <thead><tr><th>Dish</th><th>Qty</th><th>Unit Price</th><th>Subtotal</th><th></th></tr></thead>
                <tbody>
                  ${order.items.map((it) => {
                    const d = getDish(it.dishId);
                    return `<tr>
                      <td><strong>${esc(d ? d.name : "Dish #" + it.dishId)}</strong></td>
                      <td>${it.quantity}</td>
                      <td>${money(it.unitPrice)}</td>
                      <td><strong>${money(it.quantity * it.unitPrice)}</strong></td>
                      <td>
                        ${canModifyItems ? `<button class="btn btn-danger btn-sm" data-void-dish="${it.dishId}">Void (PIN)</button>` : ""}
                      </td>
                    </tr>`;
                  }).join("")}
                </tbody>
              </table>
            </div>
          `}
        </section>

        <section class="panel">
          <div class="panel-header"><h2>Financial Preview (RN-003)</h2></div>
          <div class="summary-box">
            <div class="summary-row"><span>Order Subtotal</span><span>${money(total)}</span></div>
            <div class="summary-row"><span>Estimated VAT (19%)</span><span>${money(Math.round(total * VAT_RATE * 100) / 100)}</span></div>
            <div class="summary-row total"><span>Estimated Invoice Total</span><span>${money(Math.round(total * (1 + VAT_RATE) * 100) / 100)}</span></div>
          </div>
          ${!canInvoice && !linkedInv && order.status !== "CANCELLED" ? `
            <p style="margin-top:12px;font-size:0.83rem;color:var(--text-muted);">
              Order must reach <strong>READY</strong> or <strong>DELIVERED</strong> status (with at least 1 dish) before generating an invoice.
            </p>
          ` : ""}
        </section>
      </div>
    `;

    $("#odBack").onclick = () => navigate("orders");
    if ($("#odInvoice")) $("#odInvoice").onclick = () => openGenerateInvoiceModal(order);
    if ($("#odOpenInv")) $("#odOpenInv").onclick = () => navigate("invoice-detail", { invoiceId: linkedInv.id });
    if ($("#odAddDishBtn")) $("#odAddDishBtn").onclick = () => openAddDishModal(order);
    if ($("#odCancelBtn")) {
      $("#odCancelBtn").onclick = () => openConfirm({
        title: `Cancel Order #${order.id}`,
        message: "Cancelling this order is irreversible and will automatically release the table if no other active orders exist.",
        confirmLabel: "Cancel Order",
        onConfirm: () => {
          order.status = "CANCELLED";
          const freed = tryReleaseTable(order.tableId);
          showToast(freed ? `Order #${order.id} cancelled and Table #${tbl.number} released.` : `Order #${order.id} cancelled.`, "warning");
          renderOrderDetail();
        }
      });
    }

    document.querySelectorAll("[data-od-transition]").forEach((b) => {
      b.onclick = () => {
        order.status = b.dataset.odTransition;
        showToast(`Order #${order.id} moved to ${order.status}.`, "success");
        renderOrderDetail();
      };
    });

    document.querySelectorAll("[data-void-dish]").forEach((b) => {
      b.onclick = () => {
        const dishId = Number(b.dataset.voidDish);
        const dish = getDish(dishId);
        openAdminPinDialog(`Void "${dish ? dish.name : "Dish #" + dishId}" from Order #${order.id}.`, () => {
          order.items = order.items.filter((i) => i.dishId !== dishId);
          showToast(`Item "${dish ? dish.name : dishId}" voided with Admin authorization.`, "warning");
          renderOrderDetail();
        });
      };
    });
  }

  function openAddDishModal(order) {
    const availDishes = state.dishes.filter((d) => d.available);
    openModal({
      title: `Add Dish to Order #${order.id} (HU-07)`,
      bodyHtml: `
        <div id="adErr" class="form-error hidden"></div>
        <div class="form-field">
          <label for="adDish">Available Dish</label>
          <select id="adDish">
            ${availDishes.map((d) => `<option value="${d.id}">${esc(d.name)} — ${money(d.price)}</option>`).join("")}
          </select>
        </div>
        <div class="form-field">
          <label for="adQty">Quantity (&gt; 0)</label>
          <input id="adQty" class="input" type="number" min="1" value="1" />
        </div>
      `,
      footerHtml: `
        <button class="btn btn-secondary" id="adCancel">Cancel</button>
        <button class="btn btn-primary" id="adSave">Add to Order</button>
      `
    });
    $("#adCancel").onclick = closeModal;
    $("#adSave").onclick = () => {
      const dishId = Number($("#adDish").value);
      const qty = Number($("#adQty").value);
      const err = $("#adErr");
      if (!Number.isInteger(qty) || qty <= 0) {
        err.textContent = "Quantity must be greater than 0 (RN-004).";
        err.classList.remove("hidden"); return;
      }
      const existing = order.items.find((i) => i.dishId === dishId);
      const dish = getDish(dishId);
      if (existing) existing.quantity += qty;
      else order.items.push({ dishId, quantity: qty, unitPrice: dish.price });
      if (order.status === "PENDING") order.status = "IN_PREPARATION";
      closeModal();
      showToast(`Added ${qty}x ${dish.name} to Order #${order.id}.`, "success");
      renderOrderDetail();
    };
  }

  // ==================== 6. BILLING & INVOICES ====================
  function renderInvoices() {
    $("#breadcrumb").textContent = "Home / Billing & Payments";
    $("#pageTitle").textContent = "Invoicing & Payments (RF-005, RF-006 / HU-09, HU-10)";

    const billableOrders = state.orders.filter(
      (o) => ["READY", "DELIVERED"].includes(o.status) && !state.invoices.some((i) => i.orderId === o.id) && calcOrderTotal(o) > 0
    );

    $("#topbarActions").innerHTML = `
      <button class="btn btn-primary" id="btnGenInvTop" ${billableOrders.length === 0 ? "disabled" : ""}>+ Generate Invoice</button>
    `;

    const filtered = state.invoices.filter((i) => state.filters.invoiceStatus === "ALL" || i.paymentStatus === state.filters.invoiceStatus);

    $("#viewContainer").innerHTML = `
      <section class="panel">
        <div class="toolbar">
          <div class="filter-group">
            <label for="fInvStatus">Payment Status:</label>
            <select id="fInvStatus">
              <option value="ALL">All</option>
              <option value="PENDING">PENDING</option>
              <option value="PARTIAL">PARTIAL</option>
              <option value="PAID">PAID</option>
              <option value="VOIDED">VOIDED</option>
            </select>
          </div>
          <div style="font-size:0.84rem;color:var(--text-muted);">${billableOrders.length} orders ready to invoice</div>
        </div>

        <div class="table-wrap">
          <table>
            <thead>
              <tr><th>Invoice #</th><th>Order</th><th>Table</th><th>Subtotal</th><th>VAT 19%</th><th>Total</th><th>Paid</th><th>Status</th><th>Action</th></tr>
            </thead>
            <tbody>
              ${filtered.map((inv) => {
                const ord = state.orders.find((o) => o.id === inv.orderId);
                const tbl = ord ? getTable(ord.tableId) : null;
                return `<tr>
                  <td><strong>${esc(inv.number)}</strong></td>
                  <td>#${inv.orderId}</td>
                  <td>Table #${tbl ? tbl.number : "-"}</td>
                  <td>${money(inv.subtotal)}</td>
                  <td>${money(inv.taxes)}</td>
                  <td><strong>${money(inv.total)}</strong></td>
                  <td>${money(calcInvoicePaid(inv))}</td>
                  <td>${badgeHtml(inv.paymentStatus)}</td>
                  <td><button class="btn btn-primary btn-sm" data-inv-open="${inv.id}">Detail / Pay</button></td>
                </tr>`;
              }).join("")}
            </tbody>
          </table>
        </div>
      </section>
    `;

    $("#fInvStatus").value = state.filters.invoiceStatus;
    $("#fInvStatus").onchange = (e) => { state.filters.invoiceStatus = e.target.value; renderInvoices(); };
    $("#btnGenInvTop").onclick = () => {
      if (billableOrders.length === 0) {
        showToast("No READY or DELIVERED orders awaiting invoicing.", "warning");
        return;
      }
      openGenerateInvoiceModal(billableOrders[0], billableOrders);
    };
    document.querySelectorAll("[data-inv-open]").forEach((b) => {
      b.onclick = () => navigate("invoice-detail", { invoiceId: Number(b.dataset.invOpen) });
    });
  }

  function openGenerateInvoiceModal(defaultOrder, candidateOrders = null) {
    const ordersList = candidateOrders || [defaultOrder];
    const subtotal = calcOrderTotal(defaultOrder);
    const vat = Math.round(subtotal * VAT_RATE * 100) / 100;
    const total = Math.round((subtotal + vat) * 100) / 100;

    openModal({
      title: "Generate Invoice (RN-003: 19% VAT)",
      bodyHtml: `
        <div class="form-field">
          <label for="giOrder">Order to Invoice</label>
          <select id="giOrder">
            ${ordersList.map((o) => `<option value="${o.id}" ${o.id === defaultOrder.id ? "selected" : ""}>Order #${o.id} — Table #${getTable(o.tableId)?.number} (${money(calcOrderTotal(o))})</option>`).join("")}
          </select>
        </div>
        <div class="form-field">
          <label for="giMethod">Payment Method</label>
          <select id="giMethod">
            <option value="CASH">CASH (Marks PAID &amp; releases table immediately)</option>
            <option value="CREDIT_CARD">CREDIT_CARD (Creates PENDING invoice)</option>
            <option value="DEBIT_CARD">DEBIT_CARD (Creates PENDING invoice)</option>
            <option value="TRANSFER">TRANSFER (Creates PENDING invoice)</option>
            <option value="QR">QR (Creates PENDING invoice)</option>
            <option value="MIXED">MIXED (Supports partial payments)</option>
          </select>
        </div>
        <div class="summary-box" id="giPreview">
          <div class="summary-row"><span>Subtotal</span><span>${money(subtotal)}</span></div>
          <div class="summary-row"><span>VAT (19%)</span><span>${money(vat)}</span></div>
          <div class="summary-row total"><span>Total Payable</span><span>${money(total)}</span></div>
        </div>
      `,
      footerHtml: `
        <button class="btn btn-secondary" id="giCancel">Cancel</button>
        <button class="btn btn-success" id="giCreate">Generate Invoice</button>
      `
    });

    $("#giOrder").onchange = () => {
      const o = state.orders.find((x) => x.id === Number($("#giOrder").value));
      const sub = calcOrderTotal(o);
      const tx = Math.round(sub * VAT_RATE * 100) / 100;
      const tot = Math.round((sub + tx) * 100) / 100;
      $("#giPreview").innerHTML = `
        <div class="summary-row"><span>Subtotal</span><span>${money(sub)}</span></div>
        <div class="summary-row"><span>VAT (19%)</span><span>${money(tx)}</span></div>
        <div class="summary-row total"><span>Total Payable</span><span>${money(tot)}</span></div>
      `;
    };

    $("#giCancel").onclick = closeModal;
    $("#giCreate").onclick = () => {
      const ord = state.orders.find((x) => x.id === Number($("#giOrder").value));
      const method = $("#giMethod").value;
      const sub = calcOrderTotal(ord);
      const tx = Math.round(sub * VAT_RATE * 100) / 100;
      const tot = Math.round((sub + tx) * 100) / 100;
      const id = state.nextIds.invoice++;
      const isCash = method === "CASH";

      const inv = {
        id,
        orderId: ord.id,
        number: `INV-20260406-${String(id).padStart(3, "0")}`,
        issueDate: nowStamp(),
        subtotal: sub,
        taxes: tx,
        total: tot,
        paymentMethod: method,
        paymentStatus: isCash ? "PAID" : "PENDING",
        notes: isCash ? "Paid in full with cash" : "Pending payment settlement",
        payments: isCash
          ? [{ id: state.nextIds.payment++, amount: tot, method: "CASH", reference: `CASH-${id}`, date: nowStamp() }]
          : []
      };
      ord.status = "INVOICED";
      state.invoices.unshift(inv);

      let msg = `Invoice ${inv.number} generated.`;
      if (isCash && tryReleaseTable(ord.tableId)) {
        msg += ` Paid in CASH — Table #${getTable(ord.tableId).number} released (RN-001).`;
      }
      closeModal();
      showToast(msg, "success");
      navigate("invoice-detail", { invoiceId: inv.id });
    };
  }

  // ==================== 7. INVOICE DETAIL SCREEN ====================
  function renderInvoiceDetail() {
    const inv = state.invoices.find((i) => i.id === state.detailInvoiceId);
    if (!inv) { navigate("invoices"); return; }

    const ord = state.orders.find((o) => o.id === inv.orderId);
    const tbl = ord ? getTable(ord.tableId) : null;
    const cust = ord ? getCustomer(ord.customerId) : null;
    const paid = calcInvoicePaid(inv);
    const balance = Math.max(0, Math.round((inv.total - paid) * 100) / 100);
    const canPay = ["PENDING", "PARTIAL"].includes(inv.paymentStatus);

    $("#breadcrumb").textContent = `Home / Billing / ${inv.number}`;
    $("#pageTitle").textContent = `Invoice ${inv.number}`;
    $("#topbarActions").innerHTML = `
      <button class="btn btn-secondary" id="idBack">&larr; Back to Invoices</button>
      ${canPay ? `<button class="btn btn-success" id="idPayBtn">+ Register Payment</button>` : ""}
      ${canPay ? `<button class="btn btn-danger" id="idVoidBtn">Void Invoice</button>` : ""}
    `;

    $("#viewContainer").innerHTML = `
      <div class="two-col">
        <section class="panel">
          <div class="panel-header">
            <div>
              <h2>Invoice Details</h2>
              <div style="font-size:0.85rem;color:var(--text-muted);">
                Issued: ${esc(inv.issueDate)} &bull; Order #${inv.orderId} &bull; Table #${tbl ? tbl.number : "-"} &bull; Customer: <strong>${cust ? esc(cust.name) : "Walk-in"}</strong>
              </div>
            </div>
            ${badgeHtml(inv.paymentStatus)}
          </div>

          <div class="table-wrap" style="margin-bottom:16px;">
            <table>
              <thead><tr><th>Dish</th><th>Qty</th><th>Unit Price</th><th>Subtotal</th></tr></thead>
              <tbody>
                ${(ord ? ord.items : []).map((it) => {
                  const d = getDish(it.dishId);
                  return `<tr>
                    <td>${esc(d ? d.name : "Dish #" + it.dishId)}</td>
                    <td>${it.quantity}</td>
                    <td>${money(it.unitPrice)}</td>
                    <td>${money(it.quantity * it.unitPrice)}</td>
                  </tr>`;
                }).join("")}
              </tbody>
            </table>
          </div>

          <div class="summary-box">
            <div class="summary-row"><span>Subtotal</span><span>${money(inv.subtotal)}</span></div>
            <div class="summary-row"><span>VAT (19% — RN-003)</span><span>${money(inv.taxes)}</span></div>
            <div class="summary-row total"><span>Invoice Total</span><span>${money(inv.total)}</span></div>
            <div class="summary-row" style="color:var(--success);"><span>Total Paid</span><span>${money(paid)}</span></div>
            <div class="summary-row" style="font-weight:700;color:${balance > 0 ? "var(--danger)" : "var(--success)"};">
              <span>Remaining Balance</span><span>${money(balance)}</span>
            </div>
          </div>
        </section>

        <section class="panel">
          <div class="panel-header">
            <h2>Payment History (${inv.payments.length})</h2>
          </div>
          ${inv.payments.length === 0 ? `<div class="empty-state">No payments recorded yet.</div>` : `
            <div class="table-wrap">
              <table>
                <thead><tr><th>Date</th><th>Method</th><th>Reference</th><th>Amount</th></tr></thead>
                <tbody>
                  ${inv.payments.map((p) => `<tr>
                    <td>${esc(p.date)}</td>
                    <td>${esc(p.method)}</td>
                    <td>${esc(p.reference || "-")}</td>
                    <td><strong>${money(p.amount)}</strong></td>
                  </tr>`).join("")}
                </tbody>
              </table>
            </div>
          `}
        </section>
      </div>
    `;

    $("#idBack").onclick = () => navigate("invoices");
    if ($("#idPayBtn")) $("#idPayBtn").onclick = () => openPaymentModal(inv, balance);
    if ($("#idVoidBtn")) {
      $("#idVoidBtn").onclick = () => openConfirm({
        title: `Void Invoice ${inv.number}`,
        message: "Voiding this unpaid invoice will mark it VOIDED and release the associated table if no other active orders remain.",
        confirmLabel: "Void Invoice",
        onConfirm: () => {
          inv.paymentStatus = "VOIDED";
          const freed = ord ? tryReleaseTable(ord.tableId) : false;
          showToast(freed ? `Invoice ${inv.number} voided and table released.` : `Invoice ${inv.number} voided.`, "warning");
          renderInvoiceDetail();
        }
      });
    }
  }

  function openPaymentModal(inv, balance) {
    openModal({
      title: `Register Payment — ${inv.number} (HU-10)`,
      bodyHtml: `
        <div id="payErr" class="form-error hidden"></div>
        <p style="margin-bottom:12px;font-size:0.9rem;">Remaining Balance: <strong>${money(balance)}</strong></p>
        <div class="form-grid">
          <div class="form-field">
            <label for="pmAmount">Payment Amount</label>
            <input id="pmAmount" class="input" type="number" step="0.01" min="0.01" max="${balance}" value="${balance}" />
          </div>
          <div class="form-field">
            <label for="pmMethod">Payment Method</label>
            <select id="pmMethod">
              <option value="CASH">CASH</option>
              <option value="CREDIT_CARD">CREDIT_CARD</option>
              <option value="DEBIT_CARD">DEBIT_CARD</option>
              <option value="TRANSFER">TRANSFER</option>
              <option value="QR">QR</option>
            </select>
          </div>
          <div class="form-field full">
            <label for="pmRef">Transaction Reference (Optional)</label>
            <input id="pmRef" class="input" type="text" placeholder="e.g., AUTH-55210, NEQUI-8821" />
          </div>
        </div>
      `,
      footerHtml: `
        <button class="btn btn-secondary" id="pmCancel">Cancel</button>
        <button class="btn btn-success" id="pmSave">Confirm Payment</button>
      `
    });
    $("#pmCancel").onclick = closeModal;
    $("#pmSave").onclick = () => {
      const amount = Number($("#pmAmount").value);
      const method = $("#pmMethod").value;
      const reference = $("#pmRef").value.trim();
      const err = $("#payErr");

      if (!(amount > 0)) {
        err.textContent = "Payment amount must be greater than zero.";
        err.classList.remove("hidden"); return;
      }
      if (amount > balance + 0.01) {
        err.textContent = `Overpayment rejected: maximum remaining balance is ${money(balance)}.`;
        err.classList.remove("hidden"); return;
      }

      inv.payments.push({ id: state.nextIds.payment++, amount, method, reference, date: nowStamp() });
      const totalPaid = calcInvoicePaid(inv);
      let msg = `Payment of ${money(amount)} registered.`;

      if (totalPaid >= inv.total - 0.01) {
        inv.paymentStatus = "PAID";
        const ord = state.orders.find((o) => o.id === inv.orderId);
        if (ord && tryReleaseTable(ord.tableId)) {
          msg = `Invoice ${inv.number} PAID in full! Table #${getTable(ord.tableId).number} released (RN-001).`;
        } else {
          msg = `Invoice ${inv.number} PAID in full!`;
        }
      } else {
        inv.paymentStatus = "PARTIAL";
      }

      closeModal();
      showToast(msg, "success");
      renderInvoiceDetail();
    };
  }

  // ==================== 8. CUSTOMERS ====================
  function renderCustomers() {
    $("#breadcrumb").textContent = "Home / Customers";
    $("#pageTitle").textContent = "Customer Directory (RF-007 / HU-11, HU-12)";
    $("#topbarActions").innerHTML = `<button class="btn btn-primary" id="btnAddCust">+ Register Customer</button>`;

    const q = state.filters.customerSearch.toLowerCase();
    const filtered = state.customers.filter((c) => {
      if (!c.active) return false;
      if (q && !c.name.toLowerCase().includes(q) && !c.email.toLowerCase().includes(q)) return false;
      return true;
    });

    $("#viewContainer").innerHTML = `
      <section class="panel">
        <div class="toolbar">
          <input id="fCustSearch" class="input" type="search" placeholder="Search by name or email..." value="${esc(state.filters.customerSearch)}" style="min-width:260px;" />
          <div style="font-size:0.84rem;color:var(--text-muted);">${filtered.length} active customers</div>
        </div>

        <div class="table-wrap">
          <table>
            <thead><tr><th>ID</th><th>Name</th><th>Phone</th><th>Email</th><th>Address</th><th>Orders</th><th>Actions</th></tr></thead>
            <tbody>
              ${filtered.map((c) => {
                const count = state.orders.filter((o) => o.customerId === c.id).length;
                return `<tr>
                  <td>#${c.id}</td>
                  <td><strong>${esc(c.name)}</strong></td>
                  <td>${esc(c.phone)}</td>
                  <td>${esc(c.email)}</td>
                  <td>${esc(c.address || "-")}</td>
                  <td><span class="badge badge-info">${count}</span></td>
                  <td style="white-space:nowrap;">
                    <button class="btn btn-secondary btn-sm" data-c-edit="${c.id}">Edit</button>
                    <button class="btn btn-danger btn-sm" data-c-del="${c.id}">Deactivate</button>
                  </td>
                </tr>`;
              }).join("")}
            </tbody>
          </table>
        </div>
      </section>
    `;

    $("#fCustSearch").oninput = (e) => { state.filters.customerSearch = e.target.value; renderCustomers(); };
    $("#btnAddCust").onclick = () => openCustomerFormModal(null);

    document.querySelectorAll("[data-c-edit]").forEach((b) => {
      b.onclick = () => openCustomerFormModal(getCustomer(Number(b.dataset.cEdit)));
    });
    document.querySelectorAll("[data-c-del]").forEach((b) => {
      b.onclick = () => {
        const c = getCustomer(Number(b.dataset.cDel));
        openConfirm({
          title: "Deactivate Customer",
          message: `Deactivate customer "${c.name}"? Their historical orders and invoices will be preserved.`,
          confirmLabel: "Deactivate",
          onConfirm: () => {
            c.active = false;
            showToast(`Customer "${c.name}" deactivated.`, "warning");
            render();
          }
        });
      };
    });
  }

  function openCustomerFormModal(cust) {
    const isEdit = Boolean(cust);
    openModal({
      title: isEdit ? `Edit Customer: ${cust.name}` : "Register Customer (HU-11)",
      bodyHtml: `
        <div id="cfErr" class="form-error hidden"></div>
        <div class="form-grid">
          <div class="form-field full">
            <label for="cfName">Full Name</label>
            <input id="cfName" class="input" type="text" value="${isEdit ? esc(cust.name) : ""}" />
          </div>
          <div class="form-field">
            <label for="cfPhone">Phone (7-20 chars)</label>
            <input id="cfPhone" class="input" type="text" placeholder="3104567890" value="${isEdit ? esc(cust.phone) : ""}" />
          </div>
          <div class="form-field">
            <label for="cfEmail">Email (Unique)</label>
            <input id="cfEmail" class="input" type="email" placeholder="name@email.com" value="${isEdit ? esc(cust.email) : ""}" />
          </div>
          <div class="form-field full">
            <label for="cfAddr">Address</label>
            <input id="cfAddr" class="input" type="text" value="${isEdit ? esc(cust.address) : ""}" />
          </div>
        </div>
      `,
      footerHtml: `
        <button class="btn btn-secondary" id="cfCancel">Cancel</button>
        <button class="btn btn-primary" id="cfSave">${isEdit ? "Save Changes" : "Register Customer"}</button>
      `
    });
    $("#cfCancel").onclick = closeModal;
    $("#cfSave").onclick = () => {
      const name = $("#cfName").value.trim();
      const phone = $("#cfPhone").value.trim();
      const email = $("#cfEmail").value.trim().toLowerCase();
      const address = $("#cfAddr").value.trim();
      const err = $("#cfErr");

      if (!name) { err.textContent = "Customer name is required."; err.classList.remove("hidden"); return; }
      if (!/^[0-9+()\s-]{7,20}$/.test(phone)) { err.textContent = "Phone must be 7-20 valid characters (digits, +, -, spaces)."; err.classList.remove("hidden"); return; }
      if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) { err.textContent = "Please provide a valid email address."; err.classList.remove("hidden"); return; }
      if (state.customers.some((c) => c.email.toLowerCase() === email && (!isEdit || c.id !== cust.id))) {
        err.textContent = "A customer with this email already exists.";
        err.classList.remove("hidden"); return;
      }

      if (isEdit) {
        Object.assign(cust, { name, phone, email, address });
        showToast(`Customer "${name}" updated.`, "success");
      } else {
        state.customers.push({ id: state.nextIds.customer++, name, phone, email, address, active: true });
        showToast(`Customer "${name}" registered.`, "success");
      }
      closeModal();
      render();
    };
  }

  // ==================== 9. REPORTS & ANALYTICS ====================
  function renderReports() {
    $("#breadcrumb").textContent = "Home / Reports & Analytics";
    $("#pageTitle").textContent = "Queries & Reports (RF-009, RF-010 / HU-13, HU-14, HU-15)";
    $("#topbarActions").innerHTML = `<button class="btn btn-secondary" id="btnRepRefresh">Refresh Data</button>`;

    const paidInvoices = state.invoices.filter((i) => i.paymentStatus === "PAID");
    const byMethod = { CASH: 0, CREDIT_CARD: 0, DEBIT_CARD: 0, TRANSFER: 0, QR: 0, MIXED: 0 };
    paidInvoices.forEach((inv) => {
      if (inv.payments.length > 0) {
        inv.payments.forEach((p) => { byMethod[p.method] = (byMethod[p.method] || 0) + p.amount; });
      } else {
        byMethod[inv.paymentMethod] = (byMethod[inv.paymentMethod] || 0) + inv.total;
      }
    });
    const totalSales = Object.values(byMethod).reduce((a, b) => a + b, 0);

    // Top dishes aggregation (non-cancelled orders)
    const dishStats = new Map();
    state.orders.filter((o) => o.status !== "CANCELLED").forEach((o) => {
      o.items.forEach((it) => {
        const cur = dishStats.get(it.dishId) || { dishId: it.dishId, qty: 0, revenue: 0 };
        cur.qty += it.quantity;
        cur.revenue += it.quantity * it.unitPrice;
        dishStats.set(it.dishId, cur);
      });
    });
    const topDishes = Array.from(dishStats.values()).sort((a, b) => b.qty - a.qty || b.revenue - a.revenue).slice(0, 10);
    const maxQty = topDishes.length > 0 ? topDishes[0].qty : 1;

    const statusFilteredOrders = state.orders.filter((o) => o.status === state.filters.reportOrderStatus);

    $("#viewContainer").innerHTML = `
      <div class="two-col">
        <section class="panel">
          <div class="panel-header">
            <h2>Daily Sales Breakdown (<code>v_daily_sales</code> / HU-13)</h2>
            <strong>${money(totalSales)}</strong>
          </div>
          <p style="font-size:0.84rem;color:var(--text-muted);margin-bottom:14px;">
            Consolidated from ${paidInvoices.length} PAID invoice(s) including 19% VAT.
          </p>
          <div class="bar-list">
            ${Object.entries(byMethod).filter(([, v]) => v > 0).map(([method, val]) => {
              const pct = totalSales > 0 ? Math.round((val / totalSales) * 100) : 0;
              return `
                <div>
                  <div class="bar-item-head"><span><strong>${method}</strong></span><span>${money(val)} (${pct}%)</span></div>
                  <div class="bar-track"><div class="bar-fill" style="width:${pct}%"></div></div>
                </div>
              `;
            }).join("") || `<div class="empty-state">No paid sales recorded yet.</div>`}
          </div>
        </section>

        <section class="panel">
          <div class="panel-header">
            <h2>Top 10 Best-Selling Dishes (<code>v_top_dishes</code> / HU-14)</h2>
          </div>
          <div class="bar-list">
            ${topDishes.map((row, idx) => {
              const d = getDish(row.dishId);
              const pct = Math.round((row.qty / maxQty) * 100);
              return `
                <div>
                  <div class="bar-item-head">
                    <span><strong>#${idx + 1} ${esc(d ? d.name : "Dish #" + row.dishId)}</strong> (${esc(d ? getCategoryName(d.categoryId) : "")})</span>
                    <span>${row.qty} sold &bull; ${money(row.revenue)}</span>
                  </div>
                  <div class="bar-track"><div class="bar-fill" style="width:${pct}%"></div></div>
                </div>
              `;
            }).join("") || `<div class="empty-state">No dish sales yet.</div>`}
          </div>
        </section>
      </div>

      <section class="panel">
        <div class="toolbar">
          <h2>Orders by Status Query (HU-15)</h2>
          <div class="filter-group">
            <label for="fRepOrdStatus">Status:</label>
            <select id="fRepOrdStatus">
              <option value="PENDING">PENDING</option>
              <option value="IN_PREPARATION">IN_PREPARATION</option>
              <option value="READY">READY</option>
              <option value="DELIVERED">DELIVERED</option>
              <option value="INVOICED">INVOICED</option>
              <option value="CANCELLED">CANCELLED</option>
            </select>
          </div>
        </div>
        ${statusFilteredOrders.length === 0 ? `<div class="empty-state">No orders in status ${esc(state.filters.reportOrderStatus)}.</div>` : `
          <div class="table-wrap">
            <table>
              <thead><tr><th>Order</th><th>Table</th><th>Customer</th><th>Items</th><th>Total</th><th>Action</th></tr></thead>
              <tbody>
                ${statusFilteredOrders.map((o) => `<tr>
                  <td><strong>#${o.id}</strong></td>
                  <td>Table #${getTable(o.tableId)?.number || o.tableId}</td>
                  <td>${getCustomer(o.customerId)?.name || "Walk-in"}</td>
                  <td>${o.items.reduce((s, i) => s + i.quantity, 0)}</td>
                  <td><strong>${money(calcOrderTotal(o))}</strong></td>
                  <td><button class="btn btn-secondary btn-sm" data-rep-order="${o.id}">View Detail</button></td>
                </tr>`).join("")}
              </tbody>
            </table>
          </div>
        `}
      </section>
    `;

    $("#btnRepRefresh").onclick = () => { renderReports(); showToast("Reports refreshed.", "info"); };
    $("#fRepOrdStatus").value = state.filters.reportOrderStatus;
    $("#fRepOrdStatus").onchange = (e) => { state.filters.reportOrderStatus = e.target.value; renderReports(); };
    document.querySelectorAll("[data-rep-order]").forEach((b) => {
      b.onclick = () => navigate("order-detail", { orderId: Number(b.dataset.repOrder) });
    });
  }

  // ==================== BOOTSTRAP ====================
  bindLoginEvents();
})();
