# Sistema de Gestión de Restaurante

Sistema completo de gestión para restaurantes desarrollado en Java con MySQL, implementando arquitectura en capas (Modelo, DAO, Servicios) y procedimientos almacenados para lógica de negocio.

## 📋 Características

- **Gestión de Mesas**: CRUD completo, estados (LIBRE, OCUPADA, RESERVADA, MANTENIMIENTO), validaciones de negocio
- **Gestión del Menú**: Platos, categorías, disponibilidad, precios
- **Gestión de Pedidos**: Ciclo de vida completo (PENDIENTE → EN_PREPARACIÓN → LISTO → ENTREGADO → FACTURADO/CANCELADO)
- **Facturación**: Generación automática con IVA 19%, pagos parciales/mixtos, anulación
- **Gestión de Clientes**: CRUD, búsqueda por email
- **Reportes**: Ventas diarias, facturas pendientes, pedidos en cocina, historial por cliente

## 🏗️ Arquitectura

```
src/
├── modelo/          # Entidades (POJOs)
├── dao/             # Acceso a datos (JDBC + Stored Procedures)
├── servicios/       # Lógica de negocio
└── Main.java        # Punto de entrada (consola interactiva)
```

## 🗄️ Base de Datos

8 tablas normalizadas a 3FN:
- `customer`, `table_restaurant`, `dish_category`, `dish`
- `order_restaurant`, `order_detail`, `invoice`, `invoice_payment`

Incluye: vistas, triggers, stored procedures, datos semilla.

## 🚀 Instalación y Ejecución

### Prerrequisitos
- Java 17+
- MySQL 8.0+
- Maven 3.8+ (opcional, para build)

### 1. Base de Datos
```bash
mysql -u root -p < database/restaurante_db.sql
```

### 2. Configuración
Copiar y editar `src/main/resources/db.properties`:
```properties
db.url=jdbc:mysql://localhost:3306/restaurante_db?useSSL=false&serverTimezone=UTC
db.user=root
db.password=tu_password
```

### 3. Compilar y Ejecutar

**Opción A: Maven**
```bash
mvn clean compile exec:java -Dexec.mainClass=Main
```

**Opción B: javac/java**
```bash
# Compilar
javac -d out src/modelo/*.java src/dao/*.java src/servicios/*.java src/Main.java

# Ejecutar (requiere mysql-connector-j en classpath)
java -cp "out:lib/*" Main
```

## 📁 Estructura del Proyecto

```
.
├── database/
│   ├── restaurante_db.sql      # Script completo (DDL + DML + SPs + Triggers + Vistas + Seed)
│   ├── ERD.md                  # Diagrama Entidad-Relación (Mermaid)
│   ├── MODELO_LOGICO.md        # Modelo lógico formal
│   └── README.md               # Documentación BD
├── src/
│   ├── main/resources/db.properties   # Configuración BD
│   ├── modelo/                    # 7 entidades
│   ├── dao/                       # 8 DAOs + ConexionDB
│   ├── servicios/                 # 5 servicios
│   └── Main.java                  # Menú interactivo
├── pom.xml                        # Maven build
├── .gitignore
└── README.md
```

## 🔧 Stored Procedures (Lógica de Negocio en BD)

| SP | Descripción |
|----|-------------|
| `sp_create_order` | Crear pedido validando mesa libre |
| `sp_add_dish_to_order` | Agregar plato validando estado y disponibilidad |
| `sp_generate_invoice` | Generar factura con IVA 19% |
| `sp_register_payment` | Registrar pago (soporta parcial/mixtos) |
| `sp_change_table_status` | Cambiar estado con validaciones |

## 👥 Equipo

- **Grupo**: 04
- **Ficha**: 3239188
- **Programa**: ADSO - SENA
- **Integrantes**: Félix Antonio Murcia Cedeño, Sergio Andrés García Salazar, Diego Andrés Ávila Herrera

## 📄 Licencia

Proyecto educativo - SENA ADSO