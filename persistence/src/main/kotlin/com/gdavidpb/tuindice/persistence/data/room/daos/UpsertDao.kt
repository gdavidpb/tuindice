package com.gdavidpb.tuindice.persistence.data.room.daos

import androidx.room.Upsert

abstract class UpsertDao<T> {
	@Upsert
	abstract suspend fun upsertEntity(entity: T)

	@Upsert
	abstract suspend fun upsertEntities(entities: List<T>)
}