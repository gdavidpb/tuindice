package com.gdavidpb.tuindice.scenarios

import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationType
import com.gdavidpb.tuindice.evaluations.presentation.mapper.asIcon
import com.gdavidpb.tuindice.summary.presentation.mapper.toIcon
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The icon of an evaluation type reads the same on the summary hub and in Evaluations. Each of the two modules keeps
 * its own mapping from the type to its icon: a module cannot read another's, so this test is what keeps the two equal.
 * The icons are told apart by their names, so that the comparison means something.
 */
class HubAndEvaluationsIconParityTest {
	@Test
	fun theIconOfEachEvaluationTypeReadsTheSameOnTheHubAndInEvaluations() {
		EvaluationType.entries.forEach { type ->
			assertEquals(
				type.asIcon().name,
				type.toIcon().name,
				"the icon of '$type' reads differently"
			)
		}
	}

	@Test
	fun theIconsOfTheEvaluationTypesAreToldApart() {
		val names = EvaluationType.entries.map { type -> type.toIcon().name }

		assertEquals(EvaluationType.entries.size, names.toSet().size)
	}
}
