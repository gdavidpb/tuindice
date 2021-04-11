package com.gdavidpb.tuindice.domain.usecase

import com.gdavidpb.tuindice.data.model.database.QuarterUpdate
import com.gdavidpb.tuindice.data.model.database.SubjectUpdate
import com.gdavidpb.tuindice.domain.model.Quarter
import com.gdavidpb.tuindice.domain.repository.AuthRepository
import com.gdavidpb.tuindice.domain.repository.DatabaseRepository
import com.gdavidpb.tuindice.domain.usecase.coroutines.ResultUseCase
import com.gdavidpb.tuindice.domain.usecase.request.UpdateQuarterRequest
import com.gdavidpb.tuindice.utils.extensions.computeCredits
import com.gdavidpb.tuindice.utils.extensions.computeGrade
import com.gdavidpb.tuindice.utils.extensions.computeGradeSum
import com.gdavidpb.tuindice.utils.mappers.applyUpdate

// TODO optimize use case
class UpdateQuarterUseCase(
        private val authRepository: AuthRepository,
        private val databaseRepository: DatabaseRepository
) : ResultUseCase<UpdateQuarterRequest, Quarter, Nothing>() {
    override suspend fun executeOnBackground(params: UpdateQuarterRequest): Quarter? {
        val activeUId = authRepository.getActiveAuth().uid

        return if (params.dispatchChanges) {
            val subjectUpdate = SubjectUpdate(grade = params.update.grade)

            databaseRepository.updateSubject(uid = activeUId, sid = params.sid, update = subjectUpdate)

            val quarters = databaseRepository.getQuarters(uid = activeUId)
            val quarter = quarters.find { it.id == params.qid }

            checkNotNull(quarter)

            val grade = quarter.subjects.computeGrade()
            val gradeSum = quarters.computeGradeSum(until = quarter)
            val credits = quarter.subjects.computeCredits()

            val quarterUpdate = QuarterUpdate(grade = grade, gradeSum = gradeSum, credits = credits)

            databaseRepository.updateQuarter(uid = activeUId, qid = params.qid, update = quarterUpdate)
        } else {
            val quarters = databaseRepository.getQuarters(uid = activeUId)
            val quarter = quarters.find { it.id == params.qid }

            checkNotNull(quarter)

            val subject = quarter.subjects.find { it.id == params.sid }
            val subjectIndex = quarter.subjects.indexOfFirst { it.id == params.sid }

            checkNotNull(subject)

            val updatedSubject = subject.copy(grade = params.update.grade)

            quarter.subjects[subjectIndex] = updatedSubject

            val grade = quarter.subjects.computeGrade()
            val gradeSum = quarters.computeGradeSum(until = quarter)
            val credits = quarter.subjects.computeCredits()

            val quarterUpdate = QuarterUpdate(grade = grade, gradeSum = gradeSum, credits = credits)

            quarter.applyUpdate(quarterUpdate)
        }
    }
}