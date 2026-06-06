package com.gdavidpb.tuindice.pensum.presentation.model

import com.gdavidpb.tuindice.base.ui.view.DropdownMenuItem

data class PensumModalityItem(
	val id: String,
	val name: String,
	val isDefault: Boolean,
	override val text: String
) : DropdownMenuItem
