package com.gdavidpb.tuindice.sampleflow.data.repository

interface SampleFlowLocalDataSource {
	fun read(): String
}

data class SampleFlowSnapshot(
	val value: String
)

interface SampleFlowDataRepository {
	fun read(): String
}
