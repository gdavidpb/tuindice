package com.gdavidpb.tuindice.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.gdavidpb.tuindice.domain.model.Evaluation
import com.gdavidpb.tuindice.domain.model.Subject
import com.gdavidpb.tuindice.domain.usecase.*
import com.gdavidpb.tuindice.domain.usecase.request.SubjectUpdateRequest
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
    val evaluationUpdate = LiveResult<Evaluation, Nothing>()
    val subjectUpdate = LiveCompletable<Nothing>()
    val remove = LiveCompletable<Nothing>()

    fun getSubject(sid: String) =
            execute(useCase = getSubjectUseCase, params = sid, liveData = subject)

    fun updateSubject(sid: String, grade: Int) =
            execute(useCase = updateSubjectUseCase, params = SubjectUpdateRequest(sid, grade), liveData = subjectUpdate)

    fun getSubjectEvaluations(sid: String) =
            execute(useCase = getSubjectEvaluationsUseCase, params = sid, liveData = evaluations)

    fun updateEvaluation(evaluation: Evaluation) =
            execute(useCase = updateEvaluationUseCase, params = evaluation, liveData = evaluationUpdate)

    fun removeEvaluation(id: String) =
            execute(useCase = removeEvaluationUseCase, params = id, liveData = remove)
}