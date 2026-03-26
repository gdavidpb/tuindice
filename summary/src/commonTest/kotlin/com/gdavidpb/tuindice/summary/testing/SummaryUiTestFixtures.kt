package com.gdavidpb.tuindice.summary.testing

import androidx.compose.ui.graphics.Color
import com.gdavidpb.tuindice.summary.presentation.contract.Summary
import com.gdavidpb.tuindice.summary.presentation.mapper.toSummaryItemList
import com.gdavidpb.tuindice.summary.presentation.model.SummaryItemsColors
import com.gdavidpb.tuindice.summary.presentation.model.SummaryItemsLabels

fun summaryContentState(
	profilePictureUrl: String = "https://cdn.tuindice.app/profile.jpg",
	isProfilePictureLoading: Boolean = false,
	isUserRefreshing: Boolean = false
): Summary.State.Content = Summary.State.Content(
	name = "Ana Diaz",
	lastUpdate = "Última actualización: Hoy",
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
	isProfilePictureLoading = isProfilePictureLoading,
	isUserRefreshing = isUserRefreshing
)

fun summaryItemsFor(
	state: Summary.State.Content = summaryContentState()
) = state.toSummaryItemList(
	labels = SummaryItemsLabels(
		subjectsHeader = "${state.enrolledSubjects} materias cursadas",
		subjectsApprovedLabel = "Aprobadas",
		subjectsFailedLabel = "Reprobadas",
		subjectsRetiredLabel = "Retiradas",
		creditsHeader = "${state.enrolledCredits} creditos cursados",
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
