package com.gdavidpb.tuindice.ui.fragments

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.ACTION_STATE_SWIPE
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_IDLE
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.domain.model.Evaluation
import com.gdavidpb.tuindice.domain.model.SubjectEvaluations
import com.gdavidpb.tuindice.domain.usecase.coroutines.Result
import com.gdavidpb.tuindice.presentation.model.EvaluationItem
import com.gdavidpb.tuindice.presentation.viewmodel.SubjectViewModel
import com.gdavidpb.tuindice.ui.adapters.EvaluationAdapter
import com.gdavidpb.tuindice.ui.dialogs.AddEvaluationDialog
import com.gdavidpb.tuindice.ui.dialogs.CalendarDialog
import com.gdavidpb.tuindice.utils.ARG_SUBJECT_ID
import com.gdavidpb.tuindice.utils.extensions.*
import com.gdavidpb.tuindice.utils.mappers.toEvaluation
import com.gdavidpb.tuindice.utils.mappers.toEvaluationItem
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_subject.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*

open class SubjectFragment : Fragment() {

    private val viewModel by viewModel<SubjectViewModel>()

    private val evaluationManager = EvaluationManager()

    private val evaluationAdapter = EvaluationAdapter(manager = evaluationManager)

    private val subjectId by lazy {
        requireArguments().getString(ARG_SUBJECT_ID, "")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_subject, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
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
            observe(add, ::addEvaluationObserver)
            observe(evaluations, ::evaluationsObserver)

            getSubjectEvaluations(sid = subjectId)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.subject_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_calendar -> {
                val startDate = Date().add(Calendar.YEAR, -1)
                val endDate = Date().add(Calendar.YEAR, 1)

                CalendarDialog(startDate, endDate).show(childFragmentManager)

                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun onAddEvaluationClicked() {
        val selectedSubject = viewModel.getSelectedSubject() ?: return

        AddEvaluationDialog(subject = selectedSubject) { newEvaluation ->
            viewModel.addEvaluation(evaluation = newEvaluation.toEvaluation())
        }.show(childFragmentManager)
    }

    private fun updateGrades() {
        val gradeSum = evaluationAdapter.computeGradeSum()

        tViewTotalGrade.animateGrade(value = gradeSum)
        tViewGrade.animateGrade(value = gradeSum.toGrade())
    }

    private fun addEvaluationObserver(result: Result<Evaluation>?) {
        when (result) {
            is Result.OnSuccess -> {
                val context = requireContext()
                val response = result.value

                evaluationAdapter.addSortedItem(
                        item = response.toEvaluationItem(context),
                        comparator = compareBy(EvaluationItem::isDone, EvaluationItem::date)
                )

                updateGrades()
            }
            is Result.OnError -> longToast(R.string.toast_try_again_later)
        }
    }

    private fun evaluationsObserver(result: Result<SubjectEvaluations>?) {
        when (result) {
            is Result.OnLoading -> {

            }
            is Result.OnSuccess -> {
                val context = requireContext()
                val response = result.value
                val subject = response.subject
                val evaluations = response.evaluations

                tViewSubjectName.text = subject.name
                tViewSubjectCode.text = subject.code

                val items = evaluations.map { evaluation ->
                    evaluation.toEvaluationItem(context)
                }

                evaluationAdapter.swapItems(new = items)

                updateGrades()

                if (evaluations.isEmpty()) {
                    groupEvaluations.gone()
                    tViewEvaluations.visible()
                } else {
                    tViewEvaluations.gone()
                    groupEvaluations.visible()
                }
            }
            is Result.OnError -> {

            }
        }
    }

    inner class EvaluationManager : EvaluationAdapter.AdapterManager, ItemTouchHelper.Callback() {
        override fun onEvaluationChanged(item: EvaluationItem, position: Int, dispatchChanges: Boolean) {
            evaluationAdapter.replaceItemAt(item, position, true)

            updateGrades()

            if (dispatchChanges)
                viewModel.updateEvaluation(evaluation = item.toEvaluation())
        }

        override fun onEvaluationDoneChanged(item: EvaluationItem, position: Int, dispatchChanges: Boolean) {
            evaluationAdapter.replaceItemAt(item, position, true)

            evaluationAdapter.sortItemAt(
                    position = position,
                    comparator = compareBy(EvaluationItem::isDone, EvaluationItem::date)
            )

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

                action(text = getString(R.string.snack_bar_action_undone)) {
                    rViewEvaluations.scrollToPosition(0)

                    val updatedItem = item.copy(isSwiping = false)

                    evaluationAdapter.addItemAt(updatedItem, position)
                }

                onDismissed { event ->
                    if (event != Snackbar.Callback.DISMISS_EVENT_ACTION) {
                        if (isAdded) updateGrades()

                        viewModel.removeEvaluation(id = item.id)
                    }
                }
            }.build().show()
        }

        override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
            super.onSelectedChanged(viewHolder, actionState)

            viewHolder ?: return

            if (actionState == ACTION_STATE_SWIPE) {
                val position = viewHolder.adapterPosition

                if (position == RecyclerView.NO_POSITION) return

                val item = evaluationAdapter.getItem(position)
                val updatedItem = item.copy(isSwiping = true)

                evaluationAdapter.replaceItemAt(updatedItem, position)
            }
        }

        override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
            super.clearView(recyclerView, viewHolder)

            val position = viewHolder.adapterPosition

            if (position == RecyclerView.NO_POSITION) return

            val item = evaluationAdapter.getItem(position)
            val updatedItem = item.copy(isSwiping = false)

            evaluationAdapter.replaceItemAt(updatedItem, position)
        }
    }
}