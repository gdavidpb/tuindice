package com.gdavidpb.tuindice.subjects.data.mapper

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicPensumNodeStatus
import com.gdavidpb.tuindice.base.domain.model.GradingMode
import com.gdavidpb.tuindice.persistence.data.room.entity.SubjectCatalogCacheEntity
import com.gdavidpb.tuindice.base.domain.utils.SubjectCatalogSearchNormalizer
import com.gdavidpb.tuindice.subjects.data.model.SearchSubjectsResponse
import com.gdavidpb.tuindice.subjects.data.model.SubjectSearchResultResponse
import com.gdavidpb.tuindice.subjects.domain.model.SubjectDetail
import com.gdavidpb.tuindice.subjects.domain.model.SubjectSearchResult

fun SearchSubjectsResponse.toSubjectSearchResults(): List<SubjectSearchResult> {
	return results.map(SubjectSearchResultResponse::toDomain)
}

fun SubjectSearchResult.toSubjectCatalogCacheEntity(updatedAt: Long): SubjectCatalogCacheEntity {
	return SubjectCatalogCacheEntity(
		subjectCode = subjectCode,
		name = name,
		credits = credits,
		gradingMode = gradingMode?.name,
		normalizedCode = SubjectCatalogSearchNormalizer.normalize(subjectCode),
		normalizedName = SubjectCatalogSearchNormalizer.normalize(name),
		updatedAt = updatedAt
	)
}

fun SubjectCatalogCacheEntity.toSubjectSearchResult(
	pensumStatus: AcademicPensumNodeStatus? = null
): SubjectSearchResult {
	return SubjectSearchResult(
		subjectCode = subjectCode,
		name = name,
		credits = credits,
		gradingMode = gradingMode?.let(GradingMode::valueOf),
		pensumStatus = pensumStatus
	)
}

fun SubjectDetail.toSubjectSearchResult(): SubjectSearchResult {
	return SubjectSearchResult(
		subjectCode = id,
		name = name,
		credits = credits,
		gradingMode = gradingMode
	)
}

private fun SubjectSearchResultResponse.toDomain(): SubjectSearchResult {
	return SubjectSearchResult(
		subjectCode = subjectCode,
		name = name,
		credits = credits,
		gradingMode = gradingMode
	)
}
