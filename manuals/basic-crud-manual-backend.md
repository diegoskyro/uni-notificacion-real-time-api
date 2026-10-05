# Manual de Referencia: Skill `basic-crud`

Este manual describe el funcionamiento y el uso del skill **`basic-crud`**, diseñado para automatizar la generación y construcción completa de módulos CRUD (Crear, Leer, Actualizar y Eliminar) siguiendo los estándares de arquitectura y base de datos de la institución.

---
   
## 🏷️ Nombre del Skill
*   **Identificador:** `basic-crud`
*   **Propósito:** Generar la estructura completa de un módulo CRUD (Entidad, Repositorio, DTO de Respuesta, Servicio, Controlador, Script DDL SQL Server y JSON de prueba) de acuerdo a las convenciones y patrones del proyecto `unisimon-api`.

---

## ⚙️ ¿Qué hace este Skill de forma automática?

Cuando solicitas la generación de un CRUD para una nueva entidad, el asistente de IA genera las siguientes piezas clave de forma 100% automatizada y articulada:

### 1. Entidad JPA (`entity/`)
*   Crea la clase de entidad que extiende de `Auditoria` (heredando automáticamente `uuid`, `usuarioCreacion`, `fechaCreacion`, etc.).
*   Aplica las anotaciones `@Entity`, `@Table` (con schema y tabla especificados), `@Data` y `@EqualsAndHashCode(callSuper = false)`.
*   Añade el campo `esActivo` (`Boolean`) por defecto en `true` y los campos de negocio validados mediante `@NotBlank` y `@Size`.

### 2. Repositorio Spring Data JPA (`repository/`)
*   Crea la interfaz que extiende `JpaRepository<{Entidad}, Integer>`.
*   Define la búsqueda obligatoria por identificador público `findByUuid(UUID uuid)`.
*   Añade validadores de unicidad como `existsByNombreIgnoreCase(String nombre)` si la entidad posee un campo único de negocio.

### 3. DTO de Respuesta (`dto/response/`)
*   Define una estructura plana `{Entidad}ResponseDTO` que expone los datos necesarios, incluyendo `id` y `uuid`.
*   Implementa un constructor manual que mapea la entidad al DTO, asegurando no exponer metadatos de auditoría interna y evitando el uso de MapStruct.

### 4. Servicio de Lógica de Negocio (`service/`)
*   Genera la clase de servicio anotada con `@Service`, `@Transactional` y `@RequiredArgsConstructor`.
*   Implementa los 5 métodos estándar en español: `guardar`, `listar`, `get`, `actualizar` y `eliminar`.
*   Integra el manejo de excepciones mediante `BusinessException` y `ResourceNotFoundException`.

### 5. Controlador REST (`controller/`)
*   Define el `@RestController` mapeado bajo la ruta plural `/{entidades}`.
*   Expone endpoints estandarizados usando `UUID` para búsquedas, actualizaciones y eliminaciones.
*   Acepta la entidad validada con `@Valid` en el cuerpo de las peticiones (`POST`/`PUT`) y siempre retorna un `ResponseEntity<ResponseApi<T>>` con estado `HTTP 200 (OK)`.

### 6. Script de Base de Datos (DDL SQL Server)
> [!IMPORTANT]
> El script generado cumple de manera estricta con las directrices del skill `db-conventions`:
> *   Utiliza minúsculas y `snake_case` para tablas y columnas.
> *   Define los tipos correctos (`NVARCHAR` para cadenas, `DATETIME2` para fechas, `BIT` para booleanos).
> *   Integra las columnas de auditoría al final y nombra explícitamente los constraints (`PK_`, `UQ_`, `FK_`, `IX_`).
> *   Incluye un índice obligatorio `IX_{tabla}_uuid` para optimizar las consultas del JPA.

### 7. JSON de Solicitud (Postman)
*   Genera un JSON de ejemplo con datos representativos listo para enviar peticiones `POST` al nuevo endpoint.

---

## 📂 Lógica de Nomenclatura y Estructura

El skill implementa una correspondencia rigurosa para garantizar la homogeneidad en todas las capas del backend:

| Elemento | Convención | Ejemplo |
| :--- | :--- | :--- |
| **Clase Entity** | PascalCase | `Conocimiento` |
| **Tabla BD** | snake_case (Singular) | `conocimiento` |
| **Schema BD** | lowercase | `nav`, `core`, `auth` |
| **Repository** | `{Entidad}Repository` | `ConocimientoRepository` |
| **Service** | `{Entidad}Service` | `ConocimientoService` |
| **Controller** | `{Entidad}Controller` | `ConocimientoController` |
| **ResponseDTO** | `{Entidad}ResponseDTO` | `ConocimientoResponseDTO` |
| **Endpoint base** | `/{entidades-plural}` | `/conocimientos` |

---

## 🚀 Cómo usar este Skill

Para iniciar la generación automatizada, debes indicarle al asistente de IA los requerimientos del nuevo módulo. Proporciona el nombre de la entidad, el schema, la ruta base y el listado de campos con sus especificaciones.

### Prompt de Ejemplo:
```text
Generar CRUD para la entidad Sede en el schema core con los siguientes campos:
- nombre: String (obligatorio, único, máximo 150 caracteres)
- direccion: String (opcional, máximo 250 caracteres)
- telefono: String (opcional, máximo 20 caracteres)

Ruta base: /sedes
```
*(El asistente procesará los datos, aplicará las convenciones institucionales y generará el juego completo de archivos listos para integrarse al proyecto).*
