# Logical Data Model - Restaurant Management System

## Relational Schema (Formal Notation)

### Base Relations

```
CUSTOMER(id_customer, name, phone, email, address, registration_date, active)
    PK: id_customer
    UK: email
    CHECK: valid phone format

TABLE_RESTAURANT(id_table, number, capacity, status, location)
    PK: id_table
    UK: number
    CHECK: capacity > 0 AND capacity <= 20
    CHECK: number > 0
    DOMAIN status: {FREE, OCCUPIED, RESERVED, MAINTENANCE}

DISH_CATEGORY(id_category, name, description, display_order)
    PK: id_category
    UK: name

DISH(id_dish, id_category, name, description, price, available, prep_time_minutes)
    PK: id_dish
    FK: id_category → DISH_CATEGORY(id_category) [ON UPDATE CASCADE, ON DELETE RESTRICT]
    CHECK: price >= 0
    CHECK: prep_time_minutes > 0 AND prep_time_minutes <= 180

ORDER(id_order, id_table, id_customer, order_datetime, status, notes, total)
    PK: id_order
    FK: id_table → TABLE_RESTAURANT(id_table) [ON UPDATE CASCADE, ON DELETE RESTRICT]
    FK: id_customer → CUSTOMER(id_customer) [ON UPDATE CASCADE, ON DELETE SET NULL]
    CHECK: total >= 0
    DOMAIN status: {PENDING, IN_PREPARATION, READY, DELIVERED, CANCELLED, INVOICED}

ORDER_DETAIL(id_detail, id_order, id_dish, quantity, unit_price, subtotal, notes)
    PK: id_detail
    FK: id_order → ORDER(id_order) [ON UPDATE CASCADE, ON DELETE CASCADE]
    FK: id_dish → DISH(id_dish) [ON UPDATE CASCADE, ON DELETE RESTRICT]
    UK: (id_order, id_dish)
    CHECK: quantity > 0
    CHECK: unit_price >= 0
    GENERATED: subtotal = quantity * unit_price

INVOICE(id_invoice, id_order, issue_date, subtotal, taxes, total, payment_method, payment_status, invoice_number, notes)
    PK: id_invoice
    FK: id_order → ORDER(id_order) [ON UPDATE CASCADE, ON DELETE RESTRICT]
    UK: id_order (1:1 relationship)
    UK: invoice_number
    CHECK: subtotal >= 0
    CHECK: taxes >= 0
    CHECK: total >= 0
    DOMAIN payment_method: {CASH, CREDIT_CARD, DEBIT_CARD, TRANSFER, QR, MIXED}
    DOMAIN payment_status: {PENDING, PAID, VOIDED, PARTIAL}

INVOICE_PAYMENT(id_payment, id_invoice, payment_date, amount, payment_method, reference)
    PK: id_payment
    FK: id_invoice → INVOICE(id_invoice) [ON UPDATE CASCADE, ON DELETE CASCADE]
    CHECK: amount > 0
    DOMAIN payment_method: {CASH, CREDIT_CARD, DEBIT_CARD, TRANSFER, QR}
```

## Functional Dependency Analysis

### CUSTOMER
- id_customer → {name, phone, email, address, registration_date, active}
- email → id_customer (unique)

### TABLE_RESTAURANT
- id_table → {number, capacity, status, location}
- number → id_table (unique)

### DISH_CATEGORY
- id_category → {name, description, display_order}
- name → id_category (unique)

### DISH
- id_dish → {id_category, name, description, price, available, prep_time_minutes}
- No transitive dependencies (id_category is FK, doesn't determine other DISH attributes)

### ORDER
- id_order → {id_table, id_customer, order_datetime, status, notes, total}
- total is functionally derived from ORDER_DETAIL (SUM(subtotal))

### ORDER_DETAIL
- id_detail → {id_order, id_dish, quantity, unit_price, notes}
- (id_order, id_dish) → id_detail (alternate candidate key)
- subtotal = quantity × unit_price (full functional dependency)

### INVOICE
- id_invoice → {id_order, issue_date, subtotal, taxes, total, payment_method, payment_status, invoice_number, notes}
- id_order → id_invoice (1:1)
- invoice_number → id_invoice (unique)
- total = subtotal + taxes (functional dependency)

### INVOICE_PAYMENT
- id_payment → {id_invoice, payment_date, amount, payment_method, reference}

## Business Rules Implemented at Database Level

### Referential Integrity Constraints
1. **CASCADE UPDATE** on all FKs: PK changes propagated
2. **RESTRICT DELETE** on: DISH_CATEGORY→DISH, TABLE_RESTAURANT→ORDER, DISH→ORDER_DETAIL, ORDER→INVOICE
   - Prevents deletion of actively referenced data
3. **SET NULL** on: CUSTOMER→ORDER
   - If customer deleted, order retains history without customer link
4. **CASCADE DELETE** on: ORDER→ORDER_DETAIL, INVOICE→INVOICE_PAYMENT
   - Deleting order removes its lines; deleting invoice removes its payments

### Domain Constraints (CHECK)
- Table capacity: 1-20 diners
- Prices ≥ 0
- Quantities > 0
- Prep time: 1-180 minutes
- Totals ≥ 0
- Valid phone format (regex)

### Uniqueness Constraints
- Table number unique
- Customer email unique
- Category name unique
- Invoice number unique
- One order = maximum one invoice
- One dish per order (quantity updated if repeated)

## Mapping to Java Classes (Phase 3)

| DB Table | Java Class | Main Attributes |
|----------|------------|-----------------|
| *(Abstract Base)* | `EntidadBase` | id, fechaCreacion, obtenerResumen(), esValido() |
| TABLE_RESTAURANT | `Mesa` | idMesa, numero, capacidad, estado, ubicacion |
| CUSTOMER | `Cliente` | idCliente, nombre, telefono, email, direccion, activo |
| DISH_CATEGORY | `CategoriaPlato` | idCategoria, nombre, descripcion, orden |
| DISH | `Plato` | idPlato, idCategoria, nombre, descripcion, precio, disponible |
| ORDER_RESTAURANT | `Pedido` | idPedido, idMesa, idCliente, fechaHora, estado, detalles, total |
| ORDER_DETAIL | `DetallePedido` | idDetalle, idPedido, idPlato, cantidad, precioUnitario, subtotal |
| INVOICE | `Factura` | idFactura, idPedido, subtotal, impuestos, total, metodoPago, estadoPago |

**Note**: All model classes in `src/modelo/` are fully implemented and inherit from `EntidadBase`, applying OOP encapsulation, inheritance, polymorphism, and composition.

## Data Dictionary

| Table | Field | Type | Length | Null | Default | Description |
|-------|-------|------|--------|------|---------|-------------|
| CUSTOMER | id_customer | INT | 11 | NO | AUTO_INCREMENT | PK |
| CUSTOMER | name | VARCHAR | 100 | NO | | Full name |
| CUSTOMER | phone | VARCHAR | 20 | YES | | Contact phone |
| CUSTOMER | email | VARCHAR | 100 | YES | | Unique email |
| CUSTOMER | address | VARCHAR | 200 | YES | | Address |
| CUSTOMER | registration_date | TIMESTAMP | | NO | CURRENT_TIMESTAMP | Registration |
| CUSTOMER | active | BOOLEAN | | NO | TRUE | Status |
| TABLE_RESTAURANT | id_table | INT | 11 | NO | AUTO_INCREMENT | PK |
| TABLE_RESTAURANT | number | INT | 11 | NO | | Visible number |
| TABLE_RESTAURANT | capacity | INT | 11 | NO | | Max diners |
| TABLE_RESTAURANT | status | ENUM | | NO | FREE | Table status |
| TABLE_RESTAURANT | location | VARCHAR | 50 | YES | | Restaurant zone |
| DISH_CATEGORY | id_category | INT | 11 | NO | AUTO_INCREMENT | PK |
| DISH_CATEGORY | name | VARCHAR | 50 | NO | | Category name |
| DISH_CATEGORY | description | VARCHAR | 200 | YES | | Description |
| DISH_CATEGORY | display_order | INT | 11 | NO | 0 | Visual order |
| DISH | id_dish | INT | 11 | NO | AUTO_INCREMENT | PK |
| DISH | id_category | INT | 11 | NO | | FK Category |
| DISH | name | VARCHAR | 100 | NO | | Dish name |
| DISH | description | VARCHAR | 300 | YES | | Description |
| DISH | price | DECIMAL | 10,2 | NO | | Sale price |
| DISH | available | BOOLEAN | | NO | TRUE | Available |
| DISH | prep_time_minutes | INT | 11 | NO | 15 | Est. minutes |
| ORDER | id_order | INT | 11 | NO | AUTO_INCREMENT | PK |
| ORDER | id_table | INT | 11 | NO | | FK Table |
| ORDER | id_customer | INT | 11 | YES | | FK Customer |
| ORDER | order_datetime | TIMESTAMP | | NO | CURRENT_TIMESTAMP | Creation |
| ORDER | status | ENUM | | NO | PENDING | Order status |
| ORDER | notes | VARCHAR | 500 | YES | | Notes |
| ORDER | total | DECIMAL | 10,2 | NO | 0.00 | Calculated total |
| ORDER_DETAIL | id_detail | INT | 11 | NO | AUTO_INCREMENT | PK |
| ORDER_DETAIL | id_order | INT | 11 | NO | | FK Order |
| ORDER_DETAIL | id_dish | INT | 11 | NO | | FK Dish |
| ORDER_DETAIL | quantity | INT | 11 | NO | 1 | Units |
| ORDER_DETAIL | unit_price | DECIMAL | 10,2 | NO | | Historical price |
| ORDER_DETAIL | subtotal | DECIMAL | 10,2 | NO | GENERATED | Calculated |
| ORDER_DETAIL | notes | VARCHAR | 300 | YES | | Item notes |
| INVOICE | id_invoice | INT | 11 | NO | AUTO_INCREMENT | PK |
| INVOICE | id_order | INT | 11 | NO | | FK Order (1:1) |
| INVOICE | issue_date | TIMESTAMP | | NO | CURRENT_TIMESTAMP | Issue |
| INVOICE | subtotal | DECIMAL | 10,2 | NO | | Before taxes |
| INVOICE | taxes | DECIMAL | 10,2 | NO | 0.00 | VAT 19% |
| INVOICE | total | DECIMAL | 10,2 | NO | | With taxes |
| INVOICE | payment_method | ENUM | | NO | | Payment method |
| INVOICE | payment_status | ENUM | | NO | PENDING | Payment status |
| INVOICE | invoice_number | VARCHAR | 20 | NO | | Sequential number |
| INVOICE | notes | VARCHAR | 500 | YES | | Notes |
| INVOICE_PAYMENT | id_payment | INT | 11 | NO | AUTO_INCREMENT | PK |
| INVOICE_PAYMENT | id_invoice | INT | 11 | NO | | FK Invoice |
| INVOICE_PAYMENT | payment_date | TIMESTAMP | | NO | CURRENT_TIMESTAMP | Payment |
| INVOICE_PAYMENT | amount | DECIMAL | 10,2 | NO | | Amount paid |
| INVOICE_PAYMENT | payment_method | ENUM | | NO | | Payment method |
| INVOICE_PAYMENT | reference | VARCHAR | 100 | YES | | Auth code |

## Typical System Queries

### 1. Get active menu by category
```sql
SELECT * FROM v_active_menu;
```

### 2. Free tables for new order
```sql
SELECT * FROM v_available_tables WHERE capacity >= ?;
```

### 3. Kitchen orders (pending/preparation)
```sql
SELECT * FROM v_kitchen_orders;
```

### 4. Pending invoices
```sql
SELECT * FROM v_pending_invoices;
```

### 5. Daily sales
```sql
SELECT * FROM v_daily_sales WHERE date = CURDATE();
```

### 6. Customer history
```sql
SELECT o.*, i.invoice_number, i.total, i.payment_status
FROM order_restaurant o
LEFT JOIN invoice i ON o.id_order = i.id_order
WHERE o.id_customer = ?
ORDER BY o.order_datetime DESC;
```

### 7. Order detail for printing
```sql
SELECT od.quantity, d.name, od.unit_price, od.subtotal, od.notes
FROM order_detail od
JOIN dish d ON od.id_dish = d.id_dish
WHERE od.id_order = ?;
```

### 8. Top 10 best-selling dishes
```sql
SELECT * FROM v_top_dishes LIMIT 10;
```

## Migration / Versioning Strategy

```sql
-- Schema version control table
CREATE TABLE schema_version (
    version VARCHAR(20) PRIMARY KEY,
    applied_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    description VARCHAR(200)
);

INSERT INTO schema_version VALUES ('1.0.0', NOW(), 'Phase 4 initial schema');
```

## Performance Considerations

1. **Composite indexes** on frequently filtered columns (table+status, date)
2. **Materialized views** not needed (transactional data, real-time)
3. **Future partitioning** by date in ORDER/INVOICE if growth demands
4. **Lightweight triggers**: only update totals/statuses, no complex calculations
5. **Prepared Statements** mandatory in Java to prevent SQL Injection

## Security

- Dedicated DB user with minimal permissions (SELECT, INSERT, UPDATE, EXECUTE)
- No DROP, ALTER, GRANT permissions in production
- Stored Procedures encapsulate critical business logic
- Dual validation: DB (constraints) + Java (pre-validation)