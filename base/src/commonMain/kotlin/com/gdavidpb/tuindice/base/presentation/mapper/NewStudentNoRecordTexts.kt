package com.gdavidpb.tuindice.base.presentation.mapper

import com.gdavidpb.tuindice.base.presentation.model.UiText
import tuindice.base.generated.resources.Res
import tuindice.base.generated.resources.new_student_no_record_message
import tuindice.base.generated.resources.new_student_no_record_title

// The copy of a student the university has no academic record for yet, shared by every screen that
// has nothing to show because of it (Summary, Record, Evaluations).
object NewStudentNoRecordTexts {
	val title: UiText = UiText.Resource(Res.string.new_student_no_record_title)
	val message: UiText = UiText.Resource(Res.string.new_student_no_record_message)
}
