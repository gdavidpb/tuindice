package com.gdavidpb.tuindice.record.presentation.mapper

import com.gdavidpb.tuindice.record.domain.model.SyntheticTermSubject
import com.gdavidpb.tuindice.record.presentation.model.CreateTermSubjectItem

fun SyntheticTermSubject.toCreateTermSubjectItem(): CreateTermSubjectItem {
	return CreateTermSubjectItem(
		subject = this,
		nameText = name.uppercase()
	)
}
