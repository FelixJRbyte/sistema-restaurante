# Phase 2: Software Requirements Specification (IEEE 830)

**Project**: Restaurant Management System (Group 04)  
**Standard**: IEEE Std 830  
**Cohort**: 3239188 - SENA ADSO  

---

## 1. Introduction

### 1.1 Purpose
This document formally specifies the functional requirements (RF), non-functional requirements (RNF), business rules (RN), technical constraints (RC), and traceability matrix for the **Restaurant Management System (Group 04)**. It addresses operational challenges in table management, menu administration, kitchen order tracking, automated tax billing, and managerial reporting.

### 1.2 Scope
The system provides a complete layered Java application backed by a 3NF MySQL relational database:
- **Included in Scope**: Table management, menu & category catalog, order creation and lifecycle tracking, kitchen order display, invoice generation with 19% VAT, partial/mixed payment registration, automatic table release, customer management, and sales/product analytics.
- **Out of Scope**: External customer online booking portal, GPS delivery tracking, and automated supplier procurement.

---

## 2. Overall Description

### 2.1 User Roles
- **Administrator**: Configures tables and menu items, authorizes restricted item cancellations (`RN-002`), and consults sales/ranking reports.
- **Waiter**: Queries available tables, opens orders, adds dishes with notes, and updates delivery states.
- **Cook / Kitchen Staff**: Consults pending kitchen orders (`v_kitchen_orders`) and updates preparation states (`IN_PREPARATION`, `READY`).
- **Cashier**: Generates invoices, processes single or mixed payments, and verifies pending balances.

---

## 3. Specific Requirements

### 3.1 Functional Requirements (RF)

| ID | Requirement | Description | Implemented In |
| :--- | :--- | :--- | :--- |
| **RF-001** | Table Management & Availability | Allows viewing all tables, available tables by minimum capacity, occupied tables, and registering/deleting tables. | `MesaDAO`, `GestionMesas`, `v_available_tables` |
| **RF-002** | Table Status Control | Manages table state transitions (`FREE`, `OCCUPIED`, `RESERVED`, `MAINTENANCE`) and blocks freeing tables with active orders. | `sp_change_table_status`, `MesaDAO` |
| **RF-003** | Menu & Category Catalog | Allows creating, updating, searching by name, and deactivating dishes and categories, validating `price > 0`. | `PlatoDAO`, `CategoriaPlatoDAO`, `GestionMenu` |
| **RF-004** | Order Registration | Opens an order on a `FREE` table, assigns an optional customer, and automatically transitions the table to `OCCUPIED`. | `sp_create_order`, `PedidoDAO` |
| **RF-005** | Order Item Addition & Cancellation | Adds available dishes to open orders (`sp_add_dish_to_order`) and allows cancelling items with Admin PIN validation (`RN-002`). | `sp_add_dish_to_order`, `PedidoDAO`, `Main` |
| **RF-006** | Kitchen Order Visualization | Displays active kitchen orders (`PENDING`, `IN_PREPARATION`) ordered by timestamp and allows advancing state to `READY` / `DELIVERED`. | `v_kitchen_orders`, `PedidoDAO` |
| **RF-007** | Invoice Generation | Generates an invoice for an order, automatically calculating 19% VAT (`taxes`) and updating order status to `INVOICED`. | `sp_generate_invoice`, `FacturaDAO` |
| **RF-008** | Payment Processing | Registers full, partial, or mixed payments (`CASH`, `CREDIT_CARD`, `DEBIT_CARD`, `TRANSFER`, `QR`) and releases the table upon full payment. | `sp_register_payment`, `trg_invoice_payment_after_insert` |
| **RF-009** | Daily Sales Report | Consolidates daily paid invoices and revenue segmented by payment method. | `v_daily_sales`, `FacturaDAO.listarVentasDiarias` |
| **RF-010** | Top 10 Best-Selling Dishes Report | Generates a strategic ranking of the top 10 dishes by units sold and total revenue. | `v_top_dishes`, `PlatoDAO.listarMasVendidos` |

---

### 3.2 Non-Functional Requirements (RNF)

- **RNF-001 Performance**: All SQL queries and Stored Procedures execute in `< 100 ms` on local LAN using composite indexes (`idx_order_table_status`, `idx_order_date`, `idx_invoice_date`, `idx_dish_category`).
- **RNF-002 Security & Authorization**: Sensitive operations (such as cancelling an order item) require Administrator PIN validation (`RN-002`), and all database queries use parameterized `PreparedStatement` / `CallableStatement` to prevent SQL Injection.
- **RNF-003 Reliability & Transactional Integrity**: All write operations run with manual transaction control (`autoCommit = false`, explicit `commit()` / `rollback()`) over InnoDB tables with foreign key constraints.
- **RNF-004 Usability**: Structured interactive console menu with input validation, confirmation prompts on destructive actions, and clear tabular formatting.
- **RNF-005 Maintainability**: Strict separation of concerns across 3 layers (`modelo`, `dao`, `servicios`) plus OOP inheritance (`EntidadBase`) and automated JUnit 5 tests in CI.

---

### 3.3 Business Rules (RN)

- **RN-001 Automatic Table Lifecycle**: Opening an order transitions the table from `FREE` to `OCCUPIED`. Paying the invoice in full (`PAID`) or cancelling the order (`CANCELLED`) automatically releases the table back to `FREE`.
- **RN-002 Item Cancellation Authorization**: Removing a dish from an active order requires prior validation of the Administrator PIN (`Main.anularItemDePedido`).
- **RN-003 Mandatory Tax Calculation**: Every invoice automatically calculates and applies 19% VAT (`taxes = subtotal * 0.19`) in `sp_generate_invoice`.
- **RN-004 Active Order Protection**: A table with active orders (`PENDING`, `IN_PREPARATION`, `READY`, `DELIVERED`) cannot be manually set to `FREE` or deleted (`sp_change_table_status`).

---

### 3.4 Technical Constraints (RC)

- **RC-001 Language & Build**: Java 17 (`maven-compiler-plugin` target 17) with Maven (`pom.xml`).
- **RC-002 Database Engine**: MySQL 8.0+ using InnoDB, `utf8mb4`, triggers, views, and stored procedures.
- **RC-003 Architecture**: Layered OOP architecture (`modelo`, `dao`, `servicios`, `Main`) using JDBC (`mysql-connector-j`).
- **RC-004 Continuous Integration**: Automated verification via GitHub Actions (`.github/workflows/ci.yml`).

---

## 4. Traceability Matrix (100% Aligned Phase 1 ↔ Phase 2 ↔ Phase 3/4)

| Phase 1 User Story | Phase 2 Functional Requirement | Priority | Phase 3 Java Classes | Phase 4 DB Objects |
| :--- | :--- | :---: | :--- | :--- |
| **HU-01**, **HU-02** | **RF-001** Table Management & Availability | Must Have | `Mesa`, `MesaDAO`, `GestionMesas` | `table_restaurant`, `v_available_tables` |
| **HU-03** | **RF-002** Table Status Control | Must Have | `Mesa`, `MesaDAO`, `GestionMesas` | `sp_change_table_status` |
| **HU-04**, **HU-05**, **HU-06**, **HU-07** | **RF-003** Menu & Category Catalog | Must Have | `Plato`, `CategoriaPlato`, `PlatoDAO`, `GestionMenu` | `dish`, `dish_category`, `v_active_menu` |
| **HU-08** | **RF-004** Order Registration | Must Have | `Pedido`, `PedidoDAO`, `GestionPedidos` | `order_restaurant`, `sp_create_order` |
| **HU-09** | **RF-005** Order Item Addition & Cancellation | Must Have | `DetallePedido`, `PedidoDAO`, `GestionPedidos` | `order_detail`, `sp_add_dish_to_order`, `trg_order_detail_*` |
| **HU-10** | **RF-006** Kitchen Order Visualization | Must Have | `Pedido`, `PedidoDAO`, `GestionPedidos` | `v_kitchen_orders`, `trg_order_after_update` |
| **HU-11** | **RF-007** Invoice Generation | Must Have | `Factura`, `FacturaDAO`, `GestionFacturas` | `invoice`, `sp_generate_invoice`, `trg_invoice_after_insert` |
| **HU-12** | **RF-008** Payment Processing | Must Have | `Factura`, `FacturaDAO`, `GestionFacturas` | `invoice_payment`, `sp_register_payment`, `trg_invoice_payment_after_insert` |
| **HU-13** | **RF-009** Daily Sales Report | Should Have | `Factura`, `FacturaDAO`, `GestionFacturas` | `v_daily_sales` |
| **HU-14** | **RF-010** Top 10 Best-Selling Dishes Report | Should Have | `Plato`, `PlatoDAO`, `GestionMenu` | `v_top_dishes` |
| **HU-15** | **RF-006** / **RF-009** Orders by Status & Customer | Should Have | `Pedido`, `PedidoDAO`, `GestionPedidos` | `order_restaurant`, `idx_order_table_status` |
