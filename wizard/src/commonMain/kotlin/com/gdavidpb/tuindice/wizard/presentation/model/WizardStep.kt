package com.gdavidpb.tuindice.wizard.presentation.model

import com.gdavidpb.tuindice.base.presentation.model.TopBarConfig
import org.jetbrains.compose.resources.StringResource

data class WizardStep(
	val id: WizardStepId,
	val title: StringResource,
	val message: StringResource,
	val topBarTitle: String,
	val topBarConfig: TopBarConfig? = null,
	val showsRecordViewMode: Boolean = false
)
