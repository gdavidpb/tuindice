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
- Pantallas con entrada de texto aplican `imePadding()` en su contenedor raíz; `ConfirmationDialog` ya lo aplica para todos los bottom sheets.

## Terminología

- Borrar datos: **"Eliminar"** (no "Remover").
- Confirmar: **"Aceptar"** (no "Confirmar").
- Dominio: "materia", "nota", "trimestre".
