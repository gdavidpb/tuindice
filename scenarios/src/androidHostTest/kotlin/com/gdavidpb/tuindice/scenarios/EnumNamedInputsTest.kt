package com.gdavidpb.tuindice.scenarios

import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOutcome
import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationType
import com.gdavidpb.tuindice.scenarios.fixture.E2eInputs
import com.gdavidpb.tuindice.subjects.domain.model.SubjectSearchPensumStatus
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * A tag the app builds from the name of an enum (a type chip, an attempt status, a search status) is built here from
 * that enum too, not retyped: a value renamed or removed in the product stops compiling instead of leaving a tag
 * that no screen has. The test looks for the lowercase name of any of those enums written as a string literal in the
 * catalog and the fixtures.
 */
class EnumNamedInputsTest {
	private val enumNames: List<String> = EvaluationType.entries.map { it.name } +
		AttemptOutcome.entries.map { it.name } +
		SubjectSearchPensumStatus.entries.map { it.name }

	private val tagSuffixes: Set<String> = enumNames.map { it.lowercase() }.toSet()

	private val sources = File("src/commonMain/kotlin").walkTopDown().filter { it.isFile && it.extension == "kt" }.toList()

	@Test
	fun noSourceOfTheCatalogOrTheFixturesSpellsAnEnumNameAsALiteral() {
		val offenders = sources.flatMap { file ->
			literalsOf(file.readText()).map { "${file.name}: \"$it\"" }
		}

		assertTrue(sources.size > MINIMUM_SOURCES, "only ${sources.size} sources found")
		assertTrue(offenders.isEmpty(), "enum names typed as literals instead of derived: $offenders")
	}

	@Test
	fun aLiteralEnumNameIsCaughtAndAnotherStringIsNot() {
		val source = """
			tap(Tags.evaluationTypeChip("written_work"))
			tap(Tags.attemptStatusValue(id, "approved"))
			waitVisible(text("Aprobada"))
			val approvedCount = 3
		""".trimIndent()

		assertEquals(listOf("written_work", "approved"), literalsOf(source))
	}

	@Test
	fun theDerivedInputsAreTheNamesTheTagsAreBuiltFrom() {
		assertEquals(EvaluationType.TEST.name.lowercase(), E2eInputs.EvaluationTypeTest)
		assertEquals(EvaluationType.WRITTEN_WORK.name.lowercase(), E2eInputs.EvaluationTypeWrittenWork)
		assertEquals(AttemptOutcome.APPROVED.name.lowercase(), E2eInputs.AttemptApproved)
		assertEquals(SubjectSearchPensumStatus.APPROVED.name.lowercase(), E2eInputs.SearchStatusApproved)
	}

	private fun literalsOf(source: String): List<String> =
		Regex(""""([a-z_]+)"""").findAll(source).map { it.groupValues[1] }.filter { it in tagSuffixes }.toList()

	private companion object {
		const val MINIMUM_SOURCES = 20
	}
}
