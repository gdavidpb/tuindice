package com.gdavidpb.tuindice.base.utils.extension

import com.gdavidpb.tuindice.base.domain.annotation.PublicApi
import okhttp3.Request

fun Request.isPublicApi() = hasAnnotation<PublicApi>()