package com.gdavidpb.tuindice.persistence.di

import com.gdavidpb.tuindice.persistence.data.room.TuIndiceDatabase
import org.koin.core.module.Module
import org.koin.core.scope.Scope

fun Module.registerIosPersistencePlatformStorage(
	databasePathProvider: Scope.() -> String
) {
	single<TuIndiceDatabase> {
		createIosDatabase(path = databasePathProvider(this))
	}
}
