package com.gdavidpb.tuindice.record.data.repository.quarter.source.store

import com.gdavidpb.tuindice.base.domain.model.quarter.Quarter

sealed class QuarterKey(open val uid: String) {
	sealed class Read(uid: String) : QuarterKey(uid) {
		class All(uid: String) : Read(uid)
		class ById(uid: String, val qid: String) : Read(uid)
	}

	sealed class Write(uid: String) : QuarterKey(uid) {
		class SaveAll(uid: String, val quarters: List<Quarter>, val dispatchToRemote: Boolean) : Write(uid)
	}

	sealed class Remove(uid: String) : QuarterKey(uid) {
		class ById(uid: String, val qid: String) : Remove(uid)
	}

	sealed class Compute(uid: String) : QuarterKey(uid) {
		class BySetSubjectGrade(uid: String, val qid: String, val sid: String, val grade: Int) : Compute(uid)
	}
}