package com.gdavidpb.tuindice.assistant.presentation.mapper

import com.gdavidpb.tuindice.assistant.presentation.model.CardChipItem
import com.gdavidpb.tuindice.assistant.presentation.model.CardChipTone
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.chatcontract.component.PensumComparisonComponent
import com.gdavidpb.tuindice.chatcontract.component.PensumProgressComponent
import com.gdavidpb.tuindice.chatcontract.component.RequirementsComponent
import com.gdavidpb.tuindice.chatcontract.component.SubjectImpactComponent
import com.gdavidpb.tuindice.chatcontract.component.SubjectRef
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_card_a_outcome_approved
import tuindice.assistant.generated.resources.assistant_card_b_link_subject_named
import tuindice.assistant.generated.resources.assistant_card_b_progress_approved
import tuindice.assistant.generated.resources.assistant_card_b_requirements_prerequisites
import tuindice.assistant.generated.resources.assistant_card_b_requirements_unlocks
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PensumComponentItemMappingTest {
	@Test
	fun progress_countsEachStatusAndBarsTheShareOfTheCredits() {
		val item = PensumProgressComponent(
			id = "p1",
			fallbackText = "Avance",
			approvedCredits = 80,
			totalCredits = 180,
			approved = 20,
			inProgress = 3,
			available = 5,
			blocked = 7
		).toPensumProgressItem()

		assertEquals(UiText.Resource(Res.string.assistant_card_b_progress_approved), item.statusRows[0].label)
		assertEquals(listOf("20", "3", "5", "7"), item.statusRows.map { row -> row.value })
		assertEquals(80f / 180f, item.progressFraction)
	}

	@Test
	fun requirements_leavesOutTheSectionsWithoutSubjects_andLinksEachSubject() {
		val item = RequirementsComponent(
			id = "q1",
			fallbackText = "Requisitos",
			subjectCode = "IS-201",
			subjectName = "Estructuras",
			prerequisites = listOf(SubjectRef("IS-101", "Programación I", status = "approved")),
			corequisites = emptyList(),
			unlocks = listOf(SubjectRef("IS-301", "Bases de datos"))
		).toRequirementsItem()

		assertEquals(
			listOf(
				UiText.Resource(Res.string.assistant_card_b_requirements_prerequisites),
				UiText.Resource(Res.string.assistant_card_b_requirements_unlocks)
			),
			item.sections.map { section -> section.label }
		)
		val subject = item.sections[0].subjects.single()

		assertEquals(
			CardChipItem(UiText.Resource(Res.string.assistant_card_a_outcome_approved), CardChipTone.Approved),
			subject.statusChip
		)
		assertEquals("IS-101", subject.link?.entityRefs?.single()?.id)
		assertEquals(
			UiText.Resource(Res.string.assistant_card_b_link_subject_named, listOf("IS-101")),
			subject.link?.accessibilityText
		)
	}

	@Test
	fun impact_hasNoLinkOnItsSubjects_andCountsTheTermsItDelays() {
		val item = SubjectImpactComponent(
			id = "i1",
			fallbackText = "Impacto",
			subjectCode = "IS-101",
			subjectName = "Programación I",
			chain = listOf(SubjectRef("IS-201", "Estructuras")),
			lengthInTerms = 2
		).toSubjectImpactItem()

		assertEquals(UiText.Raw("2"), item.termsFigure.value)
		assertNull(item.chain.single().link)
	}

	@Test
	fun comparison_missingSubjectsCarryNoStatusNorLink() {
		val item = PensumComparisonComponent(
			id = "c1",
			fallbackText = "Comparación",
			otherCareerName = "Matemática",
			otherPensumLabel = "2014",
			sharedCredits = 40,
			missingSubjects = listOf(SubjectRef("MAT-101", "Cálculo I"))
		).toPensumComparisonItem()

		assertEquals(UiText.Raw("40"), item.sharedFigure.value)
		assertEquals(UiText.Raw("Cálculo I"), item.missingSubjects.single().nameText)
		assertNull(item.missingSubjects.single().statusChip)
		assertNull(item.missingSubjects.single().link)
	}
}
