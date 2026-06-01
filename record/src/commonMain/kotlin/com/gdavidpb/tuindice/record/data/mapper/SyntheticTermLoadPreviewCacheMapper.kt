package com.gdavidpb.tuindice.record.data.mapper

import com.gdavidpb.tuindice.persistence.data.room.entity.SyntheticTermLoadPreviewCacheEntity
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermLoadBand
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermLoadBasis
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermLoadConfidence
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermLoadDetail
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermLoadPreview

internal fun syntheticTermLoadPreviewCacheKey(
	termKey: String,
	subjectCodes: List<String>
): String {
	return listOf(
		SyntheticTermLoadPreviewCacheKeyPrefix,
		termKey.trim().uppercase(),
		subjectCodes.toSyntheticTermLoadPreviewSubjectCodesKey()
	).joinToString(separator = SyntheticTermLoadPreviewCacheKeySeparator)
}

internal fun SyntheticTermLoadPreview.toSyntheticTermLoadPreviewCacheEntity(
	cacheKey: String,
	termKey: String,
	subjectCodes: List<String>,
	updatedAt: Long
): SyntheticTermLoadPreviewCacheEntity {
	return SyntheticTermLoadPreviewCacheEntity(
		cacheKey = cacheKey,
		termKey = termKey,
		subjectCodesKey = subjectCodes.toSyntheticTermLoadPreviewSubjectCodesKey(),
		available = available,
		reason = reason,
		band = band?.name,
		credits = credits,
		weightedDifficulty = weightedDifficulty,
		loadIndex = loadIndex,
		baselineLoadIndex = baselineLoadIndex,
		effectiveTerms = effectiveTerms,
		basis = basis?.name,
		confidence = confidence?.name,
		detail = detail?.name,
		updatedAt = updatedAt,
		expiresAt = updatedAt + SyntheticTermLoadPreviewCacheTtlMillis
	)
}

internal fun SyntheticTermLoadPreviewCacheEntity.toSyntheticTermLoadPreview(): SyntheticTermLoadPreview {
	return SyntheticTermLoadPreview(
		available = available,
		reason = reason,
		band = band?.let { value -> runCatching { SyntheticTermLoadBand.valueOf(value) }.getOrNull() },
		credits = credits,
		weightedDifficulty = weightedDifficulty,
		loadIndex = loadIndex,
		baselineLoadIndex = baselineLoadIndex,
		effectiveTerms = effectiveTerms,
		basis = basis?.let { value -> runCatching { SyntheticTermLoadBasis.valueOf(value) }.getOrNull() },
		confidence = confidence?.let { value -> runCatching { SyntheticTermLoadConfidence.valueOf(value) }.getOrNull() },
		detail = detail?.let { value -> runCatching { SyntheticTermLoadDetail.valueOf(value) }.getOrNull() }
	)
}

private fun List<String>.toSyntheticTermLoadPreviewSubjectCodesKey(): String {
	return map { subjectCode -> subjectCode.trim().uppercase() }
		.filter { subjectCode -> subjectCode.isNotEmpty() }
		.distinct()
		.sorted()
		.joinToString(separator = SyntheticTermLoadPreviewSubjectCodeSeparator)
}

private const val SyntheticTermLoadPreviewCacheKeyPrefix = "synthetic-term-load-preview"
private const val SyntheticTermLoadPreviewCacheKeySeparator = "::"
private const val SyntheticTermLoadPreviewSubjectCodeSeparator = "|"
private const val SyntheticTermLoadPreviewCacheTtlMillis = 24L * 60L * 60L * 1_000L
