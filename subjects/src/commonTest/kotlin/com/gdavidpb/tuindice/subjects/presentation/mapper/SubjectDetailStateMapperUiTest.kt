package com.gdavidpb.tuindice.subjects.presentation.mapper

import com.gdavidpb.tuindice.academiccore.domain.model.GradingMode
import com.gdavidpb.tuindice.subjects.domain.model.SubjectDetailResult
import com.gdavidpb.tuindice.subjects.domain.model.SubjectSegmentTab
import com.gdavidpb.tuindice.subjects.domain.model.SubjectStatsSegment
import com.gdavidpb.tuindice.subjects.presentation.contract.SubjectDetail
import com.gdavidpb.tuindice.subjects.presentation.model.SubjectDetailItem
import com.gdavidpb.tuindice.subjects.testing.readySubjectDetail
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SubjectDetailStateMapperUiTest {
	@Test
	fun when_careerSegmentIsPresent_then_selectsCareerTabAndFormatsMetrics() = runTest {
		val state = readySubjectDetail(subjectCode = "MAT101").toViewState()

		val content = assertIs<SubjectDetail.State.Content>(state)
		val item = content.detail

		assertEquals("MAT101", item.id)
		assertEquals("Calculo I", item.name)
		assertEquals("5 UC", item.creditsText)
		assertNull(item.gradingModeText)
		assertTrue(item.generatedAtText.matches(Regex("""Actualizado \d{2}/\d{2}/\d{4}""")))
		assertEquals(SubjectSegmentTab.CAREER, item.selectedTab)
		assertFalse(item.hasSegmentTabs)
		assertEquals(SubjectDetailItem.ChartMode.NUMERIC_GRADES, item.chartMode)
		assertNull(item.globalSegment)

		val career = assertNotNull(item.careerSegment)
		assertEquals("18", career.studentsText)
		assertEquals("24", career.attemptsText)
		assertEquals("41 / 100", career.difficultyScoreText)
		assertEquals("Media", career.difficultyBandText)
		assertEquals("55%", career.firstAttemptPassRateText)
		assertEquals("72%", career.approvalRateText)
		assertEquals("22%", career.failureRateText)
		assertEquals("6%", career.withdrawalRateText)
		assertEquals(12, career.latestApprovedCount)
		assertEquals(4, career.latestFailedCount)
		assertEquals(1, career.latestRetiredCount)
		assertEquals(1, career.latestUnreportedCount)
		assertEquals(4.0, career.medianGrade)
		assertEquals(0.8, career.stddevGrade)
		assertEquals(
			listOf(
				SubjectDetailItem.GradeBinItem(grade = 1, count = 1),
				SubjectDetailItem.GradeBinItem(grade = 5, count = 3)
			),
			career.latestGradeBins
		)
		assertEquals(
			listOf(
				SubjectDetailItem.AttemptBinItem(bucket = "1", count = 8),
				SubjectDetailItem.AttemptBinItem(bucket = "3_plus", count = 2)
			),
			career.attemptsToPassBins
		)
	}

	@Test
	fun when_onlyGlobalSegmentIsPresent_then_selectsGlobalTab() = runTest {
		val ready = readySubjectDetail(subjectCode = "MAT101")
		val onlyGlobal = SubjectDetailResult.Ready(
			detail = ready.detail.copy(
				careerSegment = null,
				globalSegment = ready.detail.careerSegment
			)
		)

		val state = onlyGlobal.toViewState()

		val content = assertIs<SubjectDetail.State.Content>(state)
		assertEquals(SubjectSegmentTab.GLOBAL, content.detail.selectedTab)
		assertFalse(content.detail.hasSegmentTabs)
		assertNull(content.detail.careerSegment)
		assertEquals(content.detail.globalSegment, content.detail.selectedSegment)
	}

	@Test
	fun when_bothSegmentsArePresent_then_enablesSegmentTabsAndPrefersCareer() = runTest {
		val ready = readySubjectDetail(subjectCode = "MAT101")
		val bothSegments = SubjectDetailResult.Ready(
			detail = ready.detail.copy(
				globalSegment = ready.detail.careerSegment?.copy(sampleStudents = 2400)
			)
		)

		val state = bothSegments.toViewState()

		val content = assertIs<SubjectDetail.State.Content>(state)
		assertEquals(SubjectSegmentTab.CAREER, content.detail.selectedTab)
		assertTrue(content.detail.hasSegmentTabs)
		assertEquals("2.4k", assertNotNull(content.detail.globalSegment).studentsText)
		assertEquals(content.detail.careerSegment, content.detail.selectedSegment)
	}

	@Test
	fun when_gradingIsQualitativeAndMetricsAreMissing_then_usesQualitativeAndEmptyTexts() = runTest {
		val ready = readySubjectDetail(subjectCode = "CSA215")
		val qualitative = SubjectDetailResult.Ready(
			detail = ready.detail.copy(
				gradingMode = GradingMode.QUALITATIVE_PASS_FAIL,
				careerSegment = SubjectStatsSegment(
					sampleStudents = 1200,
					closedAttempts = 1500,
					numericLatestStudents = 0,
					latestApprovedCount = 700,
					latestFailedCount = 300,
					latestRetiredCount = 150,
					latestUnreportedCount = 50
				),
				globalSegment = null
			)
		)

		val state = qualitative.toViewState()

		val content = assertIs<SubjectDetail.State.Content>(state)
		assertEquals("Cualitativa", content.detail.gradingModeText)
		assertEquals(SubjectDetailItem.ChartMode.QUALITATIVE_OUTCOMES, content.detail.chartMode)

		val career = assertNotNull(content.detail.careerSegment)
		assertEquals("1.2k", career.studentsText)
		assertEquals("1.5k", career.attemptsText)
		assertEquals("--", career.difficultyScoreText)
		assertEquals("--", career.difficultyBandText)
		assertEquals("--", career.firstAttemptPassRateText)
		assertEquals("--", career.approvalRateText)
		assertEquals("--", career.failureRateText)
		assertEquals("--", career.withdrawalRateText)
		assertEquals(emptyList(), career.latestGradeBins)
		assertEquals(emptyList(), career.attemptsToPassBins)
	}

	@Test
	fun when_resultIsUnavailable_then_mapsToUnavailableState() = runTest {
		val state = SubjectDetailResult.Unavailable(
			subjectCode = "MAT404",
			expiresAt = 123L
		).toViewState()

		assertEquals(SubjectDetail.State.Unavailable(subjectCode = "MAT404"), state)
	}

	@Test
	fun when_selectedTabChanges_then_onlyUpdatesSelectedTab() = runTest {
		val ready = readySubjectDetail(subjectCode = "MAT101")
		val bothSegments = SubjectDetailResult.Ready(
			detail = ready.detail.copy(
				globalSegment = ready.detail.careerSegment
			)
		)
		val content = assertIs<SubjectDetail.State.Content>(bothSegments.toViewState())

		val updated = content.detail.withSelectedTab(SubjectSegmentTab.GLOBAL)

		assertEquals(SubjectSegmentTab.GLOBAL, updated.selectedTab)
		assertEquals(content.detail.copy(selectedTab = SubjectSegmentTab.GLOBAL), updated)
		assertEquals(updated.globalSegment, updated.selectedSegment)
	}
}
