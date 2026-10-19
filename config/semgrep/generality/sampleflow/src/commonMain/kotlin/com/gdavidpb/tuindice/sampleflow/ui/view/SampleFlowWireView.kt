package com.gdavidpb.tuindice.sampleflow.ui.view

import androidx.compose.runtime.Composable
import com.gdavidpb.tuindice.chatcontract.turn.TurnRequest

// Positivo de ui-no-wire-contract-import: la vista importa el contrato de cable.
@Composable
fun SampleFlowWireView(request: TurnRequest) {
	request.toString()
}
