package com.gdavidpb.tuindice.persistence.di

import android.content.Context
import androidx.room.Room
import com.gdavidpb.tuindice.persistence.data.room.TuIndiceDatabase

fun createAndroidDatabase(
	context: Context,
	name: String
): TuIndiceDatabase {
	require(name.isNotBlank()) {
		"Android database name cannot be blank."
	}

	return Room.databaseBuilder(context, TuIndiceDatabase::class.java, name)
		.fallbackToDestructiveMigrationFrom(true, 1, 2, 3, 4, 5, 6, 7)
		.build()
}

fun createDefaultAndroidDatabase(
	context: Context
): TuIndiceDatabase {
	return createAndroidDatabase(
		context = context,
		name = context.packageName
	)
}
