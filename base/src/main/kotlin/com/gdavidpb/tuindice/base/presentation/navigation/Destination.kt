package com.gdavidpb.tuindice.base.presentation.navigation

import com.gdavidpb.tuindice.base.presentation.model.BottomBarConfig
import com.gdavidpb.tuindice.base.presentation.model.TopBarConfig

sealed class Destination(
	val route: String,
	val title: String = "",
	val isTopDestination: Boolean = false,
	val isBottomDestination: Boolean = false,
	val isDialogDestination: Boolean = false,
	val topBarConfig: TopBarConfig? = null,
	val bottomBarConfig: BottomBarConfig? = null
) {
	data object EnrollmentProofFetch :
		Destination(
			route = "enrollment_proof_fetch",
			isDialogDestination = true
		)

	data object UpdatePassword :
		Destination(
			route = "update_password",
			isDialogDestination = true
		)

	data object SignIn :
		Destination(
			route = "sign_in",
			title = "TuIndice",
			isTopDestination = true,
			isBottomDestination = false
		)

	data object SignOut :
		Destination(
			route = "sign_out",
			isDialogDestination = true
		)

	data object Browser :
		Destination(
			route = "browser",
			title = "{title}",
			isTopDestination = false,
			isBottomDestination = false
		)

	data object Summary :
		Destination(
			route = "summary",
			title = "Resumen",
			isTopDestination = true,
			isBottomDestination = true,
			topBarConfig = TopBarConfig.Summary,
			bottomBarConfig = BottomBarConfig.Summary
		)

	data object Record :
		Destination(
			route = "record",
			title = "Informe Académico",
			isTopDestination = true,
			isBottomDestination = true,
			topBarConfig = TopBarConfig.Record,
			bottomBarConfig = BottomBarConfig.Record
		)

	data object Evaluations :
		Destination(
			route = "evaluations",
			title = "Evaluaciones",
			isTopDestination = true,
			isBottomDestination = true,
			bottomBarConfig = BottomBarConfig.Evaluations
		)

	data object AddEvaluation
		: Destination(
		route = "add_evaluation",
		title = "Agregar evaluación",
		isTopDestination = false,
		isBottomDestination = false
	)

	data object About :
		Destination(
			route = "about",
			title = "Acerca de",
			isTopDestination = true,
			isBottomDestination = true,
			bottomBarConfig = BottomBarConfig.About
		)
}