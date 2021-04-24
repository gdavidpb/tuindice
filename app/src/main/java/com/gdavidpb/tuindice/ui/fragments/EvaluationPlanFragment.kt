package com.gdavidpb.tuindice.ui.fragments

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.ACTION_STATE_SWIPE
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_IDLE
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.data.model.database.SubjectUpdate
import com.gdavidpb.tuindice.domain.model.Evaluation
import com.gdavidpb.tuindice.domain.model.Quarter
import com.gdavidpb.tuindice.domain.usecase.coroutines.Result
import com.gdavidpb.tuindice.domain.usecase.request.UpdateQuarterRequest
import com.gdavidpb.tuindice.presentation.model.BottomMenuItem
import com.gdavidpb.tuindice.presentation.model.EvaluationItem
import com.gdavidpb.tuindice.presentation.viewmodel.EvaluationPlanViewModel
import com.gdavidpb.tuindice.ui.adapters.EvaluationAdapter
import com.gdavidpb.tuindice.ui.dialogs.MenuBottomSheetDialog
import com.gdavidpb.tuindice.utils.DECIMALS_GRADE_SUBJECT
import com.gdavidpb.tuindice.utils.extensions.*
import com.gdavidpb.tuindice.utils.mappers.toEvaluationItem
import com.gdavidpb.tuindice.utils.mappers.toUpdateRequest
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

    private object EvaluationsMenu {
        const val ID_ADD_EVALUATION = 0
        const val ID_USE_EVALUATION_PLAN_GRADE = 1
    }

    override fun onCreateView() = R.layout.fragment_evaluation_plan

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setHasOptionsMenu(false)

        tViewSubjectName.text = args.subjectName
        tViewSubjectCode.text = args.subjectCode

        with(rViewEvaluations) {
            adapter = evaluationAdapter

            onScrollStateChanged { newState ->
                if (newState == SCROLL_STATE_IDLE)
                    btnEvaluationOptions.show()
                else
                    btnEvaluationOptions.hide()
            }

            ItemTouchHelper(evaluationManager).attachToRecyclerView(this)
        }

        btnEvaluationOptions.onClickOnce(::showEvaluationsMenuDialog)

        with(viewModel) {
            getSubjectEvaluations(sid = args.subjectId)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        with(viewModel) {
            observe(evaluations, ::evaluationsObserver)
            observe(quarterUpdate, ::quarterObserver)
            observe(evaluationUpdate, ::evaluationObserver)
        }
    }

    private fun showEvaluationsMenuDialog() {
        val title = getString(R.string.label_evaluation_plan_header, args.subjectCode, args.subjectName)

        val items = mutableListOf(
                BottomMenuItem(
                        itemId = EvaluationsMenu.ID_ADD_EVALUATION,
                        textResource = R.string.menu_evaluation_add,
                        iconResource = R.drawable.ic_add
                )
        ).apply {
            if (evaluationAdapter.itemCount > 0)
                add(BottomMenuItem(
                        itemId = EvaluationsMenu.ID_USE_EVALUATION_PLAN_GRADE,
                        textResource = R.string.menu_evaluation_plan_use_grade,
                        iconResource = R.drawable.ic_done
                ))
        }

        bottomSheetDialog<MenuBottomSheetDialog> {
            titleText = title

            onItemSelected(items) { itemId ->
                onEvaluationsOptionSelected(itemId)
            }
        }
    }

    private fun onEvaluationsOptionSelected(itemId: Int) {
        when (itemId) {
            EvaluationsMenu.ID_ADD_EVALUATION -> {
                navigate(EvaluationPlanFragmentDirections.navToEvaluation(
                        title = getString(R.string.title_add_evaluation),
                        subjectId = args.subjectId,
                        subjectCode = args.subjectCode
                ))
            }
            EvaluationsMenu.ID_USE_EVALUATION_PLAN_GRADE -> {
                val update = SubjectUpdate(
                        grade = evaluationAdapter.computeGradeSum().toSubjectGrade()
                )

                val request = UpdateQuarterRequest(
                        qid = args.quarterId,
                        sid = args.subjectId,
                        update = update,
                        dispatchChanges = true
                )

                viewModel.updateQuarter(request)
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

        fViewEvaluations.displayedChild = if (evaluationAdapter.itemCount > 0) Flipper.CONTENT else Flipper.EMPTY
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
        }
    }

    private fun quarterObserver(result: Result<Quarter, Nothing>?) {
        when (result) {
            is Result.OnSuccess -> {
                navigateUp()
            }
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
        }
    }

    inner class EvaluationManager : EvaluationAdapter.AdapterManager, ItemTouchHelper.Callback() {
        override fun onEvaluationClicked(item: EvaluationItem) {
            navigate(EvaluationPlanFragmentDirections.navToEvaluation(
                    title = getString(R.string.title_edit_evaluation),
                    subjectId = args.subjectId,
                    subjectCode = args.subjectCode,
                    evaluationId = item.id
            ))
        }

        override fun onEvaluationGradeChanged(item: EvaluationItem, grade: Double, dispatchChanges: Boolean) {
            val request = item.data.toUpdateRequest(
                    grade = grade,
                    dispatchChanges = dispatchChanges
            )

            viewModel.updateEvaluation(request)
        }

        override fun onEvaluationDoneChanged(item: EvaluationItem, done: Boolean, dispatchChanges: Boolean) {
            val request = item.data.toUpdateRequest(
                    isDone = done,
                    dispatchChanges = dispatchChanges
            )

            viewModel.updateEvaluation(request)
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

        override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
            val position = viewHolder.absoluteAdapterPosition
            val item = evaluationAdapter.getEvaluation(position)

            return if (item.isDone)
                return makeMovementFlags(0, ItemTouchHelper.RIGHT or ItemTouchHelper.LEFT)
            else
                0
        }

        override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
            return false
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val position = viewHolder.absoluteAdapterPosition
            val item = evaluationAdapter.getEvaluation(position)

            evaluationAdapter.removeEvaluation(item)

            snackBar {
                message = getString(R.string.snack_item_removed, item.typeText)

                action(R.string.snack_action_undone) {
                    rViewEvaluations.scrollToPosition(0)

                    val updatedItem = item.copy(isSwiping = false)

                    evaluationAdapter.addEvaluation(item = updatedItem, position = position)
                }

                onDismissed { event ->
                    if (event != Snackbar.Callback.DISMISS_EVENT_ACTION) {
                        viewModel.removeEvaluation(id = item.id)
                    }
                }
            }
        }

        override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
            super.onSelectedChanged(viewHolder, actionState)

            viewHolder ?: return

            if (actionState != ACTION_STATE_SWIPE) return

            val position = viewHolder.absoluteAdapterPosition

            if (position == RecyclerView.NO_POSITION) return

            val item = evaluationAdapter.getEvaluation(position)

            evaluationAdapter.updateEvaluation(item.copy(isSwiping = true))
        }

        override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
            super.clearView(recyclerView, viewHolder)

            val position = viewHolder.absoluteAdapterPosition

            if (position == RecyclerView.NO_POSITION) return

            val item = evaluationAdapter.getEvaluation(position)

            evaluationAdapter.updateEvaluation(item.copy(isSwiping = false))
        }
    }
}