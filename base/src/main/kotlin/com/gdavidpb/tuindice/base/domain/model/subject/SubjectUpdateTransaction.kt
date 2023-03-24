package com.gdavidpb.tuindice.base.domain.model.subject

import com.gdavidpb.tuindice.base.domain.model.transaction.TransactionOperation

data class SubjectUpdateTransaction(
	val subjectId: String,
	val grade: Int
) : TransactionOperation