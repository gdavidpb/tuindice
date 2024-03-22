package com.gdavidpb.tuindice.base.utils.extension

import java.util.UUID

@Deprecated("This will be replaced by a repository",
	ReplaceWith("\"\${UUID.randomUUID()}\"", "java.util.UUID")
)
fun generateReference() = "${UUID.randomUUID()}"