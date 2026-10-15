package com.gdavidpb.tuindice.sampleflow.presentation.contract

import androidx.compose.ui.graphics.ImageBitmap

// Disparo de presentation-no-compose-render-import: un bitmap no entra en el contrato de presentación.
fun sampleFlowBitmapState(bitmap: ImageBitmap): Any = bitmap

// Disparo de presentation-no-toplevel-data-class: el modelo debería estar en presentation/model.
data class SampleFlowNotice(val text: String)
