package com.gdavidpb.tuindice.base.domain.repository

interface MessagingRepository {
	suspend fun subscribe()
	suspend fun unsubscribe()
}
