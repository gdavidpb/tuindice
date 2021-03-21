package com.gdavidpb.tuindice.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.gdavidpb.tuindice.domain.model.Evaluation
import com.gdavidpb.tuindice.domain.model.Subject
import com.gdavidpb.tuindice.domain.usecase.*
import com.gdavidpb.tuindice.domain.usecase.request.UpdateEvaluationRequest
import com.gdavidpb.tuindice.domain.usecase.request.UpdateSubjectRequest
import com.gdavidpb.tuindice.utils.extensions.LiveCompletable
import com.gdavidpb.tuindice.utils.extensions.LiveResult
import com.gdavidpb.tuindice.utils.extensions.execute

class SubjectViewModel(
        private val getSubjectUseCase: GetSubjectUseCase,
        private val getSubjectEvaluationsUseCase: GetSubjectEvaluationsUseCase,
        private val updateEvaluationUseCase: UpdateEvaluationUseCase,
        private val updateSubjectUseCase: UpdateSubjectUseCase,
        private val removeEvaluationUseCase: RemoveEvaluationUseCase
) : ViewModel() {
    val subject = LiveResult<Subject, Nothing>()
    val evaluations = LiveResult<List<Evaluation>, Nothing>()
    val evaluationUpdate = LiveCompletable<Nothing>()
    val subjectUpdate = LiveCompletable<Nothing>()
    val evaluationRemove = LiveCompletable<Nothing>()

    fun getSubject(sid: String) =
            execute(useCase = getSubjectUseCase, params = sid, liveData = subject)

    fun updateSubject(request: UpdateSubjectRequest) =
            execute(useCase = updateSubjectUseCase, params = request, liveData = subjectUpdate)

    fun getSubjectEvaluations(sid: String) =
            execute(useCase = getSubjectEvaluationsUseCase, params = sid, liveData = evaluations)

    fun updateEvaluation(request: UpdateEvaluationRequest) =
            execute(useCase = updateEvaluationUseCase, params = request, liveData = evaluationUpdate)

    fun removeEvaluation(id: String) =
            execute(useCase = removeEvaluationUseCase, params = id, liveData = evaluationRemove)
}