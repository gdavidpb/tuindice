// Fixture de semgrep --test para composable-boundary.yaml.
// No es código del proyecto: cada línea anotada valida una regla.
// Las exenciones por paths (navigation/, machine/, di/, base/) se validan en la
// prueba de generalidad (en --test los paths no aplican).
package com.gdavidpb.tuindice.sample.ui.screen

// ruleid: draft-only-in-machine
import com.gdavidpb.tuindice.sample.presentation.machine.SampleDraft
// ok: draft-only-in-machine
import com.gdavidpb.tuindice.sample.presentation.machine.SampleMachine
import androidx.compose.ui.Modifier

// ruleid: koinviewmodel-only-in-navigation
fun sampleScreen(viewModel: Any = koinViewModel<Any>()): Any = viewModel

fun sampleView(modifier: Modifier): Modifier {
	// ruleid: testtag-no-inline-literal
	modifier.testTag("sample_screen")
	// ok: testtag-no-inline-literal
	return modifier.testTag(SampleUiTags.Screen)
}

// ruleid: entryprovider-only-in-navigation
fun EntryProviderScope<Any>.sampleEntries(viewModel: Any): Any = viewModel

// ok: entryprovider-only-in-navigation
fun sampleEntriesHost(scope: Any): Any = scope

// ruleid: ui-view-no-shared-data-class
data class SampleViewHelper(val label: String)

// ok: ui-view-no-shared-data-class
private data class SamplePrivateHelper(val label: String)

// ruleid: ui-tags-object-suffix
object SampleScreenTags {
	const val Screen = "sample_screen"
}

// ok: ui-tags-object-suffix
object SampleUiTags {
	const val Screen = "sample_screen"
}

fun sampleToggle(sampleKey: String): Boolean {
	// ruleid: compose-state-no-delegated-remember
	var isOpen by remember { mutableStateOf(false) }
	// ruleid: compose-state-no-delegated-remember
	var isKeyed by remember(sampleKey) { mutableStateOf(false) }
	// ok: compose-state-no-delegated-remember
	val isVal = remember { mutableStateOf(false) }
	return isOpen && isKeyed && isVal.value
}

// compositionlocal-actions-only-in-route: un CompositionLocal de acciones solo se lee desde la ruta (ok) o desde una vista (ruleid).
fun SampleLauncherReadRule() {
	// ruleid: compositionlocal-actions-only-in-route
	val assistantLauncher = LocalAssistantLauncher.current
	// ruleid: compositionlocal-actions-only-in-route
	val shareText = LocalShareTextHandler.current
	// ok: compositionlocal-actions-only-in-route
	val uriHandler = LocalUriHandler.current
}

// ui-component-no-else-unit: un when de component no cierra con else vacío.
fun SampleComponentElseRule(state: SampleState): String {
	return when (state) {
		SampleState.Idle -> "idle"
		// ruleid: ui-component-no-else-unit
		else -> Unit
	}
}

fun SampleComponentElseBlockRule(state: SampleState) {
	when (state) {
		SampleState.Idle -> Unit
		// ruleid: ui-component-no-else-unit
		else -> {}
	}
}

fun SampleComponentExplicitRule(state: SampleState): String {
	// ok: ui-component-no-else-unit
	return when (state) {
		SampleState.Idle -> "idle"
		SampleState.Busy -> "busy"
	}
}

// material-modal-sheet-only-in-base: la hoja de Material y su estado no se usan fuera de TuIndiceModalBottomSheet.
fun SampleMaterialSheet(onDismissRequest: () -> Unit): Any {
	// ruleid: material-modal-sheet-only-in-base
	val materialState = rememberModalBottomSheetState()
	// ruleid: material-modal-sheet-only-in-base
	return ModalBottomSheet(onDismissRequest = onDismissRequest, sheetState = materialState) {}
}

// ok: material-modal-sheet-only-in-base
fun SampleSharedSheet(onDismissRequest: () -> Unit): Any =
	TuIndiceModalBottomSheet(onDismissRequest = onDismissRequest) {}
