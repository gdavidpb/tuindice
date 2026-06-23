package com.gdavidpb.tuindice.academiccore.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class AcademicTermPeriod(
	val sequence: Int,
	val label: String,
	val shortLabel: String,
	val supportsSyntheticPlanning: Boolean = true
) {
	@SerialName("JAN_MAR")
	JAN_MAR(
		sequence = 1,
		label = "Enero - Marzo",
		shortLabel = "Ene - Mar"
	),

	@SerialName("APR_JUL")
	APR_JUL(
		sequence = 2,
		label = "Abril - Julio",
		shortLabel = "Abr - Jul"
	),

	@SerialName("JUL_AUG")
	JUL_AUG(
		sequence = 3,
		label = "Julio - Agosto",
		shortLabel = "Jul - Ago"
	),

	@SerialName("SEP_DEC")
	SEP_DEC(
		sequence = 4,
		label = "Septiembre - Diciembre",
		shortLabel = "Sep - Dic"
	),

	@SerialName("JUL_DEC")
	JUL_DEC(
		sequence = 5,
		label = "Julio - Diciembre",
		shortLabel = "Jul - Dic",
		supportsSyntheticPlanning = false
	),

	@SerialName("APR_SEP")
	APR_SEP(
		sequence = 6,
		label = "Abril - Septiembre",
		shortLabel = "Abr - Sep",
		supportsSyntheticPlanning = false
	),

	@SerialName("JAN_MAY")
	JAN_MAY(
		sequence = 7,
		label = "Enero - Mayo",
		shortLabel = "Ene - May",
		supportsSyntheticPlanning = false
	);
}
