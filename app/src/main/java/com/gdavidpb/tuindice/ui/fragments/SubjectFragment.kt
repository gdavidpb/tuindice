package com.gdavidpb.tuindice.ui.fragments

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.ACTION_STATE_SWIPE
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_IDLE
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.domain.model.Evaluation
import com.gdavidpb.tuindice.domain.model.Subject
import com.gdavidpb.tuindice.domain.usecase.coroutines.Result
import com.gdavidpb.tuindice.domain.usecase.request.UpdateSubjectRequest
import com.gdavidpb.tuindice.presentation.model.EvaluationItem
import com.gdavidpb.tuindice.presentation.viewmodel.SubjectViewModel
import com.gdavidpb.tuindice.ui.adapters.EvaluationAdapter
import com.gdavidpb.tuindice.utils.DECIMALS_GRADE_SUBJECT
import com.gdavidpb.tuindice.utils.extensions.*
import com.gdavidpb.tuindice.utils.mappers.toEvaluationItem
import com.gdavidpb.tuindice.utils.mappers.toUpdateRequest
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_subject.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class SubjectFragment : NavigationFragment() {

    private val viewModel by viewModel<SubjectViewModel>()

    private val evaluationManager = EvaluationManager()

    private val evaluationAdapter = EvaluationAdapter(manager = evaluationManager)

    private val args by navArgs<SubjectFragmentArgs>()

    private object Flipper {
        const val CONTENT = 0
        const val EMPTY = 1
    }

    override fun onCreateView() = R.layout.fragment_subject

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setHasOptionsMenu(true)

        with(rViewEvaluations) {
            layoutManager = LinearLayoutManager(context)
            adapter = evaluationAdapter

            onScrollStateChanged { newState ->
                if (newState == SCROLL_STATE_IDLE)
                    btnAddEvaluation.show()
                else
                    btnAddEvaluation.hide()
            }

            ItemTouchHelper(evaluationManager).attachToRecyclerView(this)
        }

        btnAddEvaluation.onClickOnce(::onAddEvaluationClicked)

        with(viewModel) {
            observe(subject, ::subjectObserver)
            observe(evaluations, ::evaluationsObserver)
            observe(evaluationUpdate, ::evaluationObserver)

            getSubject(sid = args.subjectId)
            getSubjectEvaluations(sid = args.subjectId)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_subject, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_done -> {
                val request = UpdateSubjectRequest(
                        id = args.subjectId,
                        grade = evaluationAdapter.computeGradeSum().toSubjectGrade()
                )

                viewModel.updateSubject(request)

                navigateUp()

                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun onAddEvaluationClicked() {
        navigate(SubjectFragmentDirections.navToEvaluation(
                title = getString(R.string.title_add_evaluation),
                subjectId = args.subjectId,
                subjectCode = args.subjectCode
        ))
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
    }

    private fun subjectObserver(result: Result<Subject, Nothing>?) {
        when (result) {
            is Result.OnSuccess -> {
                val subject = result.value

                tViewSubjectName.text = subject.name
                tViewSubjectCode.text = subject.code
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
            navigate(SubjectFragmentDirections.navToEvaluation(
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

            fViewEvaluations.displayedChild = if (items.isNotEmpty()) Flipper.CONTENT else Flipper.EMPTY
        }

        override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
            val position = viewHolder.adapterPosition
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
            val position = viewHolder.adapterPosition
            val item = evaluationAdapter.getEvaluation(position)

            evaluationAdapter.removeEvaluation(item)

            snackBar {
                message = getString(R.string.snack_bar_message_item_removed, item.typeText)

                action(R.string.snack_bar_action_undone) {
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

            val position = viewHolder.adapterPosition

            if (position == RecyclerView.NO_POSITION) return

            val item = evaluationAdapter.getEvaluation(position)

            evaluationAdapter.updateEvaluation(item.copy(isSwiping = true))
        }

        override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
            super.clearView(recyclerView, viewHolder)

            val position = viewHolder.adapterPosition

            if (position == RecyclerView.NO_POSITION) return

            val item = evaluationAdapter.getEvaluation(position)

            evaluationAdapter.updateEvaluation(item.copy(isSwiping = false))
        }
    }
}