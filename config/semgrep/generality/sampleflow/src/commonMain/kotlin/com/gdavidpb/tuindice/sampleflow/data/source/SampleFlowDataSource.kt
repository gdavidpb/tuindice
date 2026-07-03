package com.gdavidpb.tuindice.sampleflow.data.source

import com.gdavidpb.tuindice.persistence.TuIndiceDatabase

class SampleFlowDataSource(
	private val database: TuIndiceDatabase
) {
	fun read(): String = database.toString()
}
