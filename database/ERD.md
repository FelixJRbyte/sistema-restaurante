# Entity-Relationship Model - Restaurant Management System

## ER Diagram (Mermaid)

```mermaid
erDiagram
    CUSTOMER ||--o{ ORDER : places
    TABLE_RESTAURANT ||--o{ ORDER : "has"
    ORDER ||--o{ ORDER_DETAIL : contains
    DISH ||--o{ ORDER_DETAIL : "included in"
    DISH_CATEGORY ||--o{ DISH : classifies
    ORDER ||--|| INVOICE : generates
    INVOICE ||--o{ INVOICE_PAYMENT : "has payments"

    CUSTOMER {
        INT id_customer PK
        VARCHAR name
        VARCHAR phone
        VARCHAR email UK
        VARCHAR address
        TIMESTAMP registration_date
        BOOLEAN active
    }

    TABLE_RESTAURANT {
        INT id_table PK
        INT number UK
        INT capacity
        ENUM status
        VARCHAR location
    }

    DISH_CATEGORY {
        INT id_category PK
        VARCHAR name UK
        VARCHAR description
        INT display_order
    }

    DISH {
        INT id_dish PK
        INT id_category FK
        VARCHAR name
        VARCHAR description
        DECIMAL price
        BOOLEAN available
        INT prep_time_minutes
    }

    ORDER {
        INT id_order PK
        INT id_table FK
        INT id_customer FK
        TIMESTAMP order_datetime
        ENUM status
        VARCHAR notes
        DECIMAL total
    }

    ORDER_DETAIL {
        INT id_detail PK
        INT id_order FK
        INT id_dish FK
        INT quantity
        DECIMAL unit_price
        DECIMAL subtotal "GENERATED"
        VARCHAR notes
    }

    INVOICE {
        INT id_invoice PK
        INT id_order FK UK
        TIMESTAMP issue_date
        DECIMAL subtotal
        DECIMAL taxes
        DECIMAL total
        ENUM payment_method
        ENUM payment_status
        VARCHAR invoice_number UK
        VARCHAR notes
    }

    INVOICE_PAYMENT {
        INT id_payment PK
        INT id_invoice FK
        TIMESTAMP payment_date
        DECIMAL amount
        ENUM payment_method
        VARCHAR reference
    }
```

## Entities and Attributes

### 1. CUSTOMER
| Attribute | Type | Constraints | Description |
|-----------|------|-------------|-------------|
| id_customer | INT | PK, AI | Unique identifier |
| name | VARCHAR(100) | NOT NULL | Full name |
| phone | VARCHAR(20) | CHECK (format) | Contact phone |
| email | VARCHAR(100) | UNIQUE | Unique email |
| address | VARCHAR(200) | | Address |
| registration_date | TIMESTAMP | DEFAULT NOW() | Registration date |
| active | BOOLEAN | DEFAULT TRUE | Customer status |

### 2. TABLE_RESTAURANT
| Attribute | Type | Constraints | Description |
|-----------|------|-------------|-------------|
| id_table | INT | PK, AI | Unique identifier |
| number | INT | NOT NULL, UNIQUE, >0 | Visible table number |
| capacity | INT | NOT NULL, >0, ≤20 | Maximum diners |
| status | ENUM | NOT NULL, DEFAULT 'FREE' | FREE, OCCUPIED, RESERVED, MAINTENANCE |
| location | VARCHAR(50) | | Restaurant zone |

### 3. DISH_CATEGORY
| Attribute | Type | Constraints | Description |
|-----------|------|-------------|-------------|
| id_category | INT | PK, AI | Unique identifier |
| name | VARCHAR(50) | NOT NULL, UNIQUE | Category name |
| description | VARCHAR(200) | | Description |
| display_order | INT | DEFAULT 0 | Menu display order |

### 4. DISH
| Attribute | Type | Constraints | Description |
|-----------|------|-------------|-------------|
| id_dish | INT | PK, AI | Unique identifier |
| id_category | INT | NOT NULL, FK | Category |
| name | VARCHAR(100) | NOT NULL | Dish name |
| description | VARCHAR(300) | | Description |
| price | DECIMAL(10,2) | NOT NULL, ≥0 | Sale price |
| available | BOOLEAN | DEFAULT TRUE | Availability |
| prep_time_minutes | INT | DEFAULT 15, >0, ≤180 | Estimated minutes |

### 5. ORDER
| Attribute | Type | Constraints | Description |
|-----------|------|-------------|-------------|
| id_order | INT | PK, AI | Unique identifier |
| id_table | INT | NOT NULL, FK | Associated table |
| id_customer | INT | FK, NULL | Customer (optional) |
| order_datetime | TIMESTAMP | DEFAULT NOW() | Creation timestamp |
| status | ENUM | NOT NULL, DEFAULT 'PENDING' | PENDING, IN_PREPARATION, READY, DELIVERED, CANCELLED, INVOICED |
| notes | VARCHAR(500) | | Special notes |
| total | DECIMAL(10,2) | DEFAULT 0, ≥0 | Calculated total |

### 6. ORDER_DETAIL
| Attribute | Type | Constraints | Description |
|-----------|------|-------------|-------------|
| id_detail | INT | PK, AI | Unique identifier |
| id_order | INT | NOT NULL, FK | Parent order |
| id_dish | INT | NOT NULL, FK | Ordered dish |
| quantity | INT | NOT NULL, >0 | Units |
| unit_price | DECIMAL(10,2) | NOT NULL, ≥0 | Price at time of order |
| subtotal | DECIMAL(10,2) | GENERATED | quantity × unit_price |
| notes | VARCHAR(300) | | Item notes |
| UNIQUE(id_order, id_dish) | | | Prevents duplicates |

### 7. INVOICE
| Attribute | Type | Constraints | Description |
|-----------|------|-------------|-------------|
| id_invoice | INT | PK, AI | Unique identifier |
| id_order | INT | NOT NULL, FK, UNIQUE | Invoiced order (1:1) |
| issue_date | TIMESTAMP | DEFAULT NOW() | Issue timestamp |
| subtotal | DECIMAL(10,2) | NOT NULL, ≥0 | Before taxes |
| taxes | DECIMAL(10,2) | NOT NULL, ≥0, DEFAULT 0 | VAT or other taxes |
| total | DECIMAL(10,2) | NOT NULL, ≥0 | subtotal + taxes |
| payment_method | ENUM | NOT NULL | CASH, CREDIT_CARD, DEBIT_CARD, TRANSFER, QR, MIXED |
| payment_status | ENUM | NOT NULL, DEFAULT 'PENDING' | PENDING, PAID, VOIDED, PARTIAL |
| invoice_number | VARCHAR(20) | NOT NULL, UNIQUE | Sequential number |
| notes | VARCHAR(500) | | Notes |

### 8. INVOICE_PAYMENT
| Attribute | Type | Constraints | Description |
|-----------|------|-------------|-------------|
| id_payment | INT | PK, AI | Unique identifier |
| id_invoice | INT | NOT NULL, FK | Paid invoice |
| payment_date | TIMESTAMP | DEFAULT NOW() | Payment timestamp |
| amount | DECIMAL(10,2) | NOT NULL, >0 | Amount paid |
| payment_method | ENUM | NOT NULL | CASH, CREDIT_CARD, DEBIT_CARD, TRANSFER, QR |
| reference | VARCHAR(100) | | Authorization code |

## Relationships and Cardinalities

| Entity A | Relationship | Entity B | Cardinality | Type |
|----------|--------------|----------|-------------|------|
| CUSTOMER | places | ORDER | 1:N | Identifying (optional) |
| TABLE_RESTAURANT | has | ORDER | 1:N | Identifying |
| ORDER | contains | ORDER_DETAIL | 1:N | Identifying (composition) |
| DISH | included in | ORDER_DETAIL | 1:N | Non-identifying |
| DISH_CATEGORY | classifies | DISH | 1:N | Non-identifying |
| ORDER | generates | INVOICE | 1:1 | Identifying |
| INVOICE | has payments | INVOICE_PAYMENT | 1:N | Identifying |

## Normalization (3NF)

### 1NF - First Normal Form
- All attributes are atomic
- No repeating groups
- Each table has a PK

### 2NF - Second Normal Form
- Satisfies 1NF
- No partial dependencies (all PKs are simple, not composite except ORDER_DETAIL which has a surrogate PK)
- In ORDER_DETAIL: subtotal depends on the full PK (quantity + unit_price), but is GENERATED

### 3NF - Third Normal Form
- Satisfies 2NF
- No transitive dependencies
- Verification:
  - CUSTOMER: No attributes depend on other non-PK attributes
  - TABLE_RESTAURANT: status and location depend only on id_table
  - DISH: price, available depend only on id_dish (category is FK)
  - ORDER: total is derived (maintained by trigger for performance)
  - INVOICE: taxes, total depend on subtotal (calculated in SP)

## Defined Indexes

| Table | Index | Columns | Purpose |
|-------|-------|---------|---------|
| order_restaurant | idx_order_table_status | (id_table, status) | Find active orders by table |
| order_restaurant | idx_order_date | (order_datetime) | Date-based reports |
| order_restaurant | idx_order_customer | (id_customer) | Customer history |
| order_detail | idx_detail_order | (id_order) | Get items for an order |
| invoice | idx_invoice_date | (issue_date) | Daily reports |
| invoice | idx_invoice_payment_status | (payment_status) | Pending invoices |
| dish | idx_dish_category | (id_category, available) | Menu by category |

## Defined Views

| View | Description |
|------|-------------|
| v_available_tables | Tables with FREE status |
| v_active_menu | Available dishes grouped by category |
| v_kitchen_orders | Orders in kitchen (PENDING/IN_PREPARATION) with items |
| v_pending_invoices | Unpaid invoices with table and customer info |
| v_daily_sales | Daily sales summary by payment method |
| v_top_dishes | Ranking of best-selling dishes by quantity and revenue |

## Integrity Triggers

| Trigger | Event | Action |
|---------|-------|--------|
| trg_order_detail_after_insert | INSERT on order_detail | Recalculate order total |
| trg_order_detail_after_update | UPDATE on order_detail | Recalculate order total |
| trg_order_detail_after_delete | DELETE on order_detail | Recalculate order total |
| trg_invoice_after_insert | INSERT on invoice | Change order to INVOICED |
| trg_invoice_payment_after_insert | INSERT on invoice_payment | Update invoice payment_status and release table when PAID |
| trg_order_after_update | UPDATE on order_restaurant | Release table on invoice/pay or cancel |

## Stored Procedures

| SP | Purpose |
|----|---------|
| sp_create_order | Create order validating free table |
| sp_add_dish_to_order | Add item validating state and availability |
| sp_generate_invoice | Generate invoice with 19% VAT |
| sp_register_payment | Register payment (supports partial/mixed) |
| sp_change_table_status | Change status validating business rules |

## Included Seed Data

- 6 menu categories
- 28 varied dishes
- 12 tables (different capacities and locations)
- 8 test customers
- 7 orders in various states
- 2 invoices (one paid, one pending)
- 2 payments (partial for pending invoice)