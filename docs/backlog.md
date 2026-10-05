# Backlog

Pendientes que se dejaron atrás a propósito, con la evidencia que los originó. Cada entrada dice qué
falta, por qué no se hizo todavía y qué la desbloquea, y cierra con la recomendación visual. Al
resolver una, se borra de aquí y se referencia el PR.

Este backlog es la contraparte del front del backlog del backend (`tuindice-api/docs/backlog.md`).
Los identificadores del backend que se citan aquí son **C2** (umbral de `not_enrolled`), **C4**
(`CMP_003`/`CMP_005` como estados propios) y **C8** (equivalencia de los bloques horarios con la
hora del reloj). **B1** (situación de inscripción hacia la app) y **B2** (horario, aula y sección del
trimestre en curso) ya están resueltos en el backend y se describen en F2 y F6. **B3** (semántica de
`09 Sin Prelación`) quedó aclarado con los datos del 30-sep al 5-oct-2026 (ver F7).

Guías que aplican a todo lo de aquí: `docs/design-system.md` (tokens, semántica de color, estados de
pantalla y terminología), `docs/testing/common-ui-testing.md` (tests de UI comunes) y
`docs/release-pipeline.md` (versión única y release).

## Contexto

- Desde el 30-sep-2026 el backend lee la inscripción de la API JSON de DST. Hasta el 5-oct toda
  inscripción **anulada** se trataba como "no inscrito": el trimestre en curso se eliminaba, con sus
  notas y evaluaciones, y la sync emitía `not_enrolled`.
- Los datos de esa semana (433 comprobantes de 259 carnets) mostraron que la anulada es **recuperable
  durante la semana de correcciones**: 14 carnets cambiaron de código (`01 → 09 → 00`, `00 → 01`,
  `15 → 09 → 15`, `09 → 00`) y la regla borró el trimestre 9 veces, con hasta 4 notas perdidas cada
  una. Códigos de anulada vistos: `01` límite de créditos, `06` índice académico, `10` norma de
  retiros, `12` estudiante en período de prueba y `15` reglamento de permanencia.
- Por eso el backend cambia la regla (rama `fix/dst-annulled-provisional-window`, **pendiente de
  desplegar** al 5-oct): la anulada es **provisional** hasta 14 días después del inicio de clases
  (este trimestre, hasta el 12-oct). En ese lapso el trimestre en curso **se conserva**, con las
  materias que DST lista y las notas del usuario, y la sync responde `success` con
  `sources.enrollment.situation`. Pasada la ventana, la anulada es definitiva: el trimestre se
  elimina y la sync emite `not_enrolled`, como antes.
- **Ninguna versión publicada de la app decodifica `not_enrolled`.** Por eso el backend lo degrada a
  `success` para todas las versiones (`RecordClientCompatibilityPolicy`, backend C2) y hoy la app ve
  "Sin trimestre en curso" sin saber por qué.
- Un estudiante que sincroniza por primera vez y a quien DST no entrega el expediente (probable
  nuevo ingreso) recibe **424** con `reason = "NEW_STUDENT_NO_RECORD"`. La app actual solo lee el
  status y lo trata igual que "servicio no disponible".

## Sync y estados de la universidad

| # | Pendiente | Por qué quedó atrás | Qué lo desbloquea |
|---|---|---|---|
| **F1** (crítico) | Decodificar `not_enrolled` en `SyncSourceStatusResponse` (`maincore/.../data/source/sync/api/response/`) y en `SyncSourceStatus` (`base/.../domain/model/`), y hacer tolerante el parseo de los enums de la sync. Hoy el enum solo tiene `success`, `unavailable` y `not_attempted`, y `createSharedJson()` (`maincore/.../di/KtorClient.kt`) usa `ignoreUnknownKeys` pero no `coerceInputValues`: un valor desconocido **tumba el parseo completo** de la sync (`SyncApiDataSource.kt`, `SyncDataSource.kt`). Opciones: `coerceInputValues = true` con un valor por defecto, o un caso `Unknown` explícito. | Se descubrió al preparar el backend: el umbral de capacidad que había (Android 58 / iOS 46) no correspondía a ninguna versión que decodifique el valor. | Implementar y publicar. Al publicar, **avisar al backend la versión de Android/iOS** para que fije el umbral real (backend C2); mientras tanto el backend no envía `not_enrolled` a nadie. |
| **F2** | Experiencia de **inscripción anulada**, que ahora tiene dos momentos. **Provisional** (hasta 14 días después del inicio de clases): el trimestre en curso sigue visible y editable, y la app debe avisar que la universidad lo tiene anulado y que aún se puede corregir. **Definitiva** (después): no hay trimestre en curso; hoy la app muestra "Sin trimestre en curso" (`title_no_subjects_evaluations` en `evaluations/.../strings.xml`, y el estado vacío de Record) y no puede explicarlo. | El backend ya la expone (B1): `sync.sources.enrollment.situation`, un objeto opcional `{code, description}` de strings (no enum; códigos vistos: `01`, `06`, `10`, `12`, `15`, y `description` con el texto de DST). Solo viaja en la sync y solo cuando la inscripción está anulada; el campo se omite si no hay. **Viaja con `status = success`** tanto en la provisional como en la definitiva degradada, así que decodificarlo no depende de F1: basta agregar el campo opcional al DTO de `SyncSourceReportResponse`. La app distingue los dos momentos por si el record trae o no término `current`. La app aún no lo lee. | Implementar la lectura del campo `situation` y los copys por causa y por momento (recomendación visual). Decidir con producto si el texto se arma por `code` (más control) o se muestra `description` de DST; ante un `code` desconocido usar un copy genérico de "inscripción anulada". El código puede cambiar de un día a otro: el aviso debe salir de la última sync, no guardarse como estado propio. |
| **F3** | Experiencia de **nuevo ingreso**: la sync responde 424 con `reason = "NEW_STUDENT_NO_RECORD"` (`SyncRecordErrorResponse.reason`, que ya se lee como string y llega a `SyncRemoteException.conflictReason`). Hoy `markSyncFailure` (`SyncDataSource.kt`) lo trata como `SyncStatus.Unavailable`, igual que un 424 sin razón, y el usuario reintenta sin fin. También hay un 503 con `reason = "DST_RECORD_ACCESS_DENIED"` (la cuenta tiene expediente guardado pero DST hoy lo niega): distinguirlo de un 503 normal. | La razón existe desde este ciclo del backend (record 5.14.0). | Leer `reason` en `markSyncFailure`, agregar estados propios en el dominio (p. ej. `SyncStatus.NewStudentNoRecord`) y copys nuevos. No requiere cambios del backend. |
| **F4** | Diferenciar en la UI tres causas hoy mezcladas: "servicio de la universidad caído" (503, o 424 sin `reason`), "no inscrito" (F1/F2) y, más adelante, "encuesta docente pendiente" (`CMP_003`, backend C4) o "solo pregrado" (`CMP_005`). Hoy hay un único mensaje de indisponibilidad en `summary/.../strings.xml` (`dialog_*_sync_unavailable`, `dialog_message_sync_sources_*`) y en `evaluations/.../strings.xml` (`title_enrollment_unavailable_evaluations`). | Depende de F1, F2 y F3 para tener estados con los que diferenciar; `CMP_003`/`CMP_005` dependen además del backend C4. | F1–F3; backend C4 para los estados de encuesta y de solo pregrado. |
| **F8** | Indicar que los datos están **desactualizados** cuando la inscripción no se pudo refrescar (`partial` con enrollment `unavailable`; el backend conserva el trimestre en curso anterior). | Hoy solo se muestra el diálogo de estado en Summary; el trimestre en curso no dice cuán viejo es. | Decidir dónde mostrar la fecha (ya existe `text_last_sync` en Summary). |
| **F10** | Respetar `Retry-After` y aplicar backoff en los **503 de auth**. Desde este ciclo del backend, `POST /auth/v2/bootstrap` y `POST /auth/v2/token/exchange` responden 503 con el header `Retry-After: 30` (segundos) cuando DST o el proveedor de identidad no están disponibles. Hoy la app reintenta sin esperar, y el rate limit es de **5 por minuto por cuenta**: en la semana del 22-sep hubo 40 respuestas 429 en bootstrap (27 el 24-sep), es decir, los reintentos durante una caída de DST terminan bloqueando al usuario por 429 aun cuando ya se recuperó. | El header existe desde este ciclo del backend (auth); la app no lo lee. | En el cliente de auth: ante 503, leer `Retry-After` (si falta o no es un número, usar 30 s por defecto), no reintentar antes de ese plazo y aplicar backoff creciente con tope si sigue fallando; tratar el 429 como "espera y reintenta más tarde", no como error de credenciales. Sin cambios del backend. |

## Edición de notas (overlay)

| # | Pendiente | Por qué quedó atrás | Qué lo desbloquea |
|---|---|---|---|
| **F11** | Ráfagas de **409 `STALE_PRECONDITION`** en `PUT /record/v5/overlay/attempts/{attemptId}`. En la semana del 28-sep al 5-oct hubo 335, hasta 26 del mismo usuario en un día: 242 desde Android 6.5.0, 71 desde Android 6.6.0, 19 desde iOS 6.5.0 y 7 desde iOS 6.6.0. El 409 trae `current_revision` para rebasar, pero la app **no lo lee**: `expected_revision` sale de `PendingMutationEntity` (`persistence/.../room/entity/`) y no hay ninguna referencia a `current_revision` en el código de la app, así que la cola reintenta con la revisión vieja. | Se vio al revisar los logs de Cloud Run; del lado del backend el contrato ya es correcto (acepta una revisión vieja mientras nadie haya cambiado el override de ese intento). Falta confirmar en la cola de mutaciones si el reintento es inmediato o por cada mutación encolada. | Decodificar `current_revision` del 409 y rebasar las mutaciones pendientes con él (o refrescar el record antes de reintentar), con un tope de reintentos por mutación. Sin cambios del backend. |
| **F12** | **404 al editar un intento que ya no existe.** Cuando una sync elimina el trimestre en curso (anulada definitiva, o el período cerró en el expediente), la app sigue enviando `PUT /record/v5/overlay/attempts/{attemptId}` para sus intentos y recibe 404 sin cuerpo (7 casos en la semana, uno en el mismo minuto del borrado). | La app no descarta las mutaciones pendientes de un término que la sync acaba de quitar. La ventana provisional del backend reduce los casos, pero no los elimina. | Al aplicar una sync, descartar las mutaciones pendientes cuyos intentos ya no están en el record, y tratar el 404 de overlay como "descartar y refrescar", no como error reintentable. Sin cambios del backend. |

## Comprobante de inscripción (enrollment-proof)

| # | Pendiente | Por qué quedó atrás | Qué lo desbloquea |
|---|---|---|---|
| **F5** | Revisar `EnrollmentProofDataSource.kt` (`enrollmentproof/.../data/source/`): **a)** con un PDF en caché, cualquier error del refetch (incluido un 409 de credenciales desactualizadas) se traga (`onFailure` solo relanza si no hay caché) y se abre el PDF viejo sin avisar; **b)** un 424 no está mapeado a nada específico; **c)** en la fase 1 del backend los no inscritos reciben 503 "servicio no disponible", cuando en realidad no hay comprobante que mostrar; **d)** una inscripción anulada recibe 404, que hoy se muestra como `error_enrollment_not_found` ("Comprobante no disponible"); esto vale también durante la ventana provisional, cuando el trimestre sí se ve en la app. | El comportamiento de caché fue deliberado ("fallback cuando el refetch falla"), pero no distingue el motivo del fallo. | Definir con producto qué se le dice al usuario en cada caso; d) además depende del backend B1. |

## Oportunidades nuevas

| # | Pendiente | Por qué quedó atrás | Qué lo desbloquea |
|---|---|---|---|
| **F6** | **Horario de clases** del trimestre en curso. El backend (B2) ya sirve, en cada intento del término `current` del record (`AcademicAttemptResponse`), cuatro campos opcionales: `section` (entero), `schedule[]` con `{day_of_week (1 = domingo … 7 = sábado), start_block, end_block, classroom}` (bloques tal cual los da DST, **no** horas del reloj; en los datos reales van **del 1 al 11**, no del 1 al 8 como decía la doc: 212 entradas terminan después del bloque 8, p. ej. 9–10, 9–11 y 7–11; `classroom` vacío si DST no da aula; lista vacía = "por convenir"), `enrollment_errors[]` (textos verbatim de DST, p. ej. "CHOQUE DE HORARIO"; solo si hay alguno) y `withdrawn` (booleano). En términos históricos y sintéticos no vienen. Sigue sin servirse el calendario académico (inicio de clases, último día de retiros) ni los créditos inscritos o válidos. | La app aún no decodifica los campos, y falta la equivalencia entre bloque y hora del reloj (backend C8), que decide si se puede pintar un horario en horas. | Decodificar los campos (opcionales) y diseñar la vista por bloques; C8 para mostrar horas; el calendario académico necesitaría una ampliación nueva del backend. |
| **F7** | Mostrar `09 Sin Prelación`. **Recomendación: descartarlo.** | B3 quedó aclarado: es un estado **transitorio de una inscripción normal**. De 117 respuestas con `09`, 9 carnets pasaron a `00` en días, ninguna trae errores por materia y traen las mismas materias que una `00`. El backend lo trata como inscrito y no lo envía en `situation`. | Nada; se deja anotado para no reabrirlo. Si producto igual lo quiere, el backend tendría que exponerlo (hoy `situation` solo viaja en anuladas). |

## Pruebas y mocks

| # | Pendiente | Por qué quedó atrás | Qué lo desbloquea |
|---|---|---|---|
| **F9** | Mocks y E2E para los estados nuevos. Fixtures WireMock en `mocks/__files/sync/` (hoy: `post-sync-success`, `post-sync-no-current`, `post-sync-enrollment-unavailable`, `post-sync-record-unavailable`, `post-sync-outdated-credentials`, etc.) y sus mappings en `mocks/mappings/sync/`, para: sync con `enrollment.status = "not_enrolled"`, 424 con `reason = "NEW_STUDENT_NO_RECORD"`, 503 con `reason = "DST_RECORD_ACCESS_DENIED"` y una inscripción anulada en sus dos momentos (provisional: `success` con `sources.enrollment.situation` y término `current` presente; definitiva: la misma `situation` sin término `current`), un 409 de overlay con `current_revision` y un 404 de overlay sobre un intento eliminado (F11, F12), además de intentos del término actual con `section`, `schedule[]`, `enrollment_errors[]` y `withdrawn` (B2) y un 503 de auth con `Retry-After` (F10). Flujos Maestro correspondientes en `e2e/maestro/flows/summary/` (hoy p. ej. `summary-partial-enrollment-status-dialog.yaml`), `record/`, `evaluations/` (p. ej. `evaluations-enrollment-unavailable.yaml`) y `enrollmentproof/`, y sus suites en `e2e/maestro/flows/suites/`. Los tests de UI comunes siguen `docs/testing/common-ui-testing.md`. | Sin F1–F3 y F10–F12 no hay estados que probar. | F1, F2, F3, F10, F11, F12. |

## Recomendación visual

Reglas generales, de `docs/design-system.md`: **no usar `colorScheme.error`** para "no inscrito" ni
para "nuevo ingreso" (es reprobado o destructivo); el amarillo primario es "en curso" y el verde es
"aprobado", así que tampoco sirven para estos estados; usar tonos neutros (`available()` /
`blocked()`), estados de pantalla con `ErrorView` (base), `EmptyView` (base) e
`IllustratedMessageView` (base), avisos breves con `SnackBarMessage` (base), botones con la jerarquía
`Button` / `OutlinedButton` / `TextButton`, espaciado con `TuIndiceSpacing` y terminología "materia",
"nota", "trimestre". Todo texto nuevo va en `strings.xml` del módulo que lo muestra, y cada estado
nuevo se cubre con los gates de `docs/testing/common-ui-testing.md`.

- **F1: no inscrito en el trimestre actual.** Extender `SyncStatusInfoContentDialog`
  (`summary/.../ui/dialog/`) con el caso "no inscrito", como aviso informativo, sin color de error y
  con el botón `dialog_button_understood` ("Entendido").
  - Título: "No estás inscrito en este trimestre".
  - Mensaje: "La universidad no reporta una inscripción vigente para ti en este trimestre. Tus notas
    anteriores siguen disponibles."

- **F2: inscripción anulada, provisional.** El trimestre en curso se sigue mostrando y editando
  con normalidad. Encima, un aviso neutro-cálido (no `error`) junto al encabezado del trimestre,
  en Record y en Evaluaciones:
  - Título: "Tu inscripción aparece anulada".
  - Mensaje por causa, p. ej. límite de créditos: "La universidad tiene tu inscripción de
    Septiembre–Diciembre 2026 anulada por el límite de créditos. Durante las correcciones de
    inscripción todavía puedes regularizarla con DACE. Tus notas se mantienen."
  - Sin bloquear nada y sin CTA agresivo. El aviso desaparece solo cuando una sync deja de traer
    `situation`.

- **F2: inscripción anulada, definitiva.** Estado vacío explicativo con `EmptyView` o `IllustratedMessageView`
  (el mismo que hoy usa `RecordEmptyView` en Record y el estado vacío de Evaluaciones), con un
  texto por causa. **No** se debe mostrar como "servicio no disponible". Ejemplos:
  - Título: "Tu inscripción fue anulada".
  - Anulada por límite de créditos: "La universidad anuló tu inscripción de Septiembre–Diciembre 2026
    por el límite de créditos. Consulta con DACE para regularizar tu situación."
  - Anulada por reglamento de permanencia: "La universidad anuló tu inscripción de
    Septiembre–Diciembre 2026 por el reglamento de permanencia. Consulta con DACE para conocer tus
    opciones."
  - Sin CTA agresivo; a lo sumo un `TextButton` "Entendido" o un enlace informativo a DACE.
  - En Evaluaciones, sustituir `title_no_subjects_evaluations` / `message_no_subjects_evaluations`
    ("Sin trimestre en curso") por estos textos cuando la causa sea la anulación. Usa
    `sources.enrollment.situation` (B1, ya disponible).

- **F3: nuevo ingreso sin expediente.** Pantalla u onboarding informativo, con ilustración y sin
  halo de error ni reintento automático agresivo (respetar el cooldown de sync):
  - Título: "Aún no tienes expediente en la universidad".
  - Mensaje: "La universidad todavía no tiene un expediente académico para tu cuenta. Si eres de nuevo
    ingreso, es normal: DACE lo crea más adelante. Vuelve a intentarlo en unos días."
  - Acción secundaria (`OutlinedButton`): "Reintentar", con enfriamiento; no reintentar solo.
  - **503 `DST_RECORD_ACCESS_DENIED`** (ya tienes datos guardados): diálogo o aviso distinto del de
    "servicios no disponibles": "La universidad no nos permite consultar tu expediente ahora.
    Mantenemos tus datos anteriores." Sin bloquear la app.

- **F4: causas separadas.** Copys distintos, y ícono distinto por causa, en
  `summary/.../strings.xml` (`dialog_title_sync_unavailable` y afines) y
  `evaluations/.../strings.xml` (`title_enrollment_unavailable_evaluations`):
  - Servicio caído: "Servicios de la universidad no disponibles" (el actual); ícono de nube o de
    desconexión, `Outlined`.
  - No inscrito: ver F1; ícono informativo (`Outlined.Info`), no de advertencia.
  - Encuesta docente pendiente (`CMP_003`, backend C4): "Falta completar la encuesta docente. La
    universidad la pide antes de mostrar tu inscripción." Acción: abrir el enlace de DST en el
    navegador externo (ya existe el diálogo `browser-external-dialog` en E2E de maincore).
  - Solo pregrado (`CMP_005`): "Tu cuenta no es de pregrado, así que el comprobante de inscripción
    no está disponible."

- **F5: comprobante.**
  - Con PDF en caché tras un fallo del refetch, avisar con `SnackBarMessage`: "Mostrando tu
    comprobante guardado".
  - 409 con credenciales desactualizadas: llevar al flujo de actualizar contraseña en vez de abrir
    el PDF viejo en silencio.
  - Mapear el 424 a un mensaje propio (nuevo ingreso: F3).
  - 503 de una cuenta no inscrita: "No tienes una inscripción vigente, así que no hay comprobante
    que mostrar."
  - Anulada: "Tu inscripción de este trimestre fue anulada, por eso no hay comprobante." Depende de
    B1; hasta entonces el 404 sigue con `error_enrollment_not_found`.

- **F6: horario de clases.** Vista semanal por bloques en Record (proyección) o en la tira
  semanal de Evaluaciones. No fijar la grilla en 8 filas: los bloques observados llegan al 11, así
  que el alto sale del bloque máximo que traigan los datos.
  - Cada materia con su sección y aula (p. ej. "Sección 1 · MYS-116").
  - Un chip de alerta neutro-cálido (no `error`) para las materias con `errores`: "Choque de
    horario con CI2691".
  - Calendario de fechas clave (inicio de clases, último día de retiros) como lista compacta.
  - Mostrar los bloques como "Bloque 3–4" hasta tener la equivalencia con la hora del reloj
    (backend C8). Los datos por materia ya vienen del backend (B2): `section`, `schedule[]`,
    `enrollment_errors[]` y `withdrawn` en los intentos del término actual. Una materia con
    `withdrawn = true` se muestra atenuada (no en color de error) y sin horario activo.

- **F7: `09 Sin Prelación`.** No mostrar nada: es un estado transitorio de una inscripción normal
  y no afecta a ninguna materia en particular.

- **F10: reintentos de auth.** Sin copy nuevo obligatorio: mientras dura la espera indicada por
  `Retry-After`, el botón de entrar se muestra deshabilitado con el aviso existente de servicios no
  disponibles y, si el usuario insiste, un texto de espera ("Vuelve a intentarlo en unos segundos")
  en lugar de un error; un 429 nunca se presenta como contraseña incorrecta.

- **F11 y F12: edición de notas.** Sin copy nuevo en el caso normal: el rebase con
  `current_revision` y el descarte de mutaciones huérfanas son silenciosos. Solo si una nota no se
  pudo guardar tras agotar los reintentos, avisar con `SnackBarMessage`: "No pudimos guardar tu
  nota. Revisa el valor e inténtalo de nuevo." Si la nota era de un trimestre que la universidad
  eliminó, no mostrar error: el estado vacío de F2 ya lo explica.

- **F8: datos desactualizados.** Cuando la inscripción no se pudo refrescar, mostrar "Actualizado por
  última vez el 22 sep." junto al encabezado del trimestre en curso (con `TuIndiceAlpha.Deemphasis`
  para texto secundario) y mantener el diálogo de estado actual. No es un error: no usar color de
  error.
