package com.gdavidpb.tuindice.data.source.room.utils

import com.gdavidpb.tuindice.BuildConfig

object DatabaseModel {
	const val VERSION = 1
	const val NAME = BuildConfig.APPLICATION_ID
}

object QuarterTable {
	const val START_DATE = "startDate"
	const val END_DATE = "endDate"
}