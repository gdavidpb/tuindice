package com.gdavidpb.tuindice.evaluations.data.repository.evaluation.source.store

sealed class EvaluationKey(open val uid: String) {
	sealed class Read(override val uid: String) : EvaluationKey(uid) {
		class All(uid: String) : Read(uid)
	}
}