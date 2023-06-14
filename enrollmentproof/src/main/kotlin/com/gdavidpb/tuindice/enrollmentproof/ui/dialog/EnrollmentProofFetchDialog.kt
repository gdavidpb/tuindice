package com.gdavidpb.tuindice.enrollmentproof.ui.dialog

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.gdavidpb.tuindice.enrollmentproof.R
import com.gdavidpb.tuindice.enrollmentproof.presentation.contract.Enrollment

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnrollmentProofFetchDialog(
	state: Enrollment.State,
	onDismissRequest: () -> Unit
) {
	if (state != Enrollment.State.Fetching) return

	val lottieComposition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.an_enrollment))

	val nonDismissSheetState = rememberModalBottomSheetState(
		confirmValueChange = { false }
	)

	ModalBottomSheet(
		sheetState = nonDismissSheetState,
		onDismissRequest = onDismissRequest
	) {
		LottieAnimation(
			modifier = Modifier
				.align(Alignment.CenterHorizontally)
				.width(256.dp)
				.height(128.dp),
			iterations = LottieConstants.IterateForever,
			composition = lottieComposition
		)

		Text(
			modifier = Modifier
				.align(Alignment.CenterHorizontally)
				.padding(bottom = dimensionResource(id = R.dimen.dp_24)),
			text = stringResource(id = R.string.dialog_message_enrollment_downloading),
			style = MaterialTheme.typography.titleLarge
		)
	}
}