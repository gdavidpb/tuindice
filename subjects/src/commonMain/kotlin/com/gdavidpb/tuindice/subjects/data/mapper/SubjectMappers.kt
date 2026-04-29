package com.gdavidpb.tuindice.subjects.data.mapper

import com.gdavidpb.tuindice.base.domain.model.GradingMode
import com.gdavidpb.tuindice.persistence.data.room.entity.SubjectDetailEntity
import com.gdavidpb.tuindice.persistence.data.room.entity.SubjectStatsAttemptBinEntity
import com.gdavidpb.tuindice.persistence.data.room.entity.SubjectStatsGradeBinEntity
import com.gdavidpb.tuindice.persistence.data.room.entity.SubjectStatsSegmentEntity
import com.gdavidpb.tuindice.subjects.data.model.GetSubjectStatsResponse
import com.gdavidpb.tuindice.subjects.data.model.SubjectAttemptBinResponse
import com.gdavidpb.tuindice.subjects.data.model.SubjectGradeBinResponse
import com.gdavidpb.tuindice.subjects.data.model.SubjectStatsSegmentResponse
import com.gdavidpb.tuindice.subjects.domain.model.SubjectAttemptBin
import com.gdavidpb.tuindice.subjects.domain.model.SubjectDetail
import com.gdavidpb.tuindice.subjects.domain.model.SubjectDetailResult
import com.gdavidpb.tuindice.subjects.domain.model.SubjectDifficultyBand
import com.gdavidpb.tuindice.subjects.domain.model.SubjectGradeBin
import com.gdavidpb.tuindice.subjects.domain.model.SubjectStatsSegment

internal const val CACHE_STATUS_READY = "READY"
internal const val CACHE_STATUS_INSUFFICIENT = "INSUFFICIENT_DATA"
internal const val SEGMENT_TYPE_CAREER = "CAREER"
internal const val SEGMENT_TYPE_GLOBAL = "GLOBAL"
internal const val SEGMENT_STATUS_READY = "READY"
internal const val SERIES_LATEST = "LATEST"
internal const val SERIES_ALL = "ALL"

fun GetSubjectStatsResponse.toSubjectDetailResult(): SubjectDetailResult {
	return SubjectDetailResult.Ready(
		detail = SubjectDetail(
			id = id,
			name = name,
			credits = credits,
			gradingMode = gradingMode,
			generatedAt = generatedAt,
			expiresAt = expiresAt,
			careerSegment = careerSegment?.toDomain(),
			globalSegment = globalSegment?.toDomain()
		)
	)
}

fun SubjectDetailResult.toSubjectDetailEntity(): SubjectDetailEntity {
	return when (this) {
		is SubjectDetailResult.Ready ->
			SubjectDetailEntity(
				subjectCode = detail.id,
				name = detail.name,
				credits = detail.credits,
				gradingMode = detail.gradingMode.name,
				cacheStatus = CACHE_STATUS_READY,
				generatedAt = detail.generatedAt,
				expiresAt = detail.expiresAt
			)

		is SubjectDetailResult.Unavailable ->
			SubjectDetailEntity(
				subjectCode = subjectCode,
				cacheStatus = CACHE_STATUS_INSUFFICIENT,
				generatedAt = expiresAt,
				expiresAt = expiresAt
			)
	}
}

fun SubjectDetail.toSegmentEntities(): List<SubjectStatsSegmentEntity> {
	return buildList {
		careerSegment?.let { segment ->
			add(
				segment.toEntity(
					subjectCode = id,
					segmentType = SEGMENT_TYPE_CAREER,
					generatedAt = generatedAt,
					expiresAt = expiresAt
				)
			)
		}
		globalSegment?.let { segment ->
			add(
				segment.toEntity(
					subjectCode = id,
					segmentType = SEGMENT_TYPE_GLOBAL,
					generatedAt = generatedAt,
					expiresAt = expiresAt
				)
			)
		}
	}
}

fun SubjectDetail.toGradeBinEntities(): List<SubjectStatsGradeBinEntity> {
	return buildList {
		careerSegment?.let { segment ->
			addAll(
				segment.latestGradeBins.map { bin ->
					bin.toEntity(
						subjectCode = id,
						segmentType = SEGMENT_TYPE_CAREER,
						segmentKey = null,
						series = SERIES_LATEST,
						generatedAt = generatedAt
					)
				}
			)
			addAll(
				segment.allGradeBins.map { bin ->
					bin.toEntity(
						subjectCode = id,
						segmentType = SEGMENT_TYPE_CAREER,
						segmentKey = null,
						series = SERIES_ALL,
						generatedAt = generatedAt
					)
				}
			)
		}
		globalSegment?.let { segment ->
			addAll(
				segment.latestGradeBins.map { bin ->
					bin.toEntity(
						subjectCode = id,
						segmentType = SEGMENT_TYPE_GLOBAL,
						segmentKey = null,
						series = SERIES_LATEST,
						generatedAt = generatedAt
					)
				}
			)
			addAll(
				segment.allGradeBins.map { bin ->
					bin.toEntity(
						subjectCode = id,
						segmentType = SEGMENT_TYPE_GLOBAL,
						segmentKey = null,
						series = SERIES_ALL,
						generatedAt = generatedAt
					)
				}
			)
		}
	}
}

fun SubjectDetail.toAttemptBinEntities(): List<SubjectStatsAttemptBinEntity> {
	return buildList {
		careerSegment?.let { segment ->
			addAll(
				segment.attemptsToPassBins.map { bin ->
					bin.toEntity(
						subjectCode = id,
						segmentType = SEGMENT_TYPE_CAREER,
						segmentKey = null,
						generatedAt = generatedAt
					)
				}
			)
		}
		globalSegment?.let { segment ->
			addAll(
				segment.attemptsToPassBins.map { bin ->
					bin.toEntity(
						subjectCode = id,
						segmentType = SEGMENT_TYPE_GLOBAL,
						segmentKey = null,
						generatedAt = generatedAt
					)
				}
			)
		}
	}
}

fun SubjectDetailEntity.toSubjectDetailResult(
	segments: List<SubjectStatsSegmentEntity>,
	gradeBins: List<SubjectStatsGradeBinEntity>,
	attemptBins: List<SubjectStatsAttemptBinEntity>
): SubjectDetailResult? {
	if (cacheStatus == CACHE_STATUS_INSUFFICIENT) {
		return SubjectDetailResult.Unavailable(
			subjectCode = subjectCode,
			expiresAt = expiresAt
		)
	}

	val careerSegment = segments
		.firstOrNull { entity -> entity.segmentType == SEGMENT_TYPE_CAREER && entity.status == SEGMENT_STATUS_READY }
		?.toDomain(gradeBins = gradeBins, attemptBins = attemptBins)
	val globalSegment = segments
		.firstOrNull { entity -> entity.segmentType == SEGMENT_TYPE_GLOBAL && entity.status == SEGMENT_STATUS_READY }
		?.toDomain(gradeBins = gradeBins, attemptBins = attemptBins)

	if (careerSegment == null && globalSegment == null) return null
	val resolvedName = name ?: return null
	val resolvedCredits = credits ?: return null
	val resolvedGradingMode = gradingMode ?: return null

	return SubjectDetailResult.Ready(
		detail = SubjectDetail(
			id = subjectCode,
			name = resolvedName,
			credits = resolvedCredits,
			gradingMode = GradingMode.valueOf(resolvedGradingMode),
			generatedAt = generatedAt,
			expiresAt = expiresAt,
			careerSegment = careerSegment,
			globalSegment = globalSegment
		)
	)
}

private fun SubjectStatsSegmentResponse.toDomain(): SubjectStatsSegment {
	return SubjectStatsSegment(
		sampleStudents = sampleStudents,
		closedAttempts = closedAttempts,
		numericLatestStudents = numericLatestStudents,
		latestApprovedCount = latestApprovedCount,
		latestFailedCount = latestFailedCount,
		latestRetiredCount = latestRetiredCount,
		latestUnreportedCount = latestUnreportedCount,
		averageGrade = averageGrade,
		medianGrade = medianGrade,
		stddevGrade = stddevGrade,
		firstAttemptPassRate = firstAttemptPassRate,
		approvalRate = approvalRate,
		latestFailureRate = latestFailureRate,
		latestWithdrawalRate = latestWithdrawalRate,
		retakeRate = retakeRate,
		avgAttemptsToPass = avgAttemptsToPass,
		medianAttemptsToPass = medianAttemptsToPass,
		difficultyScore = difficultyScore,
		difficultyBand = difficultyBand,
		firstClosedTermStartAt = firstClosedTermStartAt,
		lastClosedTermStartAt = lastClosedTermStartAt,
		latestGradeBins = latestGradeBins.map(SubjectGradeBinResponse::toDomain),
		allGradeBins = allGradeBins.map(SubjectGradeBinResponse::toDomain),
		attemptsToPassBins = attemptsToPassBins.map(SubjectAttemptBinResponse::toDomain)
	)
}

private fun SubjectStatsSegment.toEntity(
	subjectCode: String,
	segmentType: String,
	generatedAt: Long,
	expiresAt: Long
): SubjectStatsSegmentEntity {
	return SubjectStatsSegmentEntity(
		id = "$subjectCode:$segmentType",
		subjectCode = subjectCode,
		segmentType = segmentType,
		status = SEGMENT_STATUS_READY,
		generatedAt = generatedAt,
		expiresAt = expiresAt,
		sampleStudents = sampleStudents,
		closedAttempts = closedAttempts,
		numericLatestStudents = numericLatestStudents,
		latestApprovedCount = latestApprovedCount,
		latestFailedCount = latestFailedCount,
		latestRetiredCount = latestRetiredCount,
		latestUnreportedCount = latestUnreportedCount,
		averageGrade = averageGrade,
		medianGrade = medianGrade,
		stddevGrade = stddevGrade,
		firstAttemptPassRate = firstAttemptPassRate,
		approvalRate = approvalRate,
		latestFailureRate = latestFailureRate,
		latestWithdrawalRate = latestWithdrawalRate,
		retakeRate = retakeRate,
		avgAttemptsToPass = avgAttemptsToPass,
		medianAttemptsToPass = medianAttemptsToPass,
		difficultyScore = difficultyScore,
		difficultyBand = difficultyBand?.name,
		firstClosedTermStartAt = firstClosedTermStartAt,
		lastClosedTermStartAt = lastClosedTermStartAt
	)
}

private fun SubjectStatsSegmentEntity.toDomain(
	gradeBins: List<SubjectStatsGradeBinEntity>,
	attemptBins: List<SubjectStatsAttemptBinEntity>
): SubjectStatsSegment {
	return SubjectStatsSegment(
		sampleStudents = sampleStudents,
		closedAttempts = closedAttempts,
		numericLatestStudents = numericLatestStudents,
		latestApprovedCount = latestApprovedCount,
		latestFailedCount = latestFailedCount,
		latestRetiredCount = latestRetiredCount,
		latestUnreportedCount = latestUnreportedCount,
		averageGrade = averageGrade,
		medianGrade = medianGrade,
		stddevGrade = stddevGrade,
		firstAttemptPassRate = firstAttemptPassRate,
		approvalRate = approvalRate,
		latestFailureRate = latestFailureRate,
		latestWithdrawalRate = latestWithdrawalRate,
		retakeRate = retakeRate,
		avgAttemptsToPass = avgAttemptsToPass,
		medianAttemptsToPass = medianAttemptsToPass,
		difficultyScore = difficultyScore,
		difficultyBand = difficultyBand?.let(SubjectDifficultyBand::valueOf),
		firstClosedTermStartAt = firstClosedTermStartAt,
		lastClosedTermStartAt = lastClosedTermStartAt,
		latestGradeBins = gradeBins.filter { bin ->
			bin.segmentType == segmentType && bin.series == SERIES_LATEST
		}.sortedBy(SubjectStatsGradeBinEntity::grade).map { bin ->
			SubjectGradeBin(grade = bin.grade, count = bin.count)
		},
		allGradeBins = gradeBins.filter { bin ->
			bin.segmentType == segmentType && bin.series == SERIES_ALL
		}.sortedBy(SubjectStatsGradeBinEntity::grade).map { bin ->
			SubjectGradeBin(grade = bin.grade, count = bin.count)
		},
		attemptsToPassBins = attemptBins.filter { bin ->
			bin.segmentType == segmentType
		}.sortedBy { bin ->
			when (bin.bucket) {
				"1" -> 0
				"2" -> 1
				else -> 2
			}
		}.map { bin ->
			SubjectAttemptBin(bucket = bin.bucket, count = bin.count)
		}
	)
}

private fun SubjectGradeBinResponse.toDomain(): SubjectGradeBin {
	return SubjectGradeBin(
		grade = grade,
		count = count
	)
}

private fun SubjectAttemptBinResponse.toDomain(): SubjectAttemptBin {
	return SubjectAttemptBin(
		bucket = bucket,
		count = count
	)
}

private fun SubjectGradeBin.toEntity(
	subjectCode: String,
	segmentType: String,
	segmentKey: Int?,
	series: String,
	generatedAt: Long
): SubjectStatsGradeBinEntity {
	return SubjectStatsGradeBinEntity(
		id = "$subjectCode:$segmentType:${segmentKey ?: "none"}:$series:$grade",
		subjectCode = subjectCode,
		segmentType = segmentType,
		segmentKey = segmentKey,
		series = series,
		grade = grade,
		count = count,
		generatedAt = generatedAt
	)
}

private fun SubjectAttemptBin.toEntity(
	subjectCode: String,
	segmentType: String,
	segmentKey: Int?,
	generatedAt: Long
): SubjectStatsAttemptBinEntity {
	return SubjectStatsAttemptBinEntity(
		id = "$subjectCode:$segmentType:${segmentKey ?: "none"}:$bucket",
		subjectCode = subjectCode,
		segmentType = segmentType,
		segmentKey = segmentKey,
		bucket = bucket,
		count = count,
		generatedAt = generatedAt
	)
}
