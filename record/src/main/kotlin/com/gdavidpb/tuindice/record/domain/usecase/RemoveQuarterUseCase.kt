package com.gdavidpb.tuindice.record.domain.usecase

import com.gdavidpb.tuindice.base.domain.model.quarter.QuarterRemoveTransaction
import com.gdavidpb.tuindice.base.domain.model.transaction.Transaction
import com.gdavidpb.tuindice.base.domain.repository.AuthRepository
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.record.domain.repository.QuarterRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class RemoveQuarterUseCase(
	private val authRepository: AuthRepository,
	private val quarterRepository: QuarterRepository,
	override val reportingRepository: ReportingRepository
) : FlowUseCase<String, Unit, Nothing>() {
	override suspend fun executeOnBackground(params: String): Flow<Unit> {
		val activeUId = authRepository.getActiveAuth().uid

		val operation = QuarterRemoveTransaction(
			quarterId = params
		)

		val transaction = Transaction.Builder<QuarterRemoveTransaction>()
			.withUid(activeUId)
			.withReference(params)
			.withOperation(operation)
			.build()

		quarterRepository.removeQuarter(uid = activeUId, transaction = transaction)

		return flowOf(Unit)
	}
}