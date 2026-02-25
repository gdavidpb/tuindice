package com.gdavidpb.tuindice.persistence.di

import com.gdavidpb.tuindice.persistence.data.room.TuIndiceDatabase
import org.koin.core.module.Module
import org.koin.dsl.module

// iOS creates the database at runtime (path comes from the host app), so this module is parameterized.
fun persistenceCommonModule(
	database: TuIndiceDatabase
): Module = module {
	single { database }
}
