package com.gdavidpb.tuindice.pensum.presentation.mapper

import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.pensum.domain.usecase.error.UpdatePensumUseCaseError
import com.gdavidpb.tuindice.pensum.presentation.contract.Pensum
import com.gdavidpb.tuindice.pensum.presentation.model.PensumCanvasItem
import com.gdavidpb.tuindice.pensum.presentation.model.PensumScreenModel
import com.gdavidpb.tuindice.pensum.presentation.model.PensumScreenSelection
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import tuindice.pensum.generated.resources.Res
import tuindice.pensum.generated.resources.snack_service_unavailable

class PensumStateMutationTest {
	@Test
	fun when_visibleContentRefreshFailsWithoutNetwork_then_snackbarIsSuppressed() {
		val effect = sampleContentState().snackBarEffectOrNull(
			error = UpdatePensumUseCaseError.NoConnection(isNetworkAvailable = false)
		)

		assertNull(effect)
	}

	@Test
	fun when_visibleContentRefreshFailsWithNetworkAvailable_then_snackbarIsShown() {
		val effect = sampleContentState().snackBarEffectOrNull(
			error = UpdatePensumUseCaseError.NoConnection(isNetworkAvailable = true)
		)

		assertEquals(
			UiText.Resource(Res.string.snack_service_unavailable),
			effect?.message
		)
	}
}

private fun sampleContentState(): Pensum.State.Content {
	return Pensum.State.Content(
		model = PensumScreenModel(
			careerName = "Ingenieria de Computacion",
			selection = PensumScreenSelection(
				year = 2019,
				modalityId = "degree_project"
			),
			pensumOptions = emptyList(),
			modalityOptions = emptyList(),
			progressPercent = 0,
			approvedCredits = 0,
			totalCredits = 0,
			isCurrentFocusVisible = false,
			canvas = PensumCanvasItem(width = 0.0, height = 0.0),
			terms = emptyList(),
			nodes = emptyList(),
			edges = emptyList()
		)
	)
}
