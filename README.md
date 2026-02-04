Tienda Catys - Sistema de Gestión
Este es un proyecto de escritorio desarrollado en Java utilizando el patrón de diseño DAO (Data Access Object) y conectado a una base de datos SQL Server. El sistema permite gestionar ventas, productos, clientes y usuarios para un entorno comercial.

Características
Gestión de Ventas: Registro y procesamiento de transacciones.

Inventario: Control detallado de productos (ProductoDAO).

Clientes y Usuarios: Módulos independientes para el registro y administración de perfiles.

Generación de Documentos: Exportación de reportes en PDF.

Interfaz Gráfica (GUI): Diseñada con Java Swing para una experiencia de usuario intuitiva.

Tecnologías Utilizadas
Lenguaje: Java (JDK 17+).

IDE: Visual Studio Code.

Base de Datos: Microsoft SQL Server.

Librerías: * JDBC para la conexión a la base de datos.

Librerías para generación de PDF (iText/JasperReports).

Estructura del Proyecto
El proyecto sigue una arquitectura organizada por capas para facilitar el mantenimiento:

src/vista: Contiene los frames y paneles de la interfaz (Swing).

src/dao: Lógica de acceso a datos y consultas SQL.

src/modelo: Clases de entidad (POJOs) como Cliente, Producto y Usuario.

src/util: Clases de utilidad como la conexión a la base de datos y generadores de archivos.

Configuración
Base de Datos: Ejecuta el script SQL (ubicado en la carpeta /sql si lo incluyes) en tu instancia de SQL Server.

Conexión: Ajusta las credenciales en src/util/ConexionSQL.java.

Librerías: Asegúrate de incluir los archivos .jar de la carpeta lib/ en el classpath de tu proyecto.