package com.gdavidpb.tuindice.record.presentation.contract

import com.gdavidpb.tuindice.base.presentation.model.UiText
import kotlin.test.Test
import kotlin.test.assertEquals
import tuindice.record.generated.resources.Res
import tuindice.record.generated.resources.top_bar_create_synthetic_term
import tuindice.record.generated.resources.top_bar_edit_synthetic_term

class CreateSyntheticTermStateTest {
	@Test
	fun topBarTitle_tracksEditingMode() {
		val createState = CreateSyntheticTerm.State()
		val editState = createState.copy(editingTermId = "2026-SEP_DEC")

		assertEquals(
			UiText.Resource(Res.string.top_bar_create_synthetic_term),
			createState.topBarTitle
		)
		assertEquals(
			UiText.Resource(Res.string.top_bar_edit_synthetic_term),
			editState.topBarTitle
		)
	}
}
