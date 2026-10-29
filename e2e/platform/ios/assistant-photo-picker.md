# Asistente: foto del plan (iOS)

El botón de foto del compositor (`AssistantUiTags.PhotoButton`) abre la cámara o el selector de fotos del sistema
(`AssistantChat.Effect.OpenCamera` y `OpenPhotoPicker`), y la foto que el estudiante elige vuelve a la app como
`AssistantChat.Action.AttachPhoto`. Por eso `RequestPhoto` y `AttachPhoto` van en `ActionDispositions` como
`PlatformEdge`: el selector de fotos del sistema no es un elemento que el driver de XCUITest maneje de forma estable en
el simulador, y la lectura de las evaluaciones de la foto depende de un servicio que los mocks no simulan.

Cómo se prueba a mano, en una build de depuración con el asistente encendido:

1. Abrir el asistente desde la barra superior (el destello) y aceptar el consentimiento si lo pide.
2. Tocar el botón de foto del compositor y elegir una foto de un plan de evaluaciones (fototeca o cámara).
3. Ver el progreso «leyendo la foto» y, al terminar, la tarjeta de propuesta de alta con sus filas, sin confirmar nada.
4. Desmarcar una fila, confirmar y revisar el recibo y la lista de evaluaciones.

El resultado de cada paso se anota en la certificación como verificación manual, no como evidencia E2E.
