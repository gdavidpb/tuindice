package com.gdavidpb.tuindice.sampleflow.presentation.mapper

import com.gdavidpb.tuindice.otherflow.presentation.contract.OtherFlow

// A feature that reads another feature's contract: the rule fires.
fun OtherFlow.State.isSampleEligible(): Boolean = true
