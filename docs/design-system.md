# Design system

Tokens y convenciones de UI compartidos. La fuente de verdad vive en `base/ui/style` y `maincore/ui/theme`; los módulos de feature no deben redefinir estos valores.

## Tokens

| Token | Archivo | Contenido |
| --- | --- | --- |
| Colores de tema | `maincore/.../ui/theme/TuIndiceColorScheme.kt` | Esquemas Material3 light/dark. `onPrimary` es oscuro en ambos modos (el primario amarillo no soporta texto blanco). |
| Colores académicos | `base/.../ui/style/AcademicStatusColors.kt` | Estados de materia (`approved`, `available`, `blocked`), semáforos (`success`, `warning`) y bandas de carga. Único lugar donde viven estos hex. |
| Tipografía | `maincore/.../ui/theme/TuIndiceTypography.kt` | Escala M3 completa. `titleMedium` y `labelLarge` ya son SemiBold: no agregar `fontWeight = SemiBold` en los call sites que los usan. `FontWeight.Black` no se usa en la app; el énfasis máximo es `Bold`. |
| Radios | `base/.../ui/style/TuIndiceRadius.kt` | Usar siempre el token (`Small`, `Medium`, `Card`, `Large`, `XLarge`, `Full`); no escribir `RoundedCornerShape(X.dp)` con literales. |
| Espaciado | `base/.../ui/style/TuIndiceSpacing.kt` | `Screen` (16.dp) es el padding horizontal canónico de contenido de pantalla; `Dialog` (24.dp) el de bottom sheets/diálogos. |
| Alphas | `base/.../ui/style/TuIndiceAlpha.kt` | `Disabled` (0.38), `Muted` (0.55), `Deemphasis` (0.72), `SurfaceTint` (0.12), `BorderSubtle` (0.18), `BorderStrong` (0.9). Para texto secundario usar `Deemphasis`, no valores ad hoc. |
| Animación | `base/.../ui/style/TuIndiceAnimation.kt` | `StandardMillis` (300) para transiciones de estado y crossfades. Excepciones documentadas: las constantes del canvas de pensum (`PensumGraphTokens.kt`, 180–320 ms, ajustadas al gesto) y la rotación del ícono de sync en summary (900 ms, es una rotación continua, no una transición). |
| Tema claro/oscuro | `base/.../ui/style/TuIndiceDarkTheme.kt` | `TuIndiceDarkTheme.isDark()` sigue al tema aplicado por `TuIndiceSharedTheme` (CompositionLocal) con fallback al sistema. No usar `isSystemInDarkTheme()` directo en features: no seguiría un futuro selector manual de tema. |

## Semántica de color

- **Amarillo primario** = "en curso" / acción de marca. Nunca "aprobado".
- **Verde `AcademicStatusColors.approved()`** = aprobado, en todas las pantallas (summary, record, pensum, subjects).
- **`colorScheme.error`** = reprobado / acciones destructivas.
- **Gris `available()` / `blocked()`** = estados neutros de materia.
- **Ícono de estado de sync** (summary): dos categorías. *Problema* (`SyncProblem`, tinte y halo `error`): servicio caído, fallo, credenciales vencidas o faltantes, acceso denegado al expediente, fuente no disponible. *Informativo* (`Outlined.Info`, tinte y halo `primary`, botón habilitado): inscripción no vigente, inscripción anulada, nuevo ingreso sin expediente. Algo que la universidad reporta y que no es una falla de la app no se pinta como error, pero sí pide atención: lleva el mismo halo pulsante que un problema, en el color de acento. En ambas categorías el halo deja de pulsar cuando el usuario abre el detalle y solo vuelve cuando cambia lo anunciado (p. ej. de anulada provisional a definitiva, o de informativo a problema); una sincronización que repite el mismo anuncio no lo rearma.
- `tertiary` diverge de hue entre modos (teal en light, beige en dark) de forma intencional: no usarlo para nueva semántica sin revisar ambos temas.

## Iconografía

- Estilo por defecto: **Outlined**. `Filled` se reserva para estado seleccionado/activo (tabs inferiores, check de selección en pickers, check de estado aprobado).
- Borrar = `Icons.Outlined.DeleteOutline` (no `Delete` ni `Default.Delete`).
- Icono accionable sin texto visible ⇒ `contentDescription` con `stringResource` (claves `a11y_*`); icono decorativo o acompañado de texto ⇒ `null`.

## Botones

- Acción primaria: `Button`.
- Acción secundaria: `OutlinedButton`.
- Acción terciaria/inline: `TextButton`.
- Acción destructiva: colores de `error`/`errorContainer` sobre el componente que corresponda.
- Spinners dentro de botones usan `LocalContentColor.current`, nunca un color literal.

## Estados de pantalla

- **Loading**: `CircularProgressIndicator` centrado. Excepción: pensum conserva su loading ilustrado (`PensumLoadingView`).
- **Error**: `ErrorView` de base (ilustración + título + mensaje + reintentar).
- **Empty**: `EmptyView` de base (ilustración + título + mensaje + CTA opcional). Summary no tiene estado empty porque un usuario autenticado siempre tiene resumen; su branch `Idle` es transitorio y pinta fondo opaco.
- **Acción secundaria en estados ilustrados**: `IllustratedMessageView` acepta `isActionOutlined` para pintar la acción como `OutlinedButton` y `isActionEnabled` para deshabilitarla mientras algo sincroniza. Se usa cuando reintentar no es la salida natural (p. ej. nuevo ingreso sin expediente: "Reintentar" en el borde, no la acción primaria). `EmptyView` y `ErrorView` no exponen esos parámetros: un estado que los necesite usa `IllustratedMessageView` directamente.
- Pantallas con entrada de texto aplican `imePadding()` en su contenedor raíz; `ConfirmationDialog` ya lo aplica para todos los bottom sheets.

## Avisos en pantalla

`NoticeView` (`base/.../ui/view/NoticeView.kt`) es el aviso informativo que convive con el contenido: no bloquea ni pide acción. Fila con fondo `surfaceVariant`, borde 1dp `outlineVariant`, radio `TuIndiceRadius.Medium`, padding 12/10, ícono de 16dp (`Outlined.Info` para explicar una situación, `Outlined.Schedule` para datos desactualizados) en `onSurfaceVariant`, título opcional `titleSmall` y mensaje `bodySmall`. Nunca usa `error` ni lleva acción, y se alinea con `TuIndiceSpacing.Screen`; se muestra con `AnimatedVisibility` (parámetro `visible`): quien lo usa lo deja siempre en la composición, alterna `visible` y conserva el último texto con `rememberLastNonNull`, para que entre y salga animado en vez de hacer saltar el contenido. Deja `TuIndiceSpacing.Medium` debajo, y arriba solo cuando lo que tiene encima no trae ya su propio espacio (en Evaluaciones la pantalla ya separa su contenido de la barra superior, así que el aviso no suma margen arriba). Un solo aviso a la vez por pantalla. Se usa para la inscripción anulada en Record (provisional y definitiva) y en Evaluaciones (provisional; la definitiva es su estado vacío), y para el dato viejo de inscripción en Record; el copy compartido de la anulada vive en `base` (`EnrollmentAnnulmentTexts`).

**Horario** (Record): es una hoja inferior sobre Record, no una pestaña de la página del trimestre ni una pantalla propia: son pocas materias, se consultan de un vistazo y la hoja mide lo que mide su contenido. La barra superior de Record suma un tercer ícono `Outlined.CalendarViewWeek` ("Ver horario") solo cuando el trimestre seleccionado es el actual y trae horario; la página del trimestre conserva solo las notas (el aviso de anulada se queda en ella y no se repite en Horario). La hoja (`ConfirmationDialog` de base, sin botones; se cierra deslizando) lleva el título "Horario" en `titleLarge` con el nombre del trimestre justo debajo en `bodyMedium`/`onSurfaceVariant`, a su derecha, en la misma fila, un `SingleChoiceSegmentedButtonRow` compacto de dos íconos (`Outlined.TableRows` y `Outlined.CalendarViewWeek`, que se leen "Tabla" y "Semana"; el elegido va en `secondaryContainer`, el tono del indicador de la barra inferior; abre en Tabla y recuerda la última vista) y los márgenes `TuIndiceSpacing.Dialog` de toda hoja. Abre completa; una grilla larga se desplaza dentro. **Tabla**: una fila por materia sobre `surfaceContainerHigh` (un tono por encima de la hoja) con esquinas `TuIndiceRadius.Small`, separadas `TuIndiceSpacing.Small`; a la izquierda el código en su `SubjectCodeChip` (variante `Dense`, el mismo chip de color del resto de la app) y debajo "Sec. 1 · MYS-116" (`labelSmall`, `onSurfaceVariant`, el aula solo si todas las reuniones coinciden), luego una columna de 34dp por día (Lun a Vie; Sáb y Dom solo si algo se reúne) con el rango de bloques ("1-2", "3") centrado en `labelMedium`. Una reunión que se solapa con la de otra materia el mismo día usa el tono de alerta (`AcademicStatusColors.warning()`, relleno `SurfaceTint`, borde `BorderStrong` del grosor de `ScheduleClashDefaults.BorderWidth` (2dp, el mismo en la tabla y en la grilla), texto `warning`). El borde es lo único que dice "choque": bajo las filas no cuelga ningún texto, ni de la app ni el error de inscripción que manda la universidad (ese sigue en la tarjeta de la materia del Informe, tal cual llega); la descripción accesible de la reunión sí termina en "Choque de horario". El choque sale del cálculo de carriles de la app, así que marca todas las materias implicadas, y uno que la universidad reporta pero la app no calcula (p. ej. contra una materia sin horario) no aparece aquí. Una materia que no trae horario dice "Sin horario" sobre las columnas de días; con siete columnas la tabla desplaza en horizontal en vez de comprimir. **Semana**: la grilla por bloques con su línea "Sin horario: …"; cada celda usa los colores del chip de su materia (`CourseCodeColorGenerator`), así una materia se reconoce por color en toda la app, y un choque conserva ese color y suma el mismo borde en el tono de alerta que en la tabla (su descripción accesible termina en "Choque de horario"). Cada reunión mide 72dp de ancho fijo, lo que cabe un código y su aula completos; un día con choque se ensancha para que cada materia conserve su celda completa. La grilla se desplaza en ambos sentidos dentro de la hoja en vez de comprimirse: los nombres de los días quedan fijos arriba, los números de bloque fijos a la izquierda y la línea "Sin horario" a la vista debajo. Las dos vistas salen del mismo cálculo de carriles (`ScheduleItem`), así que un choque se ve igual en ambas. **Hoy y ahora**: en las dos vistas el día de hoy se dice como la app dice "el actual" en el selector de trimestres: su nombre en negrita `onSurface` con un punto pequeño del acento (`primary`) debajo, sin pastilla ni tinte en la columna (nunca texto en color de acento: el amarillo no se lee sobre la hoja clara); todas las cabeceras reservan el alto del punto para quedar a nivel, y el lector de pantalla dice "lunes, hoy". Si hoy no es uno de los días que la vista muestra (p. ej. un domingo sin clases) no se resalta nada. En **Semana**, una línea horizontal de 2dp con un punto de 8dp al inicio cruza la columna de hoy a la altura de la hora actual, en `onPrimaryContainer` (el tono del acento que se lee en ambos temas), y se mueve sola con el reloj, minuto a minuto; no se dibuja si la hora cae fuera de los bloques de la grilla. En **Tabla**, que no tiene eje de tiempo, el símil es la clase en curso: la celda de hoy cuyo rango de bloques contiene el bloque actual se rellena con `primary` y su texto va en `onPrimary` y negrita. Una celda puede estar en curso y en choque a la vez: el relleno dice "en curso" y el borde de alerta sigue diciendo "choque". El lector de pantalla dice "en curso" en la reunión. La equivalencia bloque → hora (bloque 1 a las 7:30, 60 minutos por bloque, hora local del dispositivo) es una **suposición no confirmada por la universidad** y vive en un solo sitio, `record/domain/model/ScheduleBlockClock.kt`: cambiarla ahí mueve la línea y la clase en curso.

## Terminología

- Borrar datos: **"Eliminar"** (no "Remover").
- Confirmar: **"Aceptar"** (no "Confirmar").
- Dominio: "materia", "nota", "trimestre".
