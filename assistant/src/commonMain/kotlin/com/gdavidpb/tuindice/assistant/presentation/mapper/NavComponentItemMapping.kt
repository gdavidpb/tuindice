package com.gdavidpb.tuindice.assistant.presentation.mapper

import com.gdavidpb.tuindice.assistant.presentation.model.OpenScreenItem
import com.gdavidpb.tuindice.chatcontract.component.OpenScreenComponent

internal fun OpenScreenComponent.toOpenScreenItem(): OpenScreenItem {
	return OpenScreenItem(
		id = id,
		opensSheet = opensSheet(),
		target = target,
		entityRefs = entityRefs,
		label = label
	)
}
