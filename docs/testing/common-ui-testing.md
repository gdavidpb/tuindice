# Common UI Testing Guide

## Objetivo

Establecer un patron único para cubrir todos los `@Composable` en `commonMain` con pruebas semánticas e interacciones reales (sin snapshot/golden).

## Alcance

- Incluido: `ui/view`, `ui/dialog`, `ui/screen`, `ui/custom`, `presentation/route`, `ui/theme` y utilidades composables en `commonMain`.
- Excluido: composables de `androidMain`/`iosMain`.
- Excluido: validación visual por pixel.

## API Unica de Test

Todas las pruebas UI common deben usar `testkit`:

```kotlin
runTuIndiceUiTest {
    setTuIndiceTestContent(
        sizeClass = TuIndiceTestSizeClass.Compact,
        density = 1f,
        locale = null
    ) {
        // host composable
    }
}
```

Helpers disponibles:

- `advanceAnimationsBy(millis)`
- `assertNodeVisible(tag)`
- `assertNodeHidden(tag)`
- `assertNodeEnabled(tag)`
- `assertNodeDisabled(tag)`

Nota de locale: en Compose Multiplatform, el locale del entorno se toma del sistema; el parámetro `locale` del host de test se usa para dirección de layout (LTR/RTL) y escenarios estructurales.

## Convenciones

- Archivo de prueba: `<ComposableOrFileName>UiTest.kt`
- Clase de prueba: `<ComposableOrFileName>UiTest`
- Método de prueba: `when_<estado>_then_<resultado>`
- Mínimo por archivo `UiTest`: umbral por modulo.
- Default: `>= 2` métodos `when_...`
- Excepción vigente: `maincore >= 3` métodos `when_...`
- Tags por modulo: `object <Module>UiTags`
- `Modifier.testTag` obligatorio en nodos interactivos críticos (inputs, botones, pickers, filas swipeables, loaders, contenedores de estado).

## Criterio de Cobertura Exhaustiva

Para cada composable:

- Render base correcto.
- Todos los estados observables.
- Todos los callbacks públicos.
- Interacciones reales (tap/input/scroll/swipe/selection).
- Reglas de enabled/disabled y visibilidad.
- Comportamiento temporal (animaciones/efectos) con reloj de test.

Para wrappers/passthrough:

- Propagación de parámetros y callbacks al hijo.

Para routes/screens:

- Mapeo `state -> UI` y `action -> callback` con doubles/fakes.

## Gates Mínimos por Modulo Tocado

```bash
./gradlew :<module>:compileTestKotlinIosSimulatorArm64 :<module>:iosSimulatorArm64Test
```

Gate agregado del proyecto:

```bash
./gradlew verifyCommonUiGate
```

Gate de densidad de casos `when_`:

```bash
./gradlew verifyCommonUiTestDensity
```

## Matriz de Cobertura

La matriz viva se genera con:

```bash
./scripts/generate-common-ui-matrix.sh
```

Salida:

- `docs/testing/common-ui-matrix.md`
- Incluye `Casos when_` y estado de cumplimiento según umbral por módulo.
