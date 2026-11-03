# Asistente: adjuntos (iOS)

El clip del compositor (`AssistantUiTags.AttachButton`) abre un menú con la cámara (si la hay) y las fotos del
dispositivo. Lo que el estudiante elige vuelve a la app como `AssistantChat.Action.AddAttachments`; quitar y reintentar
actúan sobre esas fotos. Por eso `RequestAttachment`, `AddAttachments`, `RemoveAttachment` y `RetryAttachment` van en
`ActionDispositions` como `PlatformEdge`: ningún escenario elige una foto en el selector del sistema de forma estable,
y la subida depende de un servicio que los mocks simulan solo en parte.

Cómo se prueba a mano, en una build de depuración con el asistente encendido y el mock:

1. Abrir el asistente desde la barra superior (el destello) y aceptar el consentimiento si lo pide.
2. Tocar el clip y elegir fotos (y tomar una si el dispositivo tiene cámara).
3. Ver la bandeja sobre el texto: las miniaturas de las fotos, y el velo mientras sube.
4. Quitar una foto con su × y comprobar que sale de la bandeja.
5. Sin red, subir una foto: queda con el velo de error y su reintento; con red, el reintento la sube.
6. Enviar con texto y sin texto. La burbuja muestra las fotos arriba del texto; tocar una la abre en grande.
7. Elegir cinco fotos: se quedan cuatro y el aviso dice «Puedes adjuntar hasta 4.». Con cuatro en la bandeja el clip
   queda apagado y el aviso sigue; quitar una devuelve el clip y el aviso desaparece.

El resultado de cada paso se anota en la certificación como verificación manual, no como evidencia E2E.
