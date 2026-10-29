# Módulo `assistant`

El asistente académico del estudiante: la hoja del chat, la memoria que el asistente guarda, el consentimiento y la
disponibilidad. La capa de datos habla con el servicio por SSE y HTTP; la capa de dominio decide con casos de uso; la
capa de presentación convierte lo que llega en estados que la interfaz solo pinta.

## Capas

- `data/` — API, fuentes y repositorios del servicio. Ver `domain/` para los contratos.
- `domain/` — modelos, repositorios (puertos) y casos de uso.
- `presentation/` — la máquina de cada pantalla, sus transiciones y su API hacia la interfaz.
  - `contract/` — acciones, estados y efectos de cada pantalla (`AssistantChat`, `AssistantMemory`,
    `AssistantComponentSheet`).
  - `machine/` — la máquina de la pantalla y su estado extendido (`AssistantChatSession`, `AssistantChatDraft`).
  - `machine/collaborator/` — los colaboradores de la máquina del chat (ver abajo).
  - `transition/` — las filas de la tabla de transiciones, un archivo por estado de origen.
  - `model/` — los items que la interfaz pinta. Un `*Item` por componente del chat y por pieza de la memoria.
  - `mapper/` — la conversión de lo que llega (modelos de dominio y tipos de `chatcontract`) a items, y los textos
    derivados. Es código puro: corre en el host de pruebas sin recursos de interfaz.
  - `resource/` — los puertos de texto de la presentación (`AssistantChatTexts`, `AssistantMemoryTexts`,
    `AssistantChatFeedbackTexts`) y sus implementaciones sobre los recursos de compose.
  - `route/`, `viewmodel/`, `navigation/` — el puente con la interfaz y con la navegación.
- `ui/` — composables. Ver «`ui/component`» abajo.

## Patrón de la máquina del chat

`AssistantChatMachine` es una máquina con tabla de transiciones: cada fila recibe una acción o un evento interno y dice
el estado de llegada. Lo que la máquina no hace por sí misma lo hacen sus **colaboradores**, clases en
`presentation/machine/collaborator/` que poseen un caso de uso o un puerto:

- `AssistantChatAccess`, `AssistantChatTurns`, `AssistantChatFeedback`, `AssistantChatRestrictions`,
  `AssistantChatNotificationOffer` y `AssistantChatIntents`/`AssistantChatPlanIntents` (agrupados por
  `AssistantChatProposals`).

Reglas del patrón:

1. Un colaborador ejecuta un comando (`host.launchMachineJob { ... }`) y reporta su resultado como un evento interno
   (`AssistantChatInternalEvent`), con un símbolo por resultado, para que cada fila tenga un destino fijo.
2. Un colaborador no cambia el estado: decide lo que el resultado significa la máquina, en sus filas.
3. El estado extendido que las filas leen y escriben es `AssistantChatSession`; lo escriben las filas y los comandos que
   abren la hoja antes de que la tabla tome su resultado.
4. Cada máquina tiene un test de contrato (`*StateMachineContractTest`) de cobertura del alfabeto, de alcance de los
   estados y de Λ.

La máquina de la memoria (`AssistantMemoryMachine`) sigue el mismo esquema, con sus casos de uso dentro de la máquina.

## `ui/component`

Los componentes del chat, agrupados por el catálogo al que pertenecen (`sistema`, `nav`, `perfil`, `informe`,
`horario`, `resumen`, `evaluaciones`, `materias`, `pensum`, `plan`, `conocimiento`). Cada componente dibuja un item y
vive en su propio archivo, con el nombre de su composable.

Se queda en `ui/component` y no pasa a `ui/view` porque es el mapa del catálogo de componentes del chat: quien busca el
dibujo de un componente del servicio lo encuentra por su catálogo. `ui/view` reúne las piezas del chat que no son un
componente del servicio (la burbuja, el compositor, la cabecera de la hoja).

`ui/component/common` tiene las piezas comunes de las tarjetas (filas, cifras, enlaces, etiquetas) que todos los
catálogos usan.

## Reglas de estilo del módulo

- Una declaración pública por archivo; los composables privados que dibujan van a su propio archivo.
- Los textos que la interfaz muestra van por `UiText` desde los items: la vista no decide textos ni estados.
- Los comentarios del código dicen lo que hace el código; cuando una regla depende de quién escribe un estado o de
  cuándo, lo dicen con esa precisión.
