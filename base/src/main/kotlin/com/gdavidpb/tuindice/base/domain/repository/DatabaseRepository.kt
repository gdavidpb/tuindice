package com.gdavidpb.tuindice.base.domain.repository

interface DatabaseRepository {
	suspend fun prune()
}