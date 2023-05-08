package com.gdavidpb.tuindice.evaluations.ui.custom

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.gdavidpb.tuindice.base.domain.model.EvaluationType
import com.gdavidpb.tuindice.base.utils.extension.checkedChipIndex
import com.gdavidpb.tuindice.evaluations.R
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

class EvaluationTypeChipGroup(context: Context, attrs: AttributeSet) :
	ChipGroup(context, attrs) {

	init {
		initChipGroup()
	}

	fun getEvaluationType(): EvaluationType? {
		val index = checkedChipIndex

		return if (index != -1) EvaluationType.values()[index] else null
	}

	private fun initChipGroup() {
		EvaluationType.values().forEach { evaluationType ->
			View.inflate(context, R.layout.view_evaluation_chip, null).also { chip ->
				chip as Chip

				chip.text = context.getString(evaluationType.stringRes)
			}.also(::addView)
		}
	}
}