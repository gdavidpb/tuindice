package com.gdavidpb.tuindice.base.ui.dialog

import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable

// Módulo sintético: una hoja que usa el ModalBottomSheet de Material fuera de la hoja compartida. La regla debe
// dispararse aquí, en un paquete que el repo no tiene.
@Composable
fun SampleMaterialSheet(onDismissRequest: () -> Unit) {
	val sheetState = rememberModalBottomSheetState()

	ModalBottomSheet(
		onDismissRequest = onDismissRequest,
		sheetState = sheetState
	) {
	}
}
