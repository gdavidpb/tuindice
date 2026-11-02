# Asistente: adjuntos (iOS)

El clip del compositor (`AssistantUiTags.AttachButton`) abre el selector del sistema: la cámara (si la hay), las fotos
o los archivos PDF. Lo que el estudiante elige vuelve a la app como `AssistantChat.Action.AddAttachments`; quitar y
reintentar actúan sobre esos adjuntos. Por eso `RequestAttachment`, `AddAttachments`, `RemoveAttachment` y
`RetryAttachment` van en `ActionDispositions` como `PlatformEdge`: ningún escenario elige un archivo en el selector
del sistema de forma estable, y la lectura de un PDF y la subida dependen de un servicio que los mocks simulan solo
en parte.

Cómo se prueba a mano, en una build de depuración con el asistente encendido y el mock:

1. Abrir el asistente desde la barra superior (el destello) y aceptar el consentimiento si lo pide.
2. Tocar el clip y elegir fotos (y tomar una si el dispositivo tiene cámara); después, elegir un PDF.
3. Ver la bandeja sobre el texto: las miniaturas de las fotos, la tarjeta del PDF con sus páginas cuando el servicio
   las lee, y el velo mientras sube.
4. Quitar un adjunto con su × y comprobar que sale de la bandeja.
5. Sin red, subir un adjunto: queda con el velo de error y su reintento; con red, el reintento lo sube.
6. Enviar con texto y sin texto. La burbuja muestra las fotos arriba del texto y el PDF como tarjeta; tocar una foto la
   abre en grande.
7. Elegir cinco fotos: se quedan cuatro y el aviso dice «Puedes adjuntar hasta 4.».

El resultado de cada paso se anota en la certificación como verificación manual, no como evidencia E2E.
