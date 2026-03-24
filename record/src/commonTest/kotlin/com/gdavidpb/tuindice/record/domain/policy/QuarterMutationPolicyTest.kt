package com.gdavidpb.tuindice.record.domain.policy

import com.gdavidpb.tuindice.record.testing.DEFAULT_RECORD_LOCAL_QUARTER
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class QuarterMutationPolicyTest {
	@Test
	fun canEditGrades_allowsCurrentQuarterWhileOpen() {
		val quarter = DEFAULT_RECORD_LOCAL_QUARTER.copy(
			isCurrent = true,
			isReadOnly = false
		)

		assertTrue(QuarterMutationPolicy.canEditGrades(quarter))
		assertTrue(QuarterMutationPolicy.isInstitutionalCurrentQuarter(quarter))
	}

	@Test
	fun canDelete_rejectsInstitutionalCurrentQuarter() {
		val quarter = DEFAULT_RECORD_LOCAL_QUARTER.copy(
			isCurrent = true,
			isReadOnly = false
		)

		assertFalse(QuarterMutationPolicy.canDelete(quarter))
		assertFalse(
			QuarterMutationPolicy.canApplyPendingMutation(
				quarter = quarter,
				mutationType = QuarterMutationType.RemoveQuarter
			)
		)
	}

	@Test
	fun canDelete_rejectsClosedQuarter() {
		val quarter = DEFAULT_RECORD_LOCAL_QUARTER.copy(
			isCurrent = false,
			isReadOnly = true
		)

		assertFalse(QuarterMutationPolicy.canDelete(quarter))
		assertFalse(QuarterMutationPolicy.canEditGrades(quarter))
	}

	@Test
	fun canApplyPendingMutation_rejectsAddQuarterWhenAnchorIsReadOnly() {
		val quarter = DEFAULT_RECORD_LOCAL_QUARTER.copy(
			isCurrent = true,
			isReadOnly = true
		)

		assertFalse(
			QuarterMutationPolicy.canApplyPendingMutation(
				quarter = quarter,
				mutationType = QuarterMutationType.AddQuarter
			)
		)
	}
}
