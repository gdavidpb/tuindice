package com.gdavidpb.tuindice.wizard.presentation.mapper

import com.gdavidpb.tuindice.about.presentation.contract.About
import com.gdavidpb.tuindice.base.presentation.ViewState
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluation
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluations
import com.gdavidpb.tuindice.pensum.presentation.contract.Pensum
import com.gdavidpb.tuindice.record.presentation.contract.CreateSyntheticTerm
import com.gdavidpb.tuindice.record.presentation.contract.Record
import com.gdavidpb.tuindice.record.presentation.model.RecordRouteViewState
import com.gdavidpb.tuindice.subjects.presentation.contract.SubjectDetail
import com.gdavidpb.tuindice.subjects.presentation.contract.SubjectSearch
import com.gdavidpb.tuindice.summary.presentation.contract.Summary
import com.gdavidpb.tuindice.wizard.presentation.model.CoachmarkId
import com.gdavidpb.tuindice.wizard.presentation.model.CoachmarkSurface

fun ViewState.toCoachmarkSurface(visitKey: String): CoachmarkSurface {
	return CoachmarkSurface(
		visitKey = visitKey,
		eligibleCoachmarkIds = eligibleCoachmarkIds()
	)
}

private fun ViewState.eligibleCoachmarkIds(): List<CoachmarkId> {
	return when (this) {
		is Summary.State.Content ->
			listOf(CoachmarkId.Summary)

		is Record.State.Content ->
			listOf(CoachmarkId.Record, CoachmarkId.RecordControls)

		is RecordRouteViewState ->
			if (topBarViewModeState != null) {
				listOf(CoachmarkId.Record, CoachmarkId.RecordControls)
			} else {
				emptyList()
			}

		is CreateSyntheticTerm.State ->
			listOf(CoachmarkId.SyntheticTerm)

		is Pensum.State.Content ->
			listOf(CoachmarkId.Pensum, CoachmarkId.PensumTools)

		is Evaluations.State.Content ->
			listOf(CoachmarkId.Evaluations, CoachmarkId.EvaluationsTools)

		is Evaluation.State.Content ->
			listOf(CoachmarkId.EvaluationEditor)

		is SubjectSearch.State ->
			listOf(CoachmarkId.SubjectSearch)

		is SubjectDetail.State.Content ->
			listOf(CoachmarkId.SubjectDetail)

		is About.State.Content ->
			listOf(CoachmarkId.About, CoachmarkId.AboutActions)

		else ->
			emptyList()
	}
}
