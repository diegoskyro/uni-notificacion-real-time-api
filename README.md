# 🏗️ Proyecto Base — Microservicios Unisimon 2026

Backend de referencia para la construcción y despliegue de microservicios en el ecosistema de la **Universidad Simón Bolívar**. Provee una estructura estandarizada, auditoría automática y una capa de seguridad robusta lista para producción.

---

## 🚀 Tecnologías Core

| Componente            | Tecnología                                  |
| --------------------- | ------------------------------------------- |
| **Lenguaje**          | Java 21 (LTS)                               |
| **Framework**         | Spring Boot 4.x                             |
| **Seguridad**         | unisimon-security (RBAC + `@authz`)         |
| **Documentación**     | SpringDoc OpenAPI (Swagger UI)              |
| **Base de Datos**     | SQL Server — Schema `core`                  |
| **Persistencia**      | Spring Data JPA / Hibernate                 |
| **Build Tool**        | Maven 3.8+                                  |
| **Observabilidad**    | Sentry, OpenTelemetry, Micrometer           |
| **Configuración**     | Spring Cloud Config (Bootstrap)             |
| **Extras**            | Lombok, Spring DevTools, Logstash           |

---

## 📁 Estructura del Proyecto

```
src/main/java/co/edu/unisimon/
├── config/              → Configuraciones (Swagger, RestTemplate, etc.)
├── controller/          → Controladores REST (@RestController)
├── dto/
│   ├── request/         → DTOs de entrada (cuando difieren de la entidad)
│   └── response/        → DTOs de salida (*ResponseDTO)
├── entity/              → Entidades JPA (extienden Auditoria)
├── exception/           → BusinessException, ResourceNotFoundException
├── repository/          → Interfaces JpaRepository
├── response/            → ResponseApi<T> (wrapper genérico de respuesta)
├── security/            → Configuración de seguridad y filtros JWT
└── service/             → Lógica de negocio (@Service, @Transactional)
```

### Clases Base Clave

| Clase                       | Rol                                                                                                                              |
| --------------------------- | -------------------------------------------------------------------------------------------------------------------------------- |
| `Auditoria`                 | Superclase `@MappedSuperclass`. Provee `uuid`, `usuarioCreacion`, `fechaCreacion`, `usuarioActualizacion`, `fechaActualizacion`. |
| `ResponseApi<T>`            | Wrapper genérico de respuesta: `mensaje`, `status`, `data`.                                                                      |
| `BusinessException`         | Error de reglas de negocio (duplicados, etc.).                                                                                   |
| `ResourceNotFoundException` | Recurso no encontrado por UUID.                                                                                                  |

---

## 🔐 Seguridad y Control de Acceso

El proyecto usa la librería `unisimon-security` para autenticación JWT y autorización granular por permisos.

- **Autenticación**: Filtro JWT valida el token en cada request.
- **Autorización**: Permisos específicos evaluados con el componente `@authz`.
- Los endpoints están protegidos con `@PreAuthorize` a nivel de método.

### 🛡️ Protección de Swagger UI
La documentación técnica de la API está protegida mediante **Autenticación Básica (Basic Auth)** con usuarios configurados en el servidor, garantizando que solo personal autorizado pueda visualizar los esquemas y endpoints.

---

## 📖 Documentación de API

El proyecto integra **SpringDoc OpenAPI** para generar documentación interactiva.

- **Swagger UI**: `/base/swagger-ui` (Protegido con Basic Auth)
- **OpenAPI JSON**: `/base/v3/api-docs`

La configuración se encuentra en `OpenApiConfig.java`, donde se define el esquema de seguridad **Bearer JWT** para permitir pruebas de endpoints autenticados directamente desde la interfaz.

---

## 📈 Observabilidad y Monitoreo

Para garantizar la estabilidad y trazabilidad en producción, el proyecto integra:

- **Sentry**: Reporte automático de errores y excepciones con contexto detallado.
- **OpenTelemetry**: Trazabilidad distribuida para seguir el flujo de las peticiones entre microservicios.
- **Micrometer & Prometheus**: Exposición de métricas de rendimiento y salud del sistema a través de endpoints de Actuator.
- **Logstash**: Encriptación y estructuración de logs para su ingesta en stacks ELK/Grafana.

---

## 🤖 AI Agent Skills

El proyecto integra **skills para el asistente de IA** (Antigravity / Gemini), ubicados en `.agents/skills/`. Estos skills estandarizan la generación de código siguiendo las convenciones del proyecto, permitiendo generar módulos completos de forma consistente y lista para producción.

---

### 🔧 `basic-crud`

**Archivo:** [`.agents/skills/basic-crud/SKILL.md`](.agents/skills/basic-crud/SKILL.md)

**¿Qué hace?**
Genera un **CRUD completo** a partir del nombre de una entidad y su schema. Produce automáticamente los 5 archivos Java del módulo:

| Archivo generado                         | Descripción                                                            |
| ---------------------------------------- | ---------------------------------------------------------------------- |
| `entity/{Entidad}.java`                  | Entidad JPA con validaciones Bean Validation                           |
| `repository/{Entidad}Repository.java`    | Repositorio con `findByUuid` y validación de unicidad                  |
| `dto/response/{Entidad}ResponseDTO.java` | DTO de respuesta con mapeo manual desde la entidad                     |
| `service/{Entidad}Service.java`          | Servicio con `guardar`, `listar`, `get`, `actualizar`, `eliminar`      |
| `controller/{Entidad}Controller.java`    | Controller REST con todos los endpoints protegidos por `@PreAuthorize` |

**¿Cómo se usa?**

Pide al asistente:

```
crud para Programa en schema core
```

**Convenciones que garantiza:**

- Entidad extiende `Auditoria` (UUID y auditoría automáticos).
- IDs internos como `Integer` con `IDENTITY`; identificadores públicos siempre como `UUID`.
- Validación de unicidad por nombre antes de guardar y actualizar.
- Mensajes de respuesta en español.
- `@Transactional` en el servicio, `@Valid` en los endpoints.

---

### 🗄️ `db-conventions`

**Archivo:** [`.agents/skills/db-conventions/SKILL.md`](.agents/skills/db-conventions/SKILL.md)

**¿Qué hace?**
Genera **scripts DDL para SQL Server** siguiendo el **Estándar Institucional de Gestión y Analítica de Datos**. Todo objeto generado puede pasar directamente al flujo de producción (Datawave).

Produce:

- `CREATE TABLE` con columnas de negocio + auditoría.
- Constraints nombrados: `pk_`, `fk_`, `uk_`, `ix_` (siempre en minúsculas).
- Índice sobre `uuid` para búsquedas JPA optimizadas.
- Comentarios `EXEC sp_addextendedproperty` obligatorios en tabla y cada columna.

**¿Cómo se usa?**

Se invoca junto con `basic-crud` especificando el schema y la tabla:

```
crud para Programa en core.programa con los campos nombre, descripcion
```

**Reglas clave que aplica:**

| Regla       | Detalle                                                          |
| ----------- | ---------------------------------------------------------------- |
| Nombres     | Todo en minúscula, `snake_case`, sin prefijos `tbl_`             |
| Tablas      | Siempre en singular (`sede`, no `sedes`)                         |
| Texto       | `varchar(n)` — **nunca** `nvarchar`                              |
| Booleanos   | `bit not null default 1`, nombre inicia con `es_`                |
| Fechas      | `datetime`, nombre inicia con `fecha_`                           |
| UUID        | `uniqueidentifier default newid()`                               |
| Auditoría   | Obligatoria en todas las tablas transaccionales                  |
| Comentarios | `sp_addextendedproperty` en tabla y cada columna (sin excepción) |

> ⚠️ Hibernate está configurado con `ddl-auto: none`. Todo DDL debe ejecutarse **exclusivamente** a través del flujo institucional / Datawave. Nunca directamente en producción.

---

## ⚙️ Configuración

El proyecto utiliza un modelo de configuración híbrido que combina propiedades locales y centralizadas.

### 🌐 Gestión Centralizada (Config Server)

Por defecto, el proyecto está configurado para conectarse a un **Spring Cloud Config Server** mediante la propiedad:

```properties
spring.config.import=optional:configserver:http://configUser:ConfigPass123!@192.168.3.83:8888/config
```

Este servidor provee configuraciones estandarizadas para el entorno de **Desarrollo (DEV)**, las cuales incluyen:

- **Base de Datos**: `jdbc:sqlserver://192.168.3.85:1433;databaseName=unisimon_dev`
- **Redis**: Caché centralizada en `192.168.3.83:6379`.
- **Seguridad JWT**: Secreto y tiempos de expiración compartidos.
- **Logstash**: Destino de logs en `192.168.3.212:5044`.
- **Observabilidad**: Configuración de Actuator (Prometheus) y OpenTelemetry (gRPC).

### 🛠️ Uso y Flexibilidad

1.  **Modo Compartido**: Si los parámetros del servidor de configuración coinciden con tus necesidades, no es necesario duplicar estas propiedades localmente en `application.properties`.
2.  **Modo Local (Override)**: Si deseas usar una base de datos propia u otros servicios locales, puedes **sobrescribir** cualquier propiedad agregándola directamente en el archivo local. Las propiedades locales tienen precedencia sobre las del servidor.

> [!IMPORTANT]
> **Validación y Carga**: Cualquier cambio o adición de propiedades que deba ser permanente o compartido con el equipo debe ser enviado al **Departamento de Sistemas sede Cucuta** para su validación y posterior carga en el servidor de configuraciones institucional.

---

© 2026 **Universidad Simón Bolívar**
