package com.gdavidpb.tuindice.sampleflow.presentation.mapper

import com.gdavidpb.tuindice.academiccore.domain.engine.SharedEngine
import com.gdavidpb.tuindice.base.presentation.ViewState
import com.gdavidpb.tuindice.sampleflow.presentation.model.SampleFlowItem

// A feature that reads only transversal modules and itself: the rule stays quiet.
fun ViewState.toSampleFlowItem(): SampleFlowItem? = null
