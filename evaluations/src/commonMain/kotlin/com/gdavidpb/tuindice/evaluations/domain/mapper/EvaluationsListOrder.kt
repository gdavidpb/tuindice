package com.gdavidpb.tuindice.evaluations.domain.mapper

import com.gdavidpb.tuindice.academiccore.domain.model.Evaluation
import kotlin.math.sign

// The order the Evaluations tab reads its evaluations in: the ones past [nowMillis] before the ones to come, and then
// by state. The numbers in the names ("Parcial 1", "Parcial 2") break their ties in this order, so whatever shows the
// tab's names (the assistant's cards among them) must read the evaluations in it too.
fun List<Evaluation>.sortedAsListed(nowMillis: Long): List<Evaluation> {
	return sortedWith(
		compareBy<Evaluation> { evaluation -> ((evaluation.date ?: 0L) - nowMillis).sign }
			.thenBy(Evaluation::state)
	)
}
