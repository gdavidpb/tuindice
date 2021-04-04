package com.gdavidpb.tuindice.domain.usecase

import com.gdavidpb.tuindice.domain.model.Quarter
import com.gdavidpb.tuindice.domain.model.QuarterUpdate
import com.gdavidpb.tuindice.domain.model.SubjectUpdate
import com.gdavidpb.tuindice.domain.repository.AuthRepository
import com.gdavidpb.tuindice.domain.repository.DatabaseRepository
import com.gdavidpb.tuindice.domain.usecase.coroutines.ResultUseCase
import com.gdavidpb.tuindice.domain.usecase.request.UpdateQuarterRequest
import com.gdavidpb.tuindice.utils.extensions.computeGrade
import com.gdavidpb.tuindice.utils.extensions.computeGradeSum

// TODO optimize use case
open class UpdateQuarterUseCase(
        private val authRepository: AuthRepository,
        private val databaseRepository: DatabaseRepository
) : ResultUseCase<UpdateQuarterRequest, Quarter, Nothing>() {
    override suspend fun executeOnBackground(params: UpdateQuarterRequest): Quarter? {
        val activeUId = authRepository.getActiveAuth().uid

        return if (params.dispatchChanges) {
            val subjectUpdate = SubjectUpdate(sid = params.sid, grade = params.grade)

            databaseRepository.updateSubject(uid = activeUId, update = subjectUpdate)

            val quarters = databaseRepository.getQuarters(uid = activeUId)
            val quarter = quarters.find { it.id == params.qid }

            checkNotNull(quarter)

            val grade = quarter.subjects.computeGrade()
            val gradeSum = quarters.computeGradeSum(until = quarter)

            val quarterUpdate = QuarterUpdate(qid = params.qid, grade = grade, gradeSum = gradeSum)

            databaseRepository.updateQuarter(uid = activeUId, update = quarterUpdate)
        } else {
            val quarters = databaseRepository.getQuarters(uid = activeUId)
            val quarter = quarters.find { it.id == params.qid }

            checkNotNull(quarter)

            val subject = quarter.subjects.find { it.id == params.sid }
            val subjectIndex = quarter.subjects.indexOfFirst { it.id == params.sid }

            checkNotNull(subject)

            val updatedSubject = subject.copy(grade = params.grade)

            quarter.subjects[subjectIndex] = updatedSubject

            val grade = quarter.subjects.computeGrade()
            val gradeSum = quarters.computeGradeSum(until = quarter)

            quarter.copy(grade = grade, gradeSum = gradeSum)
        }
    }
}