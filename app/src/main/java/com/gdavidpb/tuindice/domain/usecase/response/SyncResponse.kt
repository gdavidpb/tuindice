package com.gdavidpb.tuindice.domain.usecase.response

data class SyncResponse(
        val cacheUpdated: Boolean,
        val thereIsCache: Boolean
)