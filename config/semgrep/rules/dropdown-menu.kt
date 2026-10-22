// Fixture de semgrep --test para dropdown-menu.yaml.
// No es código del proyecto: cada línea anotada valida la regla. El alcance a src/commonMain se valida en la prueba de
// generalidad con módulo sintético (en --test los paths no aplican).
package com.gdavidpb.tuindice.sample.ui.view

import androidx.compose.material3.DropdownMenu
import androidx.compose.ui.Modifier

fun SampleMenuWithoutTags(expanded: Boolean, onDismissRequest: () -> Unit) {
	// ruleid: dropdown-menu-exposes-test-tags
	DropdownMenu(expanded = expanded, onDismissRequest = onDismissRequest) {}
}

fun SampleMenuWithTags(expanded: Boolean, onDismissRequest: () -> Unit) {
	// ok: dropdown-menu-exposes-test-tags
	DropdownMenu(
		modifier = Modifier.exposeTestTagsAsResourceId(),
		expanded = expanded,
		onDismissRequest = onDismissRequest
	) {}
}

fun SampleMenuWithTagsAfterHeight(expanded: Boolean, onDismissRequest: () -> Unit) {
	// ok: dropdown-menu-exposes-test-tags
	DropdownMenu(
		modifier = Modifier.heightIn(max = 200.dp).exposeTestTagsAsResourceId(),
		expanded = expanded,
		onDismissRequest = onDismissRequest
	) {}
}

fun SampleMenuMultilineWithoutTags(expanded: Boolean, onDismissRequest: () -> Unit) {
	// ruleid: dropdown-menu-exposes-test-tags
	DropdownMenu(
		modifier = Modifier.heightIn(max = 200.dp),
		expanded = expanded,
		onDismissRequest = onDismissRequest
	) {}
}
