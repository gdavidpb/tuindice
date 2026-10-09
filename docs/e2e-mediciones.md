# Mediciones del sistema E2E

Observaciones fechadas que respaldan las decisiones del skill de certificación y de su runbook
(`.codex/skills/certify-tuindice-pr/`). Son observaciones, no reglas: cada una dice qué se midió, cuándo y sobre qué
equipo, y lo que no se midió se marca como tal. Un dato nuevo entra aquí con su fecha y su origen antes de que el
runbook lo cite.

Los informes de origen (la investigación de la certificación y los informes de ejecución de las fases) no están
versionados; las cifras de abajo son las que se conservaron de ellos.

## 1. Certificación de `feat/university-states-backlog` con el runner anterior

Ventana: de 2026-10-05 23:54 a 2026-10-06 13:10 (hora local, -03), 13 h 16 min en total (796 min). Equipo: M1 Max de 10
núcleos y 64 GB. Sistema de entonces: un runner de flujos YAML por plataforma, una rotación completa por invocación y un
único reintento que reiniciaba la rotación. Método de la investigación: solo lectura de los artefactos de las corridas.

| Medición | Valor |
|---|---|
| Carga del host a las 13:16 (1, 5 y 15 min) | 8,19 / 25,99 / 34,33 |
| Tiempo encendido del host | 34 días |
| Latencia de entrega de teclas, del HID a la inserción | mediana 12-15 ms toda la noche; p99 de 30 a 44 ms |
| Entradas largas corruptas por tramo antes de las 10:00 | 9 %, 7 %, 13 % y 8 % (el tiempo desde el reinicio no influyó) |
| Entradas largas corruptas, antes y después de las 10:00 | 9,3 % antes (10 de 108 con el emulador Android apagado); 36 % después (12 de 33) |
| Origen de la carga desde las 10:00 | no registrado |
| Fallos de iOS sobre `HEAD` | 25; 23 confirmados por texto corrupto en un campo (contraseña 20, USB-ID 2, buscador 1) y uno probable |
| Teclas que UIKit entregó e insertó en la app, en las 22 entradas largas corruptas con registro completo | 100 % |
| Contraseñas erróneas vistas en el cable (WireMock, 10:51-13:10) | 7 de 26 contraseñas largas; 0 de 47 con `123456` |

Reparto de las 13 h 16 min: 129 min (16 %) de diagnóstico, paridad y evidencia que encontraron fallos reales; 27 min (3 %)
de evidencia paralela sobre `HEAD`; 116 min (15 %) de Android secuencial, que pasó 39 de 39 en 114 min 25 s; 523 min (66 %)
de iOS secuencial, que nunca pasó. Dentro de las 8 h 43 min de iOS, el runner ejecutó 8,2 h: 4,1 h fueron casos que
pasaron y se descartaron porque la rotación no se completó, y 4,1 h casos que fallaron.

Comparación entre certificaciones (entradas largas corruptas, iOS):

| Fecha | iOS | macOS | Compose MP | Kotlin | Resultado |
|---|---|---|---|---|---|
| 2026-08-08 | 26.5 | 26.5.2 | 1.11.1 | 2.4.10 | 37 de 37 casos sin un fallo |
| 2026-09-30 | 27.0 | 26.6.2 | 1.12.0 | 2.4.20 | 3 de 44 (7 %) |
| 2026-10-06 | 27.0 | 26.6.2 | 1.12.0 | 2.4.20 | 22 de 141 (16 %) |

Entre agosto y septiembre cambiaron a la vez el runtime de iOS, Xcode, el runner, macOS, Compose Multiplatform y Kotlin; los
artefactos no permiten separar cuál abrió la ventana. La cadencia de tecleo medida fue la misma con las dos versiones
del runner. Es una hipótesis, no una causa.

Otras observaciones de esa noche:

- En el cable había caracteres movidos de sitio (`record-retry-pssa`), no solo perdidos.
- `annulled-provisional-pass` se tecleó 24 veces sin una corrupción; `record-rejected-pass` falló 6 de 12 veces y
  `record-retry-pass` 5 de 20, en la misma pantalla y los mismos minutos. Sin explicación.
- Con la tasa de 9,3 % por entrada larga y 39 entradas largas por rotación, la probabilidad de una rotación limpia es 2,2 %
  (aritmética sobre la tasa medida, no una medición). Con el 36 % de las últimas horas, quince fallos seguidos de una
  suite de 8 entradas largas tienen probabilidad 65 %.
- El defecto de producto (el eco del view model pisaba lo tecleado) se corrigió con `EditableTextFieldState`; sus tests de
  host están en `base/src/commonTest/.../ui/text/EditableTextFieldStateTest.kt` y en los `*UiTest` de los campos de `auth`.

## 2. Drivers nativos y harness nuevo (2026-10-06 y 2026-10-07)

Mismo equipo del dueño; estas mediciones no repiten la descripción del host. Durante la construcción del runner de
Android se midió una carga en reposo de alrededor de 11 (WindowServer, una máquina virtual de Virtualization.framework,
el emulador y el hook del grafo tras cada commit); el registro no la fecha.

**Puente Kotlin a Swift y tecleo en iOS (2026-10-06).** Enlace estático del framework sin símbolos en la app; 60 de 60
corridas con veredicto normal de XCTest (80 de 80 con las adicionales); el fallo esperado, 10 de 10 con archivo y línea
de Kotlin; sobrecarga del puente 0,159 ms por llamada; decodificar el catálogo 3,4 ms con 4 escenarios y 98 ms con uno
sintético de 100 por 30; tecleo 30 de 30 correcto con trozos de 4 caracteres; sobrecarga de `xcodebuild` por invocación de
6,5 s de mediana en esa serie y de 11 a 14 s en los humos posteriores.

**Android bajo carga (2026-10-06).** En la primera serie, 3 corridas no verdes (`017`, `018`, `028`), las tres
`APP_NOT_RUNNING` al lanzar, durante un pico de carga del host de 22 a 31; el registro del emulador mostraba fotogramas de
10 a 20 s en `system_server`. En la repetición posterior (80 corridas: `auth` 27 de 30, `evaluations` 18 de 20, `summary`
20 de 20, el fallo esperado 10 de 10), 5 corridas no verdes, todas con `load1` final de 12,9 o más (12,9, 21, 22,7, 18,3 y
40,5): una con la entrada de la contraseña rechazada tras 40 s, dos `APP_NOT_RUNNING`, y dos esperas agotadas de 30 s y de
10 s. En la corrida de la contraseña el emulador tenía 4 vCPU y su propia carga era de 12-13, con fotogramas de 1 a 3,6 s,
730 MB de swap y 70 % de iowait. El origen de la carga del equipo en esa repetición fue otra sesión del dueño ejecutando
`pytest -n auto` con 10 trabajadores. No se midió cuántas corridas verdes ocurrieron con esa misma carga, así que estas cifras no
establecen una tasa de fallo por carga.

**Emulador con 8 núcleos, 16,2 GB y sin ventana.** Carga del dispositivo de 2,3 a 4,8 en el verbo `health` (2026-10-06) y
de 2,6 a 3,1 en una corrida posterior. Arranque en frío de 22 s y de 63 s en dos corridas; recuperación del emulador (reinicio, puerta
de disponibilidad y ajustes) de 53,5 s.

**Conformidad con `--repeat 10`.** Primera pasada: Android 16 de 17 escenarios (`conformance-foreground` falló 2 de 10),
iOS 14 de 16 (`conformance-type-replace` 0 de 10 y `conformance-swipe-from-element` 1 de 10). Tras corregir los drivers:
Android 17 de 17 (170 de 170) en 25,5 min e iOS 16 de 16 (160 de 160) en 54,5 min. Una pasada posterior de Android
(2026-10-07) dio 180 de 180 corridas.

**Sondeo del catálogo migrado (2026-10-07, una corrida por escenario).** Android 77 de 85, iOS 78 de 85, 71 verdes en
ambas; 14 escenarios con algún fallo. `auth` tres veces: iOS 54 de 54, Android 53 de 54. En paralelo, Android tardó
41 min e iOS 56 min, con 37 esperas por carga.

**Carga con trabajo ajeno (2026-10-07, 22:40-22:55).** Con `pytest -n auto` de otro proyecto (18 procesos) y Docker, la CPU
libre fue 0 %; al terminar, 9 a 23 % libre con solo el emulador, el simulador y las compilaciones del propio trabajo. De ahí
que el harness mida la CPU libre y no solo el promedio de carga. Es un episodio distinto del `pytest` con 10 trabajadores
del 2026-10-06 (sección anterior, Android bajo carga), del que no se registró la CPU libre.

**Simulador de iOS degradado (2026-10-08).** Sobre 30 intentos fallidos de iOS con `app.log`: 11 en simulador sano (10 del
navegador externo y 1 de tecleo) tenían 0 apariciones de `kAXErrorAPIDisabled`, 0 de `Couldn't read values in
CFPrefsPlistSource` y 0 de `Couldn't write values for keys`; 19 en simulador degradado tenían unas 1050, unas 111 y de 9 a
74. La separación fue limpia en esa muestra. `device.sh health` contra un simulador vivo y sano respondió `ok` en 0,55 s;
qué responde con el simulador degradado no se midió. Origen: la entrada de las 02:35 del 2026-10-08 de `reconciliaciones.md`
(el registro de ejecución de la fase H, no versionado), que nombra un intento degradado
(`…T022929Z-ios-diagnose-e0738f5/…/conformance-enabled/attempt-1-r7/app.log`, ruta abreviada allí) y no conserva las
rutas de los otros 29 `app.log`.

**Lectura del estado.** `e2e.py status` pasó de unos 125 s a unos 10 s al calcular los veredictos en `harness/verdict.py`.

## 3. Escenarios de plataforma y etapa de drivers de cierre (2026-10-08 y 2026-10-09)

Mismo equipo; Android en el emulador `Pixel_10_Pro_XL` e iOS en el simulador `TuIndice-E2E`. Cada serie de N corridas
usó `E2E_MAX_RETRIES=0 --repeat N --survey`. Los informes de origen (E2b, E2c y la etapa de drivers de cierre) no están
versionados; la hora de cada corrida no se registró, solo la de los commits que las contienen. Los registros de las
corridas viven en el scratchpad de la sesión y bajo `build/e2e`, ninguno versionado.

**Disparadores de plataforma de `about-platform-edge-triggers` (E2b, desde las 15:14 del 2026-10-08, base `ce2ac8167`; E2c,
commit `097733552` de las 22:46 y serie final, `bab0139cb`).** Android, serie de 10 con `waitBackgrounded` tras cada
disparador: tienda (Play Store) salió de la app 10 de 10 y otras 10 de 10 en la serie final, en 1,0 s; correo (Gmail,
`WelcomeTourActivity`) 10 de 10 y 10 de 10, en 1,5 s; «Reportar un error» 0 de 10: Gmail abre `ComposeActivityGmailExternal`
y la cierra solo a los 100 ms (`wm_finish_activity ... app-request` en el logcat del intento) y la app ya está de vuelta
cuando el wait mira. En E2b, tienda y correo salieron en 0,8 y 1,5 s, y «Reportar un error» una vez sí y otra no (Gmail abrió
`WelcomeTourActivity` y luego `ComposeActivityGmailExternal`; el estado de Gmail en el emulador no es determinista); el
número total de corridas de E2b no está registrado (sin fuente). iOS: el
`waitBackgrounded` tras el disparador de la tienda venció a los 20 s (E2b, una corrida); correo y reporte no se midieron en
iOS por separado. Con el escenario tal como quedó, 10 de 10 en Android y 3 de 3 en iOS.

**Hoja de compartir en iOS (E2c, commit `097733552`, 2026-10-08 22:46).** Con la hoja abierta, el árbol mostró un `Popover`
de 384 por 364 y un `PopoverDismissRegion` de pantalla completa, sin botón de cierre. Un toque por posición, una vez cada
uno: a (0,5; 0,04) de la pantalla, en la barra de estado, la hoja siguió abierta; a (0,5; 0,3), a la altura de la fila
«Creative Commons», cerró la hoja y abrió ese enlace en Safari; a (0,6; 0,1), en la parte vacía de la barra superior, cerró
la hoja y About quedó igual. El mecanismo no se probó. Con el escenario tal como quedó, 3 de 3 en iOS.

**Copia guardada del comprobante (E2c, commit `c8f30c0db`, 2026-10-08 23:00).** `enrollmentproof-saved-copy-dialog`: 3 de 3
en Android y 3 de 3 en iOS. El botón positivo del diálogo no se probó en esa fase.

**Doble toque en el lienzo del pensum en iOS (E2c, 40 minutos, commit `d84b6ea37`, 2026-10-09 00:40; la entrega del centro,
etapa de drivers del 2026-10-08).** Se leyó el efecto como la aparición de `pensum_minimap_toggle`, que sí aparece en iOS tras
el botón de zoom de `pensum-smoke`. Una corrida por entrega, ninguna con efecto: `coordinate.doubleTap()` en el centro del
lienzo (etapa de drivers, que además comprobó que el punto cae en un hueco y no en un nodo); `element.tap(withNumberOfTaps: 2,
numberOfTouches: 1)`; `element.doubleTap()`; `coordinate.doubleTap()` a un cuarto de la altura; dos `press(forDuration: 0.02)`
seguidos, donde el segundo empezó 422 ms después del primero (cada llamada de XCUITest cuesta unos 0,4 s) y el detector de
Compose espera 300 ms. Como ninguna entrega movió el lienzo ni una vez, no se repitió ninguna 10 veces. En Android el efecto
sí se observa: `conformance-double-tap-effect` 3 de 3. Si el zoom funciona en un iPhone real no se midió.

**Desplazamiento en Android (E2c, 2026-10-09).** `conformance-scroll` con `OpenKoin` como destino: 10 de 10. Con `OpenKtor`
6 de 10 fallos y con `OpenDst` 1 de 1 fallo, aunque el elemento aparece visible y centrado en la captura
(`visible-to-user="true"`, 72 % de la altura); los intentos fallidos están en
`build/e2e/runs/20261009T030947Z-android-diagnose-449a23b/scenarios/conformance-scroll/attempt-1*` de esa máquina. Causa no
determinada.

**Otros escenarios de E2c.** `summary-profile-picture-sources`: la primera versión dio 2 de 3 en iOS (el Cancel se pulsaba con
el selector aún cargando y el toque se perdía); esperando la condición, 10 de 10 en iOS y 3 de 3 en Android.
`conformance-system`, `conformance-swipe-screen`, `auth-update-password` y `evaluations-list-retry`: 3 de 3 en las dos
plataformas. `record-synthetic-term-lifecycle`: 3 de 3 en las dos.

**Pensum guardado tras relanzar (E2c, commit `6ffd3d513`, 2026-10-08 22:50).** Tras `relaunch()` y volver a la pestaña de
pensum, el journal de WireMock no registró ningún `GET /pensums/v4`; el número de corridas observadas no está registrado
(sin fuente). La causa es que la edad del pensum guardado se compara con `currentTimeMillis()` (el reloj del sistema, que
`TUINDICE_E2E_NOW` no mueve) y en una corrida es de segundos, no que el producto no revalide: ver
`e2e/platform/{android,ios}/pensum-stale-cache-revalidation.md`.

**Sondas del driver (etapa de drivers de cierre, base `847f83a58`, commit `73a06139c`, 2026-10-08 16:59).**

- Salida del primer plano en iOS (ZB-13): con Safari abierto por `simctl openurl` sobre la app, el estado cacheado y el árbol
  de la app siguen en «primer plano» durante unos 2,5 s (10 s en corridas anteriores); `app.wait(for: .runningBackground,
  timeout: 0,3)` lo ve unos 0,7 s antes, y con 0,01 a 0,1 s ve lo mismo que la caché. Tras la corrección, durante 0,43 s la
  búsqueda en caché seguía acertando y la búsqueda al frente fallaba; el coste es 0,3 s más por acierto.
- Teclado en Android (ZB-5): `dumpsys input_method` (Android 37, Gboard) tiene una línea `mInputShown=`: `false` antes de abrir,
  `true` con el teclado delante, `false` tras Atrás con el campo aún enfocado. Un volcado pesa unos 1 MB y tarda de 50 a 70 ms.
- Contrato del driver (`--driver-contract`), 10 corridas: Android 10 de 10 (31 sondas) e iOS 10 de 10 (17 sondas). Un fallo
  previo y aislado en Android (`UiDevice.click` devolvió `false`) durante una compilación de iOS; no se repitió.
- Conformidad con `--repeat 10`: Android 250 de 250 (25 escenarios); iOS 24 escenarios, todos con 10 o más pasadas válidas y
  ningún fallo de escenario; `conformance-submit-text-entry` 10 de 11 (una pasada perdida por «simulator degraded: stopped
  serving preferences»).
- `conformance-mock-state`, 3 de 3 en las dos plataformas; sin el `mockState`, la espera falló con «GET /users/v1 was not
  answered with 503 at least 2 times within 20000 ms; it answered, most recent first: 200, 503».

## 4. Etapa de drivers 2 y última tanda en dispositivos (2026-10-09)

Mismo equipo. Las series usan `E2E_MAX_RETRIES=0 --repeat N --survey`. Los registros están en el scratchpad de la sesión
(`r7-*` de la etapa de drivers 2 y `r8-*` de esta tanda); ninguno está versionado.

**Salida de la app del primer plano en iOS, residuo del estado cacheado y de la sonda de 0,3 s (etapa de drivers 2, base
`42a3d780c`).** Con Safari abierto por `simctl openurl` sobre la app, el estado de Safari muestreado cada 0,03 s en un hilo aparte
dice «primer plano» al volver el `openurl`; el estado cacheado de la app y la sonda dicen «no delante» de 2,56 a 2,89 s después
(5 corridas: 2,85; 2,87; 2,70; 2,76; 2,89 s, y 2,56 s con capturas; la sonda dentro de 0,03 s del estado cacheado). La ventaja de
0,7 s de la medición de ZB-13 no se reprodujo. La sonda cuesta 0,3 s por acierto. Esto reemplaza lo dicho sobre la sonda en la sección 3.

**Estado de Safari como fuente del «frente» en iOS (E3, base `99b1b3045`): no funcionó como se decidió.** `isAppFrontNow` =
la app en `runningForeground` y ninguna app externa conocida (`com.apple.mobilesafari`) en `runningForeground`, sin sonda de espera:
`waitBackgrounded` pasó a la primera (414 ms) pero `foreground()` no vio nunca la vuelta y `conformance-foreground` falló 6 de 6 con
«the app could not be brought to the foreground» a los 30 s. En una clase de medición aparte, `XCUIApplication(bundleIdentifier:
"com.apple.mobilesafari").state` leyó `notRunning` (1) durante los 18 s de la corrida aunque la captura mostraba Safari delante
(`simctl openurl`); el estado de Safari que se vio en la etapa de drivers 2 no se reprodujo, y no se averiguó qué distingue las dos
clases. No se commiteó nada de iOS: el código quedó como estaba.

**Coste de los lookups sin la sonda del primer plano, y el estado de Safari como fuente (E4, 2026-10-09, base `3967f130c`).** La sonda
`wait(for: .runningBackground, timeout: 0,3)` se quitó de `waitVisible`, `isVisible`, `isEnabled`, `readText`, `isChecked` y `bounds` (queda en
`isForeground`, `waitBackgrounded`, `foreground` y en la prueba de una ausencia). Media por llamada de lookup en la conformidad de iOS con `--trace`
(25 escenarios × 2 pasadas, las líneas `[driver] <llamada> <µs>`): 457,4 ms antes (464,3 ms la media de las medias por escenario) y 204,5 ms
después (209,0 ms); `waitVisible` 501 → 245 ms, `isVisible` 349 → 141, `isEnabled` 436 → 150, `readText` 546 → 257, `isChecked` 430 → 135,
`bounds` 145 → 74. Residuo que queda: tras salir la app sin esperarlo, un lookup puede acertar con otra app delante durante unos 2,7 s
(2,56 a 2,89 s). Fuente alternativa: con un muestreo cada 30 ms en un hilo aparte, durante `conformance-foreground` (Safari abierto desde la
app), el estado de `XCUIApplication(bundleIdentifier: "com.apple.mobilesafari")` pasó a `runningForeground` de 0,015 a 0,095 s después de volver
el toque (8 de 8 corridas, con el proxy creado antes y con uno nuevo en cada lectura, 2 de ellas con Safari sin arrancar; `wait(for: .runningForeground,
timeout: 0)` dio lo mismo; cada lectura costó 2 ms), mientras el estado cacheado de la app tardó de 0,9 a 2,8 s. Pero ese estado de Safari se queda
en `runningForeground` al menos 8 s después de que la app vuelve (3 de 3): no es una lectura de «Safari delante» sino de «Safari estuvo delante», y sin
estado previo no distingue la ventana de salida de la de regreso, así que `foreground()` no terminaría (lo visto en E3, 6 de 6). No se implementó; la
lectura de `notRunning` de E3 no se reprodujo y no se averiguó qué la causó (esta vez se leyó desde otro hilo). No se probó el `simctl openurl`.

**`conformance-submit-text-entry` y la tecla de acción en iOS (E4).** Los artefactos de los dos fallos de E3 ya no existían (la retención de
`build/e2e/runs` los liberó), así que no se pudo ver el `driver.log` de esos intentos. En la corrida de coste de E4 apareció un fallo de la misma
familia en `conformance-type-replace-after-back` (clave `Search`): «frame unreadable after 2 reads» a los 1,46 s; la primera lectura del snapshot de la tecla
sirvió, la segunda lanzó, y `SettleWatch` lo trató como «la tecla se fue» aunque `exists` la había visto un momento antes. Series sin carga inducida, 30
pasadas de cada escenario (CPU libre al empezar 31 %, con Android corriendo a la vez en la máquina): 30 de 30 en `conformance-submit-text-entry` y 30 de 30 en
`conformance-type-replace-after-back`; la tecla asentó siempre a las 3 lecturas y `submitTextEntry` tardó de 1,83 a 3,21 s (mediana 2,30 s, 60 llamadas). Con
10 procesos `yes` (CPU libre 0 %): 12 pasadas completas de cada uno sin fallos de la tecla, y una pasada de `conformance-submit-text-entry` que agotó los 180 s
sin completar ningún paso (carga extrema; el orquestador paró los `yes` a las 13:41, lo posterior no cuenta como carga inducida). No se llegó a 30 con carga. Cambio:
una lectura fallida de la tecla ya no es «desapareció» (`SettleWatch.feedUnreadable`: cuenta para los límites, reinicia la racha y solo termina como `moving`), y
el error de cada lectura fallida va al `driver.log`. La causa de la lectura fallida no se observó (el texto del error no existía); la próxima aparición lo trae.

**Desplazamiento en Android con carga (E3).** `conformance-scroll` con `OpenKtor` como destino (escenario temporal, restaurado),
serie de 50 con el host cargado por la conformidad de iOS corriendo a la vez, y la duración de cada swipe leída de una línea
`swipe: N events in X ms` que se añadió al `driver.log` en ambas variantes. Con `UiDevice.swipe` (80 eventos síncronos): 1 fallo de
50 (`attempt-1-r15`); 299 swipes de 2701 a 10 554 ms (mediana 3241, media 3357, p90 3963) para 400 ms nominales; 5,98 swipes por
intento. Con 12 eventos y tiempos explícitos (`downTime`/`eventTime` espaciados sobre 400 ms, el último esperado en la inyección):
0 fallos de 50, 250 swipes de 400 a 415 ms (mediana 403), 5,00 por intento, y el paso de desplazamiento pasó de unos 11 s a 1,2 s.
`conformance-swipe-screen`, `conformance-scroll` (con `OpenKoin`), `conformance-double-tap-effect` y `pensum-smoke` ×10, 10 de 10
cada uno. **Pero el cambio alteró el efecto del gesto:** con la dinámica nominal, `scrollUntilVisible(record_enrollment_proof_button)`
no terminó en 10 de 24 intentos de `--tag enrollmentproof --repeat 3` (el botón flotante del informe, al 80,7 % de la altura, nunca
quedó «asentado» en 20 s, tras 35 swipes), y con el driver anterior los mismos 24 pasaron. El cambio se revirtió (`14d9993b5` revierte
`1f746b0ad`); queda para decisión.

**Hoja de compartir en iOS (etapa de drivers 2, base `42a3d780c`).** Con la hoja abierta, `tapAtScreen(0,5; 0,3)` sacó la app a
Safari 3 de 3. Tres configuraciones de presentación del producto (`IosShareTextHandler`), una a la vez y recompilando, con el mismo
toque: popover con `sourceView = topController.view` y `passthroughViews = []`: el defecto sigue (con captura, la app no está
delante); `modalPresentationStyle = PageSheet`: 3 de 3 igual; `OverFullScreen`: 3 de 3 igual. El aspecto de la hoja se comparó
con captura solo en la primera. No se probó `sourceRect`. Ver `e2e/platform/ios/share-sheet-touch-through.md`.

**Disparadores de iOS «Contacto» y «Reportar un error» (etapa de drivers 2).** Con `waitBackgrounded` puesto temporalmente en cada
uno, 3 corridas y 20 s cada una: la app siguió delante 3 de 3 en el correo y 3 de 3 en el reporte.

**Página web que se desplaza (E3).** `mocks/__files/e2e/terms.html` con cinco cláusulas y una última línea; con 8 cláusulas un solo
swipe de Android no llegaba al final (capturas del paso fallido). Con 5: `about-internal-browser-links` 3 de 3 en Android y 3 de 3
en iOS, afirmando que la última línea no está visible al cargar y sí tras un swipe. En iOS, con `isNativeAccessibilityEnabled = false`
(producto, temporal, restaurado) la página se desplaza igual que con `true`: cuatro capturas (`r8-ios-web-{true,false}-{noswipe,swipe}-capture.png`)
muestran el mismo estado antes del swipe (título y cláusulas 1 a 4) y el mismo después (cláusulas 3 a 5 y la última línea). Android registró
un `GET /favicon.ico` sin stub con la página alta; se añadió `browser-favicon.json`.

**Valor por defecto de `isInteractive` (Compose Multiplatform 1.12.0).** En `ui-iosSimulatorArm64Main-1.12.0.klib`
(`klib dump-ir`), `UIKitInteropProperties(isInteractive: Boolean, isNativeAccessibilityEnabled: Boolean)` no tiene valores por
defecto; el constructor con `interactionMode: UIKitInteropInteractionMode? = Cooperative` y `isNativeAccessibilityEnabled = false`
es el que los tiene, y `Cooperative` equivale a `isInteractive = true`. Pasar solo `isNativeAccessibilityEnabled = true` compila contra
ese segundo constructor y da el mismo comportamiento interactivo. El producto no se tocó.

**Escenarios nuevos y tocados (E3), N de N por plataforma.** Android y iOS, 3 de 3: `about-platform-edge-triggers` (con la hoja vista antes de
cerrarla), `--tag enrollmentproof` (8 escenarios × 3, incluidos el botón positivo de la copia guardada y
`enrollmentproof-saved-copy-gone-after-sign-out`), `auth-login-invalid` y `auth-login-disabled` (con `waitGone(snackbar, Probe)`),
`maincore-browser-load-failed-retry` (reinicio de la conexión de WireMock sobre `privacy.html`, con `mockState`), `conformance-submit-search`,
`conformance-system`, `evaluations-edit-submit`, `evaluations-swipe-delete`, `record-attempt-overrides` y `record-synthetic-term-lifecycle`
(con `Authorization: Bearer` exigido en los siete mappings de `urlPathPattern`). Rojos: repuesto el snackbar del rechazo
(producto, temporal), `auth-login-invalid` y `auth-login-disabled` fallaron en `waitGone(snackbar)` en las dos plataformas; con el
borrado de `clearSessionMemory()` anulado (producto, temporal), `enrollmentproof-saved-copy-gone-after-sign-out` falló en iOS en
`WaitVisible(snackbar)` porque se ofreció la copia. `ReportBug`: 0 de 10 en Android, 3 de 3 «sigue delante» en iOS.

## 5. El swipe de 12 eventos y el botón flotante del comprobante (E4, Android, 2026-10-09)

**Síntoma.** Con el swipe de 12 eventos con tiempos (commit `f5ec4ca43`), `scrollUntilVisible(RecordUiTags.EnrollmentProofButton)`
no terminaba («did not scroll into view within 20000 ms», ~35 swipes): 10 fallos de 24 en `--tag enrollmentproof --repeat 3`.

**Causa, con datos.** Instrumentando cada vuelta del sondeo (límites del elemento y de la pantalla, visibilidad, rama de `placement`) se vio:
- El botón es flotante y no se mueve: siempre `[1104,2332][1272,2500]` de una pantalla de 1344x2992 (centro al 80,7 %, fuera de la banda
  15 %–80 % por poco). Ni se oculta ni cambia de límites.
- `isVisible` daba `true` en todas las vueltas, pero `bounds(q)` daba `null` en 3 de cada 4 lecturas tras un swipe. Con trazas en
  `ElementProber.attempt`: cada `null` era un `StaleObjectException` al leer `visibleBounds` (el árbol de accesibilidad cambia justo después
  del swipe). El patrón fue cíclico: lectura buena, tres `StaleObjectException`, lectura buena.
- El motor trataba «visible sin posición» como `HIDDEN`, volvía a deslizar y borraba `beforeSwipe`. La regla «no se movió» (`isStill`)
  necesita dos lecturas buenas seguidas separadas por un swipe y con esa secuencia no se daba nunca.
- El swipe lento anterior no tropezaba con esto porque el árbol ya estaba quieto cuando se leía.

**Corrección** (`ScrollEngine`): un elemento visible cuya posición no se puede leer es `UNREAD`; el motor espera una vuelta del sondeo sin
deslizar y conserva dónde estaba antes del último swipe. Es una espera de condición con el plazo del paso; si vence, el fallo dice
«it was on screen but its position could not be read». Rojo y verde en `StepKindsTest` (`FakeDriver` con `unreadableReadsAfterSwipe`).

**Series en Android tras la corrección.** `--tag enrollmentproof --repeat 3`: 24/24 (antes 14/24). Los 22 escenarios restantes que usan
`scrollUntilVisible`, `swipe` o `swipeScreen`, ×3: 66/66. `conformance-scroll` con «Ktor» ×50: 50/50 (el escenario temporal se restauró).
Conformidad completa ×10: 269/270; el fallo, `conformance-system` repetición 7, `WaitVisible(chooser_container)` a los 10 s con el tap
de 6,5 s, con la máquina saturada por la serie de iOS de la otra persona (0 % de CPU libre en esa franja); sin relación con el swipe.
`DriverContract` ×10: 7/10; los tres fallos son de teclado/escritura (`the keyboard must show` dos veces, y `long-secure-typing`) con la misma carga.

**Duración de los swipes** (`swipe: N events in X ms (nominal 400 ms)` del `driver.log`):

| Serie | Swipes | Mediana | Máximo |
|---|---|---|---|
| Driver anterior, 80 eventos, carga de iOS (E3) | 299 | 3241 ms | 10 554 ms |
| 12 eventos con tiempos, `conformance-scroll` «Ktor» ×50 | 200 | 402 ms | 917 ms |
| 12 eventos con tiempos, conformidad ×10 (logs conservados) | 80 | 504,5 ms | 610 ms |

## 6. Lo que no se midió

- La tasa de aprobación de iOS y de Android con carga baja y sostenida sobre el catálogo completo con el harness nuevo.
- El efecto de cada cambio de herramienta de agosto a septiembre de 2026 por separado.
- El origen de la carga a partir de las 10:00 del 2026-10-06.
- El umbral de 30 escenarios verdes entre dos recuperaciones del simulador de iOS degradado: es una decisión del harness,
  sin medición que lo respalde.
- Los umbrales del chequeo de entorno (`e2e.py env-check`) son provisionales hasta compararlos con los datos de
  `e2e-profile.py --compare`.
