package com.gdavidpb.tuindice.domain.usecase.response

data class SyncResponse(
        val isDataUpdated: Boolean,
        val hasDataInCache: Boolean
)