package com.gdavidpb.tuindice.base.ui.style

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object TuIndiceShellColors {
	@Composable
	fun topBarContainer(): Color = MaterialTheme.colorScheme.surface

	@Composable
	fun topBarContent(): Color = MaterialTheme.colorScheme.onSurface

	@Composable
	fun bottomBarContainer(): Color = MaterialTheme.colorScheme.onSecondary

	@Composable
	fun bottomBarIndicator(): Color = MaterialTheme.colorScheme.secondaryContainer
}
