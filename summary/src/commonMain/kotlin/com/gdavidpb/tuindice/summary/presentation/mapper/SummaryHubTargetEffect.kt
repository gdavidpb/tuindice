package com.gdavidpb.tuindice.summary.presentation.mapper

import com.gdavidpb.tuindice.summary.presentation.contract.Summary
import com.gdavidpb.tuindice.summary.presentation.model.SummaryHubTarget

// The effect that takes the student to the screen a line of the hub leads to.
internal fun SummaryHubTarget.toSummaryEffect(): Summary.Effect {
	return when (this) {
		SummaryHubTarget.Schedule -> Summary.Effect.NavigateToSchedule

		SummaryHubTarget.Record -> Summary.Effect.NavigateToRecord

		SummaryHubTarget.Pensum -> Summary.Effect.NavigateToPensum

		is SummaryHubTarget.EvaluationsWeek -> Summary.Effect.NavigateToEvaluationsWeek(
			weekStart = weekStart,
			evaluationId = evaluationId
		)

		is SummaryHubTarget.RecordAttempt -> Summary.Effect.NavigateToRecordAttempt(attemptId = attemptId)

		SummaryHubTarget.AddEvaluation -> Summary.Effect.NavigateToAddEvaluation

		SummaryHubTarget.CreateTerm -> Summary.Effect.NavigateToCreateTerm

		SummaryHubTarget.Evaluations -> Summary.Effect.NavigateToEvaluations

		SummaryHubTarget.Statistics -> Summary.Effect.NavigateToStatistics
	}
}
