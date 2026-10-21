# Phase 1: User Stories (HU-01 to HU-15)

**Project**: Restaurant Management System (Group 04)  
**Program**: Analysis and Software Development (ADSO - Cohort 3239188) - SENA  
**Authors**: Sergio Andrés García Salazar, Félix Antonio Murcia Cedeño, Diego Andrés Ávila Herrera  

---

## 1. Epic 1: Table Management

### HU-01 — Register Table
- **As an** Administrator,
- **I want to** register a new table with its number, seating capacity, and location,
- **So that** the restaurant's physical table inventory is available in the system.
- **Acceptance Criteria**:
  1. Duplicate table numbers (`number`) are rejected by unique constraint (`uk_table_number`).
  2. Capacity must be between 1 and 20 diners (`chk_table_capacity`).
  3. Newly created tables default to `FREE` status.

### HU-02 — Query Available Tables
- **As a** Waiter,
- **I want to** view available tables filtered by minimum seating capacity,
- **So that** I can quickly assign arriving customers to a suitable table.
- **Acceptance Criteria**:
  1. Only tables in `FREE` status meeting the minimum capacity are returned (`v_available_tables`).
  2. If no tables match, the system displays a clear message.

### HU-03 — Update Table Status
- **As a** Waiter or Administrator,
- **I want to** update the status of a table (`FREE`, `OCCUPIED`, `RESERVED`, `MAINTENANCE`),
- **So that** the system reflects the real-time state of the dining hall.
- **Acceptance Criteria**:
  1. Status must belong to the valid domain (`FREE`, `OCCUPIED`, `RESERVED`, `MAINTENANCE`).
  2. A table in `OCCUPIED` status cannot be manually changed to `FREE` while it has active orders (`sp_change_table_status`).

---

## 2. Epic 2: Menu Management

### HU-04 — Register Dish
- **As an** Administrator,
- **I want to** add a new dish with its category, name, description, price, and preparation time,
- **So that** it can be ordered by customers.
- **Acceptance Criteria**:
  1. Each dish receives an auto-incremented unique ID (`id_dish`).
  2. Price must be greater than zero and preparation time between 1 and 180 minutes.
  3. New dishes default to `available = TRUE`.

### HU-05 — Update Dish
- **As an** Administrator,
- **I want to** modify the name, description, category, price, preparation time, or availability of an existing dish,
- **So that** the menu remains accurate when ingredients or prices change.
- **Acceptance Criteria**:
  1. Individual fields can be updated while keeping existing values for unchanged fields.
  2. Historical orders retain the `unit_price` recorded at the time the order was placed.

### HU-06 — Remove / Deactivate Dish
- **As an** Administrator,
- **I want to** deactivate or remove a dish from the active menu,
- **So that** discontinued items can no longer be ordered without breaking historical order records.
- **Acceptance Criteria**:
  1. Removing a dish performs a soft delete (`available = FALSE`), preserving foreign key integrity in `order_detail`.

### HU-07 — Query Active Menu
- **As a** Waiter,
- **I want to** list available dishes grouped by category or search dishes by name,
- **So that** I can inform customers and take their orders accurately.
- **Acceptance Criteria**:
  1. Displays dish ID, name, category, price, and estimated preparation time (`v_active_menu`).

---

## 3. Epic 3: Order Management

### HU-08 — Create Order
- **As a** Waiter,
- **I want to** open a new order associated with an available table and an optional customer,
- **So that** I can begin recording the customer's consumption.
- **Acceptance Criteria**:
  1. Orders can only be created on tables in `FREE` status (`sp_create_order`).
  2. Creating the order automatically transitions the table status to `OCCUPIED` and initializes the order in `PENDING` status.

### HU-09 — Add or Cancel Order Items
- **As a** Waiter,
- **I want to** add available dishes with quantities to an open order, or cancel an item with Administrator authorization,
- **So that** the order reflects the exact items prepared for the table.
- **Acceptance Criteria**:
  1. Items can only be added to orders in `PENDING` or `IN_PREPARATION` status (`sp_add_dish_to_order`).
  2. Unavailable dishes (`available = FALSE`) are rejected.
  3. Adding the first dish transitions the order from `PENDING` to `IN_PREPARATION`.
  4. Removing an item from an active order requires Administrator PIN validation (`RN-002`).
  5. Database triggers (`trg_order_detail_after_*`) automatically recalculate the order total.

### HU-10 — Update & Finalize Order Preparation
- **As a** Cook / Waiter,
- **I want to** view pending kitchen orders and advance the order status (`IN_PREPARATION` → `READY` → `DELIVERED`),
- **So that** kitchen staff and waiters coordinate dish preparation and table delivery.
- **Acceptance Criteria**:
  1. Kitchen view (`v_kitchen_orders`) lists all orders in `PENDING` and `IN_PREPARATION` status ordered by arrival time.
  2. Cancelling an order (`CANCELLED`) automatically releases the table back to `FREE`.

---

## 4. Epic 4: Invoicing & Payments

### HU-11 — Generate Invoice
- **As a** Cashier,
- **I want to** generate an invoice for a delivered/ready order specifying the payment method,
- **So that** subtotal, 19% VAT taxes, and total payable are calculated automatically.
- **Acceptance Criteria**:
  1. An order can only be invoiced once (`uk_invoice_order` and validation in `sp_generate_invoice`).
  2. Calculates 19% VAT (`taxes = subtotal * 0.19`) and transitions the order to `INVOICED`.
  3. If paid in `CASH`, the invoice is immediately marked `PAID` and the table is released to `FREE`.

### HU-12 — Query Invoices & Register Payments
- **As a** Cashier,
- **I want to** query pending invoices by number or status and register partial or full payments,
- **So that** customers can pay with single or mixed payment methods (`CASH`, `CREDIT_CARD`, `DEBIT_CARD`, `TRANSFER`, `QR`).
- **Acceptance Criteria**:
  1. Registering payments via `sp_register_payment` updates `invoice.payment_status` to `PARTIAL` or `PAID` (`trg_invoice_payment_after_insert`).
  2. Once total payments reach the invoice total (`PAID`), the associated table is automatically released to `FREE`.

---

## 5. Epic 5: Reports & Analytics

### HU-13 — Query Daily Sales
- **As an** Administrator,
- **I want to** consult daily total sales broken down by payment method,
- **So that** I can audit daily cash flow and financial performance.
- **Acceptance Criteria**:
  1. Consolidates paid invoices by date showing total invoices, total revenue, cash, card, transfer, and QR totals (`v_daily_sales`).

### HU-14 — Query Top 10 Best-Selling Dishes
- **As an** Administrator,
- **I want to** view the ranking of the top 10 most sold dishes by units and revenue,
- **So that** I can make data-driven decisions on menu engineering and inventory.
- **Acceptance Criteria**:
  1. Aggregates non-cancelled order details ordered descending by total units sold and revenue (`v_top_dishes`).

### HU-15 — Query Orders by Status & Customer
- **As an** Administrator,
- **I want to** filter orders by status (`PENDING`, `IN_PREPARATION`, `READY`, `DELIVERED`, `CANCELLED`, `INVOICED`) or customer,
- **So that** I can supervise operational bottlenecks and customer history.
- **Acceptance Criteria**:
  1. Returns matching orders with table number, customer name, item count, and total amount.

---

## 6. MoSCoW Prioritization

| Priority | User Stories | Justification |
| :--- | :--- | :--- |
| **Must Have** | `HU-01` to `HU-12` | Core operational cycle: tables, menu, orders, kitchen, invoicing, and payments. |
| **Should Have** | `HU-13`, `HU-14`, `HU-15` | Managerial reporting, sales analytics, and Top 10 dish ranking. |

---

## 7. Unified Domain Glossary

| Domain | Status / Value | Meaning |
| :--- | :--- | :--- |
| **Table (`table_restaurant`)** | `FREE` | Available to seat new customers. |
| **Table (`table_restaurant`)** | `OCCUPIED` | Assigned to customers with an active order. |
| **Table (`table_restaurant`)** | `RESERVED` | Reserved for upcoming guests. |
| **Table (`table_restaurant`)** | `MAINTENANCE` | Out of service / soft-deleted. |
| **Order (`order_restaurant`)** | `PENDING` | Order opened, awaiting dish selection. |
| **Order (`order_restaurant`)** | `IN_PREPARATION` | Dishes sent to kitchen and being prepared. |
| **Order (`order_restaurant`)** | `READY` | Dishes prepared and ready to be served. |
| **Order (`order_restaurant`)** | `DELIVERED` | Dishes served at the table, ready for invoicing. |
| **Order (`order_restaurant`)** | `INVOICED` | Invoice generated for the order. |
| **Order (`order_restaurant`)** | `CANCELLED` | Order cancelled; table automatically released to `FREE`. |
| **Invoice (`invoice`)** | `PENDING` / `PARTIAL` / `PAID` / `VOIDED` | Payment lifecycle states of an issued invoice. |
