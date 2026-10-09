# Fidelidad de la migración de los flujos a escenarios

Registro histórico de la migración de los 86 flujos del runner anterior (YAML) a 85 escenarios de Kotlin. Es la
tabla escenario por escenario de la auditoría del catálogo, con el flujo del que viene cada escenario. No se mantiene:
dice lo que se encontró entonces y no lo que dicen hoy los escenarios.

- Auditoría hecha sobre el catálogo de `47ff97432` (diff `538ac4c6a..47ff97432`), leyendo los 86 flujos, los 199
  mappings de `mocks/` y los escenarios; sin ejecutar nada.
- Veredicto: de los 85 escenarios, ninguno afirma menos que su flujo a nivel de aserción; 24 afirman más (credencial recibida
  por el backend, relectura de lo tecleado, coachmarks y diálogos que pasan de «si visible» a obligatorios) y el resto
  es equivalente paso a paso. Conteo de la tabla: fiel 60, afirma más 18, distinto 1, distinto y afirma más 6.
- Condicionales del flujo que pasaron a obligatorios: el coachmark tras el inicio de sesión (4 escenarios), el diálogo de
  `summary-outdated-credentials` y los coachmarks de `record-schedule-view-remembered`. Obligatorios que pasaron a
  condicionales: ninguno. Escenarios que terminan antes que su flujo: ninguno.
- El flujo `login-summary-ready` no tiene escenario: era un preludio de otros flujos, y cada escenario declara ahora su
  propio inicio.
- Los flujos viven en el historial de git, en el directorio `flows/` del runner anterior, y la etiqueta
  `pre-corte-maestro` (commit `aecd97de0f7ae5013fbce58966a3e39db8ac4f84`, el último antes del corte) los conserva. Para
  leer uno: `git ls-tree -r --name-only pre-corte-maestro | grep 'flows/auth/login-success.yaml'` da su ruta completa, y
  `git show pre-corte-maestro:<esa ruta>` su contenido. La columna «Flujo de origen» es la ruta bajo `flows/`. La
  etiqueta es local hasta que la persona dueña la suba; el SHA es el dato que sobrevive a un squash.
- Los nombres y los conteos son los del momento de la migración: el paso que la tabla llama `finishTextEntry` se llama
  hoy `submitTextEntry`, y el catálogo vigente (`e2e/catalog/scenarios.json`) tiene más escenarios que esos 85 y cambia
  con cada escenario nuevo. La tabla sigue siendo la de los 85 de entonces.
- Las notas con «heredado» describen una debilidad que el flujo ya tenía y el escenario conservó; las que dicen «después»
  agregan lo que se hizo tras la auditoría.

| Escenario | Flujo de origen | Veredicto | Nota |
|---|---|---|---|
| `auth-login-success` | `auth/login-success.yaml` | afirma más | credencial en backend, relectura del USB-ID, coachmark obligatorio |
| `auth-login-usb-email` | `auth/login-usb-email-success.yaml` | afirma más | ídem (`mail:123456`) |
| `auth-login-usb-email-usbid` | `auth/login-usb-email-usbid-success.yaml` | afirma más | ídem |
| `auth-usage-data-consent` | `auth/usage-data-consent.yaml` | afirma más | ídem; el estado del consentimiento sigue sin afirmarse |
| `auth-login-invalid` | `auth/login-invalid.yaml` | afirma más | snackbar, mensaje, texto, marca de rechazo y credencial |
| `auth-login-disabled` | `auth/login-disabled.yaml` | afirma más | credencial |
| `auth-login-retry-after-unavailable` | `auth/login-retry-after-unavailable.yaml` | afirma más | credencial |
| `auth-login-cancel` | `auth/login-cancel.yaml` | afirma más | credencial |
| `auth-login-outdated-app` | `auth/login-outdated-app.yaml` | afirma más | credencial; `relaunch` con argumentos de inicio limpio |
| `auth-login-password-toggle` | `auth/login-password-toggle.yaml` | fiel | aserción débil heredada del flujo |
| `auth-terms-privacy-from-login` | `auth/terms-privacy-from-login.yaml` | fiel | - |
| `auth-session-invalidated` | `auth/session-invalidated.yaml` | fiel | sembrado; la sync responde 409 igual que tras el login |
| `auth-update-password` | `auth/update-password.yaml` | afirma más | credencial de la reemisión; en la auditoría la contraseña llegaba con una «v» delante en Android (la causa era un toque sobre una posición desactualizada; se corrigió después en el driver de Android) |
| `auth-update-password-failure` | `auth/update-password-failure.yaml` | afirma más | credencial; botón habilitado tras el 401 |
| `auth-sign-out-cancel` | `auth/sign-out-cancel.yaml` | fiel | - |
| `auth-sign-out` | `auth/sign-out.yaml` | fiel | - |
| `auth-pending-sign-out` | `auth/pending-sign-out.yaml` | fiel | - |
| `auth-pending-sign-out-flush-success` | `auth/pending-sign-out-flush-success.yaml` | fiel | - |
| `maincore-app-availability-notice` | `maincore/app-availability-notice.yaml` | fiel | - |
| `maincore-bottom-bar-state` | `maincore/bottom-bar-state.yaml` | fiel | - |
| `maincore-back-stack` | `maincore/back-stack.yaml` | fiel | - |
| `maincore-tab-stack-preservation` | `maincore/tab-stack-preservation.yaml` | fiel | - |
| `maincore-browser-external-dialog` | `maincore/browser-external-dialog.yaml` | afirma más | iOS espera el fin de carga antes del toque por coordenada |
| `coachmarks-contextual-summary` | `coachmarks/coachmark-contextual-summary.yaml` | fiel | - |
| `coachmarks-progressive-record` | `coachmarks/coachmark-progressive-record.yaml` | fiel | - |
| `summary-smoke` | `summary/summary-smoke.yaml` | fiel | - |
| `summary-profile-picture` | `summary/summary-profile-picture.yaml` | fiel | - |
| `summary-refresh-retry` | `summary/summary-refresh-retry.yaml` | fiel | tolerancia de Android heredada del flujo (después se unificó a un solo fallo en ambas plataformas) |
| `summary-status-dialog` | `summary/summary-status-dialog.yaml` | fiel | - |
| `summary-partial-enrollment-status-dialog` | `summary/summary-partial-enrollment-status-dialog.yaml` | fiel | - |
| `summary-outdated-credentials` | `summary/summary-outdated-credentials.yaml` | afirma más | el diálogo de contraseña pasa a obligatorio |
| `summary-new-student-no-record` | `summary/summary-new-student-no-record.yaml` | fiel | - |
| `summary-record-access-denied` | `summary/summary-record-access-denied.yaml` | fiel | - |
| `record-smoke` | `record/record-smoke.yaml` | fiel | - |
| `record-refresh-retry` | `record/record-refresh-retry.yaml` | fiel | dependía del orden de llegada entre la sync y la lectura (después se fijó en los mocks y se replica en `RetryOrderMocksTest`) |
| `record-term-selection` | `record/record-term-selection.yaml` | fiel | - |
| `record-attempt-overrides` | `record/record-attempt-overrides.yaml` | fiel | sin `retryTapIfNoChange` |
| `record-synthetic-term-search-empty` | `record/record-synthetic-term-search-empty.yaml` | afirma más | relectura de lo tecleado; condicional heredado del flujo |
| `record-synthetic-term-search-states` | `record/record-synthetic-term-search-states.yaml` | afirma más | teclea y relee (el flujo pegaba); `finishTextEntry` (hoy `submitTextEntry`) al volver |
| `record-synthetic-term-discard` | `record/record-synthetic-term-discard.yaml` | fiel | dependía de la fecha del dispositivo (después, reloj fijo con `TUINDICE_E2E_NOW`) |
| `record-synthetic-term-lifecycle` | `record/record-synthetic-term-lifecycle.yaml` | afirma más | relectura; sin los 5 `retry`; dependía de la fecha del dispositivo (después, reloj fijo) |
| `record-synthetic-term-rejected` | `record/record-synthetic-term-rejected.yaml` | afirma más | relectura; sin `retry`; dependía de la fecha del dispositivo (después, reloj fijo) |
| `record-annulled-provisional-schedule` | `record/record-annulled-provisional-schedule.yaml` | fiel | - |
| `record-schedule-view-remembered` | `record/record-schedule-view-remembered.yaml` | afirma más | credencial; coachmarks deterministas en vez de «si visible ×4» |
| `record-stale-enrollment-notice` | `record/record-stale-enrollment-notice.yaml` | fiel | - |
| `record-withdrawn-subject` | `record/record-withdrawn-subject.yaml` | fiel | - |
| `record-annulled-final-notice` | `record/record-annulled-final-notice.yaml` | fiel | - |
| `enrollmentproof-smoke` | `enrollmentproof/enrollmentproof-smoke.yaml` | fiel | - |
| `enrollmentproof-fetching-cancel` | `enrollmentproof/enrollmentproof-fetching-cancel.yaml` | fiel | - |
| `enrollmentproof-error-unavailable` | `enrollmentproof/enrollmentproof-error-unavailable.yaml` | fiel | aserción débil heredada del flujo |
| `enrollmentproof-not-found` | `enrollmentproof/enrollmentproof-not-found.yaml` | fiel | - |
| `enrollmentproof-annulled-not-found` | `enrollmentproof/enrollmentproof-annulled-not-found.yaml` | fiel | - |
| `enrollmentproof-outdated-credentials` | `enrollmentproof/enrollmentproof-outdated-credentials.yaml` | fiel | - |
| `pensum-smoke` | `pensum/pensum-smoke.yaml` | fiel | - |
| `pensum-selection` | `pensum/pensum-selection.yaml` | fiel | - |
| `pensum-node-detail` | `pensum/pensum-node-detail.yaml` | fiel | - |
| `pensum-detail-navigation` | `pensum/pensum-detail-navigation.yaml` | fiel | - |
| `pensum-refresh-not-found` | `pensum/pensum-refresh-not-found.yaml` | fiel | - |
| `pensum-refresh-failed-retry` | `pensum/pensum-refresh-failed-retry.yaml` | fiel | - |
| `pensum-record-unavailable` | `pensum/pensum-record-unavailable.yaml` | fiel | - |
| `pensum-current-absent` | `pensum/pensum-current-absent.yaml` | fiel | - |
| `pensum-cache-refresh-failed` | `pensum/pensum-cache-refresh-failed.yaml` | distinto, sin perder aserciones | se borró la tautología del aviso de datos locales; el escenario se borró después (E2c, ΔE-4): ni al volver a abrir la app ni al volver a la pestaña el producto refresca el pensum guardado (ninguna petición `GET /pensums/v4` tras el relanzamiento), así que el 503 nunca se ejerce |
| `pensum-equivalence-fulfilled` | `pensum/pensum-equivalence-fulfilled.yaml` | fiel | - |
| `subjects-smoke` | `subjects/subjects-smoke.yaml` | distinto, y afirma más | relectura; `finishTextEntry` (hoy `submitTextEntry`) también en iOS |
| `subjects-search-query-clear` | `subjects/subjects-search-query-clear.yaml` | distinto, y afirma más | ídem |
| `subjects-search-failed-retry` | `subjects/subjects-search-failed-retry.yaml` | distinto, y afirma más | ídem |
| `subjects-detail-tabs-tooltip` | `subjects/subjects-detail-tabs-tooltip.yaml` | distinto, y afirma más | ídem |
| `subjects-detail-unavailable` | `subjects/subjects-detail-unavailable.yaml` | distinto, y afirma más | ídem |
| `subjects-detail-failed-retry` | `subjects/subjects-detail-failed-retry.yaml` | distinto, y afirma más | ídem |
| `evaluations-smoke` | `evaluations/evaluations-smoke.yaml` | fiel | - |
| `evaluations-filters-and-form` | `evaluations/evaluations-filters-and-form.yaml` | fiel | sin la espera tras «mes anterior» (animaciones apagadas) |
| `evaluations-grade-from-list` | `evaluations/evaluations-grade-from-list.yaml` | fiel | - |
| `evaluations-edit-submit` | `evaluations/evaluations-edit-submit.yaml` | fiel | - |
| `evaluations-swipe-delete` | `evaluations/evaluations-swipe-delete.yaml` | fiel | - |
| `evaluations-add-submit` | `evaluations/evaluations-add-submit.yaml` | fiel | ídem filters-and-form |
| `evaluations-add-validation` | `evaluations/evaluations-add-validation.yaml` | fiel | - |
| `evaluations-enrollment-unavailable` | `evaluations/evaluations-enrollment-unavailable.yaml` | fiel | - |
| `evaluations-annulled-no-attempts` | `evaluations/evaluations-annulled-no-attempts.yaml` | fiel | - |
| `evaluations-not-enrolled` | `evaluations/evaluations-not-enrolled.yaml` | fiel | - |
| `evaluations-annulled-provisional-notice` | `evaluations/evaluations-annulled-provisional-notice.yaml` | fiel | - |
| `about-smoke` | `about/about-smoke.yaml` | fiel | - |
| `about-internal-browser-links` | `about/internal-browser-links.yaml` | fiel | - |
| `about-external-url-links` | `about/external-url-links.yaml` | fiel | - |
| `about-platform-edge-triggers` | `about/platform-edge-triggers.yaml` | fiel | tolerancias heredadas del flujo |
| `about-usage-data-consent` | `about/usage-data-consent.yaml` | fiel | débil heredado |
