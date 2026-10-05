# Manual de Integración del Módulo de Chat WebSockets en PHP / PHTML

Este documento proporciona una guía paso a paso para integrar la funcionalidad de **Chat en Tiempo Real** (vía WebSockets STOMP y REST API) dentro de aplicaciones web desarrolladas en **PHP** (`.php` / `.phtml` / Laravel / Symfony / CodeIgniter).

---

## 🏗️ 1. Arquitectura y Endpoints

El servicio de chat funciona mediante dos vías:

1. **WebSockets (STOMP sobre SockJS)**: Para transmisión bidireccional en tiempo real (Mensajes instantáneos, estado En Línea, Escribiendo..., Visto).
2. **API REST (HTTP)**: Para guardar/enviar mensajes desde el servidor y consultar el historial paginado de mensajes.

### Parámetros de Conexión:

* **Endpoint WebSocket**: `http://localhost:8092/corenotificacion/ws-chat` (o el host/puerto de tu servidor).
* **Tópico de Suscripción**: `/topic/chat.directo.{conversacionUuid}`
* **Ruta Base API REST**: `http://localhost:8092/corenotificacion/api/chats`

---

## 📦 2. Librerías Requeridas en la Vista PHP (`.phtml` / `.php`)

Para manejar la conexión de WebSockets en el navegador, debes incluir en la cabecera o pie de tu plantilla PHP las librerías `SockJS` y `Stomp.js`:

```html
<!-- SockJS Client para fallback de transporte -->
<script src="https://cdn.jsdelivr.net/npm/sockjs-client@1/dist/sockjs.min.js"></script>

<!-- Stomp.js para el protocolo de mensajería -->
<script src="https://cdnjs.cloudflare.com/ajax/libs/stomp.js/2.3.3/stomp.min.js"></script>
```

---

## 💻 3. Ejemplo Completo de Plantilla PHP (`chat.phtml`)

A continuación se muestra una plantilla completa `.phtml` lista para usar:

```php
<?php
// Variables dinámicas desde PHP (ej. Controlador PHP o sesión de usuario)
$conversacionUuid = "11111111-1111-1111-1111-111111111111";
$usuarioUuid = "00000000-0000-0000-0000-000000000001"; 
$receptorUuid = "00000000-0000-0000-0000-000000000002";
$tipoEmisor = "SOLICITANTE"; // Puede ser "SOLICITANTE", "AGENTE" o "SISTEMA"
$serverWsUrl = "http://localhost:8092/corenotificacion/ws-chat";
?>

<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Chat Institucional - Unisimon</title>
    <!-- Estilos CSS opcionales -->
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    
    <!-- Librerías JS para WebSockets -->
    <script src="https://cdn.jsdelivr.net/npm/sockjs-client@1/dist/sockjs.min.js"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/stomp.js/2.3.3/stomp.min.js"></script>
</head>
<body>

<div class="chat-wrapper">
    <!-- Encabezado con estado En Línea -->
    <div class="chat-header">
        <h4 id="chatTitle">Conversación de Soporte</h4>
        <span id="userPresenceStatus" style="color: gray;">● Desconectado</span>
    </div>

    <!-- Lista de mensajes -->
    <div id="chatMessages" style="height: 400px; overflow-y: scroll; border: 1px solid #ccc; padding: 10px;">
        <!-- Los mensajes se renderizan dinámicamente aquí -->
    </div>

    <!-- Indicador "Escribiendo..." -->
    <div id="typingIndicator" style="height: 20px; font-style: italic; color: #888;"></div>

    <!-- Formulario de envío -->
    <div class="chat-input-container">
        <input type="text" id="txtMensaje" placeholder="Escribe un mensaje..." oninput="notificarEscribiendo()" onkeypress="if(event.key==='Enter') enviarMensaje()">
        <button type="button" onclick="enviarMensaje()">Enviar</button>
    </div>
</div>

<script>
    // 1. Inyección de variables de PHP a JavaScript
    const CONFIG = {
        conversacionUuid: "<?php echo $conversacionUuid; ?>",
        usuarioUuid: "<?php echo $usuarioUuid; ?>",
        receptorUuid: "<?php echo $receptorUuid; ?>",
        tipoEmisor: "<?php echo $tipoEmisor; ?>",
        wsUrl: "<?php echo $serverWsUrl; ?>"
    };

    let stompClient = null;
    let typingTimer = null;

    // 2. Conectar al WebSocket
    function iniciarWebSocket() {
        const socket = new SockJS(CONFIG.wsUrl);
        stompClient = Stomp.over(socket);

        // Desactivar logs detallados de STOMP si deseas (opcional)
        stompClient.debug = null;

        stompClient.connect({}, function (frame) {
            console.log('Conectado exitosamente a WebSockets');
            
            // A. Suscribirse al canal de la conversación
            const canalTopic = '/topic/chat.directo.' + CONFIG.conversacionUuid;
            stompClient.subscribe(canalTopic, function (respuesta) {
                const data = JSON.parse(respuesta.body);
                procesarEventoWebSocket(data);
            });

            // B. Emitir presencia EN_LINEA
            notificarPresencia(true);

            // C. Cargar historial de mensajes previos por REST API
            cargarHistorialREST();

        }, function (error) {
            console.error('Error de conexión WebSocket:', error);
            setTimeout(iniciarWebSocket, 5000); // Reintento automático
        });
    }

    // 3. Enviar Mensaje por WebSocket STOMP
    function enviarMensaje() {
        const input = document.getElementById('txtMensaje');
        const contenido = input.value.trim();
        if (!contenido) return;

        if (stompClient && stompClient.connected) {
            const payload = {
                receptorUuid: CONFIG.receptorUuid,
                contenido: contenido,
                tipoEmisor: CONFIG.tipoEmisor
            };

            stompClient.send('/app/chat.enviar/' + CONFIG.conversacionUuid, {}, JSON.stringify(payload));
            input.value = '';
        }
    }

    // 4. Notificar evento "Escribiendo..."
    function notificarEscribiendo() {
        if (stompClient && stompClient.connected) {
            stompClient.send('/app/chat.escribiendo/' + CONFIG.conversacionUuid, {}, JSON.stringify({
                tipoEmisor: CONFIG.tipoEmisor,
                emisorUuid: CONFIG.usuarioUuid
            }));
        }
    }

    // 5. Notificar evento "Visto" (Lectura)
    function notificarLecturaVisto() {
        if (stompClient && stompClient.connected) {
            stompClient.send('/app/chat.visto/' + CONFIG.conversacionUuid, {}, JSON.stringify({
                tipoEmisor: CONFIG.tipoEmisor,
                emisorUuid: CONFIG.usuarioUuid
            }));
        }
    }

    // 6. Notificar estado En Línea
    function notificarPresencia(enLinea) {
        if (stompClient && stompClient.connected) {
            stompClient.send('/app/chat.enlinea/' + CONFIG.conversacionUuid, {}, JSON.stringify({
                tipoEmisor: CONFIG.tipoEmisor,
                emisorUuid: CONFIG.usuarioUuid,
                enLinea: enLinea
            }));
        }
    }

    // 7. Procesar los eventos entrantes del WebSocket
    function procesarEventoWebSocket(data) {
        // Evento 1: Estado En Línea
        if (data.evento === 'EN_LINEA') {
            if (data.tipoEmisor !== CONFIG.tipoEmisor) {
                const el = document.getElementById('userPresenceStatus');
                el.innerText = data.enLinea ? '● En línea' : '● Desconectado';
                el.style.color = data.enLinea ? '#10b981' : 'gray';
            }
            return;
        }

        // Evento 2: Escribiendo...
        if (data.evento === 'ESCRIBIENDO') {
            if (data.tipoEmisor !== CONFIG.tipoEmisor) {
                const el = document.getElementById('typingIndicator');
                el.innerText = 'La otra persona está escribiendo...';
                clearTimeout(typingTimer);
                typingTimer = setTimeout(() => { el.innerText = ''; }, 2500);
            }
            return;
        }

        // Evento 3: Mensaje Visto (Doble check)
        if (data.evento === 'VISTO') {
            if (data.tipoEmisor !== CONFIG.tipoEmisor) {
                document.querySelectorAll('.check-mark').forEach(i => {
                    i.innerText = '✓✓';
                    i.style.color = '#38bdf8';
                });
            }
            return;
        }

        // Evento 4: Nuevo Mensaje Recibido
        if (data.contenido) {
            renderizarMensaje(data);
            if (data.tipoEmisor !== CONFIG.tipoEmisor && document.hasFocus()) {
                notificarLecturaVisto();
            }
        }
    }

    // 8. Renderizar mensaje en la interfaz
    function renderizarMensaje(msg) {
        const contenedor = document.getElementById('chatMessages');
        const esMio = (msg.tipoEmisor === CONFIG.tipoEmisor || msg.emisorUuid === CONFIG.usuarioUuid);
        
        const div = document.createElement('div');
        div.style.textAlign = esMio ? 'right' : 'left';
        div.style.margin = '8px 0';
        
        const checkMark = esMio ? '<span class="check-mark">✓</span>' : '';
        div.innerHTML = `
            <div style="display:inline-block; padding: 8px 12px; border-radius: 8px; background:${esMio ? '#2563eb' : '#334155'}; color:white;">
                ${msg.contenido}
            </div>
            <div style="font-size: 10px; color: gray;">${checkMark}</div>
        `;

        contenedor.appendChild(div);
        contenedor.scrollTop = contenedor.scrollHeight;
    }

    // 9. Cargar Historial inicial mediante REST API
    function cargarHistorialREST() {
        const url = `http://localhost:8092/corenotificacion/api/chats/${CONFIG.conversacionUuid}/mensajes?page=0&size=50`;
        fetch(url)
            .then(res => res.json())
            .then(res => {
                if (res.datos && res.datos.content) {
                    document.getElementById('chatMessages').innerHTML = '';
                    res.datos.content.forEach(msg => renderizarMensaje(msg));
                }
            })
            .catch(err => console.error("Error al cargar historial:", err));
    }

    // Eventos del Ciclo de Vida de la Página
    window.onload = iniciarWebSocket;
    window.onbeforeunload = () => notificarPresencia(false);
</script>

</body>
</html>
```

---

## 🐘 4. Envío de Mensajes Backend a Backend desde PHP (Opcional)

Si en tu aplicación PHP prefieres guardar o enviar el mensaje haciendo un llamado HTTP desde el backend de PHP (usando cURL o Guzzle), puedes consumir el endpoint REST:

### Ejemplo cURL en PHP:

```php
<?php
function enviarMensajeChat($conversacionUuid, $emisorUuid, $receptorUuid, $contenido, $tipoEmisor = "SOLICITANTE") {
    $url = "http://localhost:8092/corenotificacion/api/chats/" . $conversacionUuid . "/mensajes";

    $payload = array(
        "receptorUuid" => $receptorUuid,
        "contenido"    => $contenido,
        "tipoEmisor"   => $tipoEmisor
    );

    $ch = curl_init($url);
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    curl_setopt($ch, CURLOPT_POST, true);
    curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($payload));
    curl_setopt($ch, CURLOPT_HTTPHEADER, array(
        'Content-Type: application/json',
        'X-User-Uuid: ' . $emisorUuid
    ));

    $response = curl_exec($ch);
    $httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
    curl_close($ch);

    return array(
        "code" => $httpCode,
        "data" => json_decode($response, true)
    );
}

// Ejemplo de uso:
$resultado = enviarMensajeChat(
    "11111111-1111-1111-1111-111111111111",
    "00000000-0000-0000-0000-000000000001",
    "00000000-0000-0000-0000-000000000002",
    "Mensaje enviado desde controlador PHP"
);
?>
```

> **Nota**: Cuando envías un mensaje por el endpoint REST de PHP, la API backend de Java automáticamente notifica en tiempo real por WebSocket a todos los usuarios suscritos en el navegador.

---

## ⚡ 5. Resumen de Eventos WebSocket

| Evento | Destino STOMP (SEND) | Estructura Payload JSON |
| :--- | :--- | :--- |
| **Enviar Mensaje** | `/app/chat.enviar/{chatUuid}` | `{"receptorUuid": "...", "contenido": "...", "tipoEmisor": "SOLICITANTE"}` |
| **Escribiendo...** | `/app/chat.escribiendo/{chatUuid}` | `{"tipoEmisor": "SOLICITANTE", "emisorUuid": "..."}` |
| **En Línea** | `/app/chat.enlinea/{chatUuid}` | `{"tipoEmisor": "SOLICITANTE", "emisorUuid": "...", "enLinea": true}` |
| **Marcar Visto** | `/app/chat.visto/{chatUuid}` | `{"tipoEmisor": "SOLICITANTE", "emisorUuid": "..."}` |
