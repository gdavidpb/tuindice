package com.gdavidpb.tuindice.base.ui.view

import androidx.compose.material3.DropdownMenu
import androidx.compose.runtime.Composable

// Synthetic module sample: a menu of commonMain without its test tags, so dropdown-menu-exposes-test-tags fires.
@Composable
fun SampleOverflowMenu(expanded: Boolean, onDismissRequest: () -> Unit) {
	DropdownMenu(expanded = expanded, onDismissRequest = onDismissRequest) {}
}
