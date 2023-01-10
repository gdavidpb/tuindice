package com.gdavidpb.tuindice.evaluations.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_IDLE
import com.gdavidpb.tuindice.base.domain.model.Evaluation
import com.gdavidpb.tuindice.base.domain.model.Quarter
import com.gdavidpb.tuindice.base.domain.usecase.base.Result
import com.gdavidpb.tuindice.base.presentation.model.BottomMenuItem
import com.gdavidpb.tuindice.base.ui.dialogs.MenuBottomSheetDialog
import com.gdavidpb.tuindice.base.ui.fragments.NavigationFragment
import com.gdavidpb.tuindice.base.utils.DECIMALS_GRADE_SUBJECT
import com.gdavidpb.tuindice.base.utils.extensions.*
import com.gdavidpb.tuindice.evaluations.R
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationItem
import com.gdavidpb.tuindice.evaluations.presentation.viewmodel.EvaluationPlanViewModel
import com.gdavidpb.tuindice.evaluations.ui.adapters.EvaluationAdapter
import com.gdavidpb.tuindice.evaluations.utils.mappers.toEvaluationItem
import com.gdavidpb.tuindice.evaluations.utils.mappers.toUpdateRequest
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_evaluation_plan.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class EvaluationPlanFragment : NavigationFragment() {

	private val viewModel by viewModel<EvaluationPlanViewModel>()

	private val evaluationManager = EvaluationManager()

	private val evaluationAdapter = EvaluationAdapter(manager = evaluationManager)

	private val args by navArgs<EvaluationPlanFragmentArgs>()

	private object Flipper {
		const val CONTENT = 0
		const val EMPTY = 1
	}

	private object EvaluationPlanMenu {
		const val ID_ADD_EVALUATION = 0
		const val ID_USE_EVALUATION_PLAN_GRADE = 1
	}

	private object EvaluationMenu {
		const val ID_EDIT_EVALUATION = 0
		const val ID_MARK_EVALUATION_AS_DONE = 1
		const val ID_DELETE_EVALUATION = 2
	}

	override fun onCreateView() = R.layout.fragment_evaluation_plan

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		tViewSubjectName.text = args.subjectName
		tViewSubjectCode.text = args.subjectCode

		with(rViewEvaluations) {
			adapter = evaluationAdapter

			onScrollStateChanged { newState ->
				if (newState == SCROLL_STATE_IDLE)
					btnEvaluationsOptions.show()
				else
					btnEvaluationsOptions.hide()
			}
		}

		btnEvaluationsOptions.onClickOnce(::showEvaluationsMenuDialog)

		with(viewModel) {
			getSubjectEvaluations(sid = args.subjectId)
		}
	}

	override fun onInitObservers() {
		with(viewModel) {
			observe(evaluations, ::evaluationsObserver)
			observe(evaluationUpdate, ::evaluationObserver)
		}
	}

	private fun showEvaluationsMenuDialog() {
		val title =
			getString(R.string.label_evaluation_plan_header, args.subjectCode, args.subjectName)

		val items = mutableListOf(
			BottomMenuItem(
				itemId = EvaluationPlanMenu.ID_ADD_EVALUATION,
				textResource = R.string.menu_evaluation_add,
				iconResource = R.drawable.ic_add
			)
		).apply {
			if (evaluationAdapter.itemCount > 0)
				add(
					BottomMenuItem(
						itemId = EvaluationPlanMenu.ID_USE_EVALUATION_PLAN_GRADE,
						textResource = R.string.menu_evaluation_plan_use_grade,
						iconResource = R.drawable.ic_done
					)
				)
		}

		bottomSheetDialog<MenuBottomSheetDialog> {
			titleText = title

			onItemSelected(items) { itemId ->
				onEvaluationsOptionSelected(itemId)
			}
		}
	}

	private fun updateGrades(animate: Boolean) {
		val gradeSum = evaluationAdapter.computeGradeSum()

		if (animate) {
			tViewTotalGrade.animateGrade(value = gradeSum, decimals = DECIMALS_GRADE_SUBJECT)
			tViewGrade.animateGrade(value = gradeSum.toSubjectGrade())
		} else {
			tViewTotalGrade.text = gradeSum.formatGrade(DECIMALS_GRADE_SUBJECT)
			tViewGrade.text = gradeSum.toSubjectGrade().formatGrade()
		}

		fViewEvaluations.displayedChild =
			if (evaluationAdapter.itemCount > 0) Flipper.CONTENT else Flipper.EMPTY
	}

	private fun onEvaluationsOptionSelected(itemId: Int) {
		when (itemId) {
			EvaluationPlanMenu.ID_ADD_EVALUATION -> addEvaluation()
			EvaluationPlanMenu.ID_USE_EVALUATION_PLAN_GRADE -> useEvaluationPlanGrade()
		}
	}

	private fun showEvaluationMenuDialog(item: EvaluationItem, position: Int) {
		val title = getString(R.string.label_evaluation_header, item.notesText, item.dateText)

		val items = mutableListOf(
			BottomMenuItem(
				itemId = EvaluationMenu.ID_MARK_EVALUATION_AS_DONE,
				iconResource = if (item.isDone)
					R.drawable.ic_check_box_outline
				else
					R.drawable.ic_check_box,
				textResource = if (item.isDone)
					R.string.menu_evaluation_un_mark_as_done
				else
					R.string.menu_evaluation_mark_as_done
			),
			BottomMenuItem(
				itemId = EvaluationMenu.ID_DELETE_EVALUATION,
				iconResource = R.drawable.ic_delete,
				textResource = R.string.menu_evaluation_delete
			)
		).apply {
			if (!item.isDone)
				add(
					0, BottomMenuItem(
						itemId = EvaluationMenu.ID_EDIT_EVALUATION,
						iconResource = R.drawable.ic_edit,
						textResource = R.string.menu_evaluation_edit
					)
				)
		}

		bottomSheetDialog<MenuBottomSheetDialog> {
			titleText = title

			onItemSelected(items) { itemId ->
				onEvaluationOptionsSelected(item, position, itemId)
			}
		}
	}

	private fun onEvaluationOptionsSelected(item: EvaluationItem, position: Int, itemId: Int) {
		when (itemId) {
			EvaluationMenu.ID_EDIT_EVALUATION -> editEvaluation(item)
			EvaluationMenu.ID_MARK_EVALUATION_AS_DONE -> markEvaluation(item, !item.isDone, true)
			EvaluationMenu.ID_DELETE_EVALUATION -> deleteEvaluation(item, position)
		}
	}

	private fun addEvaluation() {
		navigate(
			EvaluationPlanFragmentDirections.navToEvaluation(
				title = getString(R.string.title_add_evaluation),
				quarterId = args.quarterId,
				subjectId = args.subjectId,
				subjectCode = args.subjectCode
			)
		)
	}

	private fun useEvaluationPlanGrade() {
		TODO("Not yet implemented")
	}

	private fun editEvaluation(item: EvaluationItem) {
		navigate(
			EvaluationPlanFragmentDirections.navToEvaluation(
				title = getString(R.string.title_edit_evaluation),
				quarterId = args.quarterId,
				subjectId = args.subjectId,
				subjectCode = args.subjectCode,
				evaluationId = item.id
			)
		)
	}

	private fun markEvaluation(item: EvaluationItem, done: Boolean, dispatchChanges: Boolean) {
		val request = item.data.toUpdateRequest(
			isDone = done,
			dispatchChanges = dispatchChanges
		)

		viewModel.updateEvaluation(request)
	}

	private fun deleteEvaluation(item: EvaluationItem, position: Int) {
		evaluationAdapter.removeEvaluation(item)

		snackBar {
			message = getString(R.string.snack_item_removed, item.typeText)

			action(R.string.snack_action_undone) {
				rViewEvaluations.scrollToPosition(0)

				evaluationAdapter.addEvaluation(item = item, position = position)
			}

			onDismissed { event ->
				if (event != Snackbar.Callback.DISMISS_EVENT_ACTION) {
					viewModel.removeEvaluation(id = item.id)
				}
			}
		}
	}

	private fun evaluationsObserver(result: Result<List<Evaluation>, Nothing>?) {
		when (result) {
			is Result.OnSuccess -> {
				val context = requireContext()
				val evaluations = result.value

				val items = evaluations.map { evaluation ->
					evaluation.toEvaluationItem(context)
				}

				evaluationAdapter.submitEvaluations(items)
			}
			else -> {}
		}
	}

	private fun quarterObserver(result: Result<Quarter, Nothing>?) {
		when (result) {
			is Result.OnSuccess -> {
				navigateUp()
			}
			else -> {}
		}
	}

	private fun evaluationObserver(result: Result<Evaluation, Nothing>?) {
		when (result) {
			is Result.OnSuccess -> {
				val context = requireContext()
				val evaluation = result.value
				val item = evaluation.toEvaluationItem(context)

				evaluationAdapter.updateEvaluation(item)
			}
			else -> {}
		}
	}

	inner class EvaluationManager : EvaluationAdapter.AdapterManager {
		override fun onEvaluationClicked(item: EvaluationItem) {
			editEvaluation(item)
		}

		override fun onEvaluationOptionsClicked(item: EvaluationItem, position: Int) {
			showEvaluationMenuDialog(item = item, position = position)
		}

		override fun onEvaluationGradeChanged(
			item: EvaluationItem,
			grade: Double,
			dispatchChanges: Boolean
		) {
			val request = item.data.toUpdateRequest(
				grade = grade,
				dispatchChanges = dispatchChanges
			)

			viewModel.updateEvaluation(request)
		}

		override fun onEvaluationDoneChanged(
			item: EvaluationItem,
			done: Boolean,
			dispatchChanges: Boolean
		) {
			markEvaluation(item, done, dispatchChanges)
		}

		override fun onEvaluationAdded(item: EvaluationItem) {
			updateGrades(true)
		}

		override fun onEvaluationRemoved(item: EvaluationItem) {
			updateGrades(true)
		}

		override fun onEvaluationUpdated(item: EvaluationItem) {
			updateGrades(false)
		}

		override fun onSubmitEvaluations(items: List<EvaluationItem>) {
			updateGrades(true)
		}
	}
}