package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.runtime.Composable
import com.gdavidpb.tuindice.base.ui.view.EmptyStateAnimationView
import com.gdavidpb.tuindice.base.ui.view.ErrorStateAnimationView
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationsIllustration

// Paints the art an explanation already chose; which one is decided in the mapper.
@Composable
fun EvaluationsIllustrationView(
	illustration: EvaluationsIllustration
) {
	when (illustration) {
		EvaluationsIllustration.Empty -> EmptyStateAnimationView()
		EvaluationsIllustration.Error -> ErrorStateAnimationView()
	}
}
