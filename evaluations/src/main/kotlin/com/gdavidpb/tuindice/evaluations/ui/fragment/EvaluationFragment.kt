package com.gdavidpb.tuindice.evaluations.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.navArgs
import com.gdavidpb.tuindice.base.domain.model.Evaluation
import com.gdavidpb.tuindice.base.domain.model.EvaluationType
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.ui.fragment.NavigationFragment
import com.gdavidpb.tuindice.base.utils.extension.*
import com.gdavidpb.tuindice.evaluations.R
import com.gdavidpb.tuindice.evaluations.domain.usecase.error.EvaluationError
import com.gdavidpb.tuindice.evaluations.domain.usecase.param.AddEvaluationParams
import com.gdavidpb.tuindice.evaluations.domain.usecase.param.UpdateEvaluationParams
import com.gdavidpb.tuindice.evaluations.presentation.viewmodel.EvaluationViewModel
import com.google.android.material.chip.Chip
import kotlinx.android.synthetic.main.fragment_evaluation.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class EvaluationFragment : NavigationFragment() {

	private val viewModel by viewModel<EvaluationViewModel>()

	private val args by navArgs<EvaluationFragmentArgs>()

	override fun onCreateView() = R.layout.fragment_evaluation

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		initSubject()
		initChipGroup()

		btnEvaluationSave.onClickOnce(::onSaveClick)

		launchRepeatOnLifecycle {
			with(viewModel) {
				collect(getEvaluation, ::getEvaluationCollector)
				collect(addEvaluation, ::addEvaluationCollector)
				collect(updateEvaluation, ::updateEvaluationCollector)
			}
		}

		val evaluationId = args.evaluationId

		if (evaluationId != null) viewModel.getEvaluation(evaluationId = evaluationId)
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

		if (args.evaluationId == null)
			viewModel.addEvaluation(
				AddEvaluationParams(
					quarterId = "",
					subjectId = args.subjectId,
					subjectCode = args.subjectCode,
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

	private fun getEvaluationType(): EvaluationType? {
		return cGroupEvaluation
			.checkedChipIndex
			.let { index -> if (index != -1) EvaluationType.values()[index] else null }
	}

	// TODO Implement error/loading
	private fun getEvaluationCollector(result: UseCaseState<Evaluation, Nothing>?) {
		when (result) {
			is UseCaseState.Data -> {
				initEvaluation(evaluation = result.value)
			}
			else -> {}
		}
	}

	// TODO Implement loading
	private fun addEvaluationCollector(result: UseCaseState<Unit, EvaluationError>?) {
		when (result) {
			is UseCaseState.Data -> {
				navigateUp()
			}
			is UseCaseState.Error -> {
				addOrUpdateEvaluationErrorHandler(error = result.error)
			}
			else -> {}
		}
	}

	// TODO Implement loading
	private fun updateEvaluationCollector(result: UseCaseState<Evaluation, EvaluationError>?) {
		when (result) {
			is UseCaseState.Data -> {
				navigateUp()
			}
			is UseCaseState.Error -> {
				addOrUpdateEvaluationErrorHandler(error = result.error)
			}
			else -> {}
		}
	}

	private fun addOrUpdateEvaluationErrorHandler(error: EvaluationError?) {
		when (error) {
			EvaluationError.EmptyName -> tInputEvaluationName.setError(R.string.error_evaluation_name_missed)
			EvaluationError.InvalidGradeStep -> tInputEvaluationGrade.setError(R.string.error_evaluation_grade_invalid_step)
			EvaluationError.OutOfRangeGrade -> tInputEvaluationGrade.setError(R.string.error_evaluation_grade_invalid_range)
			EvaluationError.DateMissed -> {
				snackBar(R.string.toast_evaluation_date_missed)

				dPickerEvaluationDate.animateShake()
			}
			EvaluationError.TypeMissed -> {
				snackBar(R.string.toast_evaluation_type_missed)

				cGroupEvaluation.animateShake()
			}
			else -> errorSnackBar()
		}
	}
}