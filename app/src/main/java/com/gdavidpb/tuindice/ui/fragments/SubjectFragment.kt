package com.gdavidpb.tuindice.ui.fragments

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_IDLE
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.domain.model.Evaluation
import com.gdavidpb.tuindice.domain.model.EvaluationItem
import com.gdavidpb.tuindice.domain.model.SubjectEvaluations
import com.gdavidpb.tuindice.domain.usecase.coroutines.Result
import com.gdavidpb.tuindice.presentation.viewmodel.SubjectViewModel
import com.gdavidpb.tuindice.ui.adapters.EvaluationAdapter
import com.gdavidpb.tuindice.ui.dialogs.AddEvaluationDialog
import com.gdavidpb.tuindice.ui.dialogs.CalendarDialog
import com.gdavidpb.tuindice.utils.ARG_SUBJECT_ID
import com.gdavidpb.tuindice.utils.extensions.*
import com.gdavidpb.tuindice.utils.mappers.toEvaluation
import com.gdavidpb.tuindice.utils.mappers.toEvaluationItem
import kotlinx.android.synthetic.main.fragment_subject.*
import org.jetbrains.anko.support.v4.longToast
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*

open class SubjectFragment : Fragment() {

    private val viewModel by viewModel<SubjectViewModel>()

    private val evaluationManager = EvaluationManager()

    private val evaluationAdapter = EvaluationAdapter(manager = evaluationManager)

    private val subjectId by lazy {
        requireArguments().getString(ARG_SUBJECT_ID, "")
    }

    private val cachedColors by lazy {
        mapOf(
                true to requireContext().getCompatColor(R.color.color_retired),
                false to requireContext().getCompatColor(R.color.color_approved)
        )
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
                val response = result.value

                evaluationAdapter.addSortedItem(
                        item = response.toEvaluationItem(),
                        comparator = compareBy(EvaluationItem::isDone, EvaluationItem::date)
                )
            }
            is Result.OnError -> longToast(R.string.toast_try_again_later)
        }
    }

    private fun evaluationsObserver(result: Result<SubjectEvaluations>?) {
        when (result) {
            is Result.OnLoading -> {

            }
            is Result.OnSuccess -> {
                val response = result.value
                val subject = response.subject
                val evaluations = response.evaluations

                tViewSubjectName.text = subject.name
                tViewSubjectCode.text = subject.code

                val items = evaluations.map { evaluation ->
                    evaluation.toEvaluationItem()
                }

                evaluationAdapter.swapItems(new = items)

                updateGrades()

                if (evaluations.isEmpty()) {
                    rViewEvaluations.gone()
                    tViewEvaluations.visible()
                } else {
                    tViewEvaluations.gone()
                    rViewEvaluations.visible()
                }
            }
            is Result.OnError -> {

            }
        }
    }

    inner class EvaluationManager : EvaluationAdapter.AdapterManager {
        override fun onEvaluationChanged(item: EvaluationItem, position: Int) {
            evaluationAdapter.replaceItemAt(item, position, true)

            updateGrades()

            viewModel.updateEvaluation(evaluation = item.toEvaluation())
        }

        override fun onEvaluationDoneChanged(item: EvaluationItem, position: Int) {
            evaluationAdapter.replaceItemAt(item, position, true)

            evaluationAdapter.sortItemAt(
                    position = position,
                    comparator = compareBy(EvaluationItem::isDone, EvaluationItem::date)
            )

            viewModel.updateEvaluation(evaluation = item.toEvaluation())
        }

        override fun resolveDoneColor(): Int {
            return cachedColors.getValue(true)
        }

        override fun resolvePendingColor(): Int {
            return cachedColors.getValue(false)
        }
    }
}