package com.gdavidpb.tuindice.evaluations.domain.repository

// Keeps the evaluations fresh from outside their tab (after a sync), so the screens that read them
// (the summary, the schedule) do not wait for the student to open the tab. Unlike the pensum, it
// does download them even if they were never downloaded. Best effort: it honours the daily cooldown
// and the hold after a failure, and the caller ignores whatever it throws.
interface EvaluationsRevalidationRepository {
	suspend fun revalidateEvaluations()
}
