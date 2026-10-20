-- =====================================================================
-- SCRIPT DE BASE DE DATOS - SISTEMA DE GESTIÓN DE RESTAURANTE
-- Fase 4: Modelo Relacional y Persistencia SQL (3FN)
-- Grupo 04 - Ficha 3239188 - ADSO SENA
-- =====================================================================

-- Eliminar base de datos si existe y crear nueva
DROP DATABASE IF EXISTS restaurante_db;
CREATE DATABASE restaurante_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE restaurante_db;

-- =====================================================================
-- TABLA: cliente
-- =====================================================================
CREATE TABLE cliente (
    id_cliente        INT AUTO_INCREMENT PRIMARY KEY,
    nombre            VARCHAR(100) NOT NULL,
    telefono          VARCHAR(20),
    email             VARCHAR(100),
    direccion         VARCHAR(200),
    fecha_registro    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    activo            BOOLEAN DEFAULT TRUE,
    
    CONSTRAINT uk_cliente_email UNIQUE (email),
    CONSTRAINT chk_cliente_telefono CHECK (telefono IS NULL OR telefono REGEXP '^[0-9+\-\s()]{7,20}$')
) ENGINE=InnoDB COMMENT='Clientes del restaurante';

-- =====================================================================
-- TABLA: mesa
-- =====================================================================
CREATE TABLE mesa (
    id_mesa       INT AUTO_INCREMENT PRIMARY KEY,
    numero        INT NOT NULL,
    capacidad     INT NOT NULL,
    estado        ENUM('LIBRE', 'OCUPADA', 'RESERVADA', 'MANTENIMIENTO') NOT NULL DEFAULT 'LIBRE',
    ubicacion     VARCHAR(50),
    
    CONSTRAINT uk_mesa_numero UNIQUE (numero),
    CONSTRAINT chk_mesa_capacidad CHECK (capacidad > 0 AND capacidad <= 20),
    CONSTRAINT chk_mesa_numero CHECK (numero > 0)
) ENGINE=InnoDB COMMENT='Mesas del restaurante';

-- =====================================================================
-- TABLA: categoria_plato
-- =====================================================================
CREATE TABLE categoria_plato (
    id_categoria    INT AUTO_INCREMENT PRIMARY KEY,
    nombre          VARCHAR(50) NOT NULL,
    descripcion     VARCHAR(200),
    orden           INT DEFAULT 0,
    
    CONSTRAINT uk_categoria_nombre UNIQUE (nombre)
) ENGINE=InnoDB COMMENT='Categorías del menú (Entradas, Principales, Postres, Bebidas, etc.)';

-- =====================================================================
-- TABLA: plato
-- =====================================================================
CREATE TABLE plato (
    id_plato        INT AUTO_INCREMENT PRIMARY KEY,
    id_categoria    INT NOT NULL,
    nombre          VARCHAR(100) NOT NULL,
    descripcion     VARCHAR(300),
    precio          DECIMAL(10,2) NOT NULL,
    disponible      BOOLEAN DEFAULT TRUE,
    tiempo_preparacion INT DEFAULT 15 COMMENT 'Tiempo estimado en minutos',
    
    CONSTRAINT fk_plato_categoria FOREIGN KEY (id_categoria) REFERENCES categoria_plato(id_categoria)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT chk_plato_precio CHECK (precio >= 0),
    CONSTRAINT chk_plato_tiempo CHECK (tiempo_preparacion > 0 AND tiempo_preparacion <= 180)
) ENGINE=InnoDB COMMENT='Platos del menú';

-- =====================================================================
-- TABLA: pedido
-- =====================================================================
CREATE TABLE pedido (
    id_pedido       INT AUTO_INCREMENT PRIMARY KEY,
    id_mesa         INT NOT NULL,
    id_cliente      INT NULL,
    fecha_hora      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    estado          ENUM('PENDIENTE', 'EN_PREPARACION', 'LISTO', 'ENTREGADO', 'CANCELADO', 'FACTURADO') NOT NULL DEFAULT 'PENDIENTE',
    observaciones   VARCHAR(500),
    total           DECIMAL(10,2) DEFAULT 0.00,
    
    CONSTRAINT fk_pedido_mesa FOREIGN KEY (id_mesa) REFERENCES mesa(id_mesa)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_pedido_cliente FOREIGN KEY (id_cliente) REFERENCES cliente(id_cliente)
        ON UPDATE CASCADE ON DELETE SET NULL,
    CONSTRAINT chk_pedido_total CHECK (total >= 0)
) ENGINE=InnoDB COMMENT='Pedidos realizados en las mesas';

-- =====================================================================
-- TABLA: detalle_pedido (relación N:M entre pedido y plato)
-- =====================================================================
CREATE TABLE detalle_pedido (
    id_detalle      INT AUTO_INCREMENT PRIMARY KEY,
    id_pedido       INT NOT NULL,
    id_plato        INT NOT NULL,
    cantidad        INT NOT NULL DEFAULT 1,
    precio_unitario DECIMAL(10,2) NOT NULL,
    subtotal        DECIMAL(10,2) GENERATED ALWAYS AS (cantidad * precio_unitario) STORED,
    observaciones   VARCHAR(300),
    
    CONSTRAINT fk_detalle_pedido FOREIGN KEY (id_pedido) REFERENCES pedido(id_pedido)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_detalle_plato FOREIGN KEY (id_plato) REFERENCES plato(id_plato)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT uk_detalle_pedido_plato UNIQUE (id_pedido, id_plato),
    CONSTRAINT chk_detalle_cantidad CHECK (cantidad > 0),
    CONSTRAINT chk_detalle_precio CHECK (precio_unitario >= 0)
) ENGINE=InnoDB COMMENT='Líneas de detalle de cada pedido';

-- =====================================================================
-- TABLA: factura
-- =====================================================================
CREATE TABLE factura (
    id_factura      INT AUTO_INCREMENT PRIMARY KEY,
    id_pedido       INT NOT NULL,
    fecha_emision   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    subtotal        DECIMAL(10,2) NOT NULL,
    impuestos       DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    total           DECIMAL(10,2) NOT NULL,
    metodo_pago     ENUM('EFECTIVO', 'TARJETA_CREDITO', 'TARJETA_DEBITO', 'TRANSFERENCIA', 'QR', 'MIXTO') NOT NULL,
    estado_pago     ENUM('PENDIENTE', 'PAGADO', 'ANULADO', 'PARCIAL') NOT NULL DEFAULT 'PENDIENTE',
    numero_factura  VARCHAR(20) NOT NULL,
    observaciones   VARCHAR(500),
    
    CONSTRAINT fk_factura_pedido FOREIGN KEY (id_pedido) REFERENCES pedido(id_pedido)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT uk_factura_pedido UNIQUE (id_pedido),
    CONSTRAINT uk_factura_numero UNIQUE (numero_factura),
    CONSTRAINT chk_factura_subtotal CHECK (subtotal >= 0),
    CONSTRAINT chk_factura_impuestos CHECK (impuestos >= 0),
    CONSTRAINT chk_factura_total CHECK (total >= 0)
) ENGINE=InnoDB COMMENT='Facturas generadas por pedidos';

-- =====================================================================
-- TABLA: pago_factura (para pagos mixtos o parciales)
-- =====================================================================
CREATE TABLE pago_factura (
    id_pago         INT AUTO_INCREMENT PRIMARY KEY,
    id_factura      INT NOT NULL,
    fecha_pago      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    monto           DECIMAL(10,2) NOT NULL,
    metodo_pago     ENUM('EFECTIVO', 'TARJETA_CREDITO', 'TARJETA_DEBITO', 'TRANSFERENCIA', 'QR') NOT NULL,
    referencia      VARCHAR(100),
    
    CONSTRAINT fk_pago_factura FOREIGN KEY (id_factura) REFERENCES factura(id_factura)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT chk_pago_monto CHECK (monto > 0)
) ENGINE=InnoDB COMMENT='Detalle de pagos aplicados a una factura (soporte pagos mixtos)';

-- =====================================================================
-- ÍNDICES ADICIONALES PARA OPTIMIZACIÓN DE CONSULTAS
-- =====================================================================
CREATE INDEX idx_pedido_mesa_estado ON pedido(id_mesa, estado);
CREATE INDEX idx_pedido_fecha ON pedido(fecha_hora);
CREATE INDEX idx_pedido_cliente ON pedido(id_cliente);
CREATE INDEX idx_detalle_pedido ON detalle_pedido(id_pedido);
CREATE INDEX idx_factura_fecha ON factura(fecha_emision);
CREATE INDEX idx_factura_estado_pago ON factura(estado_pago);
CREATE INDEX idx_plato_categoria ON plato(id_categoria, disponible);

-- =====================================================================
-- VISTAS ÚTILES
-- =====================================================================

-- Vista: Mesas disponibles con su capacidad
CREATE VIEW v_mesas_disponibles AS
SELECT id_mesa, numero, capacidad, ubicacion
FROM mesa
WHERE estado = 'LIBRE' AND capacidad > 0;

-- Vista: Menú activo por categoría
CREATE VIEW v_menu_activo AS
SELECT p.id_plato, p.nombre, p.descripcion, p.precio, p.tiempo_preparacion,
       c.nombre AS categoria
FROM plato p
JOIN categoria_plato c ON p.id_categoria = c.id_categoria
WHERE p.disponible = TRUE
ORDER BY c.orden, p.nombre;

-- Vista: Pedidos pendientes de cocina
CREATE VIEW v_pedidos_cocina AS
SELECT pe.id_pedido, pe.id_mesa, m.numero AS numero_mesa, pe.fecha_hora,
       pe.estado, pe.observaciones,
       GROUP_CONCAT(CONCAT(dp.cantidad, 'x ', pl.nombre) SEPARATOR ', ') AS items
FROM pedido pe
JOIN mesa m ON pe.id_mesa = m.id_mesa
JOIN detalle_pedido dp ON pe.id_pedido = dp.id_pedido
JOIN plato pl ON dp.id_plato = pl.id_plato
WHERE pe.estado IN ('PENDIENTE', 'EN_PREPARACION')
GROUP BY pe.id_pedido
ORDER BY pe.fecha_hora;

-- Vista: Facturas pendientes de pago
CREATE VIEW v_facturas_pendientes AS
SELECT f.id_factura, f.numero_factura, f.fecha_emision, f.total, f.estado_pago,
       pe.id_mesa, m.numero AS numero_mesa, c.nombre AS cliente
FROM factura f
JOIN pedido pe ON f.id_pedido = pe.id_pedido
JOIN mesa m ON pe.id_mesa = m.id_mesa
LEFT JOIN cliente c ON pe.id_cliente = c.id_cliente
WHERE f.estado_pago IN ('PENDIENTE', 'PARCIAL')
ORDER BY f.fecha_emision;

-- Vista: Ventas diarias
CREATE VIEW v_ventas_diarias AS
SELECT DATE(fecha_emision) AS fecha,
       COUNT(*) AS total_facturas,
       SUM(total) AS total_ventas,
       SUM(CASE WHEN metodo_pago = 'EFECTIVO' THEN total ELSE 0 END) AS ventas_efectivo,
       SUM(CASE WHEN metodo_pago IN ('TARJETA_CREDITO', 'TARJETA_DEBITO') THEN total ELSE 0 END) AS ventas_tarjeta,
       SUM(CASE WHEN metodo_pago = 'TRANSFERENCIA' THEN total ELSE 0 END) AS ventas_transferencia,
       SUM(CASE WHEN metodo_pago = 'QR' THEN total ELSE 0 END) AS ventas_qr
FROM factura
WHERE estado_pago = 'PAGADO'
GROUP BY DATE(fecha_emision)
ORDER BY fecha DESC;

-- =====================================================================
-- TRIGGERS PARA MANTENER CONSISTENCIA
-- =====================================================================

DELIMITER //

-- Trigger: Actualizar total del pedido al insertar/modificar/eliminar detalle
CREATE TRIGGER trg_detalle_pedido_after_insert
AFTER INSERT ON detalle_pedido
FOR EACH ROW
BEGIN
    UPDATE pedido
    SET total = (
        SELECT COALESCE(SUM(subtotal), 0)
        FROM detalle_pedido
        WHERE id_pedido = NEW.id_pedido
    )
    WHERE id_pedido = NEW.id_pedido;
END//

CREATE TRIGGER trg_detalle_pedido_after_update
AFTER UPDATE ON detalle_pedido
FOR EACH ROW
BEGIN
    UPDATE pedido
    SET total = (
        SELECT COALESCE(SUM(subtotal), 0)
        FROM detalle_pedido
        WHERE id_pedido = NEW.id_pedido
    )
    WHERE id_pedido = NEW.id_pedido;
END//

CREATE TRIGGER trg_detalle_pedido_after_delete
AFTER DELETE ON detalle_pedido
FOR EACH ROW
BEGIN
    UPDATE pedido
    SET total = (
        SELECT COALESCE(SUM(subtotal), 0)
        FROM detalle_pedido
        WHERE id_pedido = OLD.id_pedido
    )
    WHERE id_pedido = OLD.id_pedido;
END//

-- Trigger: Al facturar, cambiar estado del pedido a FACTURADO
CREATE TRIGGER trg_factura_after_insert
AFTER INSERT ON factura
FOR EACH ROW
BEGIN
    UPDATE pedido
    SET estado = 'FACTURADO'
    WHERE id_pedido = NEW.id_pedido;
END//

-- Trigger: Actualizar estado_pago de factura según pagos aplicados
CREATE TRIGGER trg_pago_factura_after_insert
AFTER INSERT ON pago_factura
FOR EACH ROW
BEGIN
    DECLARE total_pagado DECIMAL(10,2);
    DECLARE total_factura DECIMAL(10,2);
    
    SELECT SUM(monto) INTO total_pagado FROM pago_factura WHERE id_factura = NEW.id_factura;
    SELECT total INTO total_factura FROM factura WHERE id_factura = NEW.id_factura;
    
    IF total_pagado >= total_factura THEN
        UPDATE factura SET estado_pago = 'PAGADO' WHERE id_factura = NEW.id_factura;
    ELSEIF total_pagado > 0 THEN
        UPDATE factura SET estado_pago = 'PARCIAL' WHERE id_factura = NEW.id_factura;
    END IF;
END//

-- Trigger: Al liberar mesa (pedido facturado y pagado), cambiar estado mesa a LIBRE
CREATE TRIGGER trg_pedido_after_update
AFTER UPDATE ON pedido
FOR EACH ROW
BEGIN
    IF NEW.estado = 'FACTURADO' AND OLD.estado != 'FACTURADO' THEN
        -- Verificar si la factura está pagada
        IF EXISTS (
            SELECT 1 FROM factura f
            WHERE f.id_pedido = NEW.id_pedido AND f.estado_pago = 'PAGADO'
        ) THEN
            UPDATE mesa SET estado = 'LIBRE' WHERE id_mesa = NEW.id_mesa;
        END IF;
    ELSEIF NEW.estado = 'CANCELADO' AND OLD.estado != 'CANCELADO' THEN
        UPDATE mesa SET estado = 'LIBRE' WHERE id_mesa = NEW.id_mesa;
    ELSEIF NEW.estado IN ('PENDIENTE', 'EN_PREPARACION', 'LISTO', 'ENTREGADO') AND OLD.estado = 'LIBRE' THEN
        UPDATE mesa SET estado = 'OCUPADA' WHERE id_mesa = NEW.id_mesa;
    END IF;
END//

DELIMITER ;

-- =====================================================================
-- PROCEDIMIENTOS ALMACENADOS ÚTILES
-- =====================================================================

DELIMITER //

-- SP: Crear pedido nuevo
CREATE PROCEDURE sp_crear_pedido(
    IN p_id_mesa INT,
    IN p_id_cliente INT,
    IN p_observaciones VARCHAR(500)
)
BEGIN
    DECLARE v_mesa_estado VARCHAR(20);
    
    SELECT estado INTO v_mesa_estado FROM mesa WHERE id_mesa = p_id_mesa;
    
    IF v_mesa_estado != 'LIBRE' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'La mesa no está disponible';
    END IF;
    
    INSERT INTO pedido (id_mesa, id_cliente, observaciones, estado)
    VALUES (p_id_mesa, p_id_cliente, p_observaciones, 'PENDIENTE');
    
    UPDATE mesa SET estado = 'OCUPADA' WHERE id_mesa = p_id_mesa;
    
    SELECT LAST_INSERT_ID() AS id_pedido;
END//

-- SP: Agregar plato a pedido
CREATE PROCEDURE sp_agregar_plato_pedido(
    IN p_id_pedido INT,
    IN p_id_plato INT,
    IN p_cantidad INT
)
BEGIN
    DECLARE v_precio DECIMAL(10,2);
    DECLARE v_pedido_estado VARCHAR(20);
    DECLARE v_plato_disponible BOOLEAN;
    
    SELECT estado INTO v_pedido_estado FROM pedido WHERE id_pedido = p_id_pedido;
    SELECT disponible, precio INTO v_plato_disponible, v_precio FROM plato WHERE id_plato = p_id_plato;
    
    IF v_pedido_estado NOT IN ('PENDIENTE', 'EN_PREPARACION') THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'No se puede modificar un pedido en este estado';
    END IF;
    
    IF NOT v_plato_disponible THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'El plato no está disponible';
    END IF;
    
    INSERT INTO detalle_pedido (id_pedido, id_plato, cantidad, precio_unitario)
    VALUES (p_id_pedido, p_id_plato, p_cantidad, v_precio)
    ON DUPLICATE KEY UPDATE 
        cantidad = cantidad + p_cantidad,
        precio_unitario = v_precio;
    
    UPDATE pedido SET estado = 'EN_PREPARACION' 
    WHERE id_pedido = p_id_pedido AND estado = 'PENDIENTE';
END//

-- SP: Generar factura para un pedido
CREATE PROCEDURE sp_generar_factura(
    IN p_id_pedido INT,
    IN p_metodo_pago VARCHAR(20),
    IN p_numero_factura VARCHAR(20)
)
BEGIN
    DECLARE v_total DECIMAL(10,2);
    DECLARE v_impuestos DECIMAL(10,2);
    DECLARE v_pedido_estado VARCHAR(20);
    DECLARE v_existe_factura INT;
    
    SELECT estado, total INTO v_pedido_estado, v_total FROM pedido WHERE id_pedido = p_id_pedido;
    SELECT COUNT(*) INTO v_existe_factura FROM factura WHERE id_pedido = p_id_pedido;
    
    IF v_existe_factura > 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'El pedido ya tiene factura generada';
    END IF;
    
    IF v_pedido_estado NOT IN ('ENTREGADO', 'EN_PREPARACION', 'LISTO') THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'El pedido no está listo para facturar';
    END IF;
    
    SET v_impuestos = v_total * 0.19; -- IVA 19%
    
    INSERT INTO factura (id_pedido, subtotal, impuestos, total, metodo_pago, numero_factura, estado_pago)
    VALUES (p_id_pedido, v_total, v_impuestos, v_total + v_impuestos, p_metodo_pago, p_numero_factura, 
            CASE WHEN p_metodo_pago = 'EFECTIVO' THEN 'PAGADO' ELSE 'PENDIENTE' END);
    
    UPDATE pedido SET estado = 'FACTURADO' WHERE id_pedido = p_id_pedido;
    
    SELECT LAST_INSERT_ID() AS id_factura;
END//

-- SP: Registrar pago de factura
CREATE PROCEDURE sp_registrar_pago(
    IN p_id_factura INT,
    IN p_monto DECIMAL(10,2),
    IN p_metodo_pago VARCHAR(20),
    IN p_referencia VARCHAR(100)
)
BEGIN
    DECLARE v_estado_actual VARCHAR(20);
    DECLARE v_total_factura DECIMAL(10,2);
    
    SELECT estado_pago, total INTO v_estado_actual, v_total_factura FROM factura WHERE id_factura = p_id_factura;
    
    IF v_estado_actual = 'PAGADO' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'La factura ya está pagada';
    END IF;
    
    INSERT INTO pago_factura (id_factura, monto, metodo_pago, referencia)
    VALUES (p_id_factura, p_monto, p_metodo_pago, p_referencia);
END//

-- SP: Cambiar estado de mesa
CREATE PROCEDURE sp_cambiar_estado_mesa(
    IN p_id_mesa INT,
    IN p_nuevo_estado VARCHAR(20)
)
BEGIN
    DECLARE v_estado_actual VARCHAR(20);
    
    SELECT estado INTO v_estado_actual FROM mesa WHERE id_mesa = p_id_mesa;
    
    IF v_estado_actual = 'OCUPADA' AND p_nuevo_estado = 'LIBRE' THEN
        -- Verificar que no haya pedidos activos
        IF EXISTS (
            SELECT 1 FROM pedido 
            WHERE id_mesa = p_id_mesa AND estado NOT IN ('FACTURADO', 'CANCELADO')
        ) THEN
            SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'La mesa tiene pedidos activos';
        END IF;
    END IF;
    
    UPDATE mesa SET estado = p_nuevo_estado WHERE id_mesa = p_id_mesa;
END//

DELIMITER ;

-- =====================================================================
-- DATOS DE PRUEBA (SEED DATA)
-- =====================================================================

-- Categorías del menú
INSERT INTO categoria_plato (nombre, descripcion, orden) VALUES
('Entradas', 'Platos para comenzar la comida', 1),
('Platos Fuertes', 'Platos principales', 2),
('Postres', 'Dulces y postres', 3),
('Bebidas Frías', 'Refrescos, jugos, aguas', 4),
('Bebidas Calientes', 'Café, té, chocolate', 5),
('Licores', 'Cervezas, vinos, cócteles', 6);

-- Platos del menú
INSERT INTO plato (id_categoria, nombre, descripcion, precio, tiempo_preparacion) VALUES
-- Entradas
(1, 'Sopa de Tomate', 'Sopa casera de tomate con albahaca', 12000.00, 10),
(1, 'Ensalada César', 'Lechuga, crutones, parmesano, aderezo césar', 15000.00, 8),
(1, 'Empanadas de Carne', '3 empanadas criollas con hogao', 10000.00, 12),
(1, 'Ceviche de Camarón', 'Camarón fresco en limón con cebolla y cilantro', 18000.00, 15),

-- Platos Fuertes
(2, 'Bandeja Paisa', 'Frijol, arroz, carne, chorizo, huevo, plátano, arepa, aguacate', 35000.00, 25),
(2, 'Pechuga a la Plancha', 'Pechuga de pollo, arroz, ensalada, papa salada', 22000.00, 20),
(2, 'Salmón al Horno', 'Salmón con verduras asadas y puré de papa', 38000.00, 30),
(2, 'Lasaña de Carne', 'Lasaña casera con carne, bechamel y queso gratinado', 25000.00, 25),
(2, 'Arroz con Pollo', 'Arroz con pollo, verduras, hogao', 20000.00, 18),
(2, 'Hamburguesa Artesanal', 'Carne 200g, queso, lechuga, tomate, papas fritas', 24000.00, 15),

-- Postres
(3, 'Tiramisú', 'Postre italiano de café y mascarpone', 12000.00, 5),
(3, 'Flan de Caramelo', 'Flan casero con caramelo líquido', 8000.00, 3),
(3, 'Brownie con Helado', 'Brownie tibio con bola de helado de vainilla', 14000.00, 8),
(3, 'Copa de Frutas', 'Frutas de temporada con yogur y miel', 10000.00, 5),

-- Bebidas Frías
(4, 'Jugo Natural', 'Jugo de fruta del día (maracuyá, mora, lulo)', 6000.00, 3),
(4, 'Limonada de Coco', 'Limonada con leche de coco', 7000.00, 3),
(4, 'Gaseosa', 'Coca Cola, Sprite, Fanta (lata 350ml)', 4000.00, 1),
(4, 'Agua Mineral', 'Botella 600ml', 3000.00, 1),
(4, 'Malteada', 'Vainilla, chocolate o fresa', 9000.00, 5),

-- Bebidas Calientes
(5, 'Café Americano', 'Café negro colombiano', 4000.00, 3),
(5, 'Café con Leche', 'Café con leche entera', 4500.00, 3),
(5, 'Chocolate Caliente', 'Chocolate colombiano con queso', 6000.00, 5),
(5, 'Té de Hierbas', 'Manzanilla, menta, limón', 4000.00, 3),

-- Licores
(6, 'Cerveza Nacional', 'Águila, Club Colombia, Poker (330ml)', 5000.00, 2),
(6, 'Cerveza Artesanal', 'Cerveza artesanal local (330ml)', 8000.00, 2),
(6, 'Copa de Vino Tinto', 'Vino tinto reserva (150ml)', 15000.00, 2),
(6, 'Mojito', 'Ron, menta, limón, azúcar, soda', 18000.00, 5);

-- Mesas del restaurante
INSERT INTO mesa (numero, capacidad, estado, ubicacion) VALUES
(1, 2, 'LIBRE', 'Terraza'),
(2, 2, 'LIBRE', 'Terraza'),
(3, 4, 'LIBRE', 'Salón Principal'),
(4, 4, 'LIBRE', 'Salón Principal'),
(5, 4, 'LIBRE', 'Salón Principal'),
(6, 6, 'LIBRE', 'Salón Principal'),
(7, 6, 'LIBRE', 'Salón Principal'),
(8, 8, 'LIBRE', 'Salón VIP'),
(9, 8, 'LIBRE', 'Salón VIP'),
(10, 10, 'LIBRE', 'Salón VIP'),
(11, 4, 'MANTENIMIENTO', 'Salón Principal'),
(12, 2, 'LIBRE', 'Barra');

-- Clientes de prueba
INSERT INTO cliente (nombre, telefono, email, direccion) VALUES
('Juan Pérez González', '3101234567', 'juan.perez@email.com', 'Calle 45 # 12-34, Bogotá'),
('María Rodríguez López', '3159876543', 'maria.rodriguez@email.com', 'Carrera 15 # 89-20, Bogotá'),
('Carlos Andrés Gómez', '3205551234', 'carlos.gomez@email.com', 'Av. 68 # 45-67, Bogotá'),
('Ana Lucía Martínez', '3124445566', 'ana.martinez@email.com', 'Calle 100 # 7-89, Bogotá'),
('Pedro Sánchez', '3187778899', 'pedro.sanchez@email.com', 'Carrera 7 # 150-25, Bogotá'),
('Laura Fernández', '3112223344', 'laura.fernandez@email.com', 'Calle 80 # 10-15, Bogotá'),
('Diego Ramírez', '3163334455', 'diego.ramirez@email.com', 'Av. Boyacá # 65-40, Bogotá'),
('Sofía Herrera', '3146667788', 'sofia.herrera@email.com', 'Calle 127 # 15-30, Bogotá');

-- Pedidos de prueba (algunos con diferentes estados)
-- Pedido 1: Mesa 3, Cliente 1 - ENTREGADO (listo para facturar)
INSERT INTO pedido (id_mesa, id_cliente, fecha_hora, estado, observaciones, total) VALUES
(3, 1, '2026-10-19 12:30:00', 'ENTREGADO', 'Sin cebolla en la ensalada', 47000.00),
(4, 2, '2026-10-19 13:15:00', 'EN_PREPARACION', 'Pechuga bien cocida', 0.00),
(5, NULL, '2026-10-19 13:45:00', 'PENDIENTE', 'Para llevar', 0.00),
(6, 3, '2026-10-19 14:00:00', 'LISTO', 'Sin picante', 0.00),
(8, 4, '2026-10-19 19:30:00', 'PENDIENTE', 'Celebración cumpleaños', 0.00),
(3, 5, '2026-10-18 20:00:00', 'FACTURADO', '', 89000.00),
(4, 6, '2026-10-18 21:30:00', 'CANCELADO', 'Cliente se fue', 0.00);

-- Detalles de pedidos
-- Pedido 1 (id=1): Entrada + Plato fuerte + Bebida
INSERT INTO detalle_pedido (id_pedido, id_plato, cantidad, precio_unitario) VALUES
(1, 2, 1, 15000.00),  -- Ensalada César
(1, 5, 1, 35000.00),  -- Bandeja Paisa
(1, 17, 1, 6000.00);  -- Jugo Natural

-- Pedido 2 (id=2): Plato fuerte + Postre
INSERT INTO detalle_pedido (id_pedido, id_plato, cantidad, precio_unitario) VALUES
(2, 6, 1, 22000.00),  -- Pechuga a la Plancha
(2, 12, 1, 12000.00); -- Tiramisú

-- Pedido 3 (id=3): 2 Hamburguesas + 2 Malteadas
INSERT INTO detalle_pedido (id_pedido, id_plato, cantidad, precio_unitario) VALUES
(3, 10, 2, 24000.00), -- Hamburguesa Artesanal
(3, 21, 2, 9000.00);  -- Malteada

-- Pedido 4 (id=4): Salmón + Copa de Vino
INSERT INTO detalle_pedido (id_pedido, id_plato, cantidad, precio_unitario) VALUES
(4, 7, 1, 38000.00),  -- Salmón al Horno
(4, 27, 1, 15000.00); -- Copa de Vino Tinto

-- Pedido 5 (id=5): Para grupo - 2 Bandejas + 2 Jugos + 2 Flanes
INSERT INTO detalle_pedido (id_pedido, id_plato, cantidad, precio_unitario) VALUES
(5, 5, 2, 35000.00),  -- Bandeja Paisa x2
(5, 17, 2, 6000.00),  -- Jugo Natural x2
(5, 13, 2, 8000.00);  -- Flan de Caramelo x2

-- Pedido 6 (id=6): Ya facturado - Lasaña + Café + Tiramisú
INSERT INTO detalle_pedido (id_pedido, id_plato, cantidad, precio_unitario) VALUES
(6, 8, 1, 25000.00),  -- Lasaña
(6, 22, 1, 4500.00),  -- Café con Leche
(6, 12, 1, 12000.00); -- Tiramisú

-- Pedido 7 (id=7): Cancelado - no tiene detalles

-- Facturas
-- Factura para pedido 6 (ya pagada en efectivo)
INSERT INTO factura (id_pedido, fecha_emision, subtotal, impuestos, total, metodo_pago, estado_pago, numero_factura) VALUES
(6, '2026-10-18 21:00:00', 41500.00, 7885.00, 49385.00, 'EFECTIVO', 'PAGADO', 'FAC-20261018-001');

-- Factura para pedido 1 (pendiente de pago)
INSERT INTO factura (id_pedido, fecha_emision, subtotal, impuestos, total, metodo_pago, estado_pago, numero_factura) VALUES
(1, '2026-10-19 13:45:00', 47000.00, 8930.00, 55930.00, 'TARJETA_DEBITO', 'PENDIENTE', 'FAC-20261019-001');

-- Pagos de factura (para factura 1 - pago parcial con tarjeta)
INSERT INTO pago_factura (id_factura, monto, metodo_pago, referencia) VALUES
(2, 30000.00, 'TARJETA_DEBITO', 'AUTH-123456'),
(2, 25930.00, 'EFECTIVO', 'Pago restante en efectivo');

-- Actualizar totales de pedidos (los triggers lo harían automáticamente, pero forzamos para seed)
UPDATE pedido SET total = (
    SELECT COALESCE(SUM(subtotal), 0) FROM detalle_pedido WHERE id_pedido = pedido.id_pedido
) WHERE id_pedido IN (1,2,3,4,5,6);

-- =====================================================================
-- CONSULTAS DE VERIFICACIÓN
-- =====================================================================

-- Verificar estructura
-- SHOW TABLES;
-- DESCRIBE mesa;
-- DESCRIBE cliente;
-- DESCRIBE plato;
-- DESCRIBE pedido;
-- DESCRIBE detalle_pedido;
-- DESCRIBE factura;

-- Verificar datos
-- SELECT * FROM v_menu_activo;
-- SELECT * FROM v_mesas_disponibles;
-- SELECT * FROM v_pedidos_cocina;
-- SELECT * FROM v_facturas_pendientes;
-- SELECT * FROM v_ventas_diarias;

-- =====================================================================
-- FIN DEL SCRIPT
-- =====================================================================