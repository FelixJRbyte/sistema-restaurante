# Phase 3: UML Modeling and Object-Oriented Design

**Project**: Restaurant Management System (Group 04)  
**Cohort**: 3239188 - SENA ADSO  

This document presents the UML diagrams synchronized 100% with the implemented Java codebase (`src/`) and the MySQL 8.0 relational schema (`database/restaurante_db.sql`).

---

## 1. OOP Principles Applied in Code

1. **Inheritance (`Herencia`)**: All 7 domain models (`Mesa`, `Cliente`, `CategoriaPlato`, `Plato`, `Pedido`, `DetallePedido`, `Factura`) extend the abstract superclass `EntidadBase` (`src/modelo/EntidadBase.java`), inheriting `fechaCreacion` and the contract methods `getId()`, `obtenerResumen()`, and `esValido()`.
2. **Polymorphism (`Polimorfismo`)**: Each subclass overrides `obtenerResumen()` and `esValido()` with its own domain-specific formatting and validation rules. `Main.mostrarResumenPolimorfico()` processes a heterogeneous `List<EntidadBase>` polymorphically.
3. **Encapsulation (`Encapsulamiento`)**: All attributes are `private` (or `protected` in `EntidadBase`) and accessed exclusively through getters, setters, and domain methods.
4. **Composition & Aggregation (`Composición y Agregación`)**: `Pedido` owns a collection `List<DetallePedido>` (composition) and recalculates its total automatically when items are added or removed.

---

## 2. Use Case Diagram

```mermaid
flowchart LR
    Waiter(["Waiter"])
    Cook(["Cook / Kitchen"])
    Cashier(["Cashier"])
    Admin(["Administrator"])

    subgraph Restaurant_Management_System ["Restaurant Management System (Group 04)"]
        UC01("UC-01 Query Available Tables")
        UC02("UC-02 Change Table Status")
        UC03("UC-03 Manage Menu & Categories")
        UC04("UC-04 Create Order & Add Dishes")
        UC05("UC-05 Cancel Order Item")
        UC06("UC-06 Validate Admin PIN")
        UC07("UC-07 Manage Kitchen Preparation")
        UC08("UC-08 Generate Invoice (19% VAT)")
        UC09("UC-09 Register Payment (Full/Partial)")
        UC10("UC-10 Manage Customers")
        UC11("UC-11 Consult Sales & Top 10 Reports")
    end

    Waiter --> UC01
    Waiter --> UC04
    Waiter --> UC05
    Cook --> UC07
    Cashier --> UC08
    Cashier --> UC09
    Cashier --> UC10
    Admin --> UC02
    Admin --> UC03
    Admin --> UC11

    UC04 -.->|"<<include>>"| UC01
    UC04 -.->|"<<include>>"| UC02
    UC05 -.->|"<<include>>"| UC06
    UC08 -.->|"<<include>>"| UC02
    UC09 -.->|"<<include>>"| UC02
```

---

## 3. Class Diagram (Model, DAO, and Service Layers)

```mermaid
classDiagram
    class EntidadBase {
        <<abstract>>
        #LocalDateTime fechaCreacion
        +getFechaCreacion() LocalDateTime
        +getId()* int
        +obtenerResumen()* String
        +esValido()* boolean
    }

    class Mesa {
        -int idMesa
        -int numero
        -int capacidad
        -String estado
        -String ubicacion
        +getId() int
        +obtenerResumen() String
        +esValido() boolean
    }

    class Cliente {
        -int idCliente
        -String nombre
        -String telefono
        -String email
        -String direccion
        -LocalDateTime fechaRegistro
        -boolean activo
        +getId() int
        +obtenerResumen() String
        +esValido() boolean
    }

    class CategoriaPlato {
        -int idCategoria
        -String nombre
        -String descripcion
        -int orden
        +getId() int
        +obtenerResumen() String
        +esValido() boolean
    }

    class Plato {
        -int idPlato
        -int idCategoria
        -String nombre
        -String descripcion
        -double precio
        -boolean disponible
        -int tiempoPreparacion
        -int cantidadVendida
        -double ingresosGenerados
        +getId() int
        +obtenerResumen() String
        +esValido() boolean
    }

    class Pedido {
        -int idPedido
        -int idMesa
        -int idCliente
        -LocalDateTime fechaHora
        -Estado estado
        -String observaciones
        -double total
        -List~DetallePedido~ detalles
        +agregarDetalle(DetallePedido) void
        +removerDetalle(int) void
        +getId() int
        +obtenerResumen() String
        +esValido() boolean
    }

    class DetallePedido {
        -int idDetalle
        -int idPedido
        -int idPlato
        -int cantidad
        -double precioUnitario
        -String observaciones
        +getSubtotal() double
        +getId() int
        +obtenerResumen() String
        +esValido() boolean
    }

    class Factura {
        -int idFactura
        -int idPedido
        -LocalDateTime fechaEmision
        -double subtotal
        -double impuestos
        -double total
        -MetodoPago metodoPago
        -EstadoPago estadoPago
        -String numeroFactura
        +getId() int
        +obtenerResumen() String
        +esValido() boolean
    }

    EntidadBase <|-- Mesa
    EntidadBase <|-- Cliente
    EntidadBase <|-- CategoriaPlato
    EntidadBase <|-- Plato
    EntidadBase <|-- Pedido
    EntidadBase <|-- DetallePedido
    EntidadBase <|-- Factura

    CategoriaPlato "1" o-- "*" Plato : classifies
    Mesa "1" o-- "*" Pedido : hosts
    Cliente "0..1" o-- "*" Pedido : places
    Pedido "1" *-- "1..*" DetallePedido : contains
    Plato "1" o-- "*" DetallePedido : referenced in
    Pedido "1" --> "0..1" Factura : generates
```

---

## 4. Sequence Diagram: Create Order, Add Dishes, and Generate Invoice

```mermaid
sequenceDiagram
    actor User as Waiter / Cashier
    participant Main as Main (Console UI)
    participant Svc as GestionPedidos / GestionFacturas
    participant DAO as PedidoDAO / FacturaDAO
    participant DB as MySQL 8.0 (Stored Procedures & Triggers)

    User->>Main: 1. Create New Order (tableId, customerId, notes)
    Main->>Svc: crearPedido(idMesa, idCliente, obs)
    Svc->>DAO: crearPedido(idMesa, idCliente, obs)
    DAO->>DB: CALL sp_create_order(idMesa, idCliente, obs)
    alt Table is FREE
        DB->>DB: INSERT INTO order_restaurant (PENDING)
        DB->>DB: UPDATE table_restaurant SET status = 'OCCUPIED'
        DB-->>DAO: id_order
        DAO->>DB: COMMIT
        DAO-->>Main: Optional.of(idPedido)
    else Table is NOT FREE
        DB-->>DAO: SIGNAL SQLSTATE '45000'
        DAO->>DB: ROLLBACK
        DAO-->>Main: Optional.empty()
    end

    User->>Main: 2. Add Dish to Order (idPedido, idPlato, cantidad)
    Main->>Svc: agregarPlatoAPedido(idPedido, idPlato, cantidad)
    Svc->>DAO: agregarPlatoAPedido(idPedido, idPlato, cantidad)
    DAO->>DB: CALL sp_add_dish_to_order(idPedido, idPlato, cantidad)
    DB->>DB: INSERT/UPDATE order_detail
    DB->>DB: Trigger trg_order_detail_after_insert recalculates order.total
    DB->>DB: UPDATE order_restaurant SET status = 'IN_PREPARATION'
    DAO->>DB: COMMIT
    DAO-->>Main: true

    User->>Main: 3. Generate Invoice (idPedido, metodoPago, numFactura)
    Main->>Svc: generarFactura(idPedido, metodoPago, numFactura)
    Svc->>DAO: generarFactura(idPedido, metodoPago, numFactura)
    DAO->>DB: CALL sp_generate_invoice(idPedido, metodoPago, numFactura)
    DB->>DB: Calculate 19% VAT & INSERT INTO invoice
    DB->>DB: UPDATE order_restaurant SET status = 'INVOICED'
    DB->>DB: Trigger trg_order_after_update releases table to 'FREE' if PAID
    DAO->>DB: COMMIT
    DAO-->>Main: Optional.of(idFactura)
```

---

## 5. State Machine Diagram: Table and Order Lifecycle

```mermaid
stateDiagram-v2
    [*] --> FREE : Table Created
    FREE --> OCCUPIED : sp_create_order() (RN-001)
    FREE --> RESERVED : sp_change_table_status()
    RESERVED --> OCCUPIED : Customer Arrives
    RESERVED --> FREE : Reservation Cancelled
    OCCUPIED --> FREE : Invoice PAID or Order CANCELLED (Trigger)
    FREE --> MAINTENANCE : Soft Delete / Maintenance
    MAINTENANCE --> FREE : Reactivated
```
