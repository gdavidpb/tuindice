package com.gdavidpb.tuindice.assistant.presentation.mapper

import com.gdavidpb.tuindice.assistant.presentation.model.CardLinkItem
import com.gdavidpb.tuindice.assistant.presentation.model.CitationItem
import com.gdavidpb.tuindice.assistant.presentation.model.CitationSourceItem
import com.gdavidpb.tuindice.assistant.presentation.model.HelpItem
import com.gdavidpb.tuindice.assistant.presentation.model.HelpStepItem
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.chatcontract.component.CitationComponent
import com.gdavidpb.tuindice.chatcontract.component.HelpComponent
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_card_b_citation_document
import tuindice.assistant.generated.resources.assistant_card_b_help_open
import tuindice.assistant.generated.resources.assistant_card_b_help_step

internal fun CitationComponent.toCitationItem(): CitationItem {
	return CitationItem(
		id = id,
		opensSheet = opensSheet(),
		sources = sources.map { source ->
			CitationSourceItem(
				documentText = UiText.Resource(Res.string.assistant_card_b_citation_document, listOf(source.document)),
				article = source.article,
				excerpt = source.excerpt,
				url = source.url
			)
		},
		recordLink = entityRefs.toRecordLinkItem()
	)
}

internal fun HelpComponent.toHelpItem(): HelpItem {
	return HelpItem(
		id = id,
		opensSheet = opensSheet(),
		title = title,
		steps = steps.mapIndexed { index, step ->
			HelpStepItem(numberText = UiText.Resource(Res.string.assistant_card_b_help_step, listOf(index + 1)), text = step)
		},
		openLink = target?.let { target ->
			CardLinkItem(
				target = target,
				text = UiText.Resource(Res.string.assistant_card_b_help_open),
				entityRefs = entityRefs
			)
		}
	)
}
