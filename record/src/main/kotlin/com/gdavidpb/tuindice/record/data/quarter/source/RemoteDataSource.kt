package com.gdavidpb.tuindice.record.data.quarter.source

import com.gdavidpb.tuindice.base.domain.model.quarter.Quarter
import com.gdavidpb.tuindice.base.domain.model.quarter.QuarterRemoveTransaction
import com.gdavidpb.tuindice.base.domain.model.subject.Subject
import com.gdavidpb.tuindice.base.domain.model.subject.SubjectUpdateTransaction
import com.gdavidpb.tuindice.base.domain.model.transaction.Transaction

interface RemoteDataSource {
	suspend fun getQuarters(): List<Quarter>
	suspend fun removeQuarter(transaction: Transaction<QuarterRemoveTransaction>)

	suspend fun updateSubject(transaction: Transaction<SubjectUpdateTransaction>): Subject
}