package com.gdavidpb.tuindice.persistence.di

import androidx.room.Room
import com.gdavidpb.tuindice.persistence.data.room.TuIndiceDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val persistenceModule = module {
	/* Database */

	single {
		val name = androidContext().packageName

		Room.databaseBuilder(androidContext(), TuIndiceDatabase::class.java, name)
			.fallbackToDestructiveMigration(true)
			.build()
	}
}