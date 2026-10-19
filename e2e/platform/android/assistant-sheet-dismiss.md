# Asistente: cerrar la hoja con el gesto del sistema (Android)

La hoja del chat se cierra con el gesto de atrás del sistema, o tocando fuera de ella. Ese cierre llega a la app como
`AssistantChat.Action.Dismiss` (`AssistantChatSheet`, `onDismissRequest`), y por eso `Dismiss` va en
`ActionDispositions` como `PlatformEdge`: el gesto lo entrega el sistema operativo, no un control de la pantalla que el
escenario pueda tocar.

Ningún escenario lo dispara: el gesto de atrás es del sistema y no hay un elemento de la hoja que el escenario pueda
tocar para cerrarla. El harness de Android tiene `back()`, pero no se usa aquí porque no reproduce la hoja tal como la
deja el sistema.

Cubierto en otros sitios: `AssistantChatSheetUiTest` (la hoja pide el cierre con `onDismissRequest`) y
`AssistantChatStateMachineContractTest` (`Dismiss` cierra el chat y emite `Effect.Dismiss`).

Cómo se prueba a mano, en una build de depuración con el asistente abierto:

1. Abrir el asistente desde la barra superior (el destello).
2. Pulsar atrás del sistema: la hoja se cierra y la pantalla de debajo queda en primer plano.
3. Volver a abrir el asistente: la conversación sigue en su sitio.

El resultado de cada paso se anota en la certificación como verificación manual, no como evidencia E2E.
