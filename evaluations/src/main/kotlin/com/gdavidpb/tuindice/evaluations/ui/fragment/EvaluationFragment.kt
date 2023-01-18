package com.gdavidpb.tuindice.evaluations.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.navArgs
import com.gdavidpb.tuindice.base.domain.model.Evaluation
import com.gdavidpb.tuindice.base.domain.model.EvaluationType
import com.gdavidpb.tuindice.base.domain.usecase.base.Result
import com.gdavidpb.tuindice.base.ui.fragments.NavigationFragment
import com.gdavidpb.tuindice.base.utils.extensions.checkedChipIndex
import com.gdavidpb.tuindice.base.utils.extensions.observe
import com.gdavidpb.tuindice.base.utils.extensions.onClickOnce
import com.gdavidpb.tuindice.base.utils.extensions.snackBar
import com.gdavidpb.tuindice.evaluations.R
import com.gdavidpb.tuindice.evaluations.domain.param.AddEvaluationParams
import com.gdavidpb.tuindice.evaluations.domain.param.UpdateEvaluationParams
import com.gdavidpb.tuindice.evaluations.domain.error.EvaluationError
import com.gdavidpb.tuindice.evaluations.presentation.viewmodel.EvaluationViewModel
import com.google.android.material.chip.Chip
import kotlinx.android.synthetic.main.fragment_evaluation.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class EvaluationFragment : NavigationFragment() {

	private val viewModel by viewModel<EvaluationViewModel>()

	private val args by navArgs<EvaluationFragmentArgs>()

	private val isNewEvaluation by lazy { (args.evaluationId == null) }

	override fun onCreateView() = R.layout.fragment_evaluation

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		initSubject()
		initChipGroup()

		btnEvaluationSave.onClickOnce(::onSaveClick)

		with(viewModel) {
			val evaluationId = args.evaluationId

			if (evaluationId != null) getEvaluation(evaluationId = evaluationId)
		}
	}

	override fun onInitObservers() {
		with(viewModel) {
			observe(getEvaluation, ::getEvaluationObserver)
			observe(addOrUpdateEvaluation, ::addOrUpdateEvaluationObserver)
		}
	}

	private fun initSubject() {
		val subjectHeader = getString(
			R.string.label_evaluation_plan_header,
			args.subjectCode,
			args.subjectName
		)

		tViewEvaluationHeader.text = subjectHeader
	}

	private fun initEvaluation(evaluation: Evaluation) {
		val name = evaluation.name
		val maxGrade = evaluation.maxGrade
		val date = evaluation.date
		val evaluationType = evaluation.type.ordinal

		tInputEvaluationName.setName(name)
		tInputEvaluationGrade.setGrade(maxGrade)
		dPickerEvaluationDate.isChecked = (date.time != 0L)
		cGroupEvaluation.checkedChipIndex = evaluationType

		dPickerEvaluationDate.selectedDate = date
	}

	private fun initChipGroup() {
		EvaluationType.values().forEach { evaluationType ->
			View.inflate(context, R.layout.view_evaluation_chip, null).also { chip ->
				chip as Chip

				chip.text = getString(evaluationType.stringRes)
			}.also(cGroupEvaluation::addView)
		}
	}

	private fun onSaveClick() {
		val maxGrade = tInputEvaluationGrade.getGrade()

		if (isNewEvaluation)
			viewModel.addEvaluation(
				AddEvaluationParams(
					subjectId = args.subjectId,
					name = tInputEvaluationName.getName(),
					grade = maxGrade,
					maxGrade = maxGrade,
					date = dPickerEvaluationDate.selectedDate.time,
					type = getEvaluationType(),
					isDone = false
				)
			)
		else
			viewModel.updateEvaluation(
				UpdateEvaluationParams(
					evaluationId = args.evaluationId ?: "",
					name = tInputEvaluationName.getName(),
					grade = maxGrade,
					maxGrade = maxGrade,
					date = dPickerEvaluationDate.selectedDate.time,
					type = getEvaluationType(),
					isDone = false
				)
			)
	}

	private fun getEvaluationType(): EvaluationType {
		return cGroupEvaluation
			.checkedChipIndex
			.let { index -> EvaluationType.values()[index] }
	}

	private fun getEvaluationObserver(result: Result<Evaluation, Nothing>?) {
		when (result) {
			is Result.OnLoading -> TODO("show loading")
			is Result.OnSuccess -> initEvaluation(evaluation = result.value)
			is Result.OnError -> TODO("show error")
			else -> {}
		}
	}

	private fun addOrUpdateEvaluationObserver(result: Result<Evaluation, EvaluationError>?) {
		when (result) {
			is Result.OnLoading -> TODO("show loading")
			is Result.OnSuccess -> navigateUp()
			is Result.OnError -> addOrUpdateEvaluationErrorHandler(error = result.error)
			else -> {}
		}
	}

	private fun addOrUpdateEvaluationErrorHandler(error: EvaluationError?) {
		when (error) {
			EvaluationError.EmptyName -> tInputEvaluationName.setError(R.string.error_evaluation_name_missed)
			EvaluationError.InvalidGradeStep -> tInputEvaluationGrade.setError(R.string.error_evaluation_grade_invalid_step)
			EvaluationError.OutOfRangeGrade -> tInputEvaluationGrade.setError(R.string.error_evaluation_grade_invalid_range)
			EvaluationError.DateMissed -> snackBar(R.string.toast_evaluation_date_missed) // TODO shake view
			EvaluationError.TypeMissed -> snackBar(R.string.toast_evaluation_type_missed) // TODO shake view
			else -> TODO("show default error")
		}
	}
}