# Entregables Fase 4 - Base de Datos Relacional

## Contenido de la Carpeta

| Archivo | Descripción |
|---------|-------------|
| `restaurante_db.sql` | **Script SQL completo** (DDL + DML + Triggers + SPs + Vistas + Seed Data) |
| `ERD.md` | **Diagrama Entidad-Relación** en formato Mermaid + documentación de entidades |
| `MODELO_LOGICO.md` | **Modelo Lógico Formal** (esquema relacional, dependencias funcionales, diccionario de datos) |

## Cómo Ejecutar el Script SQL

### Opción 1: MySQL/MariaDB (Recomendado)

```bash
# Desde terminal
mysql -u root -p < database/restaurante_db.sql

# O dentro de MySQL
SOURCE database/restaurante_db.sql;
```

### Opción 2: MySQL Workbench / DBeaver / phpMyAdmin
1. Abrir el archivo `restaurante_db.sql`
2. Ejecutar completo (Ctrl+Shift+Enter / botón "Execute")

### Opción 3: Línea de comandos Windows (PowerShell)
```powershell
Get-Content database\restaurante_db.sql | mysql -u root -p
```

## Qué Crea el Script

### Estructura (DDL)
- ✅ 8 tablas normalizadas en 3FN
- ✅ Primary Keys, Foreign Keys, Unique Constraints
- ✅ Check Constraints (validación de dominio)
- ✅ ENUMs para estados controlados
- ✅ Índices optimizados para consultas típicas
- ✅ 5 Vistas predefinidas para reportes comunes

### Lógica de Negocio (Triggers + SPs)
- ✅ 6 Triggers para integridad automática:
  - Recalculo de totales de pedido
  - Cambio estado pedido → factura
  - Actualización estado pago factura
  - Liberación automática de mesas
- ✅ 5 Stored Procedures encapsulando operaciones:
  - `sp_crear_pedido()` - Valida mesa libre
  - `sp_agregar_plato_pedido()` - Valida estado y disponibilidad
  - `sp_generar_factura()` - Calcula IVA 19%
  - `sp_registrar_pago()` - Soporta pagos parciales/mixtos
  - `sp_cambiar_estado_mesa()` - Valida reglas de negocio

### Datos de Prueba (DML)
- ✅ 6 categorías de menú
- ✅ 28 platos con precios y tiempos reales
- ✅ 12 mesas (distintas capacidades/ubicaciones)
- ✅ 8 clientes
- ✅ 7 pedidos en estados variados (PENDIENTE, EN_PREPARACION, LISTO, ENTREGADO, FACTURADO, CANCELADO)
- ✅ 2 facturas (una pagada, una con pago parcial)
- ✅ 2 pagos registrados

## Verificación Rápida

Tras ejecutar, comprobar:

```sql
-- Tablas creadas
SHOW TABLES;

-- Conteo de registros seed
SELECT 'categoria_plato' t, COUNT(*) c FROM categoria_plato
UNION ALL SELECT 'plato', COUNT(*) FROM plato
UNION ALL SELECT 'mesa', COUNT(*) FROM mesa
UNION ALL SELECT 'cliente', COUNT(*) FROM cliente
UNION ALL SELECT 'pedido', COUNT(*) FROM pedido
UNION ALL SELECT 'detalle_pedido', COUNT(*) FROM detalle_pedido
UNION ALL SELECT 'factura', COUNT(*) FROM factura
UNION ALL SELECT 'pago_factura', COUNT(*) FROM pago_factura;

-- Probar vistas
SELECT * FROM v_menu_activo LIMIT 5;
SELECT * FROM v_mesas_disponibles;
SELECT * FROM v_pedidos_cocina;
SELECT * FROM v_facturas_pendientes;
SELECT * FROM v_ventas_diarias;
```

## Integración con Java (Fase 3)

El modelo está diseñado para mapear directamente a las clases Java del proyecto:

| Tabla | Clase Java (src/modelo/) | Estado Actual |
|-------|-------------------------|---------------|
| `mesa` | `Mesa.java` | ✅ Completa |
| `cliente` | `Cliente.java` | ⬜ Vacía - completar |
| `plato` | `Plato.java` | ⬜ Vacía - completar |
| `pedido` | `Pedido.java` | ⬜ Vacía - completar |
| `factura` | `Factura.java` | ⬜ Vacía - completar |

**Siguiente paso**: Implementar las clases modelo vacías usando este esquema como referencia, y crear DAOs/Repositorios que usen los Stored Procedures.

## Cumplimiento Criterios Fase 4

| Criterio | Estado | Evidencia |
|----------|--------|-----------|
| Identificar entidades | ✅ | 8 entidades en ERD.md |
| Definir atributos | ✅ | Diccionario en MODELO_LOGICO.md |
| Definir PK | ✅ | Todas las tablas tienen PK (AUTO_INCREMENT) |
| Definir relaciones FK + cardinalidad | ✅ | 7 FKs documentadas en ERD.md |
| Normalizar hasta 3FN | ✅ | Análisis en MODELO_LOGICO.md |
| Crear Diagrama ER | ✅ | ERD.md (Mermaid + tabla) |
| Crear modelo lógico | ✅ | MODELO_LOGICO.md |
| Crear tablas (DDL) | ✅ | restaurante_db.sql |
| Definir constraints (NOT NULL, UNIQUE, CHECK, FK) | ✅ | Todas en DDL |
| Script SQL completo | ✅ | restaurante_db.sql |
| Datos mock/seed (DML) | ✅ | Sección final del .sql |

## Notas Técnicas

- **Motor**: InnoDB (transacciones, FKs)
- **Charset**: utf8mb4 (soporte emojis, caracteres latinos completos)
- **Collation**: utf8mb4_unicode_ci (ordenación correcta español)
- **IVA**: 19% hardcodeado en SP `sp_generar_factura` (configurable si se requiere)
- **Pagos mixtos**: Soportados vía tabla `PAGO_FACTURA` y metodo_pago='MIXTO' en factura
- **Soft delete**: Campo `activo` en CLIENTE; mesas usan estado `MANTENIMIENTO`

## Contacto / Equipo

- **Grupo**: 04
- **Ficha**: 3239188
- **Programa**: ADSO - SENA
- **Integrantes**: Félix Antonio Murcia Cedeño, Sergio Andrés García Salazar, Diego Andrés Ávila Herrera
- **Repositorio**: https://github.com/FelixJRbyte/sistema-restaurante