package com.gdavidpb.tuindice.record.domain.usecase

import com.gdavidpb.tuindice.base.data.model.database.QuarterUpdate
import com.gdavidpb.tuindice.base.data.model.database.SubjectUpdate
import com.gdavidpb.tuindice.base.domain.model.Quarter
import com.gdavidpb.tuindice.base.domain.repository.AuthRepository
import com.gdavidpb.tuindice.base.domain.repository.DatabaseRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.ResultUseCase
import com.gdavidpb.tuindice.base.utils.ComputationManager
import com.gdavidpb.tuindice.record.domain.request.UpdateQuarterRequest

class UpdateQuarterUseCase(
	private val authRepository: AuthRepository,
	private val databaseRepository: DatabaseRepository
) : ResultUseCase<UpdateQuarterRequest, Quarter, Nothing>() {
	override suspend fun executeOnBackground(params: UpdateQuarterRequest): Quarter {
		val activeUId = authRepository.getActiveAuth().uid

		val computedQuarter = ComputationManager.computeQuarter(
			quarterId = params.qid,
			subjectId = params.sid,
			grade = params.update.grade
		)

		if (params.dispatchChanges) {
			val subjectUpdate = SubjectUpdate(
				grade = params.update.grade
			)

			val quarterUpdate = QuarterUpdate(
				grade = computedQuarter.grade,
				gradeSum = computedQuarter.gradeSum,
				credits = computedQuarter.credits
			)

			databaseRepository.runBatch {
				updateSubject(uid = activeUId, sid = params.sid, update = subjectUpdate)
				updateQuarter(uid = activeUId, qid = params.qid, update = quarterUpdate)
			}
		}

		return computedQuarter
	}
}