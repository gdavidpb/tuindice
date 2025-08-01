package com.gdavidpb.tuindice.evaluations.data.source.store

import com.gdavidpb.tuindice.base.domain.model.Evaluation

sealed interface EvaluationKey {
	sealed class Read : EvaluationKey {
		class ById(val eid: String) : Read()
		object All : Read()
	}

	sealed class Write : EvaluationKey {
		class Add(val evaluation: Evaluation) : Write()
		class Update(val evaluation: Evaluation) : Write()
	}

	sealed class Remove : EvaluationKey {
		class ById(val eid: String) : Remove()
	}
}