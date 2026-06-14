package com.gdavidpb.tuindice.presentation.transition

import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineDefinitionBuilder
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineHost
import com.gdavidpb.tuindice.presentation.contract.Browser

internal fun MachineDefinitionBuilder<Browser.State>.browserTransitions(
	host: MachineHost<Browser.Effect>
) {
	fromAny {
		onTo<Browser.Action.NavigateTo, Browser.State.Content> { _, action ->
			Browser.State.Content(
				topBarTitle = UiText.Raw(action.title),
				url = action.url,
				isLoading = true
			)
		}

		on<Browser.Action.OpenExternalResource>(
			emits = setOf(Browser.Effect.NavigateToExternalResourceDialog::class)
		) { state, action ->
			host.sendEffect(
				Browser.Effect.NavigateToExternalResourceDialog(url = action.url)
			)
			state
		}
	}

	from<Browser.State.Content> {
		on<Browser.Action.SetLoading> { state, action ->
			state.copy(isLoading = action.isLoading)
		}
	}
}
