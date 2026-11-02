# Asistente: teclado del compositor (Android)

El compositor solicita autocorrección y mayúscula al comenzar cada frase. El escenario `assistant-smoke` comprueba
que, al enfocar el campo, los botones de foto y envío siguen en pantalla, y que se puede escribir y enviar. Ambos botones
existen también con el compositor cerrado, así que ese paso no prueba su expansión ni su posición sobre el teclado; la
fila de botones bajo el texto al abrirse y al crecer la comprueba `AssistantChatComposerViewUiTest`. El escenario no
verifica la corrección del teclado: el driver introduce caracteres exactos mediante eventos de teclas o una acción de
accesibilidad.

Comprobación manual en un dispositivo o emulador con esas opciones activadas en los ajustes del teclado:

1. Abrir el asistente y tocar «Escribe tu pregunta».
2. Verificar que el teclado comienza en mayúscula, que ofrece correcciones en el idioma seleccionado y que queda
   espacio entre el texto y los botones de foto y envío.
3. Escribir varias líneas y comprobar que los botones siguen accesibles antes de enviar la pregunta.

La disponibilidad de las correcciones depende del idioma, del teclado y de los ajustes del sistema.
