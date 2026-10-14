package com.gdavidpb.tuindice.academiccore.domain.model

/**
 * How many subjects of the closed terms are in each state, each subject counted once by its code, from its results. A
 * subject is approved when any of its results is an approval, failed when it has a failure and no approval, withdrawn
 * when it has no result and was retired, and without result otherwise. [approvedFirstTry] counts the approved subjects
 * whose first result was an approval, out of [firstTryBase], the subjects with at least one result. [repeated] counts
 * those with two or more.
 */
data class SubjectOutcomes(
	val approved: Int,
	val failed: Int,
	val withdrawn: Int,
	val noResult: Int,
	val approvedFirstTry: Int,
	val firstTryBase: Int,
	val repeated: Int
)
