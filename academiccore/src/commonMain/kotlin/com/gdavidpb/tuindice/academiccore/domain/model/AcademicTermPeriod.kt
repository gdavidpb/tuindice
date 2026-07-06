package com.gdavidpb.tuindice.academiccore.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class AcademicTermPeriod(
	val sequence: Int,
	val label: String,
	val shortLabel: String,
	// Calendar months the period spans (1 = January, 12 = December), used to bound
	// evaluation dates to the term.
	val startMonth: Int,
	val endMonth: Int,
	val supportsSyntheticPlanning: Boolean = true
) {
	@SerialName("JAN_MAR")
	JAN_MAR(
		sequence = 1,
		label = "Enero - Marzo",
		shortLabel = "Ene - Mar",
		startMonth = 1,
		endMonth = 3
	),

	@SerialName("JAN_MAY")
	JAN_MAY(
		sequence = 2,
		label = "Enero - Mayo",
		shortLabel = "Ene - May",
		startMonth = 1,
		endMonth = 5,
		supportsSyntheticPlanning = false
	),

	@SerialName("APR_JUL")
	APR_JUL(
		sequence = 3,
		label = "Abril - Julio",
		shortLabel = "Abr - Jul",
		startMonth = 4,
		endMonth = 7
	),

	@SerialName("JUL_AUG")
	JUL_AUG(
		sequence = 4,
		label = "Julio - Agosto",
		shortLabel = "Jul - Ago",
		startMonth = 7,
		endMonth = 8
	),

	@SerialName("APR_SEP")
	APR_SEP(
		sequence = 5,
		label = "Abril - Septiembre",
		shortLabel = "Abr - Sep",
		startMonth = 4,
		endMonth = 9,
		supportsSyntheticPlanning = false
	),

	@SerialName("SEP_DEC")
	SEP_DEC(
		sequence = 6,
		label = "Septiembre - Diciembre",
		shortLabel = "Sep - Dic",
		startMonth = 9,
		endMonth = 12
	),

	@SerialName("JUL_DEC")
	JUL_DEC(
		sequence = 7,
		label = "Julio - Diciembre",
		shortLabel = "Jul - Dic",
		startMonth = 7,
		endMonth = 12,
		supportsSyntheticPlanning = false
	);
}
