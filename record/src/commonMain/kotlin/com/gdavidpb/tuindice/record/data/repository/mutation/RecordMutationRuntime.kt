package com.gdavidpb.tuindice.record.data.repository.mutation

const val RECORD_MUTATION_STORE_ID = "record"
const val RECORD_MUTATION_SCOPE = "record"

interface RecordMutationAck {
	val mutationId: String
}
