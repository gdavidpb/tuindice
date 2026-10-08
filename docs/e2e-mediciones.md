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

## 3. Lo que no se midió

- La tasa de aprobación de iOS y de Android con carga baja y sostenida sobre el catálogo completo con el harness nuevo.
- El efecto de cada cambio de herramienta de agosto a septiembre de 2026 por separado.
- El origen de la carga a partir de las 10:00 del 2026-10-06.
- El umbral de 30 escenarios verdes entre dos recuperaciones del simulador de iOS degradado: es una decisión del harness,
  sin medición que lo respalde.
- Los umbrales del chequeo de entorno (`e2e.py env-check`) son provisionales hasta compararlos con los datos de
  `e2e-profile.py --compare`.
