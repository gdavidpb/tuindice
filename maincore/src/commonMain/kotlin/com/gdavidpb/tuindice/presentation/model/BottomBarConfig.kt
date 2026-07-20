package com.gdavidpb.tuindice.presentation.model

import com.gdavidpb.tuindice.base.domain.model.MainSection

sealed class BottomBarConfig(
	val section: MainSection
) {
	data object Summary : BottomBarConfig(
		section = MainSection.SUMMARY
	)

	data object Record : BottomBarConfig(
		section = MainSection.RECORD
	)

	data object Pensum : BottomBarConfig(
		section = MainSection.PENSUM
	)

	data object Evaluations : BottomBarConfig(
		section = MainSection.EVALUATIONS
	)

	data object About : BottomBarConfig(
		section = MainSection.ABOUT
	)
}
