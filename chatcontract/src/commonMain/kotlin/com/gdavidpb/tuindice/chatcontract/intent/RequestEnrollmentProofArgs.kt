package com.gdavidpb.tuindice.chatcontract.intent

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Asks for an enrollment proof. It changes no data in the app. */
@Serializable
@SerialName("comprobante.pedir/1")
public data object RequestEnrollmentProofArgs : IntentArgs
