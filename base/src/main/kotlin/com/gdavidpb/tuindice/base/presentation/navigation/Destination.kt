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
	object EnrollmentProofFetch :
		Destination(
			route = "enrollment_proof_fetch",
			isDialogDestination = true
		)

	object UpdatePassword :
		Destination(
			route = "update_password",
			isDialogDestination = true
		)

	object SignIn :
		Destination(
			route = "sign_in",
			title = "TuIndice",
			isTopDestination = true,
			isBottomDestination = false
		)

	object SignOut :
		Destination(
			route = "sign_out",
			isDialogDestination = true
		)

	object Browser :
		Destination(
			route = "browser",
			title = "{title}",
			isTopDestination = false,
			isBottomDestination = false
		)

	object Summary :
		Destination(
			route = "summary",
			title = "Resumen",
			isTopDestination = true,
			isBottomDestination = true,
			topBarConfig = TopBarConfig.Summary,
			bottomBarConfig = BottomBarConfig.Summary
		)

	object Record :
		Destination(
			route = "record",
			title = "Informe Académico",
			isTopDestination = true,
			isBottomDestination = true,
			topBarConfig = TopBarConfig.Record,
			bottomBarConfig = BottomBarConfig.Record
		)

	object Evaluations :
		Destination(
			route = "evaluations",
			title = "Evaluaciones",
			isTopDestination = true,
			isBottomDestination = true,
			bottomBarConfig = BottomBarConfig.Evaluations
		)

	object AddEvaluation :
		Destination(
			route = "add_evaluation",
			title = "Agregar evaluación",
			isTopDestination = false,
			isBottomDestination = false
		) {
		object Step1 :
			Destination(
				route = "add_evaluation_step_1",
				title = "Agregar evaluación",
				isTopDestination = false,
				isBottomDestination = false
			)

		object Step2 :
			Destination(
				route = "add_evaluation_step_2",
				title = "Agregar evaluación",
				isTopDestination = false,
				isBottomDestination = false
			)
	}

	object About :
		Destination(
			route = "about",
			title = "Acerca de",
			isTopDestination = true,
			isBottomDestination = true,
			bottomBarConfig = BottomBarConfig.About
		)
}