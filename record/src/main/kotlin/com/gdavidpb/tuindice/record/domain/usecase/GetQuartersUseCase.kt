package com.gdavidpb.tuindice.record.domain.usecase

import com.gdavidpb.tuindice.base.domain.model.quarter.Quarter
import com.gdavidpb.tuindice.base.domain.repository.AuthRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.base.utils.extension.computeCredits
import com.gdavidpb.tuindice.base.utils.extension.computeGrade
import com.gdavidpb.tuindice.base.utils.extension.computeGradeSum
import com.gdavidpb.tuindice.record.domain.repository.QuarterRepository
import com.gdavidpb.tuindice.record.domain.usecase.error.GetQuartersError
import com.gdavidpb.tuindice.record.domain.usecase.exceptionhandler.GetQuartersExceptionHandler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.concurrent.ConcurrentHashMap

class GetQuartersUseCase(
	private val authRepository: AuthRepository,
	private val quarterRepository: QuarterRepository,
	override val exceptionHandler: GetQuartersExceptionHandler
) : FlowUseCase<Unit, List<Quarter>, GetQuartersError>() {
	override suspend fun executeOnBackground(params: Unit): Flow<List<Quarter>> {
		val activeUId = authRepository.getActiveAuth().uid

		val quartersCache = ConcurrentHashMap<Int, Quarter>()

		return quarterRepository.getQuarters(uid = activeUId)
			.map { quarters ->
				quarters.map { quarter ->
					if (quarter.isEditable)
						quartersCache.getOrPut(quarter.hashCode()) {
							quarter.copy(
								grade = quarter.subjects.computeGrade(),
								gradeSum = quarters.computeGradeSum(until = quarter),
								credits = quarter.subjects.computeCredits()
							)
						}
					else
						quarter
				}
			}
	}
}