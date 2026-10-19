package com.gdavidpb.tuindice.persistence.data.room

import androidx.room.RoomDatabaseConstructor

@Suppress("KotlinNoActualForExpect")
expect object TuIndiceDatabaseConstructor : RoomDatabaseConstructor<TuIndiceDatabase> {
	override fun initialize(): TuIndiceDatabase
}
