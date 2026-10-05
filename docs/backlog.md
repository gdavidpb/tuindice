# Backlog

Pendientes que se dejaron atrás a propósito, con la evidencia que los originó. Cada entrada dice qué
falta, por qué no se hizo todavía y qué la desbloquea. Al resolver una, se borra de aquí y se
referencia el PR.

Este backlog es la contraparte del front del backlog del backend (`tuindice-api/docs/backlog.md`).
Los identificadores del backend que se citan aquí son **C2** (umbral de `not_enrolled`), **C4**
(`CMP_003`/`CMP_005` como estados propios) y **C8** (equivalencia de los bloques horarios con la hora
del reloj).

Guías que aplican a todo lo de aquí: `docs/design-system.md` (tokens, semántica de color, estados de
pantalla y terminología), `docs/testing/common-ui-testing.md` (tests de UI comunes) y
`docs/release-pipeline.md` (versión única y release).

## Contexto

La rama `fix/university-states-backlog` (versión 6.7.0, Android 67 / iOS 55) resolvió el grueso del
backlog anterior: la sync decodifica `not_enrolled` y cualquier valor desconocido de los enums de la
sync, la inscripción anulada se explica en Summary, Record, Evaluaciones y el comprobante en sus dos
momentos, el nuevo ingreso y el acceso denegado al expediente tienen estados propios, el horario de
clases (sección, aula, errores de inscripción, retiradas y la grilla por bloques) se ve en Record, el
inicio de sesión respeta `Retry-After`, la cola de notas rebasa con `current_revision` y descarta lo
que una sync deja huérfano, y los mocks y flujos E2E cubren todo eso.

## Pendientes

| # | Pendiente | Por qué quedó atrás | Qué lo desbloquea |
|---|---|---|---|
| **F1** | **Avisar al backend la versión que decodifica `not_enrolled`.** La app ya la decodifica (`SyncSourceStatusResponse.NotEnrolled`, con un `Unknown` para valores futuros), pero mientras no esté publicada el backend sigue degradando `not_enrolled` a `success` para todas las versiones (`RecordClientCompatibilityPolicy`, backend C2). | La versión 6.7.0 (Android 67 / iOS 55) aún no se publica; la entrada C2 del backend ya anota esos números. | Publicar 6.7.0 y avisar al backend para que fije el umbral real. |
| **F4** | Diferenciar en la UI `CMP_003` (encuesta docente pendiente) y `CMP_005` (solo pregrado). | El backend no los distingue de una caída (backend C4). | Backend C4; después, copys propios (la encuesta con acción hacia el enlace de DST en el navegador externo, `browser-external-dialog` en E2E de maincore). |
| **F6** | Del horario de clases queda lo que el backend no sirve: el **calendario académico** (inicio de clases, último día de retiros) y las **horas del reloj** de los bloques. La grilla de Record muestra bloques, no horas. | Falta la equivalencia entre bloque y hora (backend C8) y una ampliación nueva del backend para el calendario. | Backend C8 para mostrar horas; el calendario necesita una ampliación del backend. |
| **F7** | Mostrar `09 Sin Prelación`. **Recomendación: descartarlo.** | Es un estado **transitorio de una inscripción normal**: de 117 respuestas con `09`, 9 carnets pasaron a `00` en días, ninguna trae errores por materia y traen las mismas materias que una `00`. El backend lo trata como inscrito y no lo envía en `situation`. | Nada; se deja anotado para no reabrirlo. Si producto igual lo quiere, el backend tendría que exponerlo (hoy `situation` solo viaja en anuladas). |
| **F13** | **Los enums del record siguen siendo frágiles.** `term_kind`, `academic_outcome`, `academic_badge`, `grading_mode` y `academic_score.type` se decodifican como enums cerrados: un valor nuevo del backend en cualquiera de ellos tumba el parseo completo de la sync y del record, como pasaba con `not_enrolled` antes de esta rama. | Esta rama blindó solo el reporte de la sync (`SyncSourceStatusResponse` y `SyncReportStatusResponse` con un caso `Unknown`); el record necesita decidir qué significa un valor desconocido para el cálculo (¿se omite el intento?, ¿se trata como pendiente?). | Un serializador tolerante por enum con un caso `Unknown` y la decisión de producto/dominio de cómo se calcula con él; `createSharedJson()` no se toca (`coerceInputValues` afectaría a todo). |
| **F14** | **Podar la cola de evaluaciones de un trimestre eliminado.** Esta rama descarta, al aplicar una sync, las ediciones de notas pendientes cuyo intento ya no está en el record; la cola de evaluaciones (`evaluations`) no tiene el equivalente: sus mutaciones de un trimestre que la sync quitó siguen intentando enviarse y terminan en error o en el reintento por backoff. Tampoco tiene el tope de por vida de reintentos agotados que ahora tiene la cola de notas (`maxExhaustedExecutions`). | Quedó fuera del alcance de F11/F12, que eran del overlay del record. | Aplicar el mismo patrón (`reconcileWithConfirmedRecord` en una evaluación equivalente y `maxExhaustedExecutions` en `EvaluationMutationSyncSpec`) cuando se toque esa cola. |
