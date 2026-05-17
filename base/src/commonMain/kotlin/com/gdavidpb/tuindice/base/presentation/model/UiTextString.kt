package com.gdavidpb.tuindice.base.presentation.model

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.stringResource

@Composable
fun UiText.asString(): String {
	return when (this) {
		UiText.Empty -> ""
		is UiText.Raw -> value
		is UiText.Resource -> stringResource(resource, *args.toTypedArray())
	}
}
