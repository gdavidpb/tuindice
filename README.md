# TuIndice

Aplicación multiplataforma Kotlin para Android e iOS.

El proyecto está organizado como KMP con una sola base común para arquitectura, recursos, DI y tests compartidos, más un
host Android en `app` y un host iOS en `iosApp`.

## Módulos del workspace

- `base`: contratos compartidos, helpers base, logging, errores, repositorios de infraestructura y piezas UI
  reutilizables.
- `persistence`: Room KMP y acceso a base de datos compartida.
- `about`: información de app, enlaces y soporte.
- `login`: autenticación, sesión y actualización de credenciales.
- `summary`: resumen del perfil y foto de perfil.
- `record`: historial académico y cálculos de índice.
- `evaluations`: evaluaciones, filtros, picker de fecha y picker de nota.
- `enrollmentproof`: constancia de inscripción y apertura de archivos.
- `maincore`: navegación compartida, bootstrap común de Koin y superficie principal de la app.
- `app`: host Android.
- `iosApp`: host iOS.
- `testkit`: helpers y dobles compartidos para pruebas.

## Estado actual del proyecto

- La UI y los recursos de texto se mantienen en `commonMain` con `composeResources`.
- La DI usa `commonModule`, un solo módulo por feature y un solo módulo de plataforma por SO.
- El arranque de Koin es simétrico entre plataformas usando `startAppKoin(...)`.
- iOS inyecta capacidades/runtime host a traves de `iOSContext(...)`.
- Los settings no sensibles usan `multiplatform-settings`.
- Lo sensible usa `KSafe`.
- `RemoteConfig` se mantiene como configuración remota directa, sin cache local en settings.
- Existe un patron de smoke tests de Koin por modulo para validar wiring y entry points públicos.

## Arquitectura

La regla base es simple:

- `presentation` orquesta UI y estado.
- `domain` define reglas de negocio.
- `data` implementa acceso a datos.
- `di` conecta dependencias sin mezclar responsabilidades.

## Dependencias entre módulos

Regla general:

- Las dependencias deben apuntar hacia módulos base o infraestructura compartida.
- Evitar dependencias cruzadas entre features.

Dependencias actuales:

- `base`: sin dependencias de proyecto.
- `persistence`: depende de `:base`.
- `about`: depende de `:base`.
- `login`: depende de `:base`.
- `summary`: depende de `:base`, `:persistence`.
- `record`: depende de `:base`, `:persistence`.
- `enrollmentproof`: depende de `:base`, `:persistence`.
- `evaluations`: depende de `:base`, `:persistence`, `:record`.
- `maincore`: depende de `:base`, `:persistence` y todas las features.
- `app`: host Android; ensambla `maincore`.

Acuerdo de límites:

- No agregar nuevas dependencias feature -> feature.
- La excepción `evaluations -> record` se considera legado controlado.
- Cualquier nueva excepción requiere acuerdo explícito antes de implementarse.

## Capas por feature

Cada feature en `commonMain` debe conservar esta estructura:

- `presentation/`
- `domain/`
- `data/`
- `di/`
- `ui/`

### Presentation

Responsable de estado, acciones de UI y efectos.

Ubicación típica:

- `contract/*`: `State`, `Action`, `Effect`.
- `viewmodel/*`: clases que extienden `BaseViewModel`.
- `action/*`: `ActionProcessor` por acción relevante.
- `route/*`: traducción de `Effect` a navegación o side effects UI.
- `navigation/*`: builders de `NavGraphBuilder`.
- `composeResources/values/*`: strings y recursos de UI del feature.

Reglas:

- `ViewModel` no llama infraestructura directamente; delega en processors y use cases.
- `Route` no contiene lógica de negocio.
- `Screen` y `View` no acceden a repositorios.
- No hardcodear textos visibles en `presentation`; usar recursos.

### Domain

Responsable de negocio puro.

Ubicación típica:

- `model/*`
- `repository/*`
- `usecase/*`
- `usecase/validator/*`
- `usecase/exceptionhandler/*`

Reglas:

- `UseCase` depende de interfaces, no de implementaciones.
- Validaciones en `ParamsValidator`.
- Traducción de errores en `ExceptionHandler`.
- Cada feature expone una interfaz de fachada de negocio en `domain/repository`.
- Si la capa `data` necesita coordinar múltiples orígenes internos, ese contrato se modela como interfaz en
  `data/repository` con sufijo `DataRepository`.
- Si la capa `data` necesita abstraer un origen hoja interno como API, DB, settings o bridge de plataforma,
  ese contrato vive en `data/contract` y la implementación concreta queda en `data/source`.

### Data

Responsable de integraciones y persistencia.

Ubicación típica:

- `data/repository/*`: interfaces internas de la capa `data` para coordinación multi-origen.
- `data/contract/*`: contratos internos de orígenes hoja como API, DB, settings y bridges de plataforma.
- `data/source/*`: implementaciones concretas de contratos de `domain/repository` y `data/repository`.
- `data/mapper/*`: mapeos entre modelos remotos/locales y dominio.

Reglas:

- `domain` nunca importa clases de `data`.
- Las interfaces de dominio viven en `domain/repository` y usan sufijo `Repository`.
- Los contratos internos de la capa `data` viven en `data/repository` y usan sufijo `DataRepository`.
- Los contratos internos de orígenes hoja viven en `data/contract` y pueden conservar nombres orientados al origen,
  por ejemplo `RemoteDataSource`, `DatabaseDataSource` o `PushTokenDataSource`.
- Las implementaciones concretas viven en `data/source` y usan sufijo `DataSource`.
- Un `*DataSource` puede implementar directamente un contrato de `domain/repository`, `data/repository` o `data/contract`.
- `data/repository` no debe contener DTOs, modelos remotos, mutaciones, resolvers ni clases concretas.
- `data/contract` no debe contener implementaciones concretas, DTOs ni helpers de mapping.
- Los helpers y modelos auxiliares deben vivir en paquetes explícitos como `data/model`, `data/mutation`,
  `data/resolver`, `data/source/api`, `data/source/database` o equivalentes.
- Si una feature necesita leer contratos compartidos de otro módulo, debe hacerlo mediante adapters propios de esa
  feature.

### DI

Responsable del wiring.

Superficie publica actual:

- `commonModule`
- `<feature>Module`
- `androidPlatformModule`
- `iosPlatformModule`

Reglas:

- `di` solo registra dependencias.
- `di` no implementa adaptadores concretos.
- En `commonModule`, preferir `single` para servicios compartidos de runtime, repositorios de infraestructura y
  dependencias con estado/memoria/flows/mutexes; reservar `factory` para objetos de vida corta o sin identidad
  compartida.
- `featureModules()` agrega solo módulos de feature.
- `commonModules()` agrega `commonModule` más los módulos de feature.
- El bootstrap común entra por `startAppKoin(...)`.
- Cada plataforma aporta un `PlatformKoinBootstrap`.
- No crear `*AndroidModule` o `*IosModule` por feature.
- Los bindings específicos de plataforma de una feature viven en el módulo de plataforma correspondiente.
- `persistence` expone helpers/factories; el registro de storage por plataforma se invoca desde `androidPlatformModule`
  e `iosPlatformModule`.

## Convenciones de Koin

### Naming

- Infra compartida en `commonMain`: `commonModule`.
- Módulos de feature: `mainModule`, `loginModule`, `aboutModule`, etc.
- Wiring de plataforma: `androidPlatformModule` e `iosPlatformModule`.

### Archivos

- Infra compartida: `CommonModule.kt`.
- Modulo de feature: `FeatureModule.kt`.
- Wiring de plataforma: `AndroidPlatformModule.kt`, `IosPlatformModule.kt`.

### Declaración

- Usar `val ... = module { ... }` por defecto.
- Usar `fun ...(...): Module = module { ... }` solo si realmente necesita parámetros runtime.

### Bootstrap

- Android arranca Koin via `startAndroidAppKoin(...)`.
- iOS arranca Koin via `startIosKoin(...)`.
- Ambos terminan usando `startAppKoin(...)`.
- iOS registra su runtime host con `iOSContext(...)`.

### Patrones no permitidos

- `*CoreModule`.
- Módulos vacíos `module {}`.
- Módulos de plataforma por feature.

## Limites KMP

`commonMain` debe mantenerse portable.

No permitido en `commonMain`:

- imports `android.*`.
- imports `java.*` salvo donde el source set lo permita y no sea común.
- `BuildConfig`.
- `InputStream`, `OutputStream`, `Reader`, `Writer`.
- `koinViewModel` o `koinNavViewModel`.

Reglas adicionales:

- La navegación compartida vive en `commonMain`.
- No usar DSL Android de ViewModel dentro de source sets KMP.
- Usar `composeResources` para recursos visibles de UI.
- Evitar `expect/actual` salvo casos muy justificados.

## Flujo estándar

1. `Screen` dispara una acción en `ViewModel`.
2. `ViewModel` delega en `ActionProcessor`.
3. `ActionProcessor` ejecuta un `UseCase`.
4. `UseCase` usa interfaces de repositorio de `domain`.
5. `ActionProcessor` mapea el resultado a `Mutation<State>` y `Effect`.
6. `Route` consume `Effect` y lo traduce a navegación o side effects UI.

## Patron de smoke tests de Koin

Cada módulo Koin público debe tener una prueba de inicialización que valide su wiring mínimo.

### Módulos de feature

- Cada `<feature>Module` debe tener un `<Feature>ModuleKoinSmokeTest` en `commonTest`.
- La prueba debe iniciar Koin con el módulo del feature y los overrides mínimos de contratos externos.
- La prueba debe resolver los entry points públicos del feature, normalmente sus `ViewModel`.

### Bootstrap de plataforma

- `maincore` mantiene una prueba de humo del arranque real de iOS en `iosTest`.
- Esa prueba debe arrancar Koin a traves de `startAppKoin(...)` con `IosKoinBootstrap`.

### Helpers compartidos

En `testkit`:

- `withKoinSmokeTest(...)`
- `withStartedKoin(...)`
- `assertResolves(...)`

Objetivo:

- detectar dependencias faltantes al cambiar constructores,
- validar el wiring mínimo de cada módulo,
- y cubrir el arranque real de iOS para detectar regresiones host/plataforma.

## Checklist para cambios nuevos

- Se respeta separación `presentation/domain/data/di`.
- No se introducen nuevas dependencias cruzadas entre features.
- La navegación del feature vive en `commonMain`.
- `ViewModel` usa `ActionProcessor`.
- Los casos de uso tienen validator y exception handler cuando aplica.
- Las interfaces viven en `domain` y las implementaciones en `data`.
- Koin se registra en el módulo correcto.
- Los textos visibles van a recursos comunes.
- Si agregas o cambias wiring de Koin, agregas o actualizas el smoke test del modulo afectado.
- Ejecutas las verificaciones necesarias antes de cerrar el cambio.

## Verificaciones útiles

Ejemplos de comandos usados habitualmente:

```bash
./gradlew --continue --console=plain :app:compileDebugKotlin
./gradlew --continue --console=plain :maincore:linkDebugFrameworkIosSimulatorArm64
./gradlew --continue --console=plain :evaluations:allTests
./gradlew --continue --console=plain :maincore:iosSimulatorArm64Test --tests '*IosAppKoinSmokeTest*'
```

## Política de evolución

Si una necesidad de producto rompe estas reglas:

- pausar la implementación,
- acordar el cambio de límite,
- y actualizar este README en el mismo cambio técnico.
