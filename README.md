# Restaurant Management System (Group 04)

Complete restaurant management system developed in **Java 25** with **MySQL 8.0**, implementing a layered architecture (**Model, DAO, Services, Interactive Console**) with full OOP principles (**Encapsulation, Inheritance, Polymorphism, Composition/Aggregation**), database triggers, views, and stored procedures.

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
│   ├── PHASE_1_USER_STORIES.md     # Phase 1: Problem, Objectives, Actors, 15 User Stories (HU-01..HU-15) & MoSCoW
│   ├── PHASE_2_SRS_IEEE830.md      # Phase 2: IEEE 830 SRS, Business Rules, Acceptance Criteria & Traceability Matrix
│   └── PHASE_3_UML_DIAGRAMS.md     # Phase 3: Aligned UML Diagrams (Use Case, Class, Sequence, Activity, State)
├── database/
│   ├── restaurante_db.sql          # Phase 4: Complete SQL script (3NF DDL + Views + Triggers + 6 SPs + Seed Data)
│   ├── ERD.md                      # Phase 4: Entity-Relationship Diagram (Mermaid) & 3NF Justification
│   ├── MODELO_LOGICO.md            # Phase 4: Formal Logical Model, Functional Dependencies & Data Dictionary
│   └── README.md                   # Phase 4: Database Execution & Verification Guide
├── mockup/
│   ├── index.html                  # Phase 5: Interactive Navigable HTML/CSS/JS Prototype (100% offline)
│   ├── styles.css                  # Phase 5: Responsive UI Stylesheet (Floor Map, KDS, Modals, Reports)
│   ├── app.js                      # Phase 5: Client-side Simulation enforcing RN-001..RN-004
│   └── README.md                   # Phase 5: Navigation Flow Diagram & Requirements Traceability
├── src/
│   ├── main/resources/
│   │   └── db.properties.example   # Database & Admin PIN configuration template
│   ├── modelo/                     # Phase 6: EntidadBase (Abstract) + 7 Domain Entities
│   ├── dao/                        # Phase 6: 7 DAOs + ConexionDB (ThreadLocal non-closing proxy)
│   ├── servicios/                  # Phase 6: 5 Business Logic Service Classes
│   └── Main.java                   # Phase 6: Interactive Console Application
├── test/
│   └── SistemaRestauranteTest.java # Automated JUnit 5 Tests (OOP, Polymorphism, Composition & Validation)
├── .github/workflows/
│   └── ci.yml                      # GitHub Actions CI Pipeline (MySQL 8.0 + JDK 25 + Maven Test)
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
- **6 Stored Procedures**:

| Stored Procedure | Description |
| :--- | :--- |
| `sp_create_order` | Creates an order validating that the table is `FREE` and marks it `OCCUPIED` |
| `sp_add_dish_to_order` | Adds or increments a dish validating order status, dish availability, and quantity > 0 |
| `sp_update_order_status` | Enforces valid order lifecycle transitions (`PENDING` → `IN_PREPARATION` → `READY` → `DELIVERED` / `CANCELLED`) |
| `sp_generate_invoice` | Generates an invoice calculating 19% VAT (`RN-003`) for `READY`/`DELIVERED` orders and sets `INVOICED` |
| `sp_register_payment` | Registers partial/full payments rejecting `PAID`/`VOIDED` or overpayments and releases table when `PAID` (`RN-001`) |
| `sp_change_table_status` | Changes table status validating active order and unpaid invoice constraints |

---

## 🚀 Installation & Execution

### Prerequisites
- **Java JDK 25 LTS**
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
# Copy runtime dependencies (MySQL driver) into target/lib first, or point to your mysql-connector-j JAR
mvn dependency:copy-dependencies -DoutputDirectory=target/lib
javac -encoding UTF-8 -d out src/modelo/*.java src/dao/*.java src/servicios/*.java src/Main.java
java -cp "out;target/lib/*" Main
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
