package com.gdavidpb.tuindice.transactions.data.api.transaction

import com.gdavidpb.tuindice.base.domain.model.transaction.Transaction
import okhttp3.Request

interface RequestParser {
	fun match(request: Request): Boolean
	fun parse(request: Request): Transaction
}