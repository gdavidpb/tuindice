package com.gdavidpb.tuindice.academiccore.domain.engine

import com.gdavidpb.tuindice.academiccore.domain.model.AttemptGradingMode
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOutcome
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptScore
import com.gdavidpb.tuindice.academiccore.domain.model.MAX_SUBJECT_GRADE
import com.gdavidpb.tuindice.academiccore.domain.model.MIN_SUBJECT_GRADE
import com.gdavidpb.tuindice.academiccore.domain.model.TermKind
import com.gdavidpb.tuindice.academiccore.domain.model.isSynthetic
import com.gdavidpb.tuindice.academiccore.testing.nextRecord
import com.gdavidpb.tuindice.academiccore.testing.nextSimpleNumericRecord
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private const val ROUNDS = 150

class RecordProjectionEnginePropertyTest {
	@Test
	fun projections_keepAveragesAndCreditsWithinBounds_forArbitraryRecords() {
		repeat(ROUNDS) { seed ->
			val record = Random(seed.toLong()).nextRecord(withOverrides = true)
			val projections = listOf(
				RecordProjectionEngine.projectAcademic(record),
				RecordProjectionEngine.projectProjection(record)
			)

			projections.forEach { projection ->
				projection.terms.forEach { term ->
					assertTrue(
						term.periodAverage in MIN_SUBJECT_GRADE.toDouble()..MAX_SUBJECT_GRADE.toDouble(),
						"seed=$seed: period average ${term.periodAverage} out of bounds in ${term.id}"
					)
					assertTrue(
						term.cumulativeAverage in MIN_SUBJECT_GRADE.toDouble()..MAX_SUBJECT_GRADE.toDouble(),
						"seed=$seed: cumulative average ${term.cumulativeAverage} out of bounds in ${term.id}"
					)
					assertTrue(
						term.periodCredits >= 0 && term.cumulativeCredits >= 0,
						"seed=$seed: negative credits in ${term.id}"
					)
				}
			}
		}
	}

	@Test
	fun projections_areDeterministic() {
		repeat(ROUNDS) { seed ->
			val record = Random(seed.toLong()).nextRecord(withOverrides = true)

			assertEquals(
				RecordProjectionEngine.projectProjection(record),
				RecordProjectionEngine.projectProjection(record),
				"seed=$seed: projection is not deterministic"
			)
		}
	}

	@Test
	fun projectAcademic_excludesSyntheticTerms_andSortsTermsDescending() {
		repeat(ROUNDS) { seed ->
			val record = Random(seed.toLong()).nextRecord()
			val projection = RecordProjectionEngine.projectAcademic(record)
			val syntheticIds = record.terms
				.filter { term -> term.kind.isSynthetic }
				.map { term -> term.id }
				.toSet()

			projection.terms.forEach { term ->
				assertTrue(
					term.id !in syntheticIds,
					"seed=$seed: academic projection leaked synthetic term ${term.id}"
				)
			}
			projection.terms.zipWithNext().forEach { (newer, older) ->
				assertTrue(
					newer.termOrder >= older.termOrder,
					"seed=$seed: terms not sorted descending (${newer.termOrder} < ${older.termOrder})"
				)
			}
		}
	}

	@Test
	fun projectAcademic_matchesNaiveWeightedMean_whenThereAreNoRetakes() {
		repeat(ROUNDS) { seed ->
			val record = Random(seed.toLong()).nextSimpleNumericRecord()
			val projection = RecordProjectionEngine.projectAcademic(record)
			val termsAscending = projection.terms.asReversed()
			val seen = mutableListOf<Pair<Int, Int>>()

			record.terms.forEachIndexed { index, term ->
				val termPairs = term.attempts.map { attempt ->
					checkNotNull(attempt.academicScore.numericValue) to attempt.credits
				}
				seen += termPairs

				assertEquals(
					naiveTruncatedAverage(termPairs),
					termsAscending[index].periodAverage,
					"seed=$seed: period average mismatch in ${term.id}"
				)
				assertEquals(
					naiveTruncatedAverage(seen),
					termsAscending[index].cumulativeAverage,
					"seed=$seed: cumulative average mismatch in ${term.id}"
				)
				assertEquals(
					seen.sumOf { (_, credits) -> credits },
					termsAscending[index].cumulativeCredits,
					"seed=$seed: cumulative credits mismatch in ${term.id}"
				)
			}
		}
	}

	@Test
	fun projectProjection_freezesHistoricalMetrics_toTheAcademicProjection() {
		repeat(ROUNDS) { seed ->
			val record = Random(seed.toLong()).nextRecord(withOverrides = true)
			val academicByTermId = RecordProjectionEngine.projectAcademic(record)
				.terms.associateBy { term -> term.id }

			RecordProjectionEngine.projectProjection(record).terms
				.filter { term -> term.kind == TermKind.HISTORICAL }
				.forEach { term ->
					val academic = academicByTermId.getValue(term.id)
					assertEquals(academic.periodAverage, term.periodAverage, "seed=$seed: ${term.id}")
					assertEquals(academic.cumulativeAverage, term.cumulativeAverage, "seed=$seed: ${term.id}")
					assertEquals(academic.periodCredits, term.periodCredits, "seed=$seed: ${term.id}")
					assertEquals(academic.cumulativeCredits, term.cumulativeCredits, "seed=$seed: ${term.id}")
				}
		}
	}

	@Test
	fun projectProjection_defaultsPendingEmptyNumericAttempts_inEditableTerms() {
		repeat(ROUNDS) { seed ->
			val record = Random(seed.toLong()).nextRecord()
			val pendingEmptyIds = record.terms
				.filter { term -> term.kind != TermKind.HISTORICAL }
				.flatMap { term -> term.attempts }
				.filter { attempt ->
					attempt.gradingMode == AttemptGradingMode.NUMERIC &&
						attempt.academicOutcome == AttemptOutcome.PENDING &&
						attempt.academicScore is AttemptScore.Empty
				}
				.map { attempt -> attempt.id }
				.toSet()

			RecordProjectionEngine.projectProjection(record).terms
				.flatMap { term -> term.attempts }
				.filter { attempt -> attempt.id in pendingEmptyIds }
				.forEach { attempt ->
					assertEquals(
						AttemptScore.numeric(5),
						attempt.score,
						"seed=$seed: ${attempt.id} was not defaulted to the projection grade"
					)
					assertEquals(
						AttemptOutcome.APPROVED,
						attempt.outcome,
						"seed=$seed: ${attempt.id} defaulted grade must resolve to approved"
					)
				}
		}
	}
}

private fun naiveTruncatedAverage(pairs: List<Pair<Int, Int>>): Double {
	val weighted = pairs.sumOf { (value, credits) -> value.toLong() * credits.toLong() }
	val credits = pairs.sumOf { (_, credits) -> credits.toLong() }
	if (credits == 0L) return 0.0

	return ((weighted * 10_000L) / credits).toDouble() / 10_000.0
}
