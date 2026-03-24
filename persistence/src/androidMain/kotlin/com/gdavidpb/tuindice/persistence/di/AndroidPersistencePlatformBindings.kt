package com.gdavidpb.tuindice.persistence.di

import com.gdavidpb.tuindice.persistence.data.room.TuIndiceDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module

fun Module.registerAndroidPersistencePlatformStorage() {
	single<TuIndiceDatabase> {
		createDefaultAndroidDatabase(context = androidContext())
	}
}
