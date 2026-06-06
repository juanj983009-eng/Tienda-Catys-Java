-- ============================================================
--  CatysDB · Script de Creación Completa
--  Generado desde: ProductoRepository, VentaRepository,
--                  ClienteRepository, UsuarioRepository
--                  + DTOs y clases Modelo
--
--  Motor    : SQL Server 2017+
--  Collation: Latin1_General_CI_AS (default SQL Server)
--  Ejecutar : sqlcmd -S localhost -i CatysDB_Setup.sql
--              o abrir en SSMS y ejecutar (F5)
-- ============================================================

-- ------------------------------------------------------------
-- 0. Crear la base de datos si no existe
-- ------------------------------------------------------------
IF NOT EXISTS (
    SELECT name FROM sys.databases WHERE name = N'CatysDB'
)
BEGIN
    CREATE DATABASE CatysDB
        COLLATE Latin1_General_CI_AS;
    PRINT '✔ Base de datos CatysDB creada.';
END
ELSE
BEGIN
    PRINT '⚠ La base de datos CatysDB ya existe — se usará la existente.';
END
GO

USE CatysDB;
GO

-- ============================================================
-- 1. TABLA: Usuarios
--    Repository: UsuarioRepository
--    Columnas leídas:
--      id_usuario, nombre_completo, username, password, rol
-- ============================================================
IF OBJECT_ID('dbo.Usuarios', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.Usuarios (
        id_usuario      INT           NOT NULL IDENTITY(1,1),
        nombre_completo NVARCHAR(150) NOT NULL,
        username        NVARCHAR(80)  NOT NULL,
        password        NVARCHAR(100) NOT NULL,   -- hash BCrypt (60 chars max, 100 por seguridad)
        rol             NVARCHAR(30)  NOT NULL     -- 'ADMIN' | 'CAJERO' | 'GERENTE' ...

        CONSTRAINT PK_Usuarios        PRIMARY KEY (id_usuario),
        CONSTRAINT UQ_Usuarios_username UNIQUE     (username)
    );
    PRINT '✔ Tabla Usuarios creada.';
END
ELSE
    PRINT '⚠ Tabla Usuarios ya existe — omitida.';
GO

-- ============================================================
-- 2. TABLA: Clientes
--    Repository: ClienteRepository
--    Columnas leídas:
--      id_cliente, dni, nombre, telefono
-- ============================================================
IF OBJECT_ID('dbo.Clientes', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.Clientes (
        id_cliente INT           NOT NULL IDENTITY(1,1),
        dni        NVARCHAR(15)  NOT NULL,
        nombre     NVARCHAR(150) NOT NULL,
        telefono   NVARCHAR(20)  NULL

        CONSTRAINT PK_Clientes      PRIMARY KEY (id_cliente),
        CONSTRAINT UQ_Clientes_dni  UNIQUE       (dni)
    );
    PRINT '✔ Tabla Clientes creada.';
END
ELSE
    PRINT '⚠ Tabla Clientes ya existe — omitida.';
GO

-- ============================================================
-- 3. TABLA: Productos
--    Repository: ProductoRepository
--    Columnas leídas/escritas:
--      id_producto, nombre, precio, imagen, categoria, stock
-- ============================================================
IF OBJECT_ID('dbo.Productos', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.Productos (
        id_producto INT             NOT NULL IDENTITY(1,1),
        nombre      NVARCHAR(150)   NOT NULL,
        precio      DECIMAL(10, 2)  NOT NULL CHECK (precio >= 0),
        stock       INT             NOT NULL DEFAULT 0 CHECK (stock >= 0),
        categoria   NVARCHAR(80)    NOT NULL,
        imagen      NVARCHAR(300)   NULL      -- ruta relativa o nombre de archivo

        CONSTRAINT PK_Productos PRIMARY KEY (id_producto)
    );

    CREATE INDEX IX_Productos_categoria ON dbo.Productos (categoria);

    PRINT '✔ Tabla Productos creada.';
END
ELSE
    PRINT '⚠ Tabla Productos ya existe — omitida.';
GO

-- ============================================================
-- 4. TABLA: Ventas
--    Repository: VentaRepository
--    Columnas leídas/escritas:
--      id_venta, cliente_nombre, metodo_pago, detalle_compra,
--      total, fecha
-- ============================================================
IF OBJECT_ID('dbo.Ventas', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.Ventas (
        id_venta        INT            NOT NULL IDENTITY(1,1),
        cliente_nombre  NVARCHAR(200)  NOT NULL,
        metodo_pago     NVARCHAR(30)   NOT NULL,    -- enum.name(): EFECTIVO | TARJETA_CREDITO | YAPE_PLIN
        detalle_compra  NVARCHAR(MAX)  NULL,         -- texto del ticket
        total           DECIMAL(10, 2) NOT NULL CHECK (total >= 0),
        fecha           DATETIME       NOT NULL DEFAULT GETDATE()

        CONSTRAINT PK_Ventas PRIMARY KEY (id_venta)
    );

    CREATE INDEX IX_Ventas_fecha        ON dbo.Ventas (fecha DESC);
    CREATE INDEX IX_Ventas_metodo_pago  ON dbo.Ventas (metodo_pago);

    PRINT '✔ Tabla Ventas creada.';
END
ELSE
    PRINT '⚠ Tabla Ventas ya existe — omitida.';
GO

-- ============================================================
-- 5. TABLA: venta_detalles
--    Repository: VentaDetalleRepository
-- ============================================================
IF OBJECT_ID('dbo.venta_detalles', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.venta_detalles (
        id_venta_detalle INT            NOT NULL IDENTITY(1,1),
        id_venta         INT            NOT NULL,
        id_producto      INT            NOT NULL,
        cantidad         INT            NOT NULL CHECK (cantidad > 0),
        precio_unitario  DECIMAL(10, 2) NOT NULL CHECK (precio_unitario >= 0),

        CONSTRAINT PK_venta_detalles PRIMARY KEY (id_venta_detalle),
        CONSTRAINT FK_venta_detalles_Ventas FOREIGN KEY (id_venta) REFERENCES dbo.Ventas(id_venta),
        CONSTRAINT FK_venta_detalles_Productos FOREIGN KEY (id_producto) REFERENCES dbo.Productos(id_producto)
    );

    CREATE INDEX IX_venta_detalles_venta ON dbo.venta_detalles (id_venta);
    CREATE INDEX IX_venta_detalles_producto ON dbo.venta_detalles (id_producto);

    PRINT '✔ Tabla venta_detalles creada.';
END
ELSE
    PRINT '⚠ Tabla venta_detalles ya existe — omitida.';
GO

-- ============================================================
-- 6. DATOS INICIALES
-- ============================================================

-- ------------------------------------------------------------
-- 6.1 Usuario Administrador
--     username : admin
--     password : admin123
--     BCrypt hash (factor 10):
--       $2a$10$8.VUnmTeid7ZpYtDqV5GLeZ.p0BRsJq3CskvHnZpWlGZp8S8D9S6e
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT 1 FROM dbo.Usuarios WHERE username = 'admin')
BEGIN
    INSERT INTO dbo.Usuarios (nombre_completo, username, password, rol)
    VALUES (
        'Administrador del Sistema',
        'admin',
        '$2a$10$8.VUnmTeid7ZpYtDqV5GLeZ.p0BRsJq3CskvHnZpWlGZp8S8D9S6e',
        'ADMIN'
    );
    PRINT '✔ Usuario admin insertado correctamente.';
END
ELSE
    PRINT '⚠ Usuario admin ya existe — omitido.';
GO

-- ------------------------------------------------------------
-- 6.2 Productos de Muestra (26 Productos Oficiales)
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT 1 FROM dbo.Productos)
BEGIN
    SET IDENTITY_INSERT dbo.Productos ON;

    INSERT INTO dbo.Productos (id_producto, nombre, precio, stock, categoria, imagen) VALUES
    -- ENTRADAS
    (1, N'Alitas Broaster', 15.00, 50, 'ENTRADAS', 'alitas-broaster.jpg'),
    (2, N'Anticuchos', 18.00, 50, 'ENTRADAS', 'anticuchos.jpg'),
    (3, N'Causa Rellena', 12.00, 50, 'ENTRADAS', 'causa-rellena.jpg'),
    (4, N'Papa a la Huancaína', 12.00, 50, 'ENTRADAS', 'papa-a-la-huancaina.jpg'),
    (5, N'Rocoto Relleno', 16.00, 50, 'ENTRADAS', 'rocoto-relleno.jpg'),
    (6, N'Wantán Frito', 10.00, 50, 'ENTRADAS', 'wantan-frito.jpg'),
    -- CRIOLLA
    (7, N'Aeropuerto', 18.00, 50, 'CRIOLLA', 'aeropuerto.jpg'),
    (8, N'Ají de Gallina', 20.00, 50, 'CRIOLLA', 'aji-de-gallina.jpg'),
    (9, N'Ceviche', 25.00, 50, 'CRIOLLA', 'ceviche.jpg'),
    (10, N'Arroz Chaufa', 18.00, 50, 'CRIOLLA', 'arroz-chaufa.jpg'),
    (11, N'Chijaukay', 22.00, 50, 'CRIOLLA', 'chijaukay.jpg'),
    (12, N'Triple', 15.00, 50, 'CRIOLLA', 'triple.jpg'),
    (13, N'Hamburguesa', 12.00, 50, 'CRIOLLA', 'hamburguesa.jpg'),
    (14, N'Kamlu', 24.00, 50, 'CRIOLLA', 'kamlu.jpg'),
    (15, N'Lomo Saltado', 25.00, 50, 'CRIOLLA', 'lomo.jpg'),
    (16, N'Mostrito', 20.00, 50, 'CRIOLLA', 'mostrito.jpg'),
    (17, N'Pizza', 15.00, 50, 'CRIOLLA', 'pizza.jpg'),
    (18, N'Pollo Broaster', 18.00, 50, 'CRIOLLA', 'pollo-broaster.jpg'),
    (19, N'Salchipapa', 12.00, 50, 'CRIOLLA', 'salchipapa.jpg'),
    (20, N'Seco de Cordero', 26.00, 50, 'CRIOLLA', 'seco-de-cordero.jpg'),
    (21, N'Sopa de Gallina', 15.00, 50, 'CRIOLLA', 'sopa-de-gallina.jpg'),
    (22, N'Tallarín Saltado', 18.00, 50, 'CRIOLLA', 'tallarin-saltado.jpg'),
    -- BEBIDAS
    (23, N'Chicha', 8.00, 100, 'BEBIDAS', 'chicha.jpg'),
    (24, N'Incakola Personal', 4.50, 99, 'BEBIDAS', 'incakola-personal.jpg'),
    (25, N'Limonada', 6.00, 100, 'BEBIDAS', 'limonada.jpg'),
    (26, N'Maracuyá Helada', 6.00, 99, 'BEBIDAS', 'maracuya.jpg');

    SET IDENTITY_INSERT dbo.Productos OFF;

    PRINT '✔ Productos de muestra insertados (26 productos oficiales).';
END
ELSE
    PRINT '⚠ La tabla Productos ya tiene datos — productos de muestra omitidos.';
GO

-- ============================================================
-- 7. VERIFICACIÓN FINAL
-- ============================================================
PRINT '';
PRINT '══════════════════════════════════════════════';
PRINT '  CatysDB · Resumen de objetos creados';
PRINT '══════════════════════════════════════════════';

SELECT
    t.name          AS [Tabla],
    p.rows          AS [Filas],
    CAST(ROUND((SUM(a.used_pages) * 8) / 1024.0, 2) AS NVARCHAR) + ' KB' AS [Tamaño]
FROM
    sys.tables          t
    INNER JOIN sys.indexes      i ON t.object_id = i.object_id
    INNER JOIN sys.partitions   p ON i.object_id = p.object_id AND i.index_id = p.index_id
    INNER JOIN sys.allocation_units a ON p.partition_id = a.container_id
WHERE
    t.schema_id = SCHEMA_ID('dbo')
    AND i.index_id IN (0, 1)
GROUP BY
    t.name, p.rows
ORDER BY
    t.name;
GO

PRINT '';
PRINT '✔ Script CatysDB_Setup.sql ejecutado exitosamente.';
PRINT '  Conecta la aplicación con: databaseName=CatysDB';
GO
