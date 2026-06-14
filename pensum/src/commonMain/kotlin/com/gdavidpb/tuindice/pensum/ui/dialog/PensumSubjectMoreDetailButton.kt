package com.gdavidpb.tuindice.pensum.ui.dialog

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.pensum.ui.PensumUiTags
import com.gdavidpb.tuindice.pensum.ui.view.pensumGraphColors
import org.jetbrains.compose.resources.stringResource
import tuindice.pensum.generated.resources.Res
import tuindice.pensum.generated.resources.pensum_subject_detail_more

@Composable
fun PensumSubjectMoreDetailButton(
	onClick: () -> Unit
) {
	val graphColors = pensumGraphColors()

	OutlinedButton(
		modifier = Modifier
			.fillMaxWidth()
			.testTag(PensumUiTags.SubjectDetailMoreButton),
		colors = ButtonDefaults.outlinedButtonColors(
			containerColor = graphColors.panelBackground
		),
		onClick = onClick
	) {
		Text(text = stringResource(Res.string.pensum_subject_detail_more))
		Icon(
			imageVector = Icons.Filled.KeyboardArrowDown,
			contentDescription = null,
			modifier = Modifier.size(18.dp)
		)
	}
}
