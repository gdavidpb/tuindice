package com.gdavidpb.tuindice.wizard.presentation.model

import com.gdavidpb.tuindice.base.presentation.model.TopBarConfig
import com.gdavidpb.tuindice.base.presentation.model.UiText
import org.jetbrains.compose.resources.StringResource

data class WizardStep(
	val id: WizardStepId,
	val title: StringResource,
	val message: StringResource,
	val topBarTitle: UiText,
	val topBarConfig: TopBarConfig? = null,
	val showsRecordViewMode: Boolean = false
)
