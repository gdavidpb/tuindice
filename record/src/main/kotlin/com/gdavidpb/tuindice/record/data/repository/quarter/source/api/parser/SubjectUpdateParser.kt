package com.gdavidpb.tuindice.record.data.repository.quarter.source.api.parser

import com.gdavidpb.tuindice.transactions.domain.model.Transaction
import com.gdavidpb.tuindice.transactions.domain.model.TransactionAction
import com.gdavidpb.tuindice.transactions.domain.model.TransactionType
import com.gdavidpb.tuindice.base.utils.extension.bodyAs
import com.gdavidpb.tuindice.base.utils.extension.bodyToString
import com.gdavidpb.tuindice.record.data.repository.quarter.source.api.request.UpdateSubjectRequest
import com.gdavidpb.tuindice.transactions.data.repository.transactions.source.api.retrofit.TransactionParser
import okhttp3.Request

class SubjectUpdateParser : TransactionParser {
	override fun parse(request: Request): Transaction {
		val body = request.bodyAs<UpdateSubjectRequest>()
		val json = request.bodyToString()

		requireNotNull(body)
		requireNotNull(json)

		return Transaction(
			reference = body.subjectId,
			type = TransactionType.SUBJECT,
			action = TransactionAction.UPDATE,
			data = json
		)
	}
}