package com.gdavidpb.tuindice.wizard.presentation.model

import org.jetbrains.compose.resources.StringResource

data class Coachmark(
	val id: CoachmarkId,
	val title: StringResource,
	val message: StringResource
)
