package com.gdavidpb.tuindice.presentation.mapper

import com.gdavidpb.tuindice.otherflow.presentation.contract.OtherFlow
import com.gdavidpb.tuindice.sampleflow.presentation.contract.SampleFlow

// The composition root maps the states of every feature: exempt by path, the rule stays quiet.
fun isEligible(sample: SampleFlow.State, other: OtherFlow.State): Boolean = true
