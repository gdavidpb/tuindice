package com.gdavidpb.tuindice.pensum.ui.view

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import com.gdavidpb.tuindice.pensum.presentation.model.PensumEdgeItem
import com.gdavidpb.tuindice.pensum.presentation.model.PensumNodeItem
import com.gdavidpb.tuindice.pensum.presentation.model.PensumScreenModel
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

internal fun PensumScreenModel.edgeRoute(
	edge: PensumEdgeItem,
	endpointGap: Float,
	rerouteSpacing: Float
): List<Offset> {
	val from = nodes.firstOrNull { node -> node.id == edge.fromNodeId }
	val to = nodes.firstOrNull { node -> node.id == edge.toNodeId }
	if (from == null || to == null) {
		return edge.points.map { point -> Offset(point.x.toFloat(), point.y.toFloat()) }
	}

	val fromCenter = from.center
	val toCenter = to.center
	val dx = toCenter.x - fromCenter.x
	val dy = toCenter.y - fromCenter.y
	val shouldUseVerticalRoute = abs(dy) > abs(dx) * 0.85f && abs(dy) > from.height.toFloat()

	return if (shouldUseVerticalRoute) {
		val direction = if (dy >= 0f) 1f else -1f
		val start = Offset(
			x = fromCenter.x,
			y = if (direction > 0f) from.bottom + endpointGap else from.y.toFloat() - endpointGap
		)
		val end = Offset(
			x = toCenter.x,
			y = if (direction > 0f) to.y.toFloat() - endpointGap else to.bottom + endpointGap
		)
		val routeGap = (end.y - start.y) * direction
		val turnY = if (routeGap >= 0f) {
			(start.y + end.y) / 2f
		} else if (direction > 0f) {
			max(from.bottom, to.bottom) + rerouteSpacing
		} else {
			min(from.y.toFloat(), to.y.toFloat()) - rerouteSpacing
		}
		if (abs(start.x - end.x) < 1f) {
			listOf(start, end)
		} else {
			listOf(
				start,
				Offset(start.x, turnY),
				Offset(end.x, turnY),
				end
			)
		}
	} else {
		val direction = if (dx >= 0f) 1f else -1f
		val start = Offset(
			x = if (direction > 0f) from.right + endpointGap else from.x.toFloat() - endpointGap,
			y = fromCenter.y
		)
		val end = Offset(
			x = if (direction > 0f) to.x.toFloat() - endpointGap else to.right + endpointGap,
			y = toCenter.y
		)
		val routeGap = (end.x - start.x) * direction
		val turnX = if (routeGap >= 0f) {
			(start.x + end.x) / 2f
		} else if (direction > 0f) {
			max(from.right, to.right) + rerouteSpacing
		} else {
			min(from.x.toFloat(), to.x.toFloat()) - rerouteSpacing
		}
		if (abs(start.y - end.y) < 1f) {
			listOf(start, end)
		} else {
			listOf(
				start,
				Offset(turnX, start.y),
				Offset(turnX, end.y),
				end
			)
		}
	}
}

internal fun List<Offset>.toRoundedOrthogonalPath(cornerRadius: Float): Path {
	val compactPoints = withoutNearDuplicates()
	return Path().apply {
		if (compactPoints.isEmpty()) return@apply
		moveTo(compactPoints.first().x, compactPoints.first().y)
		if (compactPoints.size == 1) return@apply

		for (index in 1 until compactPoints.lastIndex) {
			val previous = compactPoints[index - 1]
			val current = compactPoints[index]
			val next = compactPoints[index + 1]
			if (!current.isOrthogonalTurn(previous, next)) {
				lineTo(current.x, current.y)
				continue
			}

			val radius = min(
				cornerRadius,
				min(previous.distanceTo(current) / 2f, current.distanceTo(next) / 2f)
			)
			val beforeCorner = current.toward(previous, radius)
			val afterCorner = current.toward(next, radius)
			lineTo(beforeCorner.x, beforeCorner.y)
			quadraticTo(current.x, current.y, afterCorner.x, afterCorner.y)
		}
		lineTo(compactPoints.last().x, compactPoints.last().y)
	}
}

private val PensumNodeItem.center: Offset
	get() = Offset(
		x = x.toFloat() + width.toFloat() / 2f,
		y = y.toFloat() + height.toFloat() / 2f
	)

private val PensumNodeItem.right: Float
	get() = x.toFloat() + width.toFloat()

private val PensumNodeItem.bottom: Float
	get() = y.toFloat() + height.toFloat()

private fun List<Offset>.withoutNearDuplicates(): List<Offset> {
	return fold(emptyList()) { points, point ->
		if (points.lastOrNull()?.distanceTo(point)?.let { distance -> distance < 0.5f } == true) {
			points
		} else {
			points + point
		}
	}
}

private fun Offset.isOrthogonalTurn(previous: Offset, next: Offset): Boolean {
	val incomingHorizontal = abs(x - previous.x) > 0.5f && abs(y - previous.y) < 0.5f
	val incomingVertical = abs(y - previous.y) > 0.5f && abs(x - previous.x) < 0.5f
	val outgoingHorizontal = abs(next.x - x) > 0.5f && abs(next.y - y) < 0.5f
	val outgoingVertical = abs(next.y - y) > 0.5f && abs(next.x - x) < 0.5f
	return (incomingHorizontal && outgoingVertical) || (incomingVertical && outgoingHorizontal)
}

private fun Offset.toward(target: Offset, distance: Float): Offset {
	val dx = target.x - x
	val dy = target.y - y
	val length = sqrt(dx * dx + dy * dy)
	if (length <= 0.5f) return this
	val ratio = (distance / length).coerceIn(0f, 1f)
	return Offset(
		x = x + dx * ratio,
		y = y + dy * ratio
	)
}

private fun Offset.distanceTo(other: Offset): Float {
	val dx = other.x - x
	val dy = other.y - y
	return sqrt(dx * dx + dy * dy)
}
