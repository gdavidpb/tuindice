package com.gdavidpb.tuindice.sampleflow.ui.view

import androidx.compose.runtime.Composable

// Positivo de compositionlocal-actions-only-in-route: una vista lee el CompositionLocal de acciones.
@Composable
fun SampleFlowLauncherView() {
	val launcher = LocalSampleFlowLauncher.current
	launcher?.open()
}
