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
import com.gdavidpb.tuindice.presentation.model.EvaluationItem
import com.gdavidpb.tuindice.presentation.viewmodel.SubjectViewModel
import com.gdavidpb.tuindice.ui.adapters.EvaluationAdapter
import com.gdavidpb.tuindice.utils.DECIMALS_GRADE_SUBJECT
import com.gdavidpb.tuindice.utils.extensions.*
import com.gdavidpb.tuindice.utils.mappers.toEvaluation
import com.gdavidpb.tuindice.utils.mappers.toEvaluationItem
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_subject.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class SubjectFragment : NavigationFragment() {

    private val viewModel by viewModel<SubjectViewModel>()

    private val evaluationManager = EvaluationManager()

    private val evaluationAdapter = EvaluationAdapter(manager = evaluationManager)

    private val args by navArgs<SubjectFragmentArgs>()

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
            observe(evaluationUpdate, ::updateEvaluationObserver)
            observe(subject, ::subjectObserver)
            observe(evaluations, ::evaluationsObserver)

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
                val subjectGrade = evaluationAdapter.computeGradeSum().toGrade()

                viewModel.updateSubject(sid = args.subjectId, grade = subjectGrade)

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
            tViewGrade.animateGrade(value = gradeSum.toGrade())
        } else {
            tViewTotalGrade.text = gradeSum.formatGrade(DECIMALS_GRADE_SUBJECT)
            tViewGrade.text = gradeSum.toGrade().formatGrade()
        }
    }

    private fun updateEvaluationObserver(result: Result<Evaluation, Nothing>?) {
        when (result) {
            is Result.OnSuccess -> {
                val context = requireContext()
                val response = result.value

                evaluationAdapter.replaceItem(item = response.toEvaluationItem(context))
            }
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

                evaluationAdapter.submitList(items)
            }
        }
    }

    inner class EvaluationManager : EvaluationAdapter.AdapterManager, ItemTouchHelper.Callback() {
        override fun onDataChanged() {
            updateGrades(animate = true)

            if (evaluationAdapter.itemCount > 0) {
                tViewEvaluations.gone()
                groupEvaluations.visible()

                evaluationAdapter.ensureSorting()
            } else {
                groupEvaluations.gone()
                tViewEvaluations.visible()
            }
        }

        override fun onDataUpdated() {
            updateGrades(animate = false)

            evaluationAdapter.ensureSorting()
        }

        override fun onEvaluationClicked(item: EvaluationItem, position: Int) {
            navigate(SubjectFragmentDirections.navToEvaluation(
                    title = getString(R.string.title_edit_evaluation),
                    subjectId = args.subjectId,
                    subjectCode = args.subjectCode,
                    evaluationId = item.id
            ))
        }

        override fun onEvaluationChanged(item: EvaluationItem, position: Int, dispatchChanges: Boolean) {
            evaluationAdapter.replaceItemAt(item = item, position = position)

            if (dispatchChanges)
                viewModel.updateEvaluation(evaluation = item.toEvaluation())
        }

        override fun onEvaluationDoneChanged(item: EvaluationItem, position: Int, dispatchChanges: Boolean) {
            evaluationAdapter.replaceItemAt(item = item, position = position)

            if (dispatchChanges)
                viewModel.updateEvaluation(evaluation = item.toEvaluation())
        }

        override fun getItem(position: Int): EvaluationItem {
            return evaluationAdapter.getItem(position)
        }

        override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
            val position = viewHolder.adapterPosition
            val item = evaluationAdapter.getItem(position)

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

            if (position == RecyclerView.NO_POSITION) return

            val item = evaluationAdapter.getItem(position)

            evaluationAdapter.removeItemAt(position)

            snackBar {
                message = getString(R.string.snack_bar_message_item_removed, item.typeText)

                action(R.string.snack_bar_action_undone) {
                    rViewEvaluations.scrollToPosition(0)

                    val updatedItem = item.copy(isSwiping = false)

                    evaluationAdapter.addItemAt(item = updatedItem, position = position)
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

            if (actionState == ACTION_STATE_SWIPE) {
                val position = viewHolder.adapterPosition

                if (position == RecyclerView.NO_POSITION) return

                val item = evaluationAdapter.getItem(position)
                val updatedItem = item.copy(isSwiping = true)

                evaluationAdapter.replaceItemAt(item = updatedItem, position = position)
            }
        }

        override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
            super.clearView(recyclerView, viewHolder)

            val position = viewHolder.adapterPosition

            if (position == RecyclerView.NO_POSITION) return

            val item = evaluationAdapter.getItem(position)
            val updatedItem = item.copy(isSwiping = false)

            evaluationAdapter.replaceItemAt(item = updatedItem, position = position)
        }
    }
}