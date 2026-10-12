package com.gdavidpb.tuindice.persistence.domain.repository

import com.gdavidpb.tuindice.persistence.domain.model.PensumCreditsProgress
import kotlinx.coroutines.flow.Flow

/**
 * The credit progress of the selected pensum, read from the cache the pensum feature keeps. It emits
 * only when the progress changes, and it emits `null` in exactly three cases:
 * 1. There is no pensum in the cache. This includes a student who has never opened the Pensum tab,
 *    because the tab is what downloads and caches it.
 * 2. The academic record the progress is measured against is not ready yet.
 * 3. The academic record is not available (its sync is failing).
 *
 * It never downloads anything: it only reads what the pensum feature already holds.
 */
interface PensumProgressRepository {
	fun observePensumProgressFlow(): Flow<PensumCreditsProgress?>
}
