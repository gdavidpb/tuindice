package com.gdavidpb.tuindice.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.gdavidpb.tuindice.domain.model.Evaluation
import com.gdavidpb.tuindice.domain.model.SubjectEvaluations
import com.gdavidpb.tuindice.domain.usecase.*
import com.gdavidpb.tuindice.domain.usecase.coroutines.Result
import com.gdavidpb.tuindice.domain.usecase.request.SubjectUpdateRequest
import com.gdavidpb.tuindice.utils.extensions.LiveCompletable
import com.gdavidpb.tuindice.utils.extensions.LiveResult
import com.gdavidpb.tuindice.utils.extensions.execute

class SubjectViewModel(
        private val getSubjectEvaluationsUseCase: GetSubjectEvaluationsUseCase,
        private val updateEvaluationUseCase: UpdateEvaluationUseCase,
        private val updateSubjectGradeUseCase: UpdateSubjectGradeUseCase,
        private val removeEvaluationUseCase: RemoveEvaluationUseCase,
        private val addEvaluationUseCase: AddEvaluationUseCase
) : ViewModel() {
    val evaluations = LiveResult<SubjectEvaluations>()
    val evaluationUpdate = LiveResult<Evaluation>()
    val subjectUpdate = LiveCompletable()
    val remove = LiveCompletable()
    val add = LiveResult<Evaluation>()

    fun updateSubjectGrade(sid: String, grade: Int) =
            execute(useCase = updateSubjectGradeUseCase, params = SubjectUpdateRequest(sid, grade), liveData = subjectUpdate)

    fun getSubjectEvaluations(sid: String) =
            execute(useCase = getSubjectEvaluationsUseCase, params = sid, liveData = evaluations)

    fun updateEvaluation(evaluation: Evaluation) =
            execute(useCase = updateEvaluationUseCase, params = evaluation, liveData = evaluationUpdate)

    fun removeEvaluation(id: String) =
            execute(useCase = removeEvaluationUseCase, params = id, liveData = remove)

    fun addEvaluation(evaluation: Evaluation) =
            execute(useCase = addEvaluationUseCase, params = evaluation, liveData = add)

    fun getSelectedSubject() = (evaluations.value as? Result.OnSuccess<SubjectEvaluations>)
            ?.run { value.subject }
}