package com.gdavidpb.tuindice.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.domain.model.Evaluation
import com.gdavidpb.tuindice.domain.model.SubjectEvaluations
import com.gdavidpb.tuindice.presentation.viewmodel.SubjectViewModel
import com.gdavidpb.tuindice.domain.usecase.coroutines.Result
import com.gdavidpb.tuindice.ui.adapters.EvaluationAdapter
import com.gdavidpb.tuindice.utils.ARG_SUBJECT_ID
import com.gdavidpb.tuindice.utils.extensions.animateGrade
import com.gdavidpb.tuindice.utils.extensions.gone
import com.gdavidpb.tuindice.utils.extensions.observe
import com.gdavidpb.tuindice.utils.extensions.visible
import kotlinx.android.synthetic.main.fragment_subject.*
import org.koin.androidx.viewmodel.ext.android.viewModel

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
        super.onViewCreated(view, savedInstanceState)

        setHasOptionsMenu(false)

        with(rViewEvaluations) {
            layoutManager = LinearLayoutManager(context)
            adapter = evaluationAdapter
        }

        with(viewModel) {
            observe(evaluations, ::evaluationsObserver)

            getSubjectEvaluations(sid = subjectId)
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

                tViewGrade.animateGrade(5)
                tViewTotalGrade.animateGrade(89)

                evaluationAdapter.swapItems(new = evaluations)

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
        override fun onEvaluationChanged(item: Evaluation, position: Int) {

        }
    }
}