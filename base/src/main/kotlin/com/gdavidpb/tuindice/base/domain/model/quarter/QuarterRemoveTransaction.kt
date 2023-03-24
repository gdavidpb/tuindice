package com.gdavidpb.tuindice.base.domain.model.quarter

import com.gdavidpb.tuindice.base.domain.model.transaction.TransactionOperation

data class QuarterRemoveTransaction(
	val quarterId: String
) : TransactionOperation