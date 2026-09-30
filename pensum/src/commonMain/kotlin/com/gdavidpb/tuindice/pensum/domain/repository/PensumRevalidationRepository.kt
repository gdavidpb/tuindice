package com.gdavidpb.tuindice.pensum.domain.repository

// Keeps an already cached pensum fresh from outside the pensum screen (after a sync), so the
// planner and the subject search, which read the cache directly, never keep a stale graph because
// the student stopped opening the pensum tab. It never fetches a pensum that was never cached.
interface PensumRevalidationRepository {
	suspend fun revalidateSelectedPensum()
}
