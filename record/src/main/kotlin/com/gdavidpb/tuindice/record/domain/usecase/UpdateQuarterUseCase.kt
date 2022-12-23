package com.gdavidpb.tuindice.record.domain.usecase

import com.gdavidpb.tuindice.base.domain.model.Quarter
import com.gdavidpb.tuindice.base.domain.usecase.base.ResultUseCase
import com.gdavidpb.tuindice.record.domain.request.UpdateQuarterRequest

class UpdateQuarterUseCase(
) : ResultUseCase<UpdateQuarterRequest, Quarter, Nothing>() {
	override suspend fun executeOnBackground(params: UpdateQuarterRequest): Quarter {
		TODO("Not yet implemented")
		/*
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

			databaseRepository.updateSubject(
				uid = activeUId,
				sid = params.sid,
				update = subjectUpdate
			)
			databaseRepository.updateQuarter(
				uid = activeUId,
				qid = params.qid,
				update = quarterUpdate
			)
		}

		return computedQuarter
		*/
	}
}