package com.gdavidpb.tuindice.scenarios.shared

import com.gdavidpb.tuindice.record.ui.RecordUiTags
import com.gdavidpb.tuindice.scenariokit.dsl.StepBuilder
import com.gdavidpb.tuindice.scenariokit.dsl.group
import com.gdavidpb.tuindice.scenariokit.dsl.scrollUntilVisible
import com.gdavidpb.tuindice.scenariokit.dsl.tap
import com.gdavidpb.tuindice.scenariokit.dsl.waitVisible
import com.gdavidpb.tuindice.scenariokit.model.Scroll
import com.gdavidpb.tuindice.scenarios.fixture.E2eFixtures

/**
 * From the record tab, selects the current term and opens its enrollment proof dialog. Scrolls the record
 * until the proof button shows instead of swiping a fixed distance.
 */
fun StepBuilder.openCurrentEnrollmentProof() {
	group("openCurrentEnrollmentProof") {
		tap(RecordUiTags.termChip(E2eFixtures.CurrentTerm.value))
		waitVisible(RecordUiTags.EnrollmentProofButton, Within.Action)
		scrollUntilVisible(RecordUiTags.EnrollmentProofButton, Scroll.ContentDown, Within.Wait)
		tap(RecordUiTags.EnrollmentProofButton)
	}
}
