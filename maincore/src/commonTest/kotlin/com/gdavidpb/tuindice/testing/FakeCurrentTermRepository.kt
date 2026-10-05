package com.gdavidpb.tuindice.testing

import com.gdavidpb.tuindice.summary.domain.repository.CurrentTermRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

// Summary reads it to tell a provisional annulment from a final one; these hosts never annul anything.
object FakeCurrentTermRepository : CurrentTermRepository {
	override fun observeHasCurrentTerm(): Flow<Boolean> = flowOf(false)
}
