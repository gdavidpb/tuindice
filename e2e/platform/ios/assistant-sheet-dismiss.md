# Asistente: cerrar la hoja con el gesto del sistema (iOS)

La hoja del chat se cierra deslizándola hacia abajo, o tocando fuera de ella. Ese cierre llega a la app como
`AssistantChat.Action.Dismiss` (`AssistantChatSheet`, `onDismissRequest`), y por eso `Dismiss` va en
`ActionDispositions` como `PlatformEdge`: el gesto lo entrega UIKit al contenedor de la hoja, no un control de la
pantalla que el driver de XCUITest pueda tocar de forma estable.

Ningún escenario lo dispara. El harness no traduce el gesto de cierre en iOS (`back()` es solo de Android), y la hoja
no ofrece un control de cierre que el escenario pueda tocar.

Cubierto en otros sitios: `AssistantChatSheetUiTest` (la hoja pide el cierre con `onDismissRequest`) y
`AssistantChatStateMachineContractTest` (`Dismiss` cierra el chat y emite `Effect.Dismiss`).

Cómo se prueba a mano, en una build de depuración con el asistente abierto:

1. Abrir el asistente desde la barra superior (el destello).
2. Deslizar la hoja hacia abajo hasta cerrarla: vuelve a verse la pantalla de debajo.
3. Volver a abrir el asistente: el chat anterior sigue en la conversación.

El resultado de cada paso se anota en la certificación como verificación manual, no como evidencia E2E.
