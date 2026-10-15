package com.gdavidpb.tuindice.sampleflow.ui.view

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

// Disparo de compose-state-no-delegated-remember: el estado local se declara con val y se lee con .value.
fun sampleFlowToggle(sampleKey: String): Boolean {
	var isOpen by remember(sampleKey) { mutableStateOf(false) }
	return isOpen
}
