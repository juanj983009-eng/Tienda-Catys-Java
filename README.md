# Sistema de Gestión Integral - Catys Enterprise

## 1. Descripción del Proyecto
Catys Enterprise es una plataforma de software de nivel empresarial diseñada bajo una arquitectura desacoplada y contenerizada, cuyo objetivo es la gestión operativa, el control de inventarios y la automatización del punto de venta para entornos comerciales gastronómicos. 

El sistema ha sido migrado de una arquitectura monolítica legacy basada en Java Swing a un ecosistema distribuido moderno. El backend está construido sobre Spring Boot, utilizando JPA/Hibernate para la persistencia transaccional, mientras que el frontend está desarrollado en React empleando componentes modulares y estilos optimizados. Toda la infraestructura se encuentra orquestada mediante Docker y Docker Compose, garantizando la portabilidad absoluta del sistema en entornos de desarrollo, pruebas y producción.

---

## 2. Arquitectura de Software y Patrones de Diseño

El sistema implementa rigurosamente las mejores prácticas de la industria, asegurando un código limpio, mantenible y escalable basado en los principios SOLID:

* **Principio de Responsabilidad Única (SRP):** Cada componente, servicio y controlador posee una única razón para cambiar, separando de forma estricta la lógica de negocio de la capa de acceso a datos y de los controladores REST.
* **Principio de Inversión de Dependencias (DIP):** Los componentes de alto nivel no dependen de implementaciones de bajo nivel, sino de abstracciones. El uso de interfaces en la capa de persistencia (`Repository`) e inyección de dependencias vía Spring Framework desacopla por completo el motor de base de datos de la lógica transaccional.
* **Separación de Capas (Multitier Architecture):**
    1. **Capa de Presentación (Frontend):** React SPA que consume de forma asíncrona los servicios expuestos por el backend.
    2. **Capa de Controladores (REST API):** Controladores Spring REST que gestionan las peticiones HTTP, validan los datos de entrada y orquestan las respuestas mediante Objetos de Transferencia de Datos (DTOs).
    3. **Capa de Servicio (Business Logic):** Implementa las reglas de negocio del sistema, la gestión de transacciones atómicas y las validaciones avanzadas.
    4. **Capa de Persistencia (Data Access):** Interfaces que extienden de Spring Data JPA para la abstracción de consultas sobre el motor relacional SQL Server.

---

## 3. Stack Tecnológico

### Backend
* **Lenguaje:** Java 17 / 21
* **Framework Principal:** Spring Boot 3.x (Spring Web, Spring Data JPA)
* **Manejador de Dependencias:** Maven
* **Persistencia y ORM:** Hibernate / JPA
* **Driver de Base de Datos:** Microsoft JDBC Driver para SQL Server

### Frontend
* **Librería Principal:** React
* **Herramienta de Construcción:** Vite
* **Framework de Estilos:** Tailwind CSS

### Infraestructura y Persistencia
* **Motor de Base de Datos:** Microsoft SQL Server 2019+
* **Contenerización:** Docker
* **Orquestación:** Docker Compose

---

## 4. Estructura del Proyecto

La estructura del directorio ha sido normalizada para cumplir con los estándares de proyectos de grado empresarial:

```text
TIENDA CATYS/
├── .github/                   # Configuraciones de repositorio (modernización excluida)
├── catys-web/                 # Código fuente del frontend (React + Vite)
│   ├── src/
│   │   ├── components/        # Componentes de UI reutilizables
│   │   ├── config/            # Configuraciones de la API de consumo Client-side
│   │   ├── App.jsx            # Componente raíz y control de estado global
│   │   └── main.jsx           # Punto de entrada de la aplicación React
│   └── package.json
├── database/                  # Scripts de inicialización e infraestructura de datos
│   └── CatysDB_Setup.sql      # Script idempotente de creación de esquema y catálogo real
├── src/                       # Código fuente del backend (Spring Boot)
│   └── main/
│       ├── java/
│       │   └── com/
│       │       └── catys/
│       │           ├── controller/  # Controladores REST API
│       │           ├── dto/         # Objetos de Transferencia de Datos (DTOs)
│       │           ├── model/       # Entidades mapeadas mediante JPA (Domain Models)
│       │           ├── repository/  # Interfaces de acceso a datos de Spring Data
│       │           └── service/     # Capa de servicios y lógica transaccional
│       └── resources/
│           └── application.properties # Configuraciones del entorno de la aplicación
├── Dockerfile                 # Definición de construcción de imagen del backend
├── docker-compose.yml         # Archivo de orquestación multi-contenedor
└── pom.xml                    # Archivo de configuración y dependencias de Maven 
```

---

## 5. El Modelo de Datos e Infraestructura Relacional

El esquema de la base de datos se encuentra estructurado en el archivo `database/CatysDB_Setup.sql`. Es un script completamente idempotente que valida la existencia previa de los objetos antes de su creación y maneja tipos de datos robustos como `NVARCHAR` para soportar codificación Unicode.

### Tablas Principales
* **Usuarios:** Almacena las credenciales operativas. Las contraseñas se gestionan mediante hashes encriptados con el algoritmo BCrypt para garantizar estándares avanzados de seguridad.
* **Clientes:** Registro de entidades comerciales identificadas por DNI.
* **Productos:** Catálogo comercial optimizado con índices específicos en la columna de categorías para acelerar las lecturas en el punto de venta.
* **Ventas:** Cabecera transaccional que registra los totales, métodos de pago (EFECTIVO, TARJETA_CREDITO, YAPE_PLIN) y marcas de tiempo.
* **venta_detalles:** Tabla relacional normalizada que implementa claves foráneas con integridad referencial estricta y borrado en cascada (`ON DELETE CASCADE`), vinculando de manera matemática cada transacción con sus respectivos productos y cantidades.

El script de inicialización automatiza la carga de **26 productos comerciales reales** parametrizados exactamente según el entorno de producción, resguardando la consistencia de los identificadores mediante sentencias explícitas de control de identidad (`SET IDENTITY_INSERT`).

---

## 6. Despliegue de Infraestructura y Contenerización (Docker)

Para mitigar los fallos de conectividad derivados de los tiempos de arranque de las bases de datos (*Race Conditions*), el archivo `docker-compose.yml` implementa una arquitectura defensiva basada en políticas de verificación de salud (*Healthchecks*).

El backend no inicia su proceso de ejecución hasta que el contenedor de Microsoft SQL Server se encuentre en estado completamente saludable e interceptando peticiones en el puerto parametrizado.

### Comandos de Control de Infraestructura

* **Levantar todo el entorno (Base de Datos + API + Frontend):**
  ```bash
  docker-compose up --build -d
  ```
* **Detener los servicios (Graceful Shutdown):**
  ```bash
  docker-compose stop
  ```
* **Desmontar contenedores y redes virtuales:**
  ```bash
  docker-compose down
  ```

---

## 7. Despliegue Local para Desarrollo (Sin Docker)

Si prefiere ejecutar el proyecto de forma nativa en su máquina de desarrollo para depuración rápida o pruebas locales:

### Requisitos Previos
* **Java JDK 17** o superior instalado.
* **Maven 3.8+** (opcional, o utilizar el wrapper `./mvnw`).
* **Node.js v18+** y manejador de paquetes **npm**.
* **Microsoft SQL Server** local o remoto.

### Paso 1: Configurar la Base de Datos
1. Inicie su gestor de SQL Server (SSMS, Azure Data Studio, etc.).
2. Ejecute el script idempotente de configuración ubicado en [CatysDB_Setup.sql](file:///D:/Proyectos/Tienda%20Catys/database/CatysDB_Setup.sql). Esto creará la base de datos `CatysDB` y cargará el catálogo de productos inicial de forma automatizada.

### Paso 2: Iniciar el Servidor Backend (Spring Boot)
1. Modifique el archivo de configuración `src/main/resources/application.properties` con las credenciales de su SQL Server local.
2. Inicie el servicio de backend en el puerto **8089** (definido en las propiedades):
   ```bash
   mvn spring-boot:run
   ```
   * *URL base de la API:* [http://localhost:8089/api](http://localhost:8089/api)
   * *URL de imágenes:* [http://localhost:8089/imagenes](http://localhost:8089/imagenes)

### Paso 3: Iniciar el Frontend (React + Vite)
1. Navegue al directorio del frontend:
   ```bash
   cd catys-web
   ```
2. Cree su archivo de variables de entorno `.env` a partir de la plantilla:
   ```bash
   copy .env.example .env
   ```
3. Instale las dependencias e inicie el servidor de desarrollo de Vite:
   ```bash
   npm install
   npm run dev
   ```
   * *Puerto por defecto de Vite:* [http://localhost:5173](http://localhost:5173) (o puertos correlativos de desarrollo como el `5175`).

---

## 8. Puertos de Operación y Mapeo del Sistema

| Servicio | Puerto por Defecto | Protocolo | Configuración / Variables |
| :--- | :--- | :--- | :--- |
| **Frontend (React)** | `5173` o `5175` | HTTP | Mapeado por Vite en desarrollo |
| **Backend REST API** | `8089` | HTTP | `server.port=8089` |
| **Database (SQL Server)**| `1433` | TCP/IP | Configurado en el connection string |