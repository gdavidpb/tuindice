package com.gdavidpb.tuindice.academiccore.domain.model

/**
 * Whether approving a subject that is not a fixed course of the pensum would count toward one of
 * its slots. [SlotsFilled] means some slot accepts the subject but every such slot is already
 * approved, so taking it now would count toward nothing.
 */
sealed interface AcademicPensumSlotEligibility {
	data object NotEligible : AcademicPensumSlotEligibility

	data class CountsTowardSlot(val slotKind: AcademicPensumSlotKind) : AcademicPensumSlotEligibility

	data class SlotsFilled(val slotKind: AcademicPensumSlotKind) : AcademicPensumSlotEligibility
}
