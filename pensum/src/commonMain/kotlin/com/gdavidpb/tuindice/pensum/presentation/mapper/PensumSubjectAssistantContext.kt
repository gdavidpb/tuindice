package com.gdavidpb.tuindice.pensum.presentation.mapper

import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantEntityKind
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantEntityRef
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantScreen
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantScreenContext
import com.gdavidpb.tuindice.pensum.domain.model.PensumGraph
import com.gdavidpb.tuindice.pensum.presentation.model.PensumNodeStatusType

/**
 * The context the assistant receives from a subject detail. A node with a subject code is the subject itself; a
 * wildcard node (a slot without subject code) has no subject entity, so its display code is the only identity it keeps
 * in the visible fields.
 */
internal fun subjectAssistantContext(
	node: PensumGraph.Node,
	status: PensumNodeStatusType,
	suggestedTerm: String?
): AssistantScreenContext {
	val entities = listOfNotNull(
		node.subjectCode?.let { code -> AssistantEntityRef(kind = AssistantEntityKind.SUBJECT, id = code) }
	)
	val visibleFields = buildMap<String, String> {
		put("displayCode", node.displayCode)
		put("name", node.name)
		put("credits", node.credits.toString())
		put("status", status.name)
		suggestedTerm?.let { term -> put("suggestedTerm", term) }
	}

	return AssistantScreenContext(
		screen = AssistantScreen.SUBJECT_DETAIL,
		entities = entities,
		visibleFields = visibleFields
	)
}
