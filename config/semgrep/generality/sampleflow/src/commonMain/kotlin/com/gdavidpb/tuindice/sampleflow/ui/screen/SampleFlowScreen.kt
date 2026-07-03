package com.gdavidpb.tuindice.sampleflow.ui.screen

import org.koin.compose.viewmodel.koinViewModel
import com.gdavidpb.tuindice.sampleflow.domain.repository.SampleFlowRepository

fun sampleFlowScreen(repository: SampleFlowRepository): String = repository.toString()
