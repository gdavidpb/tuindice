package com.gdavidpb.tuindice.enrollmentproof.ui.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.enrollmentproof.ui.EnrollmentProofUiTags
import io.github.alexzhirkevich.compottie.Compottie
import io.github.alexzhirkevich.compottie.LottieCompositionSpec
import io.github.alexzhirkevich.compottie.rememberLottieComposition
import io.github.alexzhirkevich.compottie.rememberLottiePainter
import org.jetbrains.compose.resources.ExperimentalResourceApi
import tuindice.enrollmentproof.generated.resources.Res

@OptIn(ExperimentalResourceApi::class)
@Composable
fun EnrollmentProofLottieLoadingContent(
	modifier: Modifier = Modifier
) {
	val composition by rememberLottieComposition {
		LottieCompositionSpec.JsonString(
			Res.readBytes(ENROLLMENT_ANIMATION_PATH).decodeToString()
		)
	}

	Image(
		modifier = modifier
			.testTag(EnrollmentProofUiTags.FetchingLottie)
			.width(256.dp)
			.height(128.dp),
		painter = rememberLottiePainter(
			composition = composition,
			iterations = Compottie.IterateForever
		),
		contentDescription = null
	)
}

private const val ENROLLMENT_ANIMATION_PATH = "files/an_enrollment.json"
