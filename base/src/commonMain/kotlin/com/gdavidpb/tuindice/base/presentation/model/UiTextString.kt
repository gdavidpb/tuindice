package com.gdavidpb.tuindice.base.presentation.model

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.stringResource

@Composable
fun UiText.asString(): String {
	return when (this) {
		UiText.Empty -> ""
		is UiText.Raw -> value
		is UiText.Resource -> {
			// An argument can itself be a UiText (a cause phrase inside a template): resolved first.
			val args = args.map { arg -> if (arg is UiText) arg.asString() else arg }

			stringResource(resource, *args.toTypedArray())
		}
	}
}
