package com.gdavidpb.tuindice.summary.domain.repository

import kotlinx.coroutines.flow.Flow

// Whether the local record has a current term. It is the one source of truth for the moment of an
// annulled enrollment (provisional keeps the term, final has dropped it), read from the same place
// Record and Evaluations read it, so a record refresh outside the sync is seen too.
interface CurrentTermRepository {
	fun observeHasCurrentTerm(): Flow<Boolean>
}
