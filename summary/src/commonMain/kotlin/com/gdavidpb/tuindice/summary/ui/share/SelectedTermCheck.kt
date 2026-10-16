package com.gdavidpb.tuindice.summary.ui.share

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * The check that marks the chosen term in its chip: a small tick, read as part of the chip and not announced on its
 * own.
 */
@Composable
fun SelectedTermCheck() {
	Icon(
		imageVector = Icons.Filled.Check,
		contentDescription = null,
		modifier = Modifier.size(18.dp)
	)
}
