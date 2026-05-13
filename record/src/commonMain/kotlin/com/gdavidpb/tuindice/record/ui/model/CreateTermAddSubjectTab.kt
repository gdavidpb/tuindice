package com.gdavidpb.tuindice.record.ui.model

import com.gdavidpb.tuindice.record.ui.RecordUiTags
import org.jetbrains.compose.resources.StringResource
import tuindice.record.generated.resources.Res
import tuindice.record.generated.resources.create_term_search_tab
import tuindice.record.generated.resources.create_term_suggested_tab

internal enum class CreateTermAddSubjectTab(
	val labelResource: StringResource,
	val testTag: String
) {
	Suggested(
		labelResource = Res.string.create_term_suggested_tab,
		testTag = RecordUiTags.CreateSyntheticTermSuggestedTab
	),
	Search(
		labelResource = Res.string.create_term_search_tab,
		testTag = RecordUiTags.CreateSyntheticTermSearchTab
	)
}
