---
trigger: always_on
---

# Reglas del Proyecto: base-api (Backend)

## Este documento define las reglas y convenciones que el asistente de IA debe seguir **siempre** al trabajar en este proyecto. Tienen precedencia sobre suposiciones o patrones genéricos.

## 🏗️ Arquitectura General

- **Lenguaje:** Java 21 — Spring Boot 4.x
- **Base de datos:** SQL Server — BD `unisimon_dev`
- **Schema:** el que indique el usuario
- **Puerto:** `8082`
- **Zona horaria:** `America/Bogota`
- **ddl-auto:** `none` — Hibernate **nunca** crea ni altera tablas. Todo DDL es manual.

---

## 📦 Empaquetado

Todas las clases deben pertenecer a subpaquetes de `co.edu.unisimon`:

| Capa          | Paquete                        |
| ------------- | ------------------------------ |
| Entidades     | `co.edu.unisimon.entity`       |
| Repositorios  | `co.edu.unisimon.repository`   |
| Servicios     | `co.edu.unisimon.service`      |
| Controladores | `co.edu.unisimon.controller`   |
| DTOs salida   | `co.edu.unisimon.dto.response` |
| DTOs entrada  | `co.edu.unisimon.dto.request`  |
| Respuesta     | `co.edu.unisimon.response`     |
| Excepciones   | `co.edu.unisimon.exception`    |

---

## 🧱 Entidades (`entity/`)

- **Siempre** extienden `Auditoria` (no declarar `uuid`, `fechaCreacion`, etc. en la entidad).
- **Siempre** anotar con `@EqualsAndHashCode(callSuper = false)`.
- La PK es `Integer id` con `GenerationType.IDENTITY`.
- El UUID es el identificador **público** (heredado de `Auditoria`).
- **Cuando el usuario lo requiera** incluir el campo `Boolean esActivo = true` con `@Column(name = "es_activo", nullable = false)`.
- Validaciones Bean Validation: `@NotBlank`, `@Size`, `@Email`, etc.

```java
@Entity
@Table(schema = "core", name = "nombre_tabla")
@Data
@EqualsAndHashCode(callSuper = false)
public class MiEntidad extends Auditoria {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    // ... campos
    @Column(name = "es_activo", nullable = false)
    private Boolean esActivo = true;
}
```

---

## 🗄️ Repositorios (`repository/`)

- Extienden `JpaRepository<Entidad, Integer>`.

---

## ⚙️ Servicios (`service/`)

- Anotaciones: `@Service`, `@Transactional`, `@RequiredArgsConstructor`.
- Transacciones: `@Transactional` a nivel de clase; `@Transactional(readOnly = true)` en métodos de solo lectura.
- Nombres de métodos **en español**: `guardar`, `listar`, `get`, `actualizar`, `eliminar`.
- Buscar siempre por UUID con `findByUuid().orElseThrow(() -> new ResourceNotFoundException(...))`.
- En `guardar`: validar unicidad y forzar `setEsActivo(true)`.
- En `actualizar`: validar unicidad **solo si el nombre cambió**.
- Excepciones: usar `BusinessException` (lógica de negocio) y `ResourceNotFoundException` (no encontrado).

---

## 🌐 Controladores (`controller/`)

- Anotaciones: `@RestController`, `@RequestMapping`, `@RequiredArgsConstructor`.
- Ruta base: `/api/{entidades-en-plural}` (e.g., `/api/sedes`).
- Identificador de ruta: **siempre UUID**, nunca Integer.
- Request body en `POST`/`PUT`: la **entidad directa** con `@Valid` (salvo casos especiales con DTO de entrada).
- Todos los métodos retornan `ResponseEntity<ResponseApi<T>>`.
- Siempre responder `HttpStatus.OK` (200), incluso en creación.
- **Seguridad**: cada endpoint con `@PreAuthorize("@authz.tienePermiso{Entidad}('PERMISO')")`.

```java
@PostMapping
@PreAuthorize("@authz.tienePermisoSede('LISTAR_PERFILES')")
public ResponseEntity<ResponseApi<SedeResponseDTO>> guardar(@Valid @RequestBody Sede request) { ... }
```

---

## 📤 DTOs de Respuesta (`dto/response/`)

- Nombre: `{Entidad}ResponseDTO`.
- **Siempre** exponer `id` (Integer) y `uuid` (UUID).
- Constructor que recibe la entidad y mapea los campos manualmente. **Sin MapStruct.**
- No exponer datos de auditoría interna (usuarioCreacion, etc.) salvo solicitud explícita.
- **Siempre** responder en estructura plana, incluyendo los campos foraneos y definir que campos retornar segun indicacion del usuario por defecto: uuid,id,nombre o descripcion.

---

## 🔐 Seguridad

- La librería `unisimon-security` gestiona JWT y autorización.
- Verificación de permisos: `@authz.tienePermiso{Entidad}('NOMBRE_PERMISO')`.
- No agregar `@PreAuthorize("isAuthenticated()")` a nivel de clase; proteger cada método individualmente.

---

## 🗃️ Base de Datos (DDL)

Seguir siempre el skill `db-conventions`. Reglas clave:

- Todo en **minúscula** y `snake_case`. Sin tildes, ñ ni prefijos `tbl_`.
- Tablas en **singular** (`sede`, no `sedes`).
- Tipos: `varchar(n)` (no `nvarchar`), `bit` (no `tinyint`), `datetime`, `int`, `decimal(18,2)`.
- PKs: `id int identity(1,1) not null`.
- FKs: formato `{tabla_referenciada}_id`.
- Booleanos: `es_*`, tipo `bit not null default 1`.
- Fechas: `fecha_*`, tipo `datetime`.
- Constraints: `pk_`, `fk_`, `uk_`, `ix_` (minúscula, sin excepción).
- Auditoría obligatoria: `fecha_creacion`, `usuario_creacion`, `fecha_actualizacion`, `usuario_actualizacion`, `uuid`.
- Comentarios `EXEC sp_addextendedproperty` en **tabla y cada columna** (obligatorio para producción).

---

## 🤖 Skills Disponibles

Antes de generar código, verificar si existe un skill aplicable:

| Skill            | Cuándo usarlo                          | Archivo                                  |
| ---------------- | -------------------------------------- | ---------------------------------------- |
| `basic-crud`     | Al crear un nuevo módulo CRUD completo | `.agents/skills/basic-crud/SKILL.md`     |
| `db-conventions` | Al generar scripts DDL para SQL Server | `.agents/skills/db-conventions/SKILL.md` |

---

## ✅ Checklist Pre-Entrega

Antes de dar por terminado cualquier CRUD, verificar:

- [ ] Entidad extiende `Auditoria` y tiene `esActivo`.
- [ ] Repository tiene `findByUuid` y `existsByNombre` (si aplica).
- [ ] Service tiene `@Transactional` y los 5 métodos estándar.
- [ ] Controller usa UUID en paths, `@Valid` en body y `@PreAuthorize` en cada método.
- [ ] ResponseDTO expone `id` y `uuid` y mapea con constructor manual.
- [ ] Mensajes de respuesta en **español**.
- [ ] Script DDL generado y validado contra `db-conventions`.
