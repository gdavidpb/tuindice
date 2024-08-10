package com.gdavidpb.tuindice.persistence.di

import androidx.room.Room
import com.gdavidpb.tuindice.persistence.data.room.TuIndiceDatabase
import com.gdavidpb.tuindice.persistence.data.room.callback.TimestampCallback
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val persistenceModule = module {
	/* Database */

	single {
		val name = androidContext().packageName

		Room.databaseBuilder(androidContext(), TuIndiceDatabase::class.java, name)
			.addCallback(get<TimestampCallback>())
			.fallbackToDestructiveMigration()
			.build()
	}

	/* Callbacks */

	singleOf(::TimestampCallback)
}