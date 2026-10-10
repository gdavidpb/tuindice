package com.gdavidpb.tuindice.sampleflow.data.source

import com.gdavidpb.tuindice.persistence.TuIndiceDatabase

class SampleFlowDataSource(
	private val database: TuIndiceDatabase
) {
	private var lastRead: String? = null

	fun read(): String = database.toString().also { value -> lastRead = value }
}
