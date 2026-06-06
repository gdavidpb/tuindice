package com.gdavidpb.tuindice.pensum.presentation.model

import com.gdavidpb.tuindice.base.ui.view.DropdownMenuItem

data class PensumOptionItem(
	val id: String,
	val year: Int,
	val modalityOptions: List<PensumModalityItem>,
	override val text: String
) : DropdownMenuItem
