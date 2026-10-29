package com.gdavidpb.tuindice.assistant.presentation.mapper

import com.gdavidpb.tuindice.assistant.presentation.model.CardLinkItem
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.chatcontract.component.Citation
import com.gdavidpb.tuindice.chatcontract.component.CitationComponent
import com.gdavidpb.tuindice.chatcontract.component.HelpComponent
import com.gdavidpb.tuindice.chatcontract.component.NavTarget
import com.gdavidpb.tuindice.chatcontract.context.EntityKind
import com.gdavidpb.tuindice.chatcontract.context.EntityRef
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_card_b_citation_document
import tuindice.assistant.generated.resources.assistant_card_b_help_open
import tuindice.assistant.generated.resources.assistant_card_b_help_step
import tuindice.assistant.generated.resources.assistant_card_b_link_record
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ConocimientoComponentItemMappingTest {
	@Test
	fun citation_namesTheDocument_andLinksTheRecordOnlyWhenTheCardNamesOne() {
		val withRecord = CitationComponent(
			id = "k1",
			fallbackText = "Cita",
			entityRefs = listOf(EntityRef(EntityKind.TERM, "term-1")),
			sources = listOf(Citation("Reglamento", "Art. 12", "Texto", url = null))
		).toCitationItem()
		val withoutRecord = CitationComponent(
			id = "k2",
			fallbackText = "Cita",
			entityRefs = emptyList(),
			sources = listOf(Citation("Reglamento", "Art. 12", "Texto"))
		).toCitationItem()

		assertEquals(
            UiText.Resource(Res.string.assistant_card_b_citation_document, listOf("Reglamento")),
            withRecord.sources.single().documentText
        )
		assertEquals(
			CardLinkItem(
				target = NavTarget.RECORD,
				text = UiText.Resource(Res.string.assistant_card_b_link_record),
				entityRefs = listOf(EntityRef(EntityKind.TERM, "term-1"))
			),
			withRecord.recordLink
		)
		assertNull(withoutRecord.recordLink)
	}

	@Test
	fun help_numbersItsStepsInOrder_andOpensItsScreenOnlyWhenItNamesOne() {
		val help = HelpComponent(
			id = "k3",
			fallbackText = "Ayuda",
			title = "Cómo agregar una evaluación",
			steps = listOf("Abre Evaluaciones", "Toca Agregar"),
			target = NavTarget.EVALUATIONS_ADD
		).toHelpItem()
		val withoutScreen = HelpComponent(
            id = "k4",
            fallbackText = "Ayuda",
            title = "Título",
            steps = emptyList()
        ).toHelpItem()

		assertEquals(UiText.Resource(Res.string.assistant_card_b_help_step, listOf(2)), help.steps[1].numberText)
		assertEquals(UiText.Resource(Res.string.assistant_card_b_help_open), help.openLink?.text)
		assertEquals(NavTarget.EVALUATIONS_ADD, help.openLink?.target)
		assertNull(withoutScreen.openLink)
	}
}
