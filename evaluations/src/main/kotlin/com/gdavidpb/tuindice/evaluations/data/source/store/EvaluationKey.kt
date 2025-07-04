package com.gdavidpb.tuindice.evaluations.data.source.store

import com.gdavidpb.tuindice.base.domain.model.Evaluation

sealed class EvaluationKey(val uid: String) {
	sealed class Read(uid: String) : EvaluationKey(uid) {
		class ById(uid: String, val eid: String) : Read(uid)
		class All(uid: String) : Read(uid)
	}

	sealed class Write(uid: String) : EvaluationKey(uid) {
		class Add(uid: String, val evaluation: Evaluation) : Write(uid)
		class Update(uid: String, val evaluation: Evaluation) : Write(uid)
	}

	sealed class Remove(uid: String) : EvaluationKey(uid) {
		class ById(uid: String, val eid: String) : Remove(uid)
	}
}