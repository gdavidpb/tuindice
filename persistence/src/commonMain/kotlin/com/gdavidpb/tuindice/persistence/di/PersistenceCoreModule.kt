package com.gdavidpb.tuindice.persistence.di

import com.gdavidpb.tuindice.persistence.data.room.TuIndiceDatabase
import org.koin.core.module.Module
import org.koin.dsl.module

fun persistenceCoreModule(
	database: TuIndiceDatabase
): Module = module {
	single { database }
}
