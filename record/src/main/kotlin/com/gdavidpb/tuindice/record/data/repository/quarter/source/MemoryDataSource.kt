package com.gdavidpb.tuindice.record.data.repository.quarter.source

import android.util.LruCache
import com.gdavidpb.tuindice.base.domain.model.quarter.Quarter
import com.gdavidpb.tuindice.base.utils.extension.getOrPut
import com.gdavidpb.tuindice.base.utils.extension.selfMapNotNull
import com.gdavidpb.tuindice.record.data.repository.quarter.CacheDataSource
import com.gdavidpb.tuindice.record.utils.extensions.computeCredits
import com.gdavidpb.tuindice.record.utils.extensions.computeGrade
import com.gdavidpb.tuindice.record.utils.extensions.computeGradeSum
import java.util.Objects

private val computationCache = LruCache<Int, Quarter>(1_000)

class MemoryDataSource : CacheDataSource {
	override suspend fun computeQuarters(
		uid: String,
		origin: Quarter,
		quarters: List<Quarter>
	): List<Quarter> {
		return quarters
			.toMutableList()
			.selfMapNotNull { quarter ->
				if (quarter.startDate >= origin.startDate) {
					val id = computeIdentifier(
						origin = quarter,
						quarters = quarters
					)

					synchronized(computationCache) {
						computationCache
							.getOrPut(id) {
								quarter.copy(
									grade = quarter.subjects.computeGrade(),
									gradeSum = quarters.computeGradeSum(until = quarter),
									credits = quarter.subjects.computeCredits()
								)
							}
					}
				} else {
					null
				}
			}
	}

	override suspend fun invalidate(uid: String) {
		synchronized(computationCache) {
			computationCache.evictAll()
		}
	}

	private fun computeIdentifier(origin: Quarter, quarters: List<Quarter>): Int {
		val grades = quarters
			.flatMap { quarter ->
				quarter.subjects.map { subject ->
					subject.grade
				}
			}

		return Objects.hash(origin.id, grades)
	}
}