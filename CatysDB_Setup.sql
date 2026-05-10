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
--    ProductoRepository.save() también escribe: nombre, precio, stock, categoria, imagen
--    ProductoRepository.update() actualiza: nombre, precio, stock, categoria, imagen
--    VentaRepository.save() lee: stock (UPDATE Productos SET stock = stock - ?)
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
--    NOTAS:
--      - metodo_pago se guarda como el enum.name() de MetodoPago
--        (ej: 'EFECTIVO', 'TARJETA_CREDITO', 'YAPE_PLIN')
--      - detalle_compra es el texto de ticket agrupado generado
--        por VentaRepository.construirDetalleTexto()
--      - cliente_nombre se guarda como String (sin FK a Clientes
--        porque la venta puede ser a un cliente no registrado)
--      - fecha la genera SQL Server con GETDATE()
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
-- 5. DATOS INICIALES
-- ============================================================

-- ------------------------------------------------------------
-- 5.1 Usuario Administrador
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
-- 5.2 Productos de Muestra
--     Categorías observadas en el código: CRIOLLO, BEBIDAS (y las
--     que defina el negocio). Se insertan ejemplos representativos.
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT 1 FROM dbo.Productos)
BEGIN
    INSERT INTO dbo.Productos (nombre, precio, stock, categoria, imagen) VALUES
    -- Platos Criollos
    ('Lomo Saltado',        25.00, 50, 'CRIOLLO',  'lomo_saltado.jpg'),
    ('Aji de Gallina',      22.00, 40, 'CRIOLLO',  'aji_gallina.jpg'),
    ('Pollo a la Brasa',    35.00, 30, 'CRIOLLO',  'pollo_brasa.jpg'),
    ('Ceviche Mixto',       38.00, 25, 'CRIOLLO',  'ceviche_mixto.jpg'),
    ('Arroz con Leche',     10.00, 60, 'CRIOLLO',  'arroz_leche.jpg'),
    -- Bebidas
    ('Inca Kola 500ml',      4.50, 100,'BEBIDAS',  'inca_kola.jpg'),
    ('Coca-Cola 500ml',      4.50, 100,'BEBIDAS',  'coca_cola.jpg'),
    ('Agua San Luis 600ml',  2.50, 120,'BEBIDAS',  'agua_sanluis.jpg'),
    ('Chicha Morada 1L',     8.00, 80, 'BEBIDAS',  'chicha_morada.jpg'),
    ('Maracuyá Helado',      6.00, 70, 'BEBIDAS',  'maracuya.jpg');

    PRINT '✔ Productos de muestra insertados (10 productos).';
END
ELSE
    PRINT '⚠ La tabla Productos ya tiene datos — productos de muestra omitidos.';
GO

-- ============================================================
-- 6. VERIFICACIÓN FINAL
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
PRINT '  Conecta la aplicación con: databaseName=CatysDB en config.properties';
GO
