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
- **Ícono de estado de sync** (summary): dos categorías. *Problema* (`SyncProblem`, tinte y halo `error`): servicio caído, fallo, credenciales vencidas o faltantes, acceso denegado al expediente, fuente no disponible. *Informativo* (`Outlined.Info`, tinte `AcademicStatusColors.available()`, **sin halo**, botón habilitado): inscripción no vigente, inscripción anulada, nuevo ingreso sin expediente. Algo que la universidad reporta y que no es una falla de la app no se pinta como error.
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

**Horario** (Record): es una pantalla propia, no una pestaña de la página del trimestre. La barra superior de Record suma un tercer ícono `Outlined.CalendarViewWeek` ("Ver horario") solo cuando el trimestre seleccionado es el actual y trae horario; la página del trimestre conserva solo las notas (el aviso de anulada se queda en ella y no se repite en Horario). La pantalla lleva flecha atrás, título "Horario", el nombre del trimestre en `bodyMedium`/`onSurfaceVariant`, un `SingleChoiceSegmentedButtonRow` "Tabla | Semana" (mismos colores que el de crear trimestre; abre en Tabla y recuerda la última vista) y márgenes `TuIndiceSpacing.Screen`. **Tabla**: una fila por materia sobre `surfaceContainerLow` con esquinas `TuIndiceRadius.Small`, separadas `TuIndiceSpacing.Small`; a la izquierda el código (`labelLarge`) y "Sec. 1 · MYS-116" (`labelSmall`, `onSurfaceVariant`, el aula solo si todas las reuniones coinciden), luego una columna de 34dp por día (Lun a Vie; Sáb y Dom solo si algo se reúne) con el rango de bloques ("1-2", "3") centrado en `labelMedium`. Una reunión que se solapa con la de otra materia el mismo día usa el tono de alerta (`AcademicStatusColors.warning()`, relleno `SurfaceTint`, borde 1dp `BorderStrong`, texto `warning`), y el error de inscripción cuelga bajo la fila como ícono + texto en `labelMedium`. Sin horario, la fila dice "Por convenir" sobre las columnas de días; con siete columnas la tabla desplaza en horizontal en vez de comprimir. **Semana**: la grilla por bloques con su línea "Por convenir: …". Las dos vistas salen del mismo cálculo de carriles (`ScheduleItem`), así que un choque se ve igual en ambas.

## Terminología

- Borrar datos: **"Eliminar"** (no "Remover").
- Confirmar: **"Aceptar"** (no "Confirmar").
- Dominio: "materia", "nota", "trimestre".
