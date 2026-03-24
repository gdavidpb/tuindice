# TuIndice

Aplicacion multiplataforma Kotlin para Android e iOS.

El proyecto esta organizado como KMP con una sola base comun para arquitectura, recursos, DI y tests compartidos, mas un host Android en `app` y un host iOS en `iosApp`.

## Modulos del workspace

- `base`: contratos compartidos, helpers base, logging, errores, repositorios de infraestructura y piezas UI reutilizables.
- `persistence`: Room KMP y acceso a base de datos compartida.
- `about`: informacion de app, enlaces y soporte.
- `login`: autenticacion, sesion y actualizacion de credenciales.
- `summary`: resumen del perfil y foto de perfil.
- `record`: historial academico y calculos de indice.
- `evaluations`: evaluaciones, filtros, picker de fecha y picker de nota.
- `enrollmentproof`: constancia de inscripcion y apertura de archivos.
- `maincore`: navegacion compartida, bootstrap comun de Koin y superficie principal de la app.
- `app`: host Android.
- `iosApp`: host iOS.
- `testkit`: helpers y dobles compartidos para pruebas.

## Estado actual del proyecto

- La UI y los recursos de texto se mantienen en `commonMain` con `composeResources`.
- La DI usa `commonModule`, `persistenceModule`, un solo modulo por feature y un solo modulo de plataforma por SO.
- El arranque de Koin es simetrico entre plataformas usando `startAppKoin(...)`.
- iOS inyecta capacidades/runtime host a traves de `iOSContext(...)`.
- Los settings no sensibles usan `multiplatform-settings`.
- Lo sensible usa `KSafe`.
- `RemoteConfig` se mantiene como configuracion remota directa, sin cache local en settings.
- Existe un patron de smoke tests de Koin por modulo para validar wiring y entry points publicos.

## Arquitectura

La regla base es simple:

- `presentation` orquesta UI y estado.
- `domain` define reglas de negocio.
- `data` implementa acceso a datos.
- `di` conecta dependencias sin mezclar responsabilidades.

## Dependencias entre modulos

Regla general:

- Las dependencias deben apuntar hacia modulos base o infraestructura compartida.
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

Acuerdo de limites:

- No agregar nuevas dependencias feature -> feature.
- La excepcion `evaluations -> record` se considera legado controlado.
- Cualquier nueva excepcion requiere acuerdo explicito antes de implementarse.

## Capas por feature

Cada feature en `commonMain` debe conservar esta estructura:

- `presentation/`
- `domain/`
- `data/`
- `di/`
- `ui/`

### Presentation

Responsable de estado, acciones de UI y efectos.

Ubicacion tipica:

- `contract/*`: `State`, `Action`, `Effect`.
- `viewmodel/*`: clases que extienden `BaseViewModel`.
- `action/*`: `ActionProcessor` por accion relevante.
- `route/*`: traduccion de `Effect` a navegacion o side effects UI.
- `navigation/*`: builders de `NavGraphBuilder`.
- `composeResources/values/*`: strings y recursos de UI del feature.

Reglas:

- `ViewModel` no llama infraestructura directamente; delega en processors y use cases.
- `Route` no contiene logica de negocio.
- `Screen` y `View` no acceden a repositorios.
- No hardcodear textos visibles en `presentation`; usar recursos.

### Domain

Responsable de negocio puro.

Ubicacion tipica:

- `model/*`
- `repository/*`
- `usecase/*`
- `usecase/validator/*`
- `usecase/exceptionhandler/*`

Reglas:

- `UseCase` depende de interfaces, no de implementaciones.
- Validaciones en `ParamsValidator`.
- Traduccion de errores en `ExceptionHandler`.
- Cada feature expone una interfaz de fachada de negocio en `domain/repository`.
- Cuando un contrato de `domain` representa estado de negocio compartido o coordina origenes internos, su implementacion debe vivir en `data/repository`, no en `data/source`.

### Data

Responsable de integraciones y persistencia.

Ubicacion tipica:

- `data/repository/*`: implementaciones de interfaces de `domain`.
- `data/source/*`: API, DB, settings, gateways y bridges de plataforma.
- `data/mapper/*`: mapeos entre modelos remotos/locales y dominio.

Reglas:

- `domain` nunca importa clases de `data`.
- La implementacion de una interfaz de dominio se define como `<Feature>DataRepository`.
- Un `*DataSource` no debe bindearse directamente como interfaz de `domain` cuando el contrato representa negocio, estado compartido o coordinacion entre origenes.
- En esos casos, el `*DataSource` queda como origen interno y un `*DataRepository` expone la interfaz de `domain`.
- Si un `DataRepository` necesita origenes internos, esos contratos se definen como `*DataSource`.
- Las concreciones de `*DataSource` viven en `data/source`.
- Los adapters hoja de plataforma o gateways simples pueden implementar su contrato de `domain` directamente desde `data/source` si no estan modelando un repositorio de negocio ni coordinando otros origenes.
- Si una feature necesita leer contratos compartidos de otro modulo, debe hacerlo mediante adapters propios de esa feature.

### DI

Responsable del wiring.

Superficie publica actual:

- `commonModule`
- `persistenceModule`
- `<feature>Module`
- `androidPlatformModule`
- `iosPlatformModule`

Reglas:

- `di` solo registra dependencias.
- `di` no implementa adaptadores concretos.
- En `commonModule`, preferir `single` para servicios compartidos de runtime, repositorios de infraestructura y dependencias con estado/memoria/flows/mutexes; reservar `factory` para objetos transientes o sin identidad compartida.
- `featureModules()` agrega solo modulos de feature.
- `commonModules()` agrega `commonModule`, `persistenceModule` y los modulos de feature.
- El bootstrap comun entra por `startAppKoin(...)`.
- Cada plataforma aporta un `PlatformKoinBootstrap`.
- No crear `*AndroidModule` o `*IosModule` por feature.
- Los bindings especificos de plataforma de una feature viven en el modulo de plataforma correspondiente.
- `persistence` define sus bindings Koin propios; el registro de storage por plataforma se invoca desde `androidPlatformModule` e `iosPlatformModule`.

## Convenciones de Koin

### Naming

- Infra compartida en `commonMain`: `commonModule`.
- Infra de persistencia en `commonMain`: `persistenceModule`.
- Modulos de feature: `mainModule`, `loginModule`, `aboutModule`, etc.
- Wiring de plataforma: `androidPlatformModule` e `iosPlatformModule`.

### Archivos

- Infra compartida: `CommonModule.kt`.
- Infra de persistencia: `PersistenceModule.kt`.
- Modulo de feature: `FeatureModule.kt`.
- Wiring de plataforma: `AndroidPlatformModule.kt`, `IosPlatformModule.kt`.

### Declaracion

- Usar `val ... = module { ... }` por defecto.
- Usar `fun ...(...): Module = module { ... }` solo si realmente necesita parametros runtime.

### Bootstrap

- Android arranca Koin via `startAndroidAppKoin(...)`.
- iOS arranca Koin via `startIosKoin(...)`.
- Ambos terminan usando `startAppKoin(...)`.
- iOS registra su runtime host con `iOSContext(...)`.

### Patrones no permitidos

- `*CoreModule`.
- Modulos vacios `module {}`.
- Modulos de plataforma por feature.

## Limites KMP

`commonMain` debe mantenerse portable.

No permitido en `commonMain`:

- imports `android.*`.
- imports `java.*` salvo donde el source set lo permita y no sea comun.
- `BuildConfig`.
- `InputStream`, `OutputStream`, `Reader`, `Writer`.
- `koinViewModel` o `koinNavViewModel`.

Reglas adicionales:

- La navegacion compartida vive en `commonMain`.
- No usar DSL Android de ViewModel dentro de source sets KMP.
- Usar `composeResources` para recursos visibles de UI.
- Evitar `expect/actual` salvo casos muy justificados.

## Flujo estandar

1. `Screen` dispara una accion en `ViewModel`.
2. `ViewModel` delega en `ActionProcessor`.
3. `ActionProcessor` ejecuta un `UseCase`.
4. `UseCase` usa interfaces de repositorio de `domain`.
5. `ActionProcessor` mapea el resultado a `Mutation<State>` y `Effect`.
6. `Route` consume `Effect` y lo traduce a navegacion o side effects UI.

## Patron de smoke tests de Koin

Cada modulo Koin publico debe tener una prueba de inicializacion que valide su wiring minimo.

### Modulos de feature

- Cada `<feature>Module` debe tener un `<Feature>ModuleKoinSmokeTest` en `commonTest`.
- La prueba debe iniciar Koin con el modulo del feature y los overrides minimos de contratos externos.
- La prueba debe resolver los entry points publicos del feature, normalmente sus `ViewModel`.

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
- validar el wiring minimo de cada modulo,
- y cubrir el arranque real de iOS para detectar regresiones host/plataforma.

## Checklist para cambios nuevos

- Se respeta separacion `presentation/domain/data/di`.
- No se introducen nuevas dependencias cruzadas entre features.
- La navegacion del feature vive en `commonMain`.
- `ViewModel` usa `ActionProcessor`.
- Los casos de uso tienen validator y exception handler cuando aplica.
- Las interfaces viven en `domain` y las implementaciones en `data`.
- Koin se registra en el modulo correcto.
- Los textos visibles van a recursos comunes.
- Si agregas o cambias wiring de Koin, agregas o actualizas el smoke test del modulo afectado.
- Ejecutas las verificaciones necesarias antes de cerrar el cambio.

## Verificaciones utiles

Ejemplos de comandos usados habitualmente:

```bash
./gradlew --continue --console=plain :app:compileDebugKotlin
./gradlew --continue --console=plain :maincore:linkDebugFrameworkIosSimulatorArm64
./gradlew --continue --console=plain :evaluations:allTests
./gradlew --continue --console=plain :maincore:iosSimulatorArm64Test --tests '*IosAppKoinSmokeTest*'
```

## Politica de evolucion

Si una necesidad de producto rompe estas reglas:

- pausar la implementacion,
- acordar el cambio de limite,
- y actualizar este README en el mismo cambio tecnico.
