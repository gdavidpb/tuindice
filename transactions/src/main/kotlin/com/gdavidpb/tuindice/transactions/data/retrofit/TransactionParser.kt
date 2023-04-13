package com.gdavidpb.tuindice.transactions.data.retrofit

import com.gdavidpb.tuindice.base.domain.model.transaction.Transaction
import okhttp3.Request

class TransactionParser(
	private val parsers: List<RequestParser>
) {
	fun fromRequest(request: Request): Transaction {
		val parser = parsers
			.find { parser -> parser.match(request) }
			?: error("No parser found.")

		return parser.parse(request)
	}
}

