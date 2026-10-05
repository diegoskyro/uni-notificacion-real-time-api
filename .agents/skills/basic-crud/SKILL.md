# Skill: Basic CRUD Generator for unisimon-api

---

name: basic-crud
description: Genera un CRUD completo (Entity, Repository, Service, DTO Response, Controller, script DDL SQL Server y JSON de ejemplo para el POST) siguiendo los patrones y convenciones del proyecto unisimon-api (Spring Boot + JPA + Lombok + UUID).

---

## Descripción

Este skill genera todos los archivos necesarios para implementar un CRUD básico en el proyecto `base-api`. Sigue estrictamente los patrones arquitectónicos del proyecto.

---

## Arquitectura del Proyecto

```
src/main/java/co/edu/unisimon/
├── entity/          → Entidades JPA (extienden Auditoria)
├── repository/      → Interfaces JpaRepository
├── service/         → Lógica de negocio (@Service, @Transactional)
├── dto/
│   ├── request/     → DTOs de entrada (si se necesitan)
│   └── response/    → DTOs de salida (ResponseDTO)
├── controller/      → Controladores REST (@RestController)
├── response/        → ResponseApi<T> (wrapper genérico)
└── exception/       → BusinessException, ResourceNotFoundException
```

### Clases base importantes

- **`Auditoria`** (`co.edu.unisimon.entity.Auditoria`): Superclase `@MappedSuperclass` que todas las entidades deben extender. Provee automáticamente:
  - `uuid` (UUID, generado en `@PrePersist`, único, no actualizable)
  - `usuarioCreacion`, `fechaCreacion` (auditoría de creación)
  - `usuarioActualizacion`, `fechaActualizacion` (auditoría de modificación)

- **`ResponseApi<T>`** (`co.edu.unisimon.response.ResponseApi`): Wrapper genérico de respuesta con campos: `mensaje`, `status`, `data`.

- **`BusinessException`** (`co.edu.unisimon.exception.BusinessException`): Para errores de reglas de negocio (duplicados, etc.).

- **`ResourceNotFoundException`** (`co.edu.unisimon.exception.ResourceNotFoundException`): Para cuando no se encuentra un recurso por UUID.

---

## Pasos para Generar el CRUD

Cuando el usuario pida crear un CRUD para una entidad, sigue estos pasos **en orden**:

### Información requerida del usuario

Antes de generar, recopila:

1. **Nombre de la entidad** (e.g., `Conocimiento`, `Programa`, `Sede`)
2. **Nombre del schema/tabla en BD** (e.g., `schema = "nav"`, `name = "conocimiento"`)
3. **Campos de la entidad** con sus tipos, validaciones y restricciones
4. **Ruta base del endpoint** (e.g., `/api/conocimientos`)
5. **Si hay campo `nombre` único** → agregar `existsByNombreIgnoreCase` en el Repository
6. **Si hay relaciones con otras tablas** (FKs) y hacia qué schema/tabla apuntan

---

### Paso 1: Entity

**Paquete:** `co.edu.unisimon.entity`
**Archivo:** `{NombreEntidad}.java`

```java
package co.edu.unisimon.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(schema = "{schema}", name = "{nombre_tabla}")
@Data
@EqualsAndHashCode(callSuper = false)
public class {NombreEntidad} extends Auditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // --- campos de la entidad ---
    // Ejemplo para campo String obligatorio:
    @NotBlank(message = "El {campo} es obligatorio")
    @Size(min = 2, max = 100, message = "El {campo} debe tener entre 2 y 100 caracteres")
    @Column(nullable = false)
    private String {campo};

    // Ejemplo para campo String opcional:
    @Size(max = 255, message = "La descripción no debe exceder los 255 caracteres")
    @Column
    private String descripcion;

    // Campo esActivo siempre presente:
    @Column(name = "es_activo", nullable = false)
    private Boolean esActivo = true;
}
```

**Reglas de la entidad:**

- Siempre extiende `Auditoria`.
- Siempre anota con `@EqualsAndHashCode(callSuper = false)`.
- El `id` es `Integer` con `GenerationType.IDENTITY`.
- El UUID es heredado de `Auditoria` (no se declara en la entidad).
- Siempre incluye `esActivo` como `Boolean` con valor por defecto `true`.
- Usa `@NotBlank` para Strings obligatorios, `@Size` para longitudes.
- Usa `@Column(name = "nombre_columna")` cuando el nombre en BD difiere del campo Java.

---

### Paso 2: Repository

**Paquete:** `co.edu.unisimon.repository`
**Archivo:** `{NombreEntidad}Repository.java`

```java
package co.edu.unisimon.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import co.edu.unisimon.entity.{NombreEntidad};

@Repository
public interface {NombreEntidad}Repository extends JpaRepository<{NombreEntidad}, Integer> {
    Optional<{NombreEntidad}> findByUuid(UUID uuid);
    boolean existsByNombreIgnoreCase(String nombre);  // Solo si hay campo nombre único
}
```

**Reglas del Repository:**

- Siempre incluir `findByUuid(UUID uuid)` para búsqueda por UUID.
- Agregar `existsByNombreIgnoreCase(String nombre)` si existe un campo `nombre` que debe ser único.
- Para otras unicidades, agregar los métodos `existsBy{Campo}IgnoreCase` correspondientes.

---

### Paso 3: ResponseDTO

**Paquete:** `co.edu.unisimon.dto.response`
**Archivo:** `{NombreEntidad}ResponseDTO.java`

```java
package co.edu.unisimon.dto.response;

import java.util.UUID;
import co.edu.unisimon.entity.{NombreEntidad};
import lombok.Data;

@Data
public class {NombreEntidad}ResponseDTO {

    private Integer id;
    private UUID uuid;
    // --- campos a exponer ---
    private String nombre;
    private String descripcion;
    private Boolean esActivo;

    public {NombreEntidad}ResponseDTO({NombreEntidad} entidad) {
        this.id = entidad.getId();
        this.uuid = entidad.getUuid();
        // mapear campos:
        this.nombre = entidad.getNombre();
        this.descripcion = entidad.getDescripcion();
        this.esActivo = entidad.getEsActivo();
    }
}
```

**Reglas del ResponseDTO:**

- Siempre exponer `id` y `uuid`.
- Usar constructor que recibe la entidad y mapea los campos manualmente.
- No usar MapStruct ni otras librerías de mapeo.
- Solo exponer los campos necesarios para el cliente (no exponer datos de auditoría internos a menos que se solicite).

---

### Paso 4: Service

**Paquete:** `co.edu.unisimon.service`
**Archivo:** `{NombreEntidad}Service.java`

```java
package co.edu.unisimon.service;

import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import co.edu.unisimon.dto.response.{NombreEntidad}ResponseDTO;
import co.edu.unisimon.entity.{NombreEntidad};
import co.edu.unisimon.exception.BusinessException;
import co.edu.unisimon.exception.ResourceNotFoundException;
import co.edu.unisimon.repository.{NombreEntidad}Repository;
import jakarta.transaction.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class {NombreEntidad}Service {

    private final {NombreEntidad}Repository {nombreEntidad}Repository;

    public {NombreEntidad}ResponseDTO guardar({NombreEntidad} request) {
        if ({nombreEntidad}Repository.existsByNombreIgnoreCase(request.getNombre())) {
            throw new BusinessException("Ya existe un/una {NombreEntidad} con ese nombre");
        }
        request.setEsActivo(true);
        {NombreEntidad} guardado = {nombreEntidad}Repository.save(request);
        return new {NombreEntidad}ResponseDTO(guardado);
    }

    public List<{NombreEntidad}ResponseDTO> listar() {
        return {nombreEntidad}Repository.findAll().stream()
                .map({NombreEntidad}ResponseDTO::new)
                .toList();
    }

    public {NombreEntidad}ResponseDTO get(UUID uuid) {
        {NombreEntidad} entidad = {nombreEntidad}Repository.findByUuid(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("{NombreEntidad}", uuid));
        return new {NombreEntidad}ResponseDTO(entidad);
    }

    public {NombreEntidad}ResponseDTO actualizar(UUID uuid, {NombreEntidad} request) {
        {NombreEntidad} actual = {nombreEntidad}Repository.findByUuid(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("{NombreEntidad}", uuid));

        String nuevoNombre = request.getNombre().trim();
        if (!actual.getNombre().equalsIgnoreCase(nuevoNombre)
                && {nombreEntidad}Repository.existsByNombreIgnoreCase(nuevoNombre)) {
            throw new BusinessException("Ya existe un/una {NombreEntidad} con ese nombre");
        }

        actual.setNombre(nuevoNombre);
        actual.setDescripcion(request.getDescripcion()); // Solo si tiene descripcion
        actual.setEsActivo(request.getEsActivo());

        {NombreEntidad} actualizado = {nombreEntidad}Repository.save(actual);
        return new {NombreEntidad}ResponseDTO(actualizado);
    }

    public void eliminar(UUID uuid) {
        {NombreEntidad} entidad = {nombreEntidad}Repository.findByUuid(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("{NombreEntidad}", uuid));
        // Agregar validaciones de integridad referencial aquí si aplica
        {nombreEntidad}Repository.delete(entidad);
    }
}
```

**Reglas del Service:**

- Siempre usar `@Service`, `@Transactional`, `@RequiredArgsConstructor`.
- Los métodos siguen la convención de nombres en español: `guardar`, `listar`, `get`, `actualizar`, `eliminar`.
- Verificar duplicados por nombre antes de guardar y antes de actualizar (solo si hay restricción de unicidad).
- En `guardar`, siempre forzar `setEsActivo(true)`.
- Buscar por UUID siempre con `findByUuid` + `orElseThrow(ResourceNotFoundException)`.
- En `actualizar`, solo validar duplicado de nombre si el nombre **cambió** (`!actual.getNombre().equalsIgnoreCase(nuevoNombre)`).

---

### Paso 5: Controller

**Paquete:** `co.edu.unisimon.controller`
**Archivo:** `{NombreEntidad}Controller.java`

```java
package co.edu.unisimon.controller;

import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import co.edu.unisimon.dto.response.{NombreEntidad}ResponseDTO;
import co.edu.unisimon.entity.{NombreEntidad};
import co.edu.unisimon.response.ResponseApi;
import co.edu.unisimon.service.{NombreEntidad}Service;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/{ruta-plural}")
@RequiredArgsConstructor
public class {NombreEntidad}Controller {

    private final {NombreEntidad}Service {nombreEntidad}Service;

    @PostMapping
    public ResponseEntity<ResponseApi<{NombreEntidad}ResponseDTO>> guardar(
            @Valid @RequestBody {NombreEntidad} request) {
        {NombreEntidad}ResponseDTO dto = {nombreEntidad}Service.guardar(request);
        return ResponseEntity.ok(new ResponseApi<>("{NombreEntidad} creado/a correctamente", HttpStatus.OK.value(), dto));
    }

    @GetMapping
    public ResponseEntity<ResponseApi<List<{NombreEntidad}ResponseDTO>>> listar() {
        return ResponseEntity.ok(
                new ResponseApi<>("Listado obtenido correctamente", HttpStatus.OK.value(),
                        {nombreEntidad}Service.listar()));
    }

    @GetMapping("/{uuid}")
    public ResponseEntity<ResponseApi<{NombreEntidad}ResponseDTO>> get(@PathVariable UUID uuid) {
        return ResponseEntity.ok(
                new ResponseApi<>("{NombreEntidad} encontrado/a", HttpStatus.OK.value(),
                        {nombreEntidad}Service.get(uuid)));
    }

    @PutMapping("/{uuid}")
    public ResponseEntity<ResponseApi<{NombreEntidad}ResponseDTO>> actualizar(
            @PathVariable UUID uuid, @Valid @RequestBody {NombreEntidad} request) {
        {NombreEntidad}ResponseDTO dto = {nombreEntidad}Service.actualizar(uuid, request);
        return ResponseEntity.ok(new ResponseApi<>("{NombreEntidad} actualizado/a", HttpStatus.OK.value(), dto));
    }

    @DeleteMapping("/{uuid}")
    public ResponseEntity<ResponseApi<Void>> eliminar(@PathVariable UUID uuid) {
        {nombreEntidad}Service.eliminar(uuid);
        return ResponseEntity.ok(
                new ResponseApi<>("{NombreEntidad} eliminado/a correctamente", HttpStatus.OK.value(), null));
    }
}
```

**Reglas del Controller:**

- Siempre usar `@RestController`, `@RequestMapping`, `@RequiredArgsConstructor`.
- Los endpoints usan `UUID` como identificador público (no `Integer id`).
- El request body para `POST` y `PUT` es directamente la **Entidad** (no un DTO de request), validada con `@Valid`.
- Todos los métodos retornan `ResponseEntity<ResponseApi<T>>`.
- Responde siempre con `HttpStatus.OK` (200), incluso para creaciones.

---

## Convenciones de Nomenclatura

| Elemento      | Convención                | Ejemplo                   |
| ------------- | ------------------------- | ------------------------- |
| Clase Entity  | PascalCase                | `Conocimiento`            |
| Tabla BD      | snake_case                | `conocimiento`            |
| Schema BD     | lowercase                 | `nav`, `core`             |
| Repository    | `{Entidad}Repository`     | `ConocimientoRepository`  |
| Service       | `{Entidad}Service`        | `ConocimientoService`     |
| Controller    | `{Entidad}Controller`     | `ConocimientoController`  |
| ResponseDTO   | `{Entidad}ResponseDTO`    | `ConocimientoResponseDTO` |
| Endpoint base | `/{entidades-plural}` | `/conocimientos`      |

---

## Casos Especiales

### Entidad sin campo `nombre`

- Omitir `existsByNombreIgnoreCase` del Repository.
- Omitir la validación de unicidad en el Service.
- Ajustar el método `actualizar` para mapear los campos correspondientes.

### Entidad con relaciones (FK)

- Declarar la FK como `@ManyToOne @JoinColumn(name = "fk_columna")` en la entidad.
- En el ResponseDTO, exponer solo el `id` o nombre del objeto relacionado (no el objeto completo), para evitar lazy-loading issues.
- En el Service, buscar el objeto relacionado por ID antes de hacer `save`.

### Entidad con RequestDTO separado

- Si los campos del body de entrada difieren significativamente de la entidad (ej. cuando hay FKs que se reciben como Integer pero son objetos JPA), crear un DTO específico en `dto/request/{Entidad}Request.java`.
- El Controller recibirá el `Request`, el Service lo procesará y construirá la entidad.

---

---

### Paso 6: Script DDL SQL Server (Validación o Generación)

> 📌 **Este paso es CRÍTICO.** Se divide en dos flujos dependiendo de si el usuario proporcionó un DDL original o no.

#### Escenario A: El usuario PROPORCIONA un DDL original
Si el usuario provee un DDL, el asistente **DEBE** validarlo contra el skill `db-conventions` y corregirlo si es necesario.

1.  **Validación**: Verificar si cumple con:
    *   Todo en **minúscula** y `snake_case`.
    *   Tabla en **singular**.
    *   Tipos oficiales (`varchar`, `bit`, `datetime`, `int`, `decimal(18,2)` y `uniqueidentifier`).
    *   Constraints con prefijos correctos (`pk_`, `fk_`, `uk_`, `ix_`) en minúscula.
    *   **Auditoría completa**: `fecha_creacion`, `usuario_creacion`, `fecha_actualizacion`, `usuario_actualizacion`, `es_activo`, `uuid`.
    *   Comentarios `sp_addextendedproperty` en tabla y **todas** las columnas.
2.  **Informe de Errores**: Listar brevemente los incumplimientos encontrados.
3.  **Resultado**: Devolver el DDL **corregido y completo**, que será el que se use para generar el código Java.

#### Escenario B: El usuario NO proporciona DDL (Solo campos)
Generar el DDL desde cero siguiendo estrictamente `db-conventions`.

**Reglas de Generación/Corrección:**
- **Auditoría obligatoria**: Incluir siempre al final de las columnas propias.
- **Identificadores**: El PK siempre se llama `id`, el UUID es para lógica pública.
- **Comentarios**: Obligatorios en formato `EXEC sp_addextendedproperty`.

**Resultado esperado (Ejemplo Corregido/Generado):**

```sql
create table {schema}.{tabla} (
    id int identity(1,1) primary key not null,
    nombre varchar(100) not null,
    descripcion varchar(255) null,
    fecha_creacion datetime not null,
    usuario_creacion varchar(100) not null,
    fecha_actualizacion datetime null,
    usuario_actualizacion varchar(100) null,
    es_activo bit not null default 1,
    uuid uniqueidentifier default newid() null,
    constraint uk_{tabla}_uuid unique (uuid)
);

create index ix_{tabla}_id on {schema}.{tabla} (id);

EXEC sp_addextendedproperty @name = 'MS_Description',
@value = 'Tabla que almacena {descripcion}',
@level0type = 'SCHEMA', @level0name = '{schema}',
@level1type = 'TABLE', @level1name = '{tabla}';

EXEC sp_addextendedproperty @name = 'MS_Description',
@value = 'Llave primaria',
@level0type = 'SCHEMA', @level0name = '{schema}',
@level1type = 'TABLE', @level1name = '{tabla}',
@level2type = 'COLUMN', @level2name = 'id';
-- (Resto de comentarios...)
```

---

### Paso 7: JSON de Ejemplo (POST)

Finalmente, genera el cuerpo JSON necesario para realizar el registro (POST) de la nueva entidad. Este JSON debe ser compatible con la estructura de la Entidad y estar listo para usarse en herramientas como Postman.

**Ejemplo de resultado esperado:**

```json
{
  "campo1": "valor de ejemplo",
  "campo2": 123,
  "objetoRelacionado": {
    "id": 1
  }
}
```

---

## Checklist de Generación

Al generar un CRUD, confirma que se crearon/generaron **todos** estos elementos:

**Archivos Java:**

- [ ] `entity/{NombreEntidad}.java`
- [ ] `repository/{NombreEntidad}Repository.java`
- [ ] `dto/response/{NombreEntidad}ResponseDTO.java`
- [ ] `service/{NombreEntidad}Service.java`
- [ ] `controller/{NombreEntidad}Controller.java`

**Script SQL:**

- [ ] Script DDL con `CREATE TABLE {schema}.{nombre_tabla}` generado
- [ ] Columnas de auditoría incluidas (`uuid`, `usuarioCreacion`, `fechaCreacion`, `usuarioActualizacion`, `fechaActualizacion`)
- [ ] Constraints nombrados (`PK_`, `UQ_uuid`, `FK_` si aplica)
- [ ] Índice `IX_{tabla}_uuid` creado
- [ ] Tipos de dato correctos según `db-conventions` (`NVARCHAR`, `DATETIME2`, `BIT`, `CHAR(36)`)
- [ ] JSON de ejemplo para el POST generado

**Validaciones de calidad:**

- [ ] Cada archivo usa el paquete correcto (`co.edu.unisimon.<capa>`)
- [ ] La entidad extiende `Auditoria`
- [ ] El Controller usa `UUID` en paths, no `Integer`
- [ ] El Service tiene `@Transactional`
- [ ] Los mensajes de respuesta están en español
- [ ] El schema de la entidad Java coincide con el schema del script SQL
