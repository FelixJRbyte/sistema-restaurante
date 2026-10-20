# Modelo Lógico de Datos - Sistema de Restaurante

## Esquema Relacional (Notación Formal)

### Relaciones Base

```
CLIENTE(id_cliente, nombre, telefono, email, direccion, fecha_registro, activo)
    PK: id_cliente
    UK: email
    CHECK: telefono formato válido

MESA(id_mesa, numero, capacidad, estado, ubicacion)
    PK: id_mesa
    UK: numero
    CHECK: capacidad > 0 AND capacidad <= 20
    CHECK: numero > 0
    DOMINIO estado: {LIBRE, OCUPADA, RESERVADA, MANTENIMIENTO}

CATEGORIA_PLATO(id_categoria, nombre, descripcion, orden)
    PK: id_categoria
    UK: nombre

PLATO(id_plato, id_categoria, nombre, descripcion, precio, disponible, tiempo_preparacion)
    PK: id_plato
    FK: id_categoria → CATEGORIA_PLATO(id_categoria) [ON UPDATE CASCADE, ON DELETE RESTRICT]
    CHECK: precio >= 0
    CHECK: tiempo_preparacion > 0 AND tiempo_preparacion <= 180

PEDIDO(id_pedido, id_mesa, id_cliente, fecha_hora, estado, observaciones, total)
    PK: id_pedido
    FK: id_mesa → MESA(id_mesa) [ON UPDATE CASCADE, ON DELETE RESTRICT]
    FK: id_cliente → CLIENTE(id_cliente) [ON UPDATE CASCADE, ON DELETE SET NULL]
    CHECK: total >= 0
    DOMINIO estado: {PENDIENTE, EN_PREPARACION, LISTO, ENTREGADO, CANCELADO, FACTURADO}

DETALLE_PEDIDO(id_detalle, id_pedido, id_plato, cantidad, precio_unitario, subtotal, observaciones)
    PK: id_detalle
    FK: id_pedido → PEDIDO(id_pedido) [ON UPDATE CASCADE, ON DELETE CASCADE]
    FK: id_plato → PLATO(id_plato) [ON UPDATE CASCADE, ON DELETE RESTRICT]
    UK: (id_pedido, id_plato)
    CHECK: cantidad > 0
    CHECK: precio_unitario >= 0
    GENERATED: subtotal = cantidad * precio_unitario

FACTURA(id_factura, id_pedido, fecha_emision, subtotal, impuestos, total, metodo_pago, estado_pago, numero_factura, observaciones)
    PK: id_factura
    FK: id_pedido → PEDIDO(id_pedido) [ON UPDATE CASCADE, ON DELETE RESTRICT]
    UK: id_pedido (relación 1:1)
    UK: numero_factura
    CHECK: subtotal >= 0
    CHECK: impuestos >= 0
    CHECK: total >= 0
    DOMINIO metodo_pago: {EFECTIVO, TARJETA_CREDITO, TARJETA_DEBITO, TRANSFERENCIA, QR, MIXTO}
    DOMINIO estado_pago: {PENDIENTE, PAGADO, ANULADO, PARCIAL}

PAGO_FACTURA(id_pago, id_factura, fecha_pago, monto, metodo_pago, referencia)
    PK: id_pago
    FK: id_factura → FACTURA(id_factura) [ON UPDATE CASCADE, ON DELETE CASCADE]
    CHECK: monto > 0
    DOMINIO metodo_pago: {EFECTIVO, TARJETA_CREDITO, TARJETA_DEBITO, TRANSFERENCIA, QR}
```

## Análisis de Dependencias Funcionales

### CLIENTE
- id_cliente → {nombre, telefono, email, direccion, fecha_registro, activo}
- email → id_cliente (único)

### MESA
- id_mesa → {numero, capacidad, estado, ubicacion}
- numero → id_mesa (único)

### CATEGORIA_PLATO
- id_categoria → {nombre, descripcion, orden}
- nombre → id_categoria (único)

### PLATO
- id_plato → {id_categoria, nombre, descripcion, precio, disponible, tiempo_preparacion}
- No hay dependencias transitivas (id_categoria es FK, no determina otros atributos de PLATO)

### PEDIDO
- id_pedido → {id_mesa, id_cliente, fecha_hora, estado, observaciones, total}
- total es derivado funcionalmente de DETALLE_PEDIDO (SUM(subtotal))

### DETALLE_PEDIDO
- id_detalle → {id_pedido, id_plato, cantidad, precio_unitario, observaciones}
- (id_pedido, id_plato) → id_detalle (candidata alternativa)
- subtotal = cantidad × precio_unitario (dependencia funcional completa)

### FACTURA
- id_factura → {id_pedido, fecha_emision, subtotal, impuestos, total, metodo_pago, estado_pago, numero_factura, observaciones}
- id_pedido → id_factura (1:1)
- numero_factura → id_factura (único)
- total = subtotal + impuestos (dependencia funcional)

### PAGO_FACTURA
- id_pago → {id_factura, fecha_pago, monto, metodo_pago, referencia}

## Reglas de Negocio Implementadas a Nivel BD

### Restricciones de Integridad Referencial
1. **CASCADE UPDATE** en todas las FK: Cambios de PK propagados
2. **RESTRICT DELETE** en: CATEGORIA_PLATO→PLATO, MESA→PEDIDO, PLATO→DETALLE_PEDIDO, PEDIDO→FACTURA
   - Evita borrar datos referenciados activos
3. **SET NULL** en: CLIENTE→PEDIDO
   - Si se borra cliente, pedido queda sin cliente (historial preservado)
4. **CASCADE DELETE** en: PEDIDO→DETALLE_PEDIDO, FACTURA→PAGO_FACTURA
   - Borrar pedido borra sus líneas; borrar factura borra sus pagos

### Restricciones de Dominio (CHECK)
- Capacidad mesa: 1-20 comensales
- Precios ≥ 0
- Cantidades > 0
- Tiempo preparación: 1-180 min
- Totales ≥ 0
- Formato teléfono válido (regex)

### Restricciones de Unicidad
- Número de mesa único
- Email cliente único
- Nombre categoría único
- Número factura único
- Un pedido = una factura máximo
- Un plato por pedido (se actualiza cantidad si repite)

## Mapeo a Clases Java (Fase 3)

| Tabla BD | Clase Java | Atributos Principales |
|----------|------------|----------------------|
| MESA | Mesa | numero, capacidad, estado |
| CLIENTE | Cliente | nombre, telefono, email, direccion |
| PLATO | Plato | nombre, descripcion, precio, categoria |
| PEDIDO | Pedido | mesa, cliente, fecha, estado, items, total |
| DETALLE_PEDIDO | (parte de Pedido) | plato, cantidad, subtotal |
| FACTURA | Factura | pedido, subtotal, impuestos, total, metodoPago, estadoPago |

**Nota**: La clase `Mesa` ya existe completa en `src/modelo/mesa.java`. Las demás clases modelo están vacías y deben completarse según este modelo.

## Diccionario de Datos

| Tabla | Campo | Tipo | Longitud | Nulo | Default | Descripción |
|-------|-------|------|----------|------|---------|-------------|
| CLIENTE | id_cliente | INT | 11 | NO | AUTO_INCREMENT | PK |
| CLIENTE | nombre | VARCHAR | 100 | NO | | Nombre completo |
| CLIENTE | telefono | VARCHAR | 20 | SÍ | | Teléfono contacto |
| CLIENTE | email | VARCHAR | 100 | SÍ | | Email único |
| CLIENTE | direccion | VARCHAR | 200 | SÍ | | Dirección |
| CLIENTE | fecha_registro | TIMESTAMP | | NO | CURRENT_TIMESTAMP | Registro |
| CLIENTE | activo | BOOLEAN | | NO | TRUE | Estado |
| MESA | id_mesa | INT | 11 | NO | AUTO_INCREMENT | PK |
| MESA | numero | INT | 11 | NO | | Número visible |
| MESA | capacidad | INT | 11 | NO | | Comensales max |
| MESA | estado | ENUM | | NO | LIBRE | Estado mesa |
| MESA | ubicacion | VARCHAR | 50 | SÍ | | Zona restaurante |
| CATEGORIA_PLATO | id_categoria | INT | 11 | NO | AUTO_INCREMENT | PK |
| CATEGORIA_PLATO | nombre | VARCHAR | 50 | NO | | Nombre categoría |
| CATEGORIA_PLATO | descripcion | VARCHAR | 200 | SÍ | | Descripción |
| CATEGORIA_PLATO | orden | INT | 11 | NO | 0 | Orden visual |
| PLATO | id_plato | INT | 11 | NO | AUTO_INCREMENT | PK |
| PLATO | id_categoria | INT | 11 | NO | | FK Categoría |
| PLATO | nombre | VARCHAR | 100 | NO | | Nombre plato |
| PLATO | descripcion | VARCHAR | 300 | SÍ | | Descripción |
| PLATO | precio | DECIMAL | 10,2 | NO | | Precio venta |
| PLATO | disponible | BOOLEAN | | NO | TRUE | Disponible |
| PLATO | tiempo_preparacion | INT | 11 | NO | 15 | Minutos est. |
| PEDIDO | id_pedido | INT | 11 | NO | AUTO_INCREMENT | PK |
| PEDIDO | id_mesa | INT | 11 | NO | | FK Mesa |
| PEDIDO | id_cliente | INT | 11 | SÍ | | FK Cliente |
| PEDIDO | fecha_hora | TIMESTAMP | | NO | CURRENT_TIMESTAMP | Creación |
| PEDIDO | estado | ENUM | | NO | PENDIENTE | Estado pedido |
| PEDIDO | observaciones | VARCHAR | 500 | SÍ | | Notas |
| PEDIDO | total | DECIMAL | 10,2 | NO | 0.00 | Total calculado |
| DETALLE_PEDIDO | id_detalle | INT | 11 | NO | AUTO_INCREMENT | PK |
| DETALLE_PEDIDO | id_pedido | INT | 11 | NO | | FK Pedido |
| DETALLE_PEDIDO | id_plato | INT | 11 | NO | | FK Plato |
| DETALLE_PEDIDO | cantidad | INT | 11 | NO | 1 | Unidades |
| DETALLE_PEDIDO | precio_unitario | DECIMAL | 10,2 | NO | | Precio histórico |
| DETALLE_PEDIDO | subtotal | DECIMAL | 10,2 | NO | GENERATED | Calculado |
| DETALLE_PEDIDO | observaciones | VARCHAR | 300 | SÍ | | Notas item |
| FACTURA | id_factura | INT | 11 | NO | AUTO_INCREMENT | PK |
| FACTURA | id_pedido | INT | 11 | NO | | FK Pedido (1:1) |
| FACTURA | fecha_emision | TIMESTAMP | | NO | CURRENT_TIMESTAMP | Emisión |
| FACTURA | subtotal | DECIMAL | 10,2 | NO | | Sin impuestos |
| FACTURA | impuestos | DECIMAL | 10,2 | NO | 0.00 | IVA 19% |
| FACTURA | total | DECIMAL | 10,2 | NO | | Con impuestos |
| FACTURA | metodo_pago | ENUM | | NO | | Forma pago |
| FACTURA | estado_pago | ENUM | | NO | PENDIENTE | Estado pago |
| FACTURA | numero_factura | VARCHAR | 20 | NO | | N° consecutivo |
| FACTURA | observaciones | VARCHAR | 500 | SÍ | | Notas |
| PAGO_FACTURA | id_pago | INT | 11 | NO | AUTO_INCREMENT | PK |
| PAGO_FACTURA | id_factura | INT | 11 | NO | | FK Factura |
| PAGO_FACTURA | fecha_pago | TIMESTAMP | | NO | CURRENT_TIMESTAMP | Pago |
| PAGO_FACTURA | monto | DECIMAL | 10,2 | NO | | Monto abonado |
| PAGO_FACTURA | metodo_pago | ENUM | | NO | | Forma pago |
| PAGO_FACTURA | referencia | VARCHAR | 100 | SÍ | | Código auth |

## Consultas Típicas del Sistema

### 1. Obtener menú activo por categoría
```sql
SELECT * FROM v_menu_activo;
```

### 2. Mesas libres para nuevo pedido
```sql
SELECT * FROM v_mesas_disponibles WHERE capacidad >= ?;
```

### 3. Pedidos en cocina (pendientes/preparación)
```sql
SELECT * FROM v_pedidos_cocina;
```

### 4. Facturas pendientes de cobro
```sql
SELECT * FROM v_facturas_pendientes;
```

### 5. Ventas del día
```sql
SELECT * FROM v_ventas_diarias WHERE fecha = CURDATE();
```

### 6. Historial de cliente
```sql
SELECT p.*, f.numero_factura, f.total, f.estado_pago
FROM pedido p
LEFT JOIN factura f ON p.id_pedido = f.id_pedido
WHERE p.id_cliente = ?
ORDER BY p.fecha_hora DESC;
```

### 7. Detalle de pedido para imprimir
```sql
SELECT dp.cantidad, pl.nombre, dp.precio_unitario, dp.subtotal, dp.observaciones
FROM detalle_pedido dp
JOIN plato pl ON dp.id_plato = pl.id_plato
WHERE dp.id_pedido = ?;
```

## Estrategia de Migración / Versionado

```sql
-- Tabla de control de versiones de esquema
CREATE TABLE schema_version (
    version VARCHAR(20) PRIMARY KEY,
    applied_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    description VARCHAR(200)
);

INSERT INTO schema_version VALUES ('1.0.0', NOW(), 'Esquema inicial Fase 4');
```

## Consideraciones de Performance

1. **Índices compuestos** en columnas de filtrado frecuente (mesa+estado, fecha)
2. **Vistas materializadas** no necesarias (datos transaccionales, tiempo real)
3. **Particionado** futuro por fecha en PEDIDO/FACTURA si crece mucho
4. **Triggers** ligeros: solo actualizan totales/estados, no cálculos complejos
5. **Prepared Statements** obligatorios en Java para evitar SQL Injection

## Seguridad

- Usuario BD dedicado con permisos mínimos (SELECT, INSERT, UPDATE, EXECUTE)
- Sin permisos DROP, ALTER, GRANT en producción
- Stored Procedures encapsulan lógica de negocio crítica
- Validación doble: BD (constraints) + Java (validación previa)