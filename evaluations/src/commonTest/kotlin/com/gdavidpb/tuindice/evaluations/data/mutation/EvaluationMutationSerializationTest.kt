package com.gdavidpb.tuindice.evaluations.data.mutation

import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationScheduleMode
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

class EvaluationMutationSerializationTest {
	@Test
	fun addMutation_serializes_with_custom_discriminator_and_type_field() {
		val mutation: EvaluationMutation = EvaluationMutation.Add(
			referenceId = "reference-1",
			attemptId = "subject-1",
			subjectCode = "INF-101",
			termId = "quarter-1",
			scheduleMode = EvaluationScheduleMode.DATED,
			grade = null,
			maxGrade = 100.0,
			date = 1_900_000_000_000L,
			type = 0
		)

		val payload = Json.encodeToString(EvaluationMutation.serializer(), mutation)

		assertContains(payload, "\"mutation_type\":\"add_evaluation\"")
		assertContains(payload, "\"type\":0")
		assertEquals(
			mutation,
			Json.decodeFromString(EvaluationMutation.serializer(), payload)
		)
	}
}
