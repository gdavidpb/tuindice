package com.gdavidpb.tuindice.record.presentation.model

import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantScreenContext

// What the record declared for the assistant. Holding one is the declaration, which the shell reads on each state
// without projecting anything. The context itself is projected on its first read, which is the bar's tap.
class RecordAssistantDeclaration(project: () -> AssistantScreenContext) {
	val context: AssistantScreenContext by lazy(project)
}
