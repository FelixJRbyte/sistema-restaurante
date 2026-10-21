# Restaurant Management System (Group 04)

Complete restaurant management system developed in **Java 17** with **MySQL 8.0**, implementing a layered architecture (**Model, DAO, Services, Interactive Console**) with full OOP principles (**Encapsulation, Inheritance, Polymorphism, Composition/Aggregation**), database triggers, views, and stored procedures.

---

## 📋 Key Features

- **Table Management (`RF-001`, `RF-002`)**: Full CRUD, real-time statuses (`FREE`, `OCCUPIED`, `RESERVED`, `MAINTENANCE`), capacity filtering, and business rule validation.
- **Menu & Category Management (`RF-003`)**: Dishes, categories, availability toggling, search by name, and price/preparation time validation.
- **Order Lifecycle & Kitchen View (`RF-004`, `RF-005`, `RF-006`)**: Complete order lifecycle (`PENDING` → `IN_PREPARATION` → `READY` → `DELIVERED` → `INVOICED` / `CANCELLED`), item cancellation with Admin PIN authorization (`RN-002`), and real-time kitchen order monitoring.
- **Invoicing & Payments (`RF-007`, `RF-008`)**: Automatic invoice generation with 19% VAT (`RN-003`), support for partial and mixed payments, automatic table release upon full payment (`RN-001`), and invoice voiding.
- **Customer Management**: Full CRUD, search by email or name, and soft-delete deactivation.
- **Reports & Analytics (`RF-009`, `RF-010`)**: Daily sales breakdown by payment method, **Top 10 best-selling dishes**, orders by status, customer order history, and polymorphic entity summary.

---

## 🏗️ Project Architecture & Deliverables by Phase

```text
.
├── docs/
│   ├── PHASE_1_USER_STORIES.md     # Phase 1: 15 User Stories (HU-01 to HU-15), MoSCoW & Glossary
│   ├── PHASE_2_SRS_IEEE830.md      # Phase 2: IEEE 830 SRS, Business Rules & Traceability Matrix
│   └── PHASE_3_UML_DIAGRAMS.md     # Phase 3: Aligned UML Diagrams (Use Case, Class, Sequence, Activity, State)
├── database/
│   ├── restaurante_db.sql          # Phase 4: Complete SQL script (3NF DDL + Views + Triggers + SPs + Seed Data)
│   ├── ERD.md                      # Phase 4: Entity-Relationship Diagram (Mermaid) & 3NF Justification
│   ├── MODELO_LOGICO.md            # Phase 4: Formal Logical Model, Functional Dependencies & Data Dictionary
│   └── README.md                   # Phase 4: Database Execution & Verification Guide
├── src/
│   ├── main/resources/
│   │   └── db.properties.example   # Database connection configuration template
│   ├── modelo/                     # Phase 3: EntidadBase (Abstract) + 7 Domain Entities
│   ├── dao/                        # Phase 3: 7 DAOs + ConexionDB (ThreadLocal non-closing proxy)
│   ├── servicios/                  # Phase 3: 5 Business Logic Service Classes
│   └── Main.java                   # Phase 3: Interactive Console Application
├── test/
│   └── SistemaRestauranteTest.java # Automated JUnit 5 Tests (OOP, Polymorphism, Composition & Validation)
├── .github/workflows/
│   └── ci.yml                      # GitHub Actions CI Pipeline (MySQL 8.0 + JDK 17 + Maven Test)
└── pom.xml                         # Maven Build Configuration
```

---

## 🗄️ Database Architecture (Phase 4 - 3NF)

- **8 Normalized Tables (3NF)**: `customer`, `table_restaurant`, `dish_category`, `dish`, `order_restaurant`, `order_detail`, `invoice`, `invoice_payment`.
- **6 Predefined Views**:
  - `v_available_tables`: Free tables with capacity and location.
  - `v_active_menu`: Available dishes ordered by category.
  - `v_kitchen_orders`: Active kitchen orders (`PENDING`, `IN_PREPARATION`) with aggregated items.
  - `v_pending_invoices`: Unpaid or partially paid invoices.
  - `v_daily_sales`: Daily revenue consolidated by payment method.
  - `v_top_dishes`: Ranking of best-selling dishes by quantity sold and revenue (`RF-010`).
- **5 Stored Procedures**:

| Stored Procedure | Description |
| :--- | :--- |
| `sp_create_order` | Creates an order validating that the table is `FREE` and marks it `OCCUPIED` |
| `sp_add_dish_to_order` | Adds or increments a dish validating order status and dish availability |
| `sp_generate_invoice` | Generates an invoice calculating 19% VAT and updates order status to `INVOICED` |
| `sp_register_payment` | Registers partial/full payments and triggers automatic table release when `PAID` |
| `sp_change_table_status` | Changes table status validating active order constraints |

---

## 🚀 Installation & Execution

### Prerequisites
- **Java JDK 17+**
- **MySQL 8.0+**
- **Maven 3.8+** (optional, for automated build and tests)

### 1. Initialize the Database
```bash
mysql -u root -p < database/restaurante_db.sql
```

### 2. Configure Database Connection
Copy `src/main/resources/db.properties.example` to `src/main/resources/db.properties` and set your credentials:
```properties
db.url=jdbc:mysql://localhost:3306/restaurante_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
db.user=root
db.password=your_password
```

### 3. Build, Test, and Run

**Option A: Using Maven**
```bash
# Run automated JUnit 5 tests
mvn clean test

# Compile and run interactive console
mvn compile exec:java -Dexec.mainClass=Main
```

**Option B: Using `javac` / `java`**
```bash
javac -encoding UTF-8 -d out src/modelo/*.java src/dao/*.java src/servicios/*.java src/Main.java
java -cp "out;out/lib/*" Main
```

---

## 👥 Team Information

- **Group**: 04
- **Cohort (Ficha)**: 3239188
- **Program**: ADSO - SENA (Centro de la Industria, la Empresa y los Servicios - Neiva)
- **Instructor**: Eng. Carlos Julio Cadena
- **Members**:
  - Félix Antonio Murcia Cedeño
  - Sergio Andrés García Salazar
  - Diego Andrés Ávila Herrera
