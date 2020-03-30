package com.gdavidpb.tuindice.domain.usecase.response

import com.gdavidpb.tuindice.domain.model.Account

data class SyncResponse(
        val cacheUpdated: Boolean,
        val thereIsCache: Boolean
)