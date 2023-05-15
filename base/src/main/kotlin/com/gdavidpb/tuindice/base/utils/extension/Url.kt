package com.gdavidpb.tuindice.base.utils.extension

import java.net.URLEncoder

fun String.encodeUrl(): String = URLEncoder.encode(this, "UTF-8")