package com.gdavidpb.tuindice.record.data.cache

import com.gdavidpb.tuindice.base.domain.model.quarter.Quarter
import com.gdavidpb.tuindice.base.utils.extension.selfMapNotNull
import com.gdavidpb.tuindice.record.data.quarter.source.CacheDataSource
import com.gdavidpb.tuindice.record.utils.extensions.computeCredits
import com.gdavidpb.tuindice.record.utils.extensions.computeGrade
import com.gdavidpb.tuindice.record.utils.extensions.computeGradeSum
import java.util.concurrent.ConcurrentHashMap

private val computationCache = ConcurrentHashMap<Int, Quarter>()

class MemoryDataSource : CacheDataSource {
	override suspend fun computeQuarters(
		uid: String,
		from: Quarter,
		quarters: List<Quarter>
	): List<Quarter> {
		return quarters
			.toMutableList()
			.selfMapNotNull { quarter ->
				if (quarter.startDate >= from.startDate) {
					val id = quarter.hashCode()

					computationCache
						.getOrPut(id) {
							quarter.copy(
								grade = quarter.subjects.computeGrade(),
								gradeSum = quarters.computeGradeSum(until = quarter),
								credits = quarter.subjects.computeCredits()
							)
						}
				} else {
					null
				}
			}
	}

	override suspend fun invalidate(uid: String) {
		computationCache.clear()
	}
}