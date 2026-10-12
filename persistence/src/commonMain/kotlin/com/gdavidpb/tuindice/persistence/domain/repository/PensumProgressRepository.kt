package com.gdavidpb.tuindice.persistence.domain.repository

import com.gdavidpb.tuindice.persistence.domain.model.PensumCreditsProgress
import kotlinx.coroutines.flow.Flow

/**
 * The credit progress of the selected pensum, read from the cache the pensum feature keeps. It emits
 * `null` while there is nothing to compute it from: no pensum cached, or the academic record it is
 * measured against is not ready. It never downloads anything.
 */
interface PensumProgressRepository {
	fun observePensumProgressFlow(): Flow<PensumCreditsProgress?>
}
