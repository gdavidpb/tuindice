package com.gdavidpb.tuindice.data.source.assistant

import com.gdavidpb.tuindice.pensum.domain.model.PensumObservation
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermSubject
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PensumSubjectCatalogDataSourceTest {
	@Test
	fun resolve_isNullForEveryCodeWhileThePensumIsNotLoaded() = runTest {
		val notLoaded = listOf(
			PensumObservation.WaitingForRecordData,
			PensumObservation.RecordDataUnavailable,
			PensumObservation.Missing
		)

		notLoaded.forEach { observation ->
			assertNull(catalogOf(observation).resolve(listOf(CALCULUS_CODE)), "for $observation")
		}
	}

	@Test
	fun resolve_answersNoSubjectForNoCodeEvenWhileThePensumIsNotLoaded() = runTest {
		val resolved = catalogOf(PensumObservation.Missing).resolve(emptyList())

		assertEquals(emptyMap<String, SyntheticTermSubject>(), resolved)
	}

	@Test
	fun resolve_answersTheCoursesTheLoadedPensumHoldsAndLeavesOutTheOthers() = runTest {
		val resolved = catalogOf(pensumWith(CALCULUS)).resolve(listOf(CALCULUS_CODE, "XYZ999"))

		assertEquals(
			mapOf(CALCULUS_CODE to SyntheticTermSubject(subjectCode = CALCULUS_CODE, name = "Cálculo I", credits = 5)),
			resolved
		)
	}

	@Test
	fun resolve_answersAnEmptyMapForCodesTheLoadedPensumDoesNotHold() = runTest {
		val resolved = catalogOf(pensumWith(CALCULUS)).resolve(listOf("XYZ999"))

		// Not null: the pensum is loaded, so an absent code is one the pensum does not hold, not one left unjudged.
		assertEquals(emptyMap<String, SyntheticTermSubject>(), resolved)
	}

	@Test
	fun resolve_normalizesTheCodesBeforeLookingThemUp() = runTest {
		val resolved = catalogOf(pensumWith(CALCULUS)).resolve(listOf(" mat101 "))

		assertEquals(setOf(CALCULUS_CODE), resolved?.keys)
	}

	private fun catalogOf(observation: PensumObservation) =
		PensumSubjectCatalogDataSource(FakePensumRepository(flowOf(observation)))

	private companion object {
		val CALCULUS = Triple(CALCULUS_CODE, "Cálculo I", 5)

		const val CALCULUS_CODE = "MAT101"
	}
}
