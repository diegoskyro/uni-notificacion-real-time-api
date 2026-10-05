# Skill: DB Conventions — SQL Server Script Generator

---

name: db-conventions
description: Genera scripts DDL para SQL Server siguiendo el Estándar Institucional de Gestión y Analítica de Datos. Tablas en singular, todo en minúscula, snake*case, varchar(n), datetime, constraints con prefijos en minúscula (pk*, fk*, uk*, ix\_), auditoría obligatoria, y comentarios sp_addextendedproperty en cada tabla y columna.

---

## Fuente de Verdad

Este skill está basado en el **Estándar Institucional de Gestión y Analítica de Datos** (documento oficial de base de datos). Tiene precedencia sobre cualquier convención previa. Todo objeto de BD generado debe poder pasar a producción bajo este estándar.

---

## Contexto del Proyecto

- **Motor de BD:** Microsoft SQL Server
- **Nombre de BD:** `unisimon_dev`
- **Schema:** Lo indica el usuario al solicitar el CRUD en el formato `{schema}.{tabla}` (ej: `auth.rol`, `nav.sede`, `core.programa`). **No asumir un schema por defecto.**
- **JPA ddl-auto:** `none` — Hibernate **nunca** crea ni altera tablas automáticamente. Todo DDL es manual y se ejecuta exclusivamente a través del flujo institucional / Datawave.
- **Zona horaria:** `America/Bogota`

---

## 1. Principio General

Todo objeto de base de datos debe:

- Estar **en minúscula**.
- Usar exclusivamente **`snake_case`**.
- **No usar** tildes, ñ, espacios ni abreviaturas no documentadas.
- **No mezclar** idiomas.
- **No usar** prefijos `tbl_`, `t_`, `tb_`.

| ✅ Correcto          | ❌ Incorrecto                                  |
| -------------------- | ---------------------------------------------- |
| `estudiante`         | `tblEstudiante`, `ESTUDIANTE`, `Estudiante`    |
| `programa_academico` | `programaAcademico`, `ProgramaAcademico`       |
| `fecha_creacion`     | `fechaCreacion`, `created_at`, `FechaCreacion` |

---

## 2. Nombre de Tablas

**Reglas obligatorias:**

- **Singular** (nunca plural).
- Todo en minúscula.
- `snake_case`.
- Representar una entidad clara del negocio.

| ✅ Correcto          | ❌ Incorrecto                       |
| -------------------- | ----------------------------------- |
| `estudiante`         | `estudiantes`, `tbl_estudiante`     |
| `programa_academico` | `programaAcademico`                 |
| `detalle_factura`    | `DetalleFactura`, `DETALLE_FACTURA` |

---

## 3. Nombre de Campos / Columnas

**Reglas obligatorias:**

- Minúscula y `snake_case`.
- Descriptivos y completos.
- **No repetir** el nombre de la tabla dentro del campo.
- Todo campo debe declarar explícitamente `null` o `not null`.

| ✅ Correcto        | ❌ Incorrecto                    |
| ------------------ | -------------------------------- |
| `numero_documento` | `NumDoc`, `numDoc`, `str_nombre` |
| `fecha_nacimiento` | `fechaNacimiento`, `fecha_nac`   |
| `valor_total`      | `ValorTotal`, `vl_total`         |

---

## 4. Llave Primaria

**Siempre se llama `id`.** Tipo `int` (o `bigint` para volúmenes altos).

```sql
id   int   identity(1,1)   not null
```

| ✅ Correcto | ❌ Incorrecto                                     |
| ----------- | ------------------------------------------------- |
| `id`        | `id_estudiante`, `estudiante_id`, `pk_estudiante` |

---

## 5. Llaves Foráneas

Formato obligatorio: `<tabla_referenciada>_id`

```sql
estudiante_id        int   not null
programa_academico_id  int   not null
```

| ✅ Correcto     | ❌ Incorrecto                                    |
| --------------- | ------------------------------------------------ |
| `estudiante_id` | `id_estudiante`, `fk_estudiante`, `estudianteId` |

---

## 6. Campos Booleanos

Deben iniciar con `es_`. Tipo `bit not null default 1`.

```sql
es_activo    bit   not null   default 1
es_anulado   bit   not null   default 0
```

| ✅ Correcto | ❌ Incorrecto                            |
| ----------- | ---------------------------------------- |
| `es_activo` | `activo`, `flag_activo`, `estado_activo` |

---

## 7. Campos de Fecha

Deben iniciar con `fecha_`. Tipo `datetime`.

```sql
fecha_creacion       datetime   not null
fecha_actualizacion  datetime   null
fecha_nacimiento     datetime   null
```

| ✅ Correcto         | ❌ Incorrecto                                |
| ------------------- | -------------------------------------------- |
| `fecha_creacion`    | `created_at`, `fechaCreacion`, `fecCreacion` |
| `fecha_vencimiento` | `vencimiento`, `fecha_vto`                   |

---

## 8. Campos de Auditoría Obligatorios

**Todas las tablas transaccionales deben incluir estos campos** al final, antes de los constraints:

```sql
-- Auditoría obligatoria (estándar institucional)
fecha_creacion        datetime      not null,
usuario_creacion      varchar(100)  not null,
fecha_actualizacion   datetime      null,
usuario_actualizacion varchar(100)  null,
es_activo             bit           not null  default 1,

-- UUID para integración JPA (Auditoria.java)
uuid                  uniqueidentifier  default newid()  null,
```

> ⚠️ **`fecha_creacion` y `usuario_creacion` son `not null`** — siempre se registra quién y cuándo creó el registro.
> ⚠️ **`uuid`** se agrega por requerimiento de `Auditoria.java` (JPA). Usa `default newid()` como fallback si Java no lo provee.

---

## 9. Tipos de Datos Oficiales

| Uso                 | Tipo oficial SQL Server | Notas                                       |
| ------------------- | ----------------------- | ------------------------------------------- |
| Texto corto/largo   | `varchar(n)`            | Obligatorio. Definir `n` explícito.         |
| Texto sin límite    | `varchar(max)`          | Solo con autorización formal. Evitar.       |
| Booleano            | `bit`                   | `not null default 1` o `not null default 0` |
| Entero PK           | `int identity(1,1)`     | O `bigint` para volúmenes altos             |
| Entero FK / general | `int`                   |                                             |
| Monetario           | `decimal(18,2)`         | Nunca `float`, `money` ni `real`            |
| Porcentaje          | `decimal(9,6)`          |                                             |
| Fecha               | `datetime`              | No usar `datetime2` salvo caso justificado  |
| UUID / GUID         | `uniqueidentifier`      | Con `default newid()`                       |

> ❌ **Prohibido:** `nvarchar`, `text`, `float` para monetarios, `tinyint` para booleanos, `varchar(max)` sin autorización.

---

## 10. Nombre de Constraints (todo en minúscula)

| Tipo   | Formato                           | Ejemplo                   |
| ------ | --------------------------------- | ------------------------- |
| PK     | `pk_<tabla>`                      | `pk_estudiante`           |
| FK     | `fk_<tabla>_<tabla_referenciada>` | `fk_matricula_estudiante` |
| Unique | `uk_<tabla>_<campo>`              | `uk_estudiante_codigo`    |
| Index  | `ix_<tabla>_<campo>`              | `ix_estudiante_uuid`      |

> ❌ **Nunca en mayúsculas:** `PK_`, `FK_`, `UQ_`, `IX_`.

---

## 11. Comentarios Obligatorios

**Toda tabla y toda columna debe tener comentario.** Sin comentarios, no pasa a producción.

Formato obligatorio: `EXEC sp_addextendedproperty` (mayúsculas), sin prefijo `N`, sin `GO` entre cada sentencia, cada parámetro en su propia línea:

```sql
-- Comentario de tabla
EXEC sp_addextendedproperty @name = 'MS_Description',
@value = 'Tabla que almacena {descripcion de la tabla}',
@level0type = 'SCHEMA',
@level0name = '{schema}',
@level1type = 'TABLE',
@level1name = '{tabla}';

-- Comentario de columna
EXEC sp_addextendedproperty @name = 'MS_Description',
@value = '{descripcion del campo}',
@level0type = 'SCHEMA',
@level0name = '{schema}',
@level1type = 'TABLE',
@level1name = '{tabla}',
@level2type = 'COLUMN',
@level2name = '{columna}';
```

> ❌ No usar `N'...'` ni `exec` en minúscula.
> ❌ No comprimir varios parámetros en una misma línea.
> ❌ No poner `GO` después de cada `EXEC sp_addextendedproperty` (solo después del `create table` e índices).

---

## 12. Plantilla Base de Script DDL

> **Schema y tabla** los provee el usuario al solicitar el CRUD: e.g. `crud en auth.rol` → schema = `auth`, tabla = `rol` (singular).
> ❌ **Sin bloque de cabecera** `-- ====...====`. El script inicia directamente con `create table`.
> ❌ **Sin comentarios `--`** dentro del cuerpo del `create table`. Las columnas se agrupan visualmente solo por líneas en blanco.

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
@value = 'Tabla que almacena {descripcion completa de la tabla}',
@level0type = 'SCHEMA',
@level0name = '{schema}',
@level1type = 'TABLE',
@level1name = '{tabla}';

EXEC sp_addextendedproperty @name = 'MS_Description',
@value = 'Identificador unico interno autoincremental. Llave primaria.',
@level0type = 'SCHEMA',
@level0name = '{schema}',
@level1type = 'TABLE',
@level1name = '{tabla}',
@level2type = 'COLUMN',
@level2name = 'id';

-- repetir EXEC sp_addextendedproperty para cada columna restante
```

---

## 13. Plantilla con Foreign Keys

```sql
create table {schema}.{tabla} (

    id                    int              identity(1,1)  not null,

    {referencia}_id       int              not null,

    nombre                varchar(100)     not null,
    es_activo             bit              not null  default 1,

    fecha_creacion        datetime         not null,
    usuario_creacion      varchar(100)     not null,
    fecha_actualizacion   datetime         null,
    usuario_actualizacion varchar(100)     null,

    uuid                  uniqueidentifier default newid()  null,

    constraint pk_{tabla}                         primary key (id),
    constraint uk_{tabla}_uuid                    unique      (uuid),
    constraint fk_{tabla}_{referencia}            foreign key ({referencia}_id)
        references {schema_ref}.{tabla_ref} (id)
);

create index ix_{tabla}_uuid on {schema}.{tabla} (uuid);

```

---

## 14. Plantilla de Tabla de Relación (ManyToMany)

```sql
create table {schema}.{tabla_a}_{tabla_b} (

    {tabla_a}_id   int   not null,
    {tabla_b}_id   int   not null,

    constraint pk_{tabla_a}_{tabla_b}        primary key ({tabla_a}_id, {tabla_b}_id),
    constraint fk_{tabla_a}_{tabla_b}_izq    foreign key ({tabla_a}_id)
        references {schema}.{tabla_a} (id),
    constraint fk_{tabla_a}_{tabla_b}_der    foreign key ({tabla_b}_id)
        references {schema}.{tabla_b} (id)
);
```

> Las tablas de relación **no** llevan auditoría ni UUID.

---

## 15. Mapeo Java → SQL (Referencia Rápida)

| Anotación Java                          | SQL Server (estándar institucional)          |
| --------------------------------------- | -------------------------------------------- |
| `@Id @GeneratedValue(IDENTITY)`         | `id int identity(1,1) not null`              |
| `@Column(nullable = false)`             | `not null`                                   |
| `@Column` (sin nullable)                | `null`                                       |
| `@Size(max = 100)` + `String`           | `varchar(100)`                               |
| `@NotBlank`                             | `not null` + validación de app               |
| `@Email`                                | `varchar(150)` + validación de app           |
| `Boolean esActivo = true`               | `es_activo bit not null default 1`           |
| `@ManyToOne @JoinColumn(name="x_id")`   | `x_id int not null` + `constraint fk_...`    |
| `uuid` (Auditoria.java)                 | `uuid uniqueidentifier default newid() null` |
| `fechaCreacion` (Auditoria.java)        | `fecha_creacion datetime not null`           |
| `fechaActualizacion` (Auditoria.java)   | `fecha_actualizacion datetime null`          |
| `usuarioCreacion` (Auditoria.java)      | `usuario_creacion varchar(100) not null`     |
| `usuarioActualizacion` (Auditoria.java) | `usuario_actualizacion varchar(100) null`    |

---

## 16. Schema de la Tabla

> ⚠️ **El schema SIEMPRE lo indica el usuario** al momento de solicitar el CRUD.
> Formato: `crud en {schema}.{tabla}` — por ejemplo:
>
> - `crud en auth.rol` → schema = `auth`, tabla = `rol`
> - `crud en nav.sede` → schema = `nav`, tabla = `sede`
> - `crud en core.programa` → schema = `core`, tabla = `programa`
> - `crud en dbo.estudiante` → schema = `dbo`, tabla = `estudiante`

Si el usuario indica un schema diferente a los conocidos, usarlo tal cual sin cuestionar.

---

## 17. Prohibiciones Absolutas

- ❌ CamelCase en cualquier objeto de BD
- ❌ `MAYÚSCULAS` en objetos de BD
- ❌ Prefijos `tbl_`, `t_`, `tb_`
- ❌ Tablas sin PK
- ❌ Tablas sin auditoría
- ❌ FKs sin constraint real nombrado
- ❌ Campos sin `null` / `not null` explícito
- ❌ `nvarchar` (usar `varchar`)
- ❌ `float`, `money`, `real` para monetarios (usar `decimal(18,2)`)
- ❌ `tinyint` para booleanos (usar `bit`)
- ❌ Tablas o columnas sin comentario `sp_addextendedproperty`
- ❌ Tablas en plural (`estudiantes` → debe ser `estudiante`)
- ❌ Constraints sin nombre o con nombre en mayúsculas
- ❌ Comentarios `--` dentro del cuerpo del `create table` (sección de columnas)

---

## 18. Gestión de Cambios

Todo cambio en BD (DDL/DML) debe ejecutarse exclusivamente a través del flujo institucional / Datawave. Nunca directamente por consola en ambientes productivos o preproducción.

Requisitos:

- Ticket/solicitud con justificación y alcance
- Script versionado en repositorio con trazabilidad del autor
- Validación técnica (DBA/Analítica de Datos) con evidencia
- Aprobación previa para ambientes productivos
- Evidencia posterior de ejecución

Cualquier cambio fuera de este flujo será considerado **NO CONFORME** y deberá reversarse.
