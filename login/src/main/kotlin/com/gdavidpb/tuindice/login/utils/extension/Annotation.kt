package com.gdavidpb.tuindice.login.utils.extension

import com.gdavidpb.tuindice.base.utils.extension.hasAnnotation
import com.gdavidpb.tuindice.login.domain.annotation.PublicApi
import okhttp3.Request

fun Request.isPublicApi() = hasAnnotation<PublicApi>()