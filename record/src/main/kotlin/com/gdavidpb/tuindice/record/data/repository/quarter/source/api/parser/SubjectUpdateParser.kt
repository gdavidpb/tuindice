package com.gdavidpb.tuindice.record.data.repository.quarter.source.api.parser

import com.gdavidpb.tuindice.base.domain.model.transaction.Transaction
import com.gdavidpb.tuindice.base.domain.model.transaction.TransactionAction
import com.gdavidpb.tuindice.base.domain.model.transaction.TransactionType
import com.gdavidpb.tuindice.base.utils.extension.bodyAs
import com.gdavidpb.tuindice.base.utils.extension.bodyToString
import com.gdavidpb.tuindice.record.data.repository.quarter.source.api.request.UpdateSubjectRequest
import com.gdavidpb.tuindice.transactions.data.api.transaction.TransactionParser
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