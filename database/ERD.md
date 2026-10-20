# Modelo Entidad-Relación - Sistema de Restaurante

## Diagrama ER (Mermaid)

```mermaid
erDiagram
    CLIENTE ||--o{ PEDIDO : realiza
    MESA ||--o{ PEDIDO : "tiene"
    PEDIDO ||--o{ DETALLE_PEDIDO : contiene
    PLATO ||--o{ DETALLE_PEDIDO : "se incluye en"
    CATEGORIA_PLATO ||--o{ PLATO : clasifica
    PEDIDO ||--|| FACTURA : genera
    FACTURA ||--o{ PAGO_FACTURA : "tiene pagos"

    CLIENTE {
        INT id_cliente PK
        VARCHAR nombre
        VARCHAR telefono
        VARCHAR email UK
        VARCHAR direccion
        TIMESTAMP fecha_registro
        BOOLEAN activo
    }

    MESA {
        INT id_mesa PK
        INT numero UK
        INT capacidad
        ENUM estado
        VARCHAR ubicacion
    }

    CATEGORIA_PLATO {
        INT id_categoria PK
        VARCHAR nombre UK
        VARCHAR descripcion
        INT orden
    }

    PLATO {
        INT id_plato PK
        INT id_categoria FK
        VARCHAR nombre
        VARCHAR descripcion
        DECIMAL precio
        BOOLEAN disponible
        INT tiempo_preparacion
    }

    PEDIDO {
        INT id_pedido PK
        INT id_mesa FK
        INT id_cliente FK
        TIMESTAMP fecha_hora
        ENUM estado
        VARCHAR observaciones
        DECIMAL total
    }

    DETALLE_PEDIDO {
        INT id_detalle PK
        INT id_pedido FK
        INT id_plato FK
        INT cantidad
        DECIMAL precio_unitario
        DECIMAL subtotal "GENERATED"
        VARCHAR observaciones
    }

    FACTURA {
        INT id_factura PK
        INT id_pedido FK UK
        TIMESTAMP fecha_emision
        DECIMAL subtotal
        DECIMAL impuestos
        DECIMAL total
        ENUM metodo_pago
        ENUM estado_pago
        VARCHAR numero_factura UK
        VARCHAR observaciones
    }

    PAGO_FACTURA {
        INT id_pago PK
        INT id_factura FK
        TIMESTAMP fecha_pago
        DECIMAL monto
        ENUM metodo_pago
        VARCHAR referencia
    }
```

## Entidades y Atributos

### 1. CLIENTE
| Atributo | Tipo | Restricciones | Descripción |
|----------|------|---------------|-------------|
| id_cliente | INT | PK, AI | Identificador único |
| nombre | VARCHAR(100) | NOT NULL | Nombre completo |
| telefono | VARCHAR(20) | CHECK (formato) | Teléfono de contacto |
| email | VARCHAR(100) | UNIQUE | Email único |
| direccion | VARCHAR(200) | | Dirección |
| fecha_registro | TIMESTAMP | DEFAULT NOW() | Fecha de registro |
| activo | BOOLEAN | DEFAULT TRUE | Estado del cliente |

### 2. MESA
| Atributo | Tipo | Restricciones | Descripción |
|----------|------|---------------|-------------|
| id_mesa | INT | PK, AI | Identificador único |
| numero | INT | NOT NULL, UNIQUE, >0 | Número de mesa visible |
| capacidad | INT | NOT NULL, >0, ≤20 | Comensales máximos |
| estado | ENUM | NOT NULL, DEFAULT 'LIBRE' | LIBRE, OCUPADA, RESERVADA, MANTENIMIENTO |
| ubicacion | VARCHAR(50) | | Zona del restaurante |

### 3. CATEGORIA_PLATO
| Atributo | Tipo | Restricciones | Descripción |
|----------|------|---------------|-------------|
| id_categoria | INT | PK, AI | Identificador único |
| nombre | VARCHAR(50) | NOT NULL, UNIQUE | Nombre de categoría |
| descripcion | VARCHAR(200) | | Descripción |
| orden | INT | DEFAULT 0 | Orden en menú |

### 4. PLATO
| Atributo | Tipo | Restricciones | Descripción |
|----------|------|---------------|-------------|
| id_plato | INT | PK, AI | Identificador único |
| id_categoria | INT | NOT NULL, FK | Categoría |
| nombre | VARCHAR(100) | NOT NULL | Nombre del plato |
| descripcion | VARCHAR(300) | | Descripción |
| precio | DECIMAL(10,2) | NOT NULL, ≥0 | Precio de venta |
| disponible | BOOLEAN | DEFAULT TRUE | Disponibilidad |
| tiempo_preparacion | INT | DEFAULT 15, >0, ≤180 | Minutos estimados |

### 5. PEDIDO
| Atributo | Tipo | Restricciones | Descripción |
|----------|------|---------------|-------------|
| id_pedido | INT | PK, AI | Identificador único |
| id_mesa | INT | NOT NULL, FK | Mesa asociada |
| id_cliente | INT | FK, NULL | Cliente (opcional) |
| fecha_hora | TIMESTAMP | DEFAULT NOW() | Cuándo se creó |
| estado | ENUM | NOT NULL, DEFAULT 'PENDIENTE' | PENDIENTE, EN_PREPARACION, LISTO, ENTREGADO, CANCELADO, FACTURADO |
| observaciones | VARCHAR(500) | | Notas especiales |
| total | DECIMAL(10,2) | DEFAULT 0, ≥0 | Total calculado |

### 6. DETALLE_PEDIDO
| Atributo | Tipo | Restricciones | Descripción |
|----------|------|---------------|-------------|
| id_detalle | INT | PK, AI | Identificador único |
| id_pedido | INT | NOT NULL, FK | Pedido padre |
| id_plato | INT | NOT NULL, FK | Plato ordenado |
| cantidad | INT | NOT NULL, >0 | Unidades |
| precio_unitario | DECIMAL(10,2) | NOT NULL, ≥0 | Precio al momento |
| subtotal | DECIMAL(10,2) | GENERATED | cantidad × precio_unitario |
| observaciones | VARCHAR(300) | | Notas del item |
| UNIQUE(id_pedido, id_plato) | | | Evita duplicados |

### 7. FACTURA
| Atributo | Tipo | Restricciones | Descripción |
|----------|------|---------------|-------------|
| id_factura | INT | PK, AI | Identificador único |
| id_pedido | INT | NOT NULL, FK, UNIQUE | Pedido facturado (1:1) |
| fecha_emision | TIMESTAMP | DEFAULT NOW() | Cuándo se emitió |
| subtotal | DECIMAL(10,2) | NOT NULL, ≥0 | Sin impuestos |
| impuestos | DECIMAL(10,2) | NOT NULL, ≥0, DEFAULT 0 | IVA u otros |
| total | DECIMAL(10,2) | NOT NULL, ≥0 | subtotal + impuestos |
| metodo_pago | ENUM | NOT NULL | EFECTIVO, TARJETA_CREDITO, TARJETA_DEBITO, TRANSFERENCIA, QR, MIXTO |
| estado_pago | ENUM | NOT NULL, DEFAULT 'PENDIENTE' | PENDIENTE, PAGADO, ANULADO, PARCIAL |
| numero_factura | VARCHAR(20) | NOT NULL, UNIQUE | Número consecutivo |
| observaciones | VARCHAR(500) | | Notas |

### 8. PAGO_FACTURA
| Atributo | Tipo | Restricciones | Descripción |
|----------|------|---------------|-------------|
| id_pago | INT | PK, AI | Identificador único |
| id_factura | INT | NOT NULL, FK | Factura pagada |
| fecha_pago | TIMESTAMP | DEFAULT NOW() | Cuándo se pagó |
| monto | DECIMAL(10,2) | NOT NULL, >0 | Monto abonado |
| metodo_pago | ENUM | NOT NULL | EFECTIVO, TARJETA_CREDITO, TARJETA_DEBITO, TRANSFERENCIA, QR |
| referencia | VARCHAR(100) | | Código autorización |

## Relaciones y Cardinalidades

| Entidad A | Relación | Entidad B | Cardinalidad | Tipo |
|-----------|----------|-----------|--------------|------|
| CLIENTE | realiza | PEDIDO | 1:N | Identificadora (opcional) |
| MESA | tiene | PEDIDO | 1:N | Identificadora |
| PEDIDO | contiene | DETALLE_PEDIDO | 1:N | Identificadora (composición) |
| PLATO | se incluye en | DETALLE_PEDIDO | 1:N | No identificadora |
| CATEGORIA_PLATO | clasifica | PLATO | 1:N | No identificadora |
| PEDIDO | genera | FACTURA | 1:1 | Identificadora |
| FACTURA | tiene pagos | PAGO_FACTURA | 1:N | Identificadora |

## Normalización (3FN)

### 1FN - Primera Forma Normal
- Todos los atributos son atómicos
- No hay grupos repetidos
- Cada tabla tiene PK

### 2FN - Segunda Forma Normal
- Cumple 1FN
- No hay dependencias parciales (todas las PK son simples, no compuestas excepto DETALLE_PEDIDO que tiene PK sustituta)
- En DETALLE_PEDIDO: subtotal depende de toda la PK (cantidad + precio_unitario), pero es GENERATED

### 3FN - Tercera Forma Normal
- Cumple 2FN
- No hay dependencias transitivas
- Verificación:
  - CLIENTE: No hay atributos que dependan de otros no-PK
  - MESA: estado y ubicacion dependen solo de id_mesa
  - PLATO: precio, disponible dependen solo de id_plato (categoria es FK)
  - PEDIDO: total es derivado (se mantiene por trigger para performance)
  - FACTURA: impuestos, total dependen de subtotal (calculados en SP)

## Índices Definidos

| Tabla | Índice | Columnas | Propósito |
|-------|--------|----------|-----------|
| pedido | idx_pedido_mesa_estado | (id_mesa, estado) | Buscar pedidos activos por mesa |
| pedido | idx_pedido_fecha | (fecha_hora) | Reportes por fecha |
| pedido | idx_pedido_cliente | (id_cliente) | Historial por cliente |
| detalle_pedido | idx_detalle_pedido | (id_pedido) | Obtener items de un pedido |
| factura | idx_factura_fecha | (fecha_emision) | Reportes diarios |
| factura | idx_factura_estado_pago | (estado_pago) | Facturas pendientes |
| plato | idx_plato_categoria | (id_categoria, disponible) | Menú por categoría |

## Vistas Definidas

| Vista | Descripción |
|-------|-------------|
| v_mesas_disponibles | Mesas con estado LIBRE |
| v_menu_activo | Platos disponibles agrupados por categoría |
| v_pedidos_cocina | Pedidos en cocina (PENDIENTE/EN_PREPARACION) con items |
| v_facturas_pendientes | Facturas no pagadas con info de mesa y cliente |
| v_ventas_diarias | Resumen de ventas por día y método de pago |

## Triggers de Integridad

| Trigger | Evento | Acción |
|---------|--------|--------|
| trg_detalle_pedido_after_insert | INSERT en detalle_pedido | Recalcular total del pedido |
| trg_detalle_pedido_after_update | UPDATE en detalle_pedido | Recalcular total del pedido |
| trg_detalle_pedido_after_delete | DELETE en detalle_pedido | Recalcular total del pedido |
| trg_factura_after_insert | INSERT en factura | Cambiar pedido a FACTURADO |
| trg_pago_factura_after_insert | INSERT en pago_factura | Actualizar estado_pago de factura |
| trg_pedido_after_update | UPDATE en pedido | Liberar mesa al facturar/pagar o cancelar |

## Stored Procedures

| SP | Propósito |
|----|-----------|
| sp_crear_pedido | Crear pedido validando mesa libre |
| sp_agregar_plato_pedido | Agregar item validando estado y disponibilidad |
| sp_generar_factura | Generar factura con IVA 19% |
| sp_registrar_pago | Registrar pago (soporta pagos parciales/mixtos) |
| sp_cambiar_estado_mesa | Cambiar estado validando reglas de negocio |

## Datos Semilla Incluidos

- 6 categorías de menú
- 28 platos variados
- 12 mesas (distintas capacidades y ubicaciones)
- 8 clientes de prueba
- 7 pedidos en distintos estados
- 2 facturas (una pagada, una pendiente)
- 2 pagos (parciales para factura pendiente)