# TuIndice

Aplicación multiplataforma Kotlin para Android e iOS.

El proyecto está organizado como KMP con una sola base común para arquitectura, recursos, DI y tests compartidos, más un
host Android en `app` y un host iOS en `iosApp`.

## Pipeline de release

El flujo de release usa PRs `feat/* -> production`, preflight obligatorio y deploy automático solo en `push` a
`production`. La versión única de Android/iOS vive en `gradle/app-version.properties` y se valida con
`./gradlew verifyAppVersionSync`. El archivo iOS `iosApp/Config/Version.xcconfig` se genera desde esa fuente.

Ver la configuración completa en `docs/release-pipeline.md`.

## Módulos del workspace

- `base`: contratos compartidos, helpers base, logging, errores, repositorios de infraestructura y piezas UI
  reutilizables.
- `academiccore`: modelos académicos compartidos y motor de proyección de historial.
- `persistence`: schema Room compartido, DAOs/entities públicas, outbox genérico e infraestructura de storage.
- `security`: subsistema compartido de attestation: modelos, contrato de repositorio, headers y helpers de
  binding/canonicalización de payloads.
- `about`: información de app, enlaces y soporte.
- `auth`: autenticación, sesión y actualización de credenciales.
- `summary`: resumen del perfil y foto de perfil.
- `record`: historial académico y cálculos de índice.
- `evaluations`: evaluaciones, filtros, picker de fecha y picker de nota.
- `enrollmentproof`: constancia de inscripción y apertura de archivos.
- `subjects`: estadísticas y detalle histórico de una materia.
- `pensum`: mapa de pensum, avance, selección de modalidad y búsqueda de estadísticas por materia.
- `wizard`: onboarding progresivo con overlays contextuales sobre pantallas reales de la app.
- `maincore`: navegación compartida, bootstrap común de Koin y superficie principal de la app.
- `app`: host Android.
- `iosApp`: host iOS.
- `testkit`: helpers, dobles compartidos y contrato reutilizable para pruebas.

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

## Eventos y datos de uso

La app publica eventos genericos desde el pipeline MVI sin acoplar features ni `base` a una herramienta concreta.
`StateMachineViewModel` emite automaticamente `screen_view`, `app_action`, `app_state`, `app_effect`,
`app_transition` y `app_invalid_transition`; cada `ViewModel`
entrega su `name` al constructor base, y el `EventPublisher` global se inyecta con `override`, igual que otros puntos
extensibles de la capa presentation.

Firebase Analytics y Firebase Performance no dependen del `EventPublisher` para activar o desactivar recoleccion:
sus flags de plataforma se sincronizan desde una `AppStartupTask` (`UsageDataCollectionDataSource`) con el mismo
consentimiento.

Reglas:

- La publicacion es fire-and-forget y no debe bloquear acciones, estados, efectos ni navegacion.
- La recoleccion esta apagada por defecto y depende de `UsageDataConsentRepository`.
- `AppEvent` es un modelo cerrado; no acepta parametros arbitrarios desde features.
- Los eventos automaticos solo exponen parametros fijos: `source`, `screen_name`, `action`, `state` y `effect`.
- Los subscribers reales se registran en plataforma; debug usa subscribers locales para inspeccion.

## Dependencias entre módulos

Regla general:

- Las dependencias deben apuntar hacia módulos base o infraestructura compartida.
- Evitar dependencias cruzadas entre features.

La fuente de verdad del grafo es `scripts/module-graph.txt`, validada contra los `build.gradle.kts` reales con
`./gradlew verifyModuleGraph` (también corre en preflight). CI deriva de ese archivo qué módulos recompilar,
testear, pasar por detekt y certificar con E2E. Si cambia una frontera, actualizar el archivo y esta sección en
el mismo cambio.

Dependencias actuales:

- `base`: sin dependencias de proyecto.
- `academiccore`: sin dependencias de proyecto.
- `persistence`: depende de `:academiccore`, `:base`.
- `security`: depende de `:base`.
- `about`: depende de `:base`.
- `auth`: depende de `:base`, `:security`.
- `summary`: depende de `:base`, `:persistence`.
- `record`: depende de `:academiccore`, `:base`, `:persistence`.
- `enrollmentproof`: depende de `:base`, `:persistence`.
- `evaluations`: depende de `:academiccore`, `:base`, `:persistence`.
- `subjects`: depende de `:academiccore`, `:base`, `:persistence`.
- `pensum`: depende de `:academiccore`, `:base`, `:persistence`.
- `wizard`: depende de `:base`, `:summary`, `:record`, `:evaluations`, `:pensum`, `:subjects` y `:about`.
- `maincore`: depende de `:base`, `:persistence`, `:security` y todas las features.
- `app`: host Android; ensambla `maincore`.

Acuerdo de límites:

- No agregar nuevas dependencias feature -> feature.
- La excepción `wizard -> features` es intencional: `wizard` no es un feature normal, sino una capa de coachmarks
  contextuales que detecta pantallas reales elegibles y muestra overlays anclados.
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
- `viewmodel/*`: clases que extienden `StateMachineViewModel`; API pura de pantalla (helpers `sendAction`).
- `machine/*`: `<Screen>Machine` (use cases + comandos + `define()`), `<Screen>InternalEvent` y, en pantallas
  formulario, `<Screen>Draft` (registros de entrada, escritos solo desde la máquina).
- `transition/*`: la tabla de transiciones como extension functions del builder, un archivo por estado origen.
- `mapper/*`: funciones de mapeo error→texto (`<Screen>ErrorMessages`) y mappers de presentación.
- `route/*`: traducción de `Effect` a navegación o side effects UI.
- `navigation/*`: builders de `NavGraphBuilder`.
- `composeResources/values/*`: strings y recursos de UI del feature.

Reglas:

- `ViewModel` no llama infraestructura directamente; la máquina posee los use cases y los ejecuta como
  comandos de transición.
- `Route` no contiene lógica de negocio.
- `Screen` y `View` no acceden a repositorios.
- No hardcodear textos visibles en `presentation`; usar recursos.

### Domain

Responsable de negocio puro.

Ubicación típica:

- `model/*`
- `mapper/*`
- `repository/*`
- `usecase/*`
- `usecase/validator/*`
- `usecase/exceptionhandler/*`

Reglas:

- `UseCase` depende de interfaces, no de implementaciones.
- Los `UseCase` no fijan dispatchers ni hacen `flowOn`; heredan el contexto del pipeline. `TuIndiceDispatchers` se
  inyecta solo en la frontera MVI (`ViewModel` → `StateMachineViewModel`), en `BufferedEventPublisher` y en `DataSource`
  concretos con trabajo bloqueante o de CPU real.
- Validaciones en `ParamsValidator`.
- Traducción de errores en `ExceptionHandler`.
- Cada feature expone una interfaz de fachada de negocio en `domain/repository`.
- Si la capa `data` necesita una abstracción interna real para coordinar múltiples orígenes, ese contrato puede
  modelarse como interfaz en `data/repository` con sufijo `DataRepository`.
- Si un repositorio de dominio se resuelve con un único origen concreto, ese `DataSource` debe implementar
  directamente la interfaz de `domain/repository`.

### Data

Responsable de integraciones y persistencia.

Ubicación típica:

- `data/repository/*`: interfaces internas de la capa `data` para coordinación multi-origen.
- `data/source/*`: implementaciones concretas de contratos de `domain/repository` y `data/repository`.
- `data/mapper/*`: mapeos entre modelos remotos/locales y dominio.

Reglas:

- `domain` nunca importa clases de `data`.
- Los modelos de dominio viven en `domain/model`.
- Los mapeos puramente de dominio pueden vivir en `domain/mapper`.
- Las interfaces de dominio viven en `domain/repository` y usan sufijo `Repository`.
- Los contratos internos de la capa `data` viven en `data/repository` y usan sufijo `DataRepository` solo cuando
  representan una abstracción interna real sobre múltiples orígenes dentro de la capa `data`.
- Los modelos internos de la capa `data` viven en `data/model`.
- Los mapeos de persistencia, red y adaptación interna viven en `data/mapper`.
- Las implementaciones concretas viven en `data/source` y usan sufijo `DataSource`.
- No deben existir interfaces `*DataSource`.
- Un `*DataSource` puede implementar directamente un contrato de `domain/repository`.
- Un `*DataSource` puede implementar un `*DataRepository` cuando es uno de los orígenes concretos detrás de una
  coordinación multi-origen.
- No crear `*DataRepository` triviales que solo extienden un `*Repository` de dominio sin agregar una abstracción interna.
- No crear `*DataRepository` para repositorios de un solo origen.
- `data/repository` no debe contener DTOs, modelos remotos, mutaciones, resolvers ni clases concretas.
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
- `persistence` expone el schema Room, sus DAOs/entities públicos, el runtime genérico de mutaciones diferidas y
  capacidades transversales de storage como transacciones y maintenance.
- Las features no reciben `TuIndiceDatabase`; consumen DAOs específicos, entities/projections públicas y contratos
  de infraestructura expuestos por `persistence`.
- El registro del database por plataforma se invoca desde `androidPlatformModule` e `iosPlatformModule`.

## Convenciones de Koin

### Naming

- Infra compartida en `commonMain`: `commonModule`.
- Módulos de feature: `mainModule`, `authModule`, `aboutModule`, etc.
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
- imports o DSL de ViewModel de `org.koin.androidx.*`.

Reglas adicionales:

- La navegación compartida vive en `commonMain`.
- No usar DSL Android de ViewModel dentro de source sets KMP.
- En navegación compartida, resolver ViewModels con el helper KMP `org.koin.compose.viewmodel.koinViewModel(...)`.
- Usar `composeResources` para recursos visibles de UI.
- Evitar `expect/actual` salvo casos muy justificados.

## Flujo estándar

1. `Screen` dispara una acción en `ViewModel` (`sendAction`, payload mínimo: solo información nueva del entorno).
2. El loop FIFO de `StateMachineViewModel` resuelve la fila en la tabla de la máquina; pares `(estado, acción)`
   no declarados se rechazan con telemetría `app_invalid_transition`.
3. La fila ejecuta `f: (S, σ) -> S` y, si es asíncrona, lanza un comando de la máquina que ejecuta un `UseCase`.
4. `UseCase` usa interfaces de repositorio de `domain`.
5. El resultado re-entra a la tabla como evento interno (partido por outcome); su fila produce el nuevo estado y
   emite los `Effect` declarados en `emits`.
6. `Route` consume `Effect` y lo traduce a navegación o side effects UI.
7. El chrome global del host (`topBar` y `bottomBar`) se deriva del `ViewState` emitido por la ruta activa y se
   mantiene en `TuIndiceAppHostRoute`; `MainViewModel` solo conserva estado de arranque y destino inicial.

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

## Patron de E2E local

La suite E2E ejecutable vive en `e2e/` y prioriza Maestro para flujos cross-platform. Los casos Android o iOS que
dependen de detalles de plataforma se registran como suites especificas bajo `e2e/platform/android` o
`e2e/platform/ios`.

En `testkit/e2e` vive el contrato de implementacion:

- `flow-catalog.yaml`: inventario de flows, modulos, plataformas y siguientes escenarios.
- `selector-policy.md`: reglas para usar `Modifier.testTag` como selector estable.
- `fixture-contract.md`: reglas de uso de WireMock como backend local de QA.
- `local-runbook.md`: comandos y variables para ejecucion local.
- `validate-e2e-contract.sh`: validador de catalogo y selectors criticos.

Reglas:

- Los flows Maestro usan `id` sobre tags estables definidos por cada modulo.
- Android debug expone `testTag` como resource id para runners black-box.
- La suite local usa WireMock desde `mocks/`; no llama servicios productivos.
- Firebase Test Lab queda fuera de esta capa local y debe agregarse con runners separados cuando corresponda.

## Checklist para cambios nuevos

- Se respeta separación `presentation/domain/data/di`.
- No se introducen nuevas dependencias cruzadas entre features.
- La navegación del feature vive en `commonMain`.
- `ViewModel` es API pura de pantalla; la lógica vive en `machine/` + `transition/` (motor en
  `base/presentation/statemachine`, doctrina en `docs/spike-kstatemachine-signin.md`).
- Los casos de uso tienen validator y exception handler cuando aplica.
- Las interfaces viven en `domain` y las implementaciones en `data`.
- Koin se registra en el módulo correcto.
- Los textos visibles van a recursos comunes.
- Si agregas o cambias wiring de Koin, agregas o actualizas el smoke test del modulo afectado.
- Si agregas un flujo E2E, actualizas `testkit/e2e/flow-catalog.yaml` y ejecutas `verifyE2eContract`.
- Ejecutas las verificaciones necesarias antes de cerrar el cambio.

## Enforcement de arquitectura (Semgrep)

Las reglas de `config/semgrep/rules/` codifican las piezas base de este README como chequeos estáticos
(43 reglas en 11 archivos por área):

- `kmp-portability`: límites KMP en `commonMain` (imports `android.*`/`java.*`, `BuildConfig`,
  Koin androidx, Firebase directo).
- `layering`: `domain` sin imports de `data`; `ui` compartida sin repositorios, use cases ni Koin;
  sin interfaces `*DataSource`; `data/repository` solo interfaces.
- `dispatchers`: sin `Dispatchers.*` crudo fuera de `base`; sin `TuIndiceDispatchers`/`flowOn`/`withContext`
  en use cases, máquinas o contratos.
- `viewmodel-purity`: `ViewModel` como API pura de pantalla (hereda de `StateMachineViewModel`; sin estado
  propio, corrutinas, use cases, lecturas de estado, `init { }` ni `sendEffect`/`processInternalEvent`
  directos).
- `koin-conventions`: sin módulos vacíos, sin `*CoreModule`, sin módulos de plataforma por feature.
- `base-components`: implementación correcta de las primitivas (`*UseCase` extiende `FlowUseCase`, sin
  try/catch ni `executeOnBackground` directo, `UseCaseState`/`AppEvent` solo desde `base`, `*Machine`
  implementa `ScreenMachine`, `MutableStateFlow` de máquina solo en `*Draft.kt`).
- `usecase-discipline`: un use case expone una sola operación (`execute`); `*ParamsValidator` en
  `usecase/validator` y `*ExceptionHandler` en `usecase/exceptionhandler`.
- `mvi-contracts` y `transition-purity`: `Action`/`Effect` solo en `presentation/contract` (el `ViewState`
  de chrome de ruta queda exento por diseño), máquinas sin `host` como propiedad y `transition/` con solo
  extension functions de la tabla.
- `composable-boundary`: `testTag` solo vía objetos `*UiTags`, `koinViewModel` solo en
  `presentation/navigation` (exención documentada: `TuIndiceAppHostRoute`, raíz del árbol), Drafts
  importables solo desde `machine`/`di`, y `navigateBackWithResult` siempre con tipo base explícito.
- `infrastructure`: `TuIndiceDatabase` solo en `persistence`, `EventPublisher` solo en la frontera MVI
  (ViewModels y `di`), y sin `println` (el logging pasa por los contratos de `base`).

Cada archivo de reglas tiene un fixture `.kt` homónimo validado con `semgrep --test`, y el módulo
sintético de `config/semgrep/generality/` prueba que toda regla dispara sobre layouts y nombres de módulo
que no existen en el repo (generalidad, no ajuste al código actual), con controles negativos para las
exenciones por paths. Las exclusiones de scan viven en `.semgrepignore`.

En preflight, `detect-changed-app.sh` expone `semgrep_required` (true cuando cambió código de módulos o la
configuración del ruleset) y el job compartido ejecuta `scripts/semgrep-architecture.sh`; la paridad local
del skill de certificación corre el mismo script, junto a los tasks de detekt que ya viajan en las tareas
Android.

```bash
scripts/semgrep-architecture.sh              # validate + fixtures + generalidad + scan
scripts/semgrep-architecture.sh generality   # solo prueba de generalidad
```

## Verificaciones útiles

Ejemplos de comandos usados habitualmente:

```bash
./gradlew --continue --console=plain verifyModuleGraph
./gradlew --continue --console=plain :app:compileDebugKotlin
./gradlew --continue --console=plain :maincore:linkDebugFrameworkIosSimulatorArm64
./gradlew --continue --console=plain :evaluations:allTests
./gradlew --continue --console=plain :maincore:iosSimulatorArm64Test --tests '*IosAppKoinSmokeTest*'
./gradlew --continue --console=plain verifyE2eContract
./gradlew --continue --console=plain e2eMaestroAndroid
./gradlew --continue --console=plain verifySharedHostTests
./gradlew --continue --console=plain detekt
./gradlew --continue --console=plain koverHtmlReport
scripts/semgrep-architecture.sh
```

Nota: `verifySharedHostTests` corre los tests compartidos en el host JVM de Android — la única plataforma donde los validadores de alfabeto/Λ de las máquinas validan de verdad (en iOS reportan SKIPPED). `detekt` usa baselines por módulo y `koverHtmlReport` es medición de cobertura sin umbral.

## Política de evolución

Si una necesidad de producto rompe estas reglas:

- pausar la implementación,
- acordar el cambio de límite,
- y actualizar este README en el mismo cambio técnico.
