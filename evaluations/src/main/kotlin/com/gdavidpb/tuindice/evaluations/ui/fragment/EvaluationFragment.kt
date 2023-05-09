package com.gdavidpb.tuindice.evaluations.ui.fragment

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.ViewFlipper
import androidx.navigation.fragment.navArgs
import com.gdavidpb.tuindice.base.ui.custom.ErrorView
import com.gdavidpb.tuindice.base.ui.fragment.NavigationFragment
import com.gdavidpb.tuindice.base.utils.extension.animateShake
import com.gdavidpb.tuindice.base.utils.extension.checkedChipIndex
import com.gdavidpb.tuindice.base.utils.extension.collect
import com.gdavidpb.tuindice.base.utils.extension.errorSnackBar
import com.gdavidpb.tuindice.base.utils.extension.launchRepeatOnLifecycle
import com.gdavidpb.tuindice.base.utils.extension.snackBar
import com.gdavidpb.tuindice.base.utils.extension.view
import com.gdavidpb.tuindice.evaluations.R
import com.gdavidpb.tuindice.evaluations.domain.usecase.param.AddEvaluationParams
import com.gdavidpb.tuindice.evaluations.domain.usecase.param.GetEvaluationParams
import com.gdavidpb.tuindice.evaluations.domain.usecase.param.UpdateEvaluationParams
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluations
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationViewState
import com.gdavidpb.tuindice.evaluations.presentation.viewmodel.EvaluationViewModel
import com.gdavidpb.tuindice.evaluations.ui.custom.EvaluationDatePickerView
import com.gdavidpb.tuindice.evaluations.ui.custom.EvaluationNameInputLayout
import com.gdavidpb.tuindice.evaluations.ui.custom.EvaluationTypeChipGroup
import com.gdavidpb.tuindice.evaluations.ui.custom.GradeInputLayout
import com.google.android.material.button.MaterialButton
import org.koin.androidx.viewmodel.ext.android.viewModel

class EvaluationFragment : NavigationFragment() {

	private val tViewEvaluationHeader by view<TextView>(R.id.tViewEvaluationHeader)
	private val tInputEvaluationName by view<EvaluationNameInputLayout>(R.id.tInputEvaluationName)
	private val tInputEvaluationGrade by view<GradeInputLayout>(R.id.tInputEvaluationGrade)
	private val dPickerEvaluationDate by view<EvaluationDatePickerView>(R.id.dPickerEvaluationDate)
	private val cGroupEvaluation by view<EvaluationTypeChipGroup>(R.id.cGroupEvaluation)
	private val btnEvaluationSave by view<MaterialButton>(R.id.btnEvaluationSave)
	private val eViewEvaluation by view<ErrorView>(R.id.eViewEvaluation)
	private val fViewEvaluation by view<ViewFlipper>(R.id.fViewEvaluation)

	private val viewModel by viewModel<EvaluationViewModel>()

	private val args by navArgs<EvaluationFragmentArgs>()

	override fun onCreateView() = R.layout.fragment_evaluation

	private object Flipper {
		const val CONTENT = 0
		const val LOADING = 1
		const val FAILED = 2
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		eViewEvaluation.setOnRetryClick { initialLoad() }
		btnEvaluationSave.setOnClickListener { onSaveClick() }

		launchRepeatOnLifecycle {
			with(viewModel) {
				collect(viewState, ::stateCollector)
				collect(viewEvent, ::eventCollector)
			}
		}

		initialLoad()
	}

	private fun stateCollector(state: Evaluations.State) {
		when (state) {
			is Evaluations.State.Loading -> fViewEvaluation.displayedChild = Flipper.LOADING
			is Evaluations.State.Loaded -> loadEvaluationViewState(value = state.value)
			is Evaluations.State.Failed -> fViewEvaluation.displayedChild = Flipper.FAILED
		}
	}

	private fun eventCollector(event: Evaluations.Event) {
		when (event) {
			is Evaluations.Event.NavigateUp -> navigateUp()
			is Evaluations.Event.ShowEmptyNameError -> tInputEvaluationName.setError(R.string.error_evaluation_name_missed)
			is Evaluations.Event.ShowInvalidGradeStepError -> tInputEvaluationGrade.setError(R.string.error_evaluation_grade_invalid_step)
			is Evaluations.Event.ShowOutOfRangeGradeError -> tInputEvaluationGrade.setError(R.string.error_evaluation_grade_invalid_range)
			is Evaluations.Event.ShowDateMissedError -> {
				snackBar(R.string.toast_evaluation_date_missed)
				dPickerEvaluationDate.animateShake()
			}

			is Evaluations.Event.ShowTypeMissedError -> {
				snackBar(R.string.toast_evaluation_type_missed)
				cGroupEvaluation.animateShake()
			}

			is Evaluations.Event.ShowDefaultErrorSnackBar -> errorSnackBar()
		}
	}

	private fun initialLoad() {
		val evaluationId = args.evaluationId

		if (evaluationId != null)
			viewModel.loadEvaluationAction(
				GetEvaluationParams(
					evaluationId = evaluationId
				)
			)
	}

	private fun loadEvaluationViewState(value: EvaluationViewState) {
		tViewEvaluationHeader.text = value.subjectHeader
		tInputEvaluationName.setName(value.name)
		tInputEvaluationGrade.setGrade(value.maxGrade)
		dPickerEvaluationDate.isChecked = value.isDateSet
		cGroupEvaluation.checkedChipIndex = value.type.ordinal
		dPickerEvaluationDate.selectedDate = value.date

		fViewEvaluation.displayedChild = Flipper.CONTENT
	}

	private fun onSaveClick() {
		val maxGrade = tInputEvaluationGrade.getGrade()

		if (args.evaluationId == null)
			viewModel.addEvaluationAction(
				AddEvaluationParams(
					quarterId = "",
					subjectId = args.subjectId,
					name = tInputEvaluationName.getName(),
					grade = maxGrade,
					maxGrade = maxGrade,
					date = dPickerEvaluationDate.selectedDate.time,
					type = cGroupEvaluation.getEvaluationType(),
					isDone = false
				)
			)
		else
			viewModel.updateEvaluationAction(
				UpdateEvaluationParams(
					evaluationId = args.evaluationId ?: "",
					name = tInputEvaluationName.getName(),
					grade = maxGrade,
					maxGrade = maxGrade,
					date = dPickerEvaluationDate.selectedDate.time,
					type = cGroupEvaluation.getEvaluationType(),
					isDone = false
				)
			)
	}
}