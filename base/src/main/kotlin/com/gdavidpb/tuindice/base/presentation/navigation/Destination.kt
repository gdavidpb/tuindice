package com.gdavidpb.tuindice.base.presentation.navigation

import com.gdavidpb.tuindice.base.presentation.model.BottomBarConfig
import com.gdavidpb.tuindice.base.presentation.model.TopBarConfig
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
abstract class Destination2

@Serializable
sealed class Destination(
	@SerialName("destination_title")
	open val title: String = "",
	@Transient
	val isTopDestination: Boolean = false,
	@Transient
	val isBottomDestination: Boolean = false,
	@Transient
	val isDialogDestination: Boolean = false,
	@Transient
	val topBarConfig: TopBarConfig? = null,
	@Transient
	val bottomBarConfig: BottomBarConfig? = null
) {
	@Serializable
	data object EnrollmentProofFetch
		: Destination(
		isDialogDestination = true
	)

	@Serializable
	data object UpdatePassword
		: Destination(
		isDialogDestination = true
	)

	@Serializable
	data object SignIn
		: Destination(
		title = "TuIndice",
		isTopDestination = true,
		isBottomDestination = false
	)

	@Serializable
	data object SignOut
		: Destination(
		isDialogDestination = true
	)

	@Serializable
	data class Browser(
		override val title: String,
		val url: String
	) : Destination(
		title = title,
		isTopDestination = false,
		isBottomDestination = false
	)

	@Serializable
	data object Record
		: Destination(
		title = "Informe Académico",
		isTopDestination = true,
		isBottomDestination = true,
		topBarConfig = TopBarConfig.Record,
		bottomBarConfig = BottomBarConfig.Record
	)

	@Serializable
	data object Evaluations
		: Destination(
		title = "Evaluaciones",
		isTopDestination = true,
		isBottomDestination = true,
		bottomBarConfig = BottomBarConfig.Evaluations
	)

	@Serializable
	data class Evaluation(
		override val title: String,
		val evaluationId: String? = null
	) : Destination(
		title = title,
		isTopDestination = false,
		isBottomDestination = false
	)

	@Serializable
	data object About
		: Destination(
		title = "Acerca de",
		isTopDestination = true,
		isBottomDestination = true,
		bottomBarConfig = BottomBarConfig.About
	)
}