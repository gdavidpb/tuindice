package com.gdavidpb.tuindice.base.domain.repository

interface PushGateway {
	suspend fun subscribe()
	suspend fun unsubscribe()
}
