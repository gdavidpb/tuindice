package com.gdavidpb.tuindice.assistant.presentation.mapper

import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.base.utils.extension.formatGrade
import org.jetbrains.compose.resources.StringResource
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_card_b_points_many
import tuindice.assistant.generated.resources.assistant_card_b_points_one

// A grade or a points figure as the Evaluations tab writes it: a whole number without decimals, any other with two, and
// the decimal point: «14», «12.50».
fun Double.toCardCompactNumberText(): String {
	val decimals = if (this == toInt().toDouble()) 0 else 2

	return formatGrade(decimals)
}

// A count with its noun: «1 evaluación», «3 evaluaciones». [one] is the noun for one, [many] for any other count.
fun chatCardCountText(count: Int, one: StringResource, many: StringResource): UiText =
	UiText.Resource(if (count == 1) one else many, listOf(count))

// Points with their noun: «1 punto», «12.50 puntos».
fun chatCardPointsText(points: Double): UiText =
	UiText.Resource(
		if (points == 1.0) Res.string.assistant_card_b_points_one else Res.string.assistant_card_b_points_many,
		listOf(points.toCardCompactNumberText())
	)

// The noun that the caption of a figure carries, with no number: the figure shows it. [one] is the noun for one,
// [many] for any other count: «semana pesada» under the figure 1, «semanas pesadas» under the figure 2.
fun chatCardNounText(count: Int, one: StringResource, many: StringResource): UiText =
	UiText.Resource(if (count == 1) one else many)

// The same for a figure of points, which can be a decimal: only exactly one point takes the singular.
fun chatCardPointsNounText(points: Double, one: StringResource, many: StringResource): UiText =
	UiText.Resource(if (points == 1.0) one else many)
