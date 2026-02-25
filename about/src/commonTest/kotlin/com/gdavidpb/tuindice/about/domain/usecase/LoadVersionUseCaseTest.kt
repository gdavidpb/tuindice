package com.gdavidpb.tuindice.about.domain.usecase

import com.gdavidpb.tuindice.about.domain.repository.AboutRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class LoadVersionUseCaseTest {
	@Test
	fun execute_returnsVersionDescriptionFromRepository() = runBlocking {
		val useCase = LoadVersionUseCase(
			aboutRepository = FakeAboutRepository(
				versionDescription = "Debug 3.0.0 (30000)"
			)
		)

		val states = useCase.execute(Unit).toList()

		assertEquals(2, states.size)
		assertIs<UseCaseState.Loading<String, Nothing>>(states[0])
		val success = assertIs<UseCaseState.Data<String, Nothing>>(states[1])
		assertEquals("Debug 3.0.0 (30000)", success.value)
	}
}

private class FakeAboutRepository(
	private val versionDescription: String
) : AboutRepository {
	override suspend fun getVersionDescription(): String = versionDescription
}
