package com.gdavidpb.tuindice.evaluations.domain.repository

/**
 * Keeps the evaluations fresh from outside their tab (after a sync), so the screens that read them
 * (the summary, the schedule) do not wait for the student to open the tab. Unlike the pensum, it
 * does download them even if they were never downloaded. Best effort: the caller ignores whatever
 * it throws.
 *
 * Two guards apply to what it does on its own:
 * - the daily cooldown: an already downloaded list is not downloaded again while it holds;
 * - the hold after a failure: a download that failed keeps the next revalidation from trying for
 *   COOLDOWN_RETRY_EVALUATIONS (the tab is never held).
 *
 * Interaction with a sync: a successful sync clears the stale feature cooldowns (the evaluations'
 * included) right BEFORE launching this, so in practice every successful sync produces one
 * `GET /evaluations/v3`, and the daily cooldown never holds it back. What the failure hold really
 * limits is how often a forced sync re-tries after a failed download. If the revalidation finds a
 * download already in progress (the tab's), it waits for that one and shares its outcome instead of
 * making a second request.
 *
 * A sync that is skipped (its own cooldown, its retry backoff) clears nothing and downloads nothing,
 * so an earlier revalidation that failed would wait for the next real sync, a day away. For that
 * case [revalidateEvaluationsIfNeverDownloaded] tries again, only for evaluations this account has
 * never downloaded, and still behind the hold after a failure: with the evaluations already
 * downloaded it adds no request.
 */
interface EvaluationsRevalidationRepository {
	suspend fun revalidateEvaluations()

	suspend fun revalidateEvaluationsIfNeverDownloaded()
}
