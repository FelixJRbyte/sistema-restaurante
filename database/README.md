# Phase 4 Deliverables - Relational Database

## Folder Contents

| File | Description |
|------|-------------|
| `restaurante_db.sql` | **Complete SQL script** (DDL + DML + Triggers + SPs + Views + Seed Data) |
| `ERD.md` | **Entity-Relationship Diagram** in Mermaid format + entity documentation |
| `MODELO_LOGICO.md` | **Formal Logical Model** (relational schema, functional dependencies, data dictionary) |

## How to Run the SQL Script

### Option 1: MySQL/MariaDB (Recommended)

```bash
# From terminal
mysql -u root -p < database/restaurante_db.sql

# Or inside MySQL
SOURCE database/restaurante_db.sql;
```

### Option 2: MySQL Workbench / DBeaver / phpMyAdmin
1. Open the `restaurante_db.sql` file
2. Execute completely (Ctrl+Shift+Enter / "Execute" button)

### Option 3: Windows Command Line (PowerShell)
```powershell
Get-Content database\restaurante_db.sql | mysql -u root -p
```

## What the Script Creates

### Structure (DDL)
- ✅ 8 tables normalized to 3NF
- ✅ Primary Keys, Foreign Keys, Unique Constraints
- ✅ Check Constraints (domain validation)
- ✅ ENUMs for controlled states
- ✅ Optimized indexes for typical queries
- ✅ 5 predefined views for common reports

### Business Logic (Triggers + SPs)
- ✅ 6 Triggers for automatic integrity:
  - Order total recalculation
  - Order status → invoice change
  - Invoice payment status update
  - Automatic table release
- ✅ 5 Stored Procedures encapsulating operations:
  - `sp_create_order()` - Validates free table
  - `sp_add_dish_to_order()` - Validates state and availability
  - `sp_generate_invoice()` - Calculates 19% VAT
  - `sp_register_payment()` - Supports partial/mixed payments
  - `sp_change_table_status()` - Validates business rules

### Test Data (DML)
- ✅ 6 menu categories
- ✅ 28 dishes with real prices and times
- ✅ 12 tables (different capacities/locations)
- ✅ 8 customers
- ✅ 7 orders in various states (PENDING, IN_PREPARATION, READY, DELIVERED, INVOICED, CANCELLED)
- ✅ 2 invoices (one paid, one with partial payment)
- ✅ 2 payments registered

## Quick Verification

After execution, verify:

```sql
-- Created tables
SHOW TABLES;

-- Seed record counts
SELECT 'dish_category' t, COUNT(*) c FROM dish_category
UNION ALL SELECT 'dish', COUNT(*) FROM dish
UNION ALL SELECT 'table_restaurant', COUNT(*) FROM table_restaurant
UNION ALL SELECT 'customer', COUNT(*) FROM customer
UNION ALL SELECT 'order', COUNT(*) FROM `order`
UNION ALL SELECT 'order_detail', COUNT(*) FROM order_detail
UNION ALL SELECT 'invoice', COUNT(*) FROM invoice
UNION ALL SELECT 'invoice_payment', COUNT(*) FROM invoice_payment;

-- Test views
SELECT * FROM v_active_menu LIMIT 5;
SELECT * FROM v_available_tables;
SELECT * FROM v_kitchen_orders;
SELECT * FROM v_pending_invoices;
SELECT * FROM v_daily_sales;
```

## Java Integration (Phase 3)

The model is designed to map directly to the project's Java classes:

| Table | Java Class (src/modelo/) | Current Status |
|-------|-------------------------|----------------|
| `table_restaurant` | `Mesa.java` | ✅ Complete |
| `customer` | `Cliente.java` | ⬜ Empty - complete |
| `dish` | `Plato.java` | ⬜ Empty - complete |
| `order` | `Pedido.java` | ⬜ Empty - complete |
| `invoice` | `Factura.java` | ⬜ Empty - complete |

**Next step**: Implement empty model classes using this schema as reference, and create DAOs/Repositories that use the Stored Procedures.

## Phase 4 Criteria Compliance

| Criterion | Status | Evidence |
|-----------|--------|----------|
| Identify entities | ✅ | 8 entities in ERD.md |
| Define attributes | ✅ | Dictionary in MODELO_LOGICO.md |
| Define PK | ✅ | All tables have PK (AUTO_INCREMENT) |
| Define FK relationships + cardinality | ✅ | 7 FKs documented in ERD.md |
| Normalize to 3NF | ✅ | Analysis in MODELO_LOGICO.md |
| Create ER Diagram | ✅ | ERD.md (Mermaid + table) |
| Create logical model | ✅ | MODELO_LOGICO.md |
| Create tables (DDL) | ✅ | restaurante_db.sql |
| Define constraints (NOT NULL, UNIQUE, CHECK, FK) | ✅ | All in DDL |
| Complete SQL script | ✅ | restaurante_db.sql |
| Mock/seed data (DML) | ✅ | Final section of .sql |

## Technical Notes

- **Engine**: InnoDB (transactions, FKs)
- **Charset**: utf8mb4 (emoji support, full Latin characters)
- **Collation**: utf8mb4_unicode_ci (correct Spanish sorting)
- **VAT**: 19% hardcoded in SP `sp_generate_invoice` (configurable if needed)
- **Mixed payments**: Supported via `INVOICE_PAYMENT` table and payment_method='MIXED' in invoice
- **Soft delete**: `active` field in CUSTOMER; tables use `MAINTENANCE` status

## Contact / Team

- **Group**: 04
- **Cohort**: 3239188
- **Program**: ADSO - SENA
- **Members**: Félix Antonio Murcia Cedeño, Sergio Andrés García Salazar, Diego Andrés Ávila Herrera
- **Repository**: https://github.com/FelixJRbyte/sistema-restaurante