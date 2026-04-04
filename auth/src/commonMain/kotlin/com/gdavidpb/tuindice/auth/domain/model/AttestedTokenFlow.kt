package com.gdavidpb.tuindice.auth.domain.model

import com.gdavidpb.tuindice.base.domain.model.ProtectedOperationCode
import com.gdavidpb.tuindice.base.domain.model.ProtectedOperationCodes

sealed interface AttestedTokenFlow {
	val headerValue: String
	val operationCode: ProtectedOperationCode

	data object ReissueTokens : AttestedTokenFlow {
		override val headerValue: String = "reissue_tokens"
		override val operationCode: ProtectedOperationCode = ProtectedOperationCodes.AuthReissueTokens
	}
}
