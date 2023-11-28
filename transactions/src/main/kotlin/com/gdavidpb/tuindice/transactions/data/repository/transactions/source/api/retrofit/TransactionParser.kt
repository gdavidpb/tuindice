package com.gdavidpb.tuindice.transactions.data.repository.transactions.source.api.retrofit

import com.gdavidpb.tuindice.transactions.domain.model.Transaction
import okhttp3.Request

interface TransactionParser {
	fun parse(request: Request): Transaction
}