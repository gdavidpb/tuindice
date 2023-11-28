package com.gdavidpb.tuindice.transactions.data.api.transaction

import com.gdavidpb.tuindice.base.domain.model.transaction.Transaction
import okhttp3.Request

interface TransactionParser {
	fun parse(request: Request): Transaction
}