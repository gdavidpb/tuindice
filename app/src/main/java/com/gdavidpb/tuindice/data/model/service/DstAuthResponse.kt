package com.gdavidpb.tuindice.data.model.service

import pl.droidsonroids.jspoon.annotation.Selector

open class DstAuthResponse {
    @Selector(value = "#status")
    var message: String? = null
}