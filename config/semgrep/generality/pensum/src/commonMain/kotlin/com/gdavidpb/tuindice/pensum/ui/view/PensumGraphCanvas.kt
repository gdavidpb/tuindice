package com.gdavidpb.tuindice.pensum.ui.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

// Control negativo: el grafo del pensum guarda su cámara y sus filtros con delegados en todo el archivo; su patrón
// establecido queda exento por path de compose-state-no-delegated-remember.
@Composable
fun SampleGraphCanvas(graphKey: String) {
	var savedScale by remember(graphKey) { mutableStateOf(1f) }
	var isMinimapVisible by remember(graphKey) { mutableStateOf(false) }
	check(savedScale > 0f || isMinimapVisible)
}
