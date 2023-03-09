package com.gdavidpb.tuindice.persistence.utils.extension

import com.gdavidpb.tuindice.persistence.data.api.model.data.SubjectData
import com.gdavidpb.tuindice.persistence.domain.model.Resolution
import com.gdavidpb.tuindice.persistence.domain.model.TransactionType

fun Resolution.isSubject() = when {
	data is SubjectData -> true
	type == TransactionType.SUBJECT -> true
	else -> false
}