package com.gdavidpb.tuindice.domain.model

import com.gdavidpb.tuindice.base.domain.model.MainSection

sealed interface StartUpTarget {
	data object Auth : StartUpTarget

	data class Main(
		val section: MainSection
	) : StartUpTarget
}
