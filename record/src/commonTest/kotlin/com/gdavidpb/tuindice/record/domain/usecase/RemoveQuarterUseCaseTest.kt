package com.gdavidpb.tuindice.record.domain.usecase

import com.gdavidpb.tuindice.base.domain.model.quarter.Quarter
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.record.domain.model.QuarterRemove
import com.gdavidpb.tuindice.record.domain.model.SubjectGradeSet
import com.gdavidpb.tuindice.record.domain.repository.QuarterRepository
import com.gdavidpb.tuindice.record.domain.usecase.param.RemoveQuarterParams
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class RemoveQuarterUseCaseTest {
	@Test
	fun execute_mapsParamsAndCallsRepository() = runBlocking {
		val quarterRepository = FakeQuarterRepository()
		val useCase = RemoveQuarterUseCase(
			quarterRepository = quarterRepository
		)

		val states = useCase.execute(RemoveQuarterParams(quarterId = "q-2026-1")).toList()

		assertEquals(2, states.size)
		assertIs<UseCaseState.Loading<Unit, Nothing>>(states[0])
		assertIs<UseCaseState.Data<Unit, Nothing>>(states[1])
		assertEquals(QuarterRemove(id = "q-2026-1"), quarterRepository.removedQuarter)
	}
}

private class FakeQuarterRepository : QuarterRepository {
	var removedQuarter: QuarterRemove? = null

	override suspend fun getQuartersFlow(): Flow<List<Quarter>> = emptyFlow()

	override suspend fun getQuarters(): List<Quarter> = emptyList()

	override suspend fun removeQuarter(remove: QuarterRemove) {
		removedQuarter = remove
	}

	override suspend fun setSubjectGrade(set: SubjectGradeSet) = Unit
}
