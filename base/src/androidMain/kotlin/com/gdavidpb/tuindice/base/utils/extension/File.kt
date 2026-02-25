package com.gdavidpb.tuindice.base.utils.extension

import java.io.File

fun File(parent: File, vararg path: String) = path.fold(parent) { acc, child -> File(acc, child) }