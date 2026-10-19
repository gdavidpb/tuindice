package com.gdavidpb.tuindice.assistant.presentation.mapper

import com.gdavidpb.tuindice.assistant.presentation.model.CardFigureItem
import com.gdavidpb.tuindice.assistant.presentation.model.CardLinkItem
import com.gdavidpb.tuindice.assistant.presentation.model.CountRowItem
import com.gdavidpb.tuindice.assistant.presentation.model.PensumComparisonItem
import com.gdavidpb.tuindice.assistant.presentation.model.PensumProgressItem
import com.gdavidpb.tuindice.assistant.presentation.model.RequirementSectionItem
import com.gdavidpb.tuindice.assistant.presentation.model.RequirementsItem
import com.gdavidpb.tuindice.assistant.presentation.model.SubjectImpactItem
import com.gdavidpb.tuindice.assistant.presentation.model.SubjectRowItem
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.chatcontract.component.NavTarget
import com.gdavidpb.tuindice.chatcontract.component.PensumComparisonComponent
import com.gdavidpb.tuindice.chatcontract.component.PensumProgressComponent
import com.gdavidpb.tuindice.chatcontract.component.RequirementsComponent
import com.gdavidpb.tuindice.chatcontract.component.SubjectImpactComponent
import com.gdavidpb.tuindice.chatcontract.component.SubjectRef
import com.gdavidpb.tuindice.chatcontract.context.EntityKind
import com.gdavidpb.tuindice.chatcontract.context.EntityRef
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_card_a_outcome_pending
import tuindice.assistant.generated.resources.assistant_card_b_comparison_career
import tuindice.assistant.generated.resources.assistant_card_b_comparison_missing
import tuindice.assistant.generated.resources.assistant_card_b_comparison_pensum
import tuindice.assistant.generated.resources.assistant_card_b_comparison_shared_caption
import tuindice.assistant.generated.resources.assistant_card_b_comparison_shared_description
import tuindice.assistant.generated.resources.assistant_card_b_fraction
import tuindice.assistant.generated.resources.assistant_card_b_impact_caption
import tuindice.assistant.generated.resources.assistant_card_b_impact_description
import tuindice.assistant.generated.resources.assistant_card_b_link_subject
import tuindice.assistant.generated.resources.assistant_card_b_progress_approved
import tuindice.assistant.generated.resources.assistant_card_b_progress_available
import tuindice.assistant.generated.resources.assistant_card_b_progress_blocked
import tuindice.assistant.generated.resources.assistant_card_b_progress_caption
import tuindice.assistant.generated.resources.assistant_card_b_progress_credits_caption
import tuindice.assistant.generated.resources.assistant_card_b_progress_credits_description
import tuindice.assistant.generated.resources.assistant_card_b_progress_description
import tuindice.assistant.generated.resources.assistant_card_b_requirements_corequisites
import tuindice.assistant.generated.resources.assistant_card_b_requirements_prerequisites
import tuindice.assistant.generated.resources.assistant_card_b_requirements_unlocks

internal fun PensumProgressComponent.toPensumProgressItem(): PensumProgressItem {
	val percent = chatCardPercentText(chatCardPercentOf(approvedCredits, totalCredits))
	val approved = approvedCredits.toString()
	val total = totalCredits.toString()

	return PensumProgressItem(
		id = id,
		opensSheet = opensSheet(),
		primaryFigure = CardFigureItem(
			value = percent,
			caption = UiText.Resource(Res.string.assistant_card_b_progress_caption),
			contentDescription = UiText.Resource(Res.string.assistant_card_b_progress_description, listOf(percent))
		),
		secondaryFigure = CardFigureItem(
			value = UiText.Resource(Res.string.assistant_card_b_fraction, listOf(approved, total)),
			caption = UiText.Resource(Res.string.assistant_card_b_progress_credits_caption),
			contentDescription = UiText.Resource(
				Res.string.assistant_card_b_progress_credits_description,
				listOf(approved, total)
			)
		),
		progressFraction = chatCardFraction(approvedCredits.toDouble(), totalCredits.toDouble()),
		statusRows = listOf(
			Res.string.assistant_card_b_progress_approved to this.approved,
			Res.string.assistant_card_a_outcome_pending to inProgress,
			Res.string.assistant_card_b_progress_available to available,
			Res.string.assistant_card_b_progress_blocked to blocked
		).map { (label, count) -> CountRowItem(label = UiText.Resource(label), value = count.toString()) },
		recordLink = entityRefs.toRecordLinkItem()
	)
}

internal fun RequirementsComponent.toRequirementsItem(): RequirementsItem {
	val sections = listOf(
		RequirementSectionItem(
			label = UiText.Resource(Res.string.assistant_card_b_requirements_prerequisites),
			subjects = prerequisites.map { subject -> subject.toSubjectRowItem(withLink = true) }
		),
		RequirementSectionItem(
			label = UiText.Resource(Res.string.assistant_card_b_requirements_corequisites),
			subjects = corequisites.map { subject -> subject.toSubjectRowItem(withLink = true) }
		),
		RequirementSectionItem(
			label = UiText.Resource(Res.string.assistant_card_b_requirements_unlocks),
			subjects = unlocks.map { subject -> subject.toSubjectRowItem(withLink = true) }
		)
	)

	return RequirementsItem(
		id = id,
		opensSheet = opensSheet(),
		subjectName = subjectName,
		sections = sections.filter { section -> section.subjects.isNotEmpty() },
		recordLink = entityRefs.toRecordLinkItem()
	)
}

internal fun SubjectImpactComponent.toSubjectImpactItem(): SubjectImpactItem {
	val terms = lengthInTerms.toString()

	return SubjectImpactItem(
		id = id,
		opensSheet = opensSheet(),
		subjectName = subjectName,
		termsFigure = CardFigureItem(
			value = UiText.Raw(terms),
			caption = UiText.Resource(Res.string.assistant_card_b_impact_caption),
			contentDescription = UiText.Resource(Res.string.assistant_card_b_impact_description, listOf(terms))
		),
		chain = chain.map { subject -> subject.toSubjectRowItem(withLink = false) },
		recordLink = entityRefs.toRecordLinkItem()
	)
}

internal fun PensumComparisonComponent.toPensumComparisonItem(): PensumComparisonItem {
	val shared = sharedCredits.toString()

	return PensumComparisonItem(
		id = id,
		opensSheet = opensSheet(),
		sharedFigure = CardFigureItem(
			value = UiText.Raw(shared),
			caption = UiText.Resource(Res.string.assistant_card_b_comparison_shared_caption),
			contentDescription = UiText.Resource(Res.string.assistant_card_b_comparison_shared_description, listOf(shared))
		),
		otherCareerName = otherCareerName,
		otherPensumLabel = otherPensumLabel,
		missingSubjects = missingSubjects.map { subject ->
			SubjectRowItem(code = subject.code, nameText = UiText.Raw(subject.name), statusText = null, link = null)
		},
		notes = notes,
		recordLink = entityRefs.toRecordLinkItem()
	)
}

// A subject as a row of a card: its name, its status, and, when the row links, the screen of the subject.
private fun SubjectRef.toSubjectRowItem(withLink: Boolean): SubjectRowItem {
	return SubjectRowItem(
		code = code,
		nameText = UiText.Raw(name),
		statusText = subjectStatusText(status),
		link = if (withLink) {
			CardLinkItem(
				target = NavTarget.SUBJECTS_DETAIL,
				text = UiText.Resource(Res.string.assistant_card_b_link_subject),
				entityRefs = listOf(EntityRef(EntityKind.SUBJECT, code))
			)
		} else {
			null
		}
	)
}
