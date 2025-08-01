package com.gdavidpb.tuindice.record.data.repository.quarter.source.store

import com.gdavidpb.tuindice.base.domain.model.quarter.Quarter

sealed interface QuarterKey {
	sealed class Read : QuarterKey {
		object All : Read()
		class ById(val qid: String) : Read()
	}

	sealed class Write : QuarterKey {
		class SaveAll(val quarters: List<Quarter>, val dispatchToRemote: Boolean) : Write()
	}

	sealed class Remove : QuarterKey {
		class ById(val qid: String) : Remove()
	}

	sealed class Compute : QuarterKey {
		class BySetSubjectGrade(val qid: String, val sid: String, val grade: Int) : Compute()
	}
}