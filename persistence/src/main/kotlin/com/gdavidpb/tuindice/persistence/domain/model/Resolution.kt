package com.gdavidpb.tuindice.persistence.domain.model

import com.gdavidpb.tuindice.base.domain.model.transaction.TransactionAction
import com.gdavidpb.tuindice.base.domain.model.transaction.TransactionData
import com.gdavidpb.tuindice.base.domain.model.transaction.TransactionType

data class Resolution(
	val uid: String,
	val localReference: String,
	val remoteReference: String,
	val type: TransactionType,
	val action: TransactionAction,
	val data: TransactionData? = null
)