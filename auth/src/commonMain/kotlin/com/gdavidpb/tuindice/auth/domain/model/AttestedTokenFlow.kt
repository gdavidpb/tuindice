package com.gdavidpb.tuindice.auth.domain.model

import com.gdavidpb.tuindice.base.domain.model.AttestedOperation

sealed interface AttestedTokenFlow {
    val headerValue: String
    val operation: AttestedOperation

    data object IssueTokens : AttestedTokenFlow {
        override val headerValue: String = "issue_tokens"
        override val operation: AttestedOperation = AttestedOperation.IssueTokens
    }

    data object ReissueTokens : AttestedTokenFlow {
        override val headerValue: String = "reissue_tokens"
        override val operation: AttestedOperation = AttestedOperation.ReissueTokens
    }
}
