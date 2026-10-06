package com.gdavidpb.tuindice.base.presentation.model

import androidx.compose.runtime.Composable
import com.gdavidpb.tuindice.base.utils.extension.capitalize
import org.jetbrains.compose.resources.stringArrayResource
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

		// Nothing to read (an index outside the array, an array not loaded yet) is blank, not a crash.
		is UiText.ArrayItem -> stringArrayResource(resource).getOrNull(index).orEmpty()
		is UiText.Capitalized -> text.asString().capitalize()
		is UiText.Uppercase -> text.asString().uppercase()
	}
}
