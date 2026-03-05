package com.gdavidpb.tuindice.summary.testing

import androidx.compose.ui.graphics.Color
import com.gdavidpb.tuindice.summary.presentation.contract.Summary
import com.gdavidpb.tuindice.summary.ui.view.SummaryItemsColors
import com.gdavidpb.tuindice.summary.ui.view.SummaryItemsLabels
import com.gdavidpb.tuindice.summary.ui.view.buildSummaryItems

fun summaryContentState(
	profilePictureUrl: String = "https://cdn.tuindice.app/profile.jpg",
	isProfilePictureLoading: Boolean = false,
	isUpdated: Boolean = true,
	isUpdating: Boolean = false
): Summary.State.Content = Summary.State.Content(
	name = "Ana Diaz",
	lastUpdate = "Ultima actualizacion: Hoy",
	careerName = "Ingenieria Informatica",
	grade = 4.25f,
	enrolledSubjects = 5,
	enrolledCredits = 21,
	approvedSubjects = 3,
	approvedCredits = 18,
	retiredSubjects = 1,
	retiredCredits = 2,
	failedSubjects = 1,
	failedCredits = 1,
	profilePictureUrl = profilePictureUrl,
	isGradeVisible = true,
	isProfilePictureLoading = isProfilePictureLoading,
	isLoading = false,
	isUpdated = isUpdated,
	isUpdating = isUpdating
)

fun summaryItemsFor(
	state: Summary.State.Content = summaryContentState()
) = buildSummaryItems(
	state = state,
	labels = SummaryItemsLabels(
		subjectsHeader = "${state.enrolledSubjects} materias inscritas",
		subjectsApprovedLabel = "Aprobadas",
		subjectsFailedLabel = "Reprobadas",
		subjectsRetiredLabel = "Retiradas",
		creditsHeader = "${state.enrolledCredits} creditos inscritos",
		creditsApprovedLabel = "Aprobados",
		creditsFailedLabel = "Reprobados",
		creditsRetiredLabel = "Retirados"
	),
	colors = SummaryItemsColors(
		approved = Color(0xFF2E7D32),
		failed = Color(0xFFC62828),
		retired = Color(0xFF616161)
	)
)
