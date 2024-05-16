package com.gdavidpb.tuindice.record.data.repository.quarter.source

import android.util.LruCache
import com.gdavidpb.tuindice.base.utils.extension.getOrPut
import com.gdavidpb.tuindice.base.utils.extension.selfMapNotNull
import com.gdavidpb.tuindice.record.data.repository.quarter.CacheDataSource
import com.gdavidpb.tuindice.record.data.repository.quarter.model.LocalQuarter
import com.gdavidpb.tuindice.record.data.utils.computeCredits
import com.gdavidpb.tuindice.record.data.utils.computeGrade
import com.gdavidpb.tuindice.record.data.utils.computeGradeSum
import java.util.Objects

class MemoryDataSource(
	private val computationCache: LruCache<Int, LocalQuarter>
) : CacheDataSource {
	override suspend fun computeQuarters(
		uid: String,
		origin: LocalQuarter,
		quarters: List<LocalQuarter>
	): List<LocalQuarter> {
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

	private fun computeIdentifier(origin: LocalQuarter, quarters: List<LocalQuarter>): Int {
		val grades = quarters
			.flatMap { quarter ->
				quarter.subjects.map { subject ->
					subject.grade
				}
			}

		return Objects.hash(origin.id, grades)
	}
}