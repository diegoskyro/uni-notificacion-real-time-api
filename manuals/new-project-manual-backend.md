# Manual de Referencia: Skill `rename-project`

Este manual describe el funcionamiento y el uso del skill **`rename-project`**, diseñado para automatizar por completo la refactorización y reinicio de repositorios al clonar el proyecto base de la institución.

---

## 🏷️ Nombre del Skill
*   **Identificador:** `rename-project`
*   **Propósito:** Renombrar el proyecto base (`base-api` / `unisimon-api`) para adaptarlo a un nuevo microservicio y reiniciar el repositorio Git apuntando al nuevo destino.

---

## ⚙️ ¿Qué hace este Skill de forma automática?

Cuando solicitas la ejecución de este skill, el asistente de IA realiza las siguientes modificaciones en el espacio de trabajo de manera 100% automatizada:

### 1. Refactorización de Configuración de Maven
*   Actualiza el `<artifactId>`, el `<name>` y la `<description>` en el archivo [pom.xml](file:///c:/Users/juan.galvisr/Documents/java-projects/backend/uni-proveedor-api/pom.xml) con el nombre derivado para el nuevo microservicio.

### 2. Propiedades de la Aplicación y Telemetría
*   Modifica todos los archivos de configuración `src/main/resources/application*.properties` (incluyendo perfiles de desarrollo y producción) para actualizar las propiedades `spring.application.name` y `OTEL_SERVICE_NAME`.

### 3. Configuración de Logs (`logback-spring.xml`)
*   Actualiza la propiedad `appName` por defecto en el archivo de configuración de logs para asegurar que el registro de trazas y eventos coincida con el nombre del nuevo servicio.

### 4. Endpoints del Sistema
*   Modifica el endpoint de estado en `StatusController.java` para que reporte correctamente el nuevo nombre de la aplicación.

### 5. Reorganización Física de Paquetes Java
*   Mueve de forma segura y física todo el árbol de carpetas y código fuente desde el paquete genérico `co.edu.unisimon.*` hacia el nuevo sub-paquete institucional correspondiente al contexto del negocio (e.g., `co.edu.unisimon.nomina`).
*   Actualiza todas las sentencias `package` e `import` en todos los archivos `.java` (código principal y de pruebas) para evitar inconsistencias de compilación.

### 6. Limpieza e Inicialización de Repositorio Git
> [!IMPORTANT]
> El skill realiza la limpieza del historial previo del repositorio base y lo inicializa desde cero para el nuevo proyecto:
> *   Elimina de raíz la carpeta oculta `.git`.
> *   Inicializa un repositorio local limpio (`git init`).
> *   Vincula el nuevo repositorio remoto (`git remote add origin ...`).
> *   Realiza el primer commit con el mensaje `"Initial commit: Proyecto refactorizado desde base-api"`.

---

## 📂 Lógica de Derivación de Nombres

El asistente calcula y deriva automáticamente todas las variables del proyecto a partir de la URL del repositorio Git proporcionada:

| Si la URL de tu repositorio es: | Nombre del Microservicio (ArtifactId) | Sub-paquete (Contexto Java) | Nuevo Package Base |
| :--- | :--- | :--- | :--- |
| `.../uni-nomina-api.git` | `nomina-api` | `nomina` | `co.edu.unisimon.nomina` |
| `.../uni-proveedor-api.git` | `proveedor-api` | `proveedor` | `co.edu.unisimon.proveedor` |
| `.../uni-factura-api.git` | `factura-api` | `factura` | `co.edu.unisimon.factura` |

---

## 🚀 Cómo usar este Skill

Para ejecutar la refactorización automatizada, simplemente indícale al asistente la URL del nuevo repositorio Git utilizando el siguiente formato en tu chat:

### Prompt de Ejemplo:
```text
Renombrar para https://github.com/Unisimon/uni-nomina-api.git
```
*(Reemplaza la URL del ejemplo por la URL real de tu repositorio de destino).*
