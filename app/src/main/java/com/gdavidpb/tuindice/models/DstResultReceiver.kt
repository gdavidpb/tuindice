package com.gdavidpb.tuindice.models

import android.os.Bundle
import android.os.Handler
import android.os.ResultReceiver

class DstResultReceiver(handler: Handler, private val receiver: Receiver) : ResultReceiver(handler) {
    interface Receiver {
        fun onReceiveResult(resultCode: Int, resultData: Bundle)
    }

    override fun onReceiveResult(resultCode: Int, resultData: Bundle) {
        receiver.onReceiveResult(resultCode, resultData)
    }
}