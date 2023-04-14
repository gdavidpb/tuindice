package com.gdavidpb.tuindice.record.data.api.parser

import com.gdavidpb.tuindice.base.domain.model.transaction.Transaction
import com.gdavidpb.tuindice.base.domain.model.transaction.TransactionAction
import com.gdavidpb.tuindice.base.domain.model.transaction.TransactionType
import com.gdavidpb.tuindice.base.utils.extension.bodyAs
import com.gdavidpb.tuindice.base.utils.extension.bodyToString
import com.gdavidpb.tuindice.base.utils.extension.findAnnotation
import com.gdavidpb.tuindice.record.data.api.request.UpdateSubjectRequest
import com.gdavidpb.tuindice.transactions.data.api.transaction.RequestParser
import okhttp3.Request
import retrofit2.http.PATCH

class SubjectUpdateParser : RequestParser {
	override fun match(request: Request): Boolean {
		return request.findAnnotation<PATCH>()?.value == "subjects"
	}

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