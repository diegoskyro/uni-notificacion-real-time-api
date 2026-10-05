# Skill: Proyecto Renombrar (Renombrar y Reiniciar Git)

---

name: rename-project
description: Renombrar el proyecto base para un nuevo uso. Renombra el artifactId, el nombre del proyecto, el paquete base Java, actualiza Jenkinsfile, propiedades y reinicia el repositorio Git vinculándolo a uno nuevo.

---

## Descripción

Este skill se utiliza cuando se clona el proyecto base `unisimon-api` para iniciar un nuevo microservicio o API. Realiza la limpieza del historial de Git y renombra todas las referencias al proyecto original en archivos de configuración, código fuente y pipelines de CI/CD.

---

## Información Requerida

Al ejecutar este skill, el usuario debe proporcionar únicamente la:

1. **URL del Nuevo Repositorio Git** (e.g., `https://github.com/Unisimon/uni-nomina-api.git`)

### Lógica de Derivación Automática

A partir de la URL del repo, el skill debe derivar:

- **Nombre Base (Slug):** Se extrae del final de la URL (e.g., `uni-nomina-api`).
- **ArtifactId / AppName:** Se elimina el prefijo `uni-` (e.g., `nomina-api`).
- **Sub-paquete (Contexto):** La parte central del nombre base antes del sufijo `-api` (e.g., `nomina`).
- **Nuevo Package Base:** `co.edu.unisimon.{sub-paquete}` (e.g., `co.edu.unisimon.nomina`).
- **ClassName:** PascalCase del sub-paquete + `ApiApplication` (e.g., `NominaApiApplication`).

---

## Pasos de Refactorización

Sigue estos pasos en orden:

### 1. Configuración de Maven (`pom.xml`)

Busca y reemplaza en el archivo `pom.xml`:

- `<artifactId>base-api</artifactId>` → `<artifactId>{ArtifactId}</artifactId>`
- `<name>base-api</name>` → `<name>{ArtifactId}</name>`
- `<description>Proyecto base-api</description>` → `<description>Proyecto {ArtifactId}</description>`

### 2. Propiedades de la Aplicación (`application*.properties`)

En **todos** los archivos `src/main/resources/application*.properties` (incluyendo `-prod`, `-dev`, etc.):

- `spring.application.name=proveedor-api` → `spring.application.name={ArtifactId}`
- `OTEL_SERVICE_NAME=proveedor-api` → `OTEL_SERVICE_NAME={ArtifactId}`
- `server.servlet.context-path=/base` → `server.servlet.context-path=/{sub-paquete}`

> 💡 El `{sub-paquete}` es la parte central del nombre base antes del sufijo `-api`.
> Ejemplo: `uni-nomina-api` → sub-paquete = `nomina` → context-path = `/nomina`.

### 3. Configuración de CI/CD (`Jenkinsfile` y `JenkinsfileProd`)

Verifica y actualiza en `Jenkinsfile` y `JenkinsfileProd`:

- Aunque usan `$NAME_APP`, asegúrate de que no existan referencias harcodeadas a `proveedor-api` o `unisimon-api`.

### 4. Configuración de Logs (`logback-spring.xml`)

En `src/main/resources/logback-spring.xml`:

- `<springProperty ... defaultValue="proveedor-api"/>` (línea 6) → `defaultValue="{ArtifactId}"`

### 5. Controladores y Código Hardcoded

Actualiza `src/main/java/co/edu/unisimon/controller/StatusController.java`:

- **Obligatorio:** Refactorizar `StatusController` para usar `@Value("${spring.application.name}")` en lugar de un string hardcodeado:

```java
@Value("${spring.application.name}")
private String applicationName;

// En getStatus():
status.put("service", applicationName);
```

### 6. Refactor de Paquetes Java

1.  **Mover Físicamente los Archivos:**
    Mueve todo el contenido de `src/main/java/co/edu/unisimon/*` a `src/main/java/co/edu/unisimon/{sub-paquete}/`.
    _(Asegúrate de crear los directorios necesarios antes de mover)._

2.  **Eliminar Carpetas del Paquete Anterior:**
    Después de mover los archivos, **elimina** todas las carpetas que quedaron vacías en el paquete raíz `co.edu.unisimon` (excepto el nuevo sub-paquete):

    ```powershell
    $baseSrc = "src\main\java\co\edu\unisimon"
    $dirsToDelete = @("controller","dto","entity","exception","repository","response","security","service")
    foreach ($d in $dirsToDelete) {
        $fullPath = "$baseSrc\$d"
        if (Test-Path $fullPath) { Remove-Item -Recurse -Force $fullPath }
    }
    ```

3.  **Actualizar Código Fuente:**
    En todos los archivos `.java` dentro de `src/main/java` y `src/test/java`:
    - Reemplaza `package co.edu.unisimon;` por `package co.edu.unisimon.{sub-paquete};`.
    - Reemplaza `package co.edu.unisimon.` por `package co.edu.unisimon.{sub-paquete}.`.
    - Reemplaza `import co.edu.unisimon.` por `import co.edu.unisimon.{sub-paquete}.`.

4.  **Renombrar Clase de Aplicación y Tests:**
    - Renombra `UnisimonApiApplication.java` → `{ClassName}.java` y actualiza el nombre de la clase y las referencias en el método `main`.
    - Mueve `src/test/java/co/edu/unisimon/UnisimonApiApplicationTests.java` a `src/test/java/co/edu/unisimon/{sub-paquete}/{ClassName}Tests.java`.
    - Actualiza el `package` e `import` de la clase de tests al nuevo sub-paquete.

### 7. Limpieza Global (Grep/Replace)

Realiza una búsqueda global de las siguientes cadenas y reemplázalas por `{ArtifactId}` si tiene sentido en el contexto:

- `unisimon-api`

### 8. Reinicio de Repositorio Git

Ejecuta los siguientes comandos en la raíz del proyecto:

```powershell
# Eliminar el histórico actual
Remove-Item -Recurse -Force .git

# Iniciar nuevo repositorio
git init

# Vincular al nuevo remoto
git remote add origin {URL_NUEVO_REPO}

# Primer commit
git add .
git commit -m "Initial commit: Proyecto refactorizado desde proveedor-api"
```

### 9. Renombrar Directorio Raíz

Como último paso, renombra la carpeta del proyecto al **Nombre Base (Slug)**:
- `uni-base-api` → `uni-nomina-api` (por ejemplo).

---

## Verificación Post-Refactor

1.  Ejecuta `./mvnw clean compile` para asegurar que no hay errores de sintaxis o de paquetes.
2.  Verifica que el archivo `UnisimonApiApplication.java` (o el renombrado) tenga el paquete correcto.
3.  Verifica que `git remote -v` apunte al nuevo repositorio.

---

## Ejemplo de Uso (Prompt Sugerido)

"He clonado el proyecto base. Por favor, realiza la refactorización para este nuevo repo:
https://github.com/Unisimon/uni-nomina-api.git"

