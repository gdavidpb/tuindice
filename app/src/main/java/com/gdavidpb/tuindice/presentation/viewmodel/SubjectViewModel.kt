package com.gdavidpb.tuindice.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.gdavidpb.tuindice.domain.model.Evaluation
import com.gdavidpb.tuindice.domain.model.SubjectEvaluations
import com.gdavidpb.tuindice.domain.usecase.GetSubjectEvaluationsUseCase
import com.gdavidpb.tuindice.domain.usecase.UpdateEvaluationUseCase
import com.gdavidpb.tuindice.utils.extensions.LiveCompletable
import com.gdavidpb.tuindice.utils.extensions.LiveResult
import com.gdavidpb.tuindice.utils.extensions.execute
import com.gdavidpb.tuindice.domain.usecase.coroutines.Result

class SubjectViewModel(
        private val getSubjectEvaluationsUseCase: GetSubjectEvaluationsUseCase,
        private val updateEvaluationUseCase: UpdateEvaluationUseCase
) : ViewModel() {
    val evaluations = LiveResult<SubjectEvaluations>()
    val update = LiveCompletable()

    fun getSubjectEvaluations(sid: String) =
            execute(useCase = getSubjectEvaluationsUseCase, params = sid, liveData = evaluations)

    fun updateEvaluation(evaluation: Evaluation) =
            execute(useCase = updateEvaluationUseCase, params = evaluation, liveData = update)

    fun getSelectedSubject() = (evaluations.value as? Result.OnSuccess<SubjectEvaluations>)
            ?.run { value.subject }
}