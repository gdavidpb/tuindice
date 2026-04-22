package com.gdavidpb.tuindice.subjects.ui.model

import com.gdavidpb.tuindice.subjects.domain.model.SubjectGradeBin
import kotlin.math.roundToInt

data class SubjectGradeChartSummary(
	val modalGrades: List<Int>,
	val medianGrade: Double? = null,
	val medianLabel: String? = null,
	val stddevGrade: Double? = null,
	val stddevLabel: String? = null,
	val stddevRangeStart: Double? = null,
	val stddevRangeEnd: Double? = null
) {
	val modeLabel: String?
		get() = modalGrades.takeIf(List<Int>::isNotEmpty)?.joinToString("·")

	companion object {
		private const val MIN_GRADE = 1.0
		private const val MAX_GRADE = 5.0

		fun from(
			latestGradeBins: List<SubjectGradeBin>,
			medianGrade: Double?,
			stddevGrade: Double?
		): SubjectGradeChartSummary {
			val modalGrades = latestGradeBins.resolveModalGrades()
			val safeMedian = medianGrade?.coerceIn(MIN_GRADE, MAX_GRADE)
			val safeStddev = stddevGrade?.coerceAtLeast(0.0)
			val rangeStart =
				if (safeMedian != null && safeStddev != null) {
					(safeMedian - safeStddev).coerceIn(MIN_GRADE, MAX_GRADE)
				} else {
					null
				}
			val rangeEnd =
				if (safeMedian != null && safeStddev != null) {
					(safeMedian + safeStddev).coerceIn(MIN_GRADE, MAX_GRADE)
				} else {
					null
				}
			return SubjectGradeChartSummary(
				modalGrades = modalGrades,
				medianGrade = safeMedian,
				medianLabel = safeMedian?.toStatLabel(),
				stddevGrade = safeStddev,
				stddevLabel = safeStddev?.toStatLabel(),
				stddevRangeStart = rangeStart,
				stddevRangeEnd = rangeEnd
			)
		}
	}
}

private fun List<SubjectGradeBin>.resolveModalGrades(): List<Int> {
	val maxCount = maxOfOrNull(SubjectGradeBin::count)
		?.takeIf { count -> count > 0 }
		?: return emptyList()
	return filter { bin -> bin.count == maxCount }
		.map(SubjectGradeBin::grade)
		.sorted()
}

private fun Double.toStatLabel(): String {
	val rounded = (this * 10).roundToInt() / 10.0
	return if (rounded % 1.0 == 0.0) {
		rounded.toInt().toString()
	} else {
		rounded.toString()
	}
}
