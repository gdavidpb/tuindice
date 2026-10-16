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
