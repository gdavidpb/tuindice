package com.gdavidpb.tuindice.enrollmentproof.ui.view

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.ui.view.LottieResourceAnimationView
import com.gdavidpb.tuindice.enrollmentproof.ui.EnrollmentProofUiTags
import org.jetbrains.compose.resources.ExperimentalResourceApi
import tuindice.enrollmentproof.generated.resources.Res

@OptIn(ExperimentalResourceApi::class)
@Composable
fun EnrollmentProofLoadingView(
	modifier: Modifier = Modifier
) {
	LottieResourceAnimationView(
		readBytes = { Res.readBytes(ENROLLMENT_ANIMATION_PATH) },
		modifier = modifier
			.width(256.dp)
			.height(128.dp),
		testTag = EnrollmentProofUiTags.FetchingLottie
	)
}

private const val ENROLLMENT_ANIMATION_PATH = "files/an_enrollment.json"
