package com.gdavidpb.tuindice.data.source.secure

import com.google.crypto.tink.Aead

internal interface AndroidSecureAeadProvider {
	fun aead(): Aead
	fun clear()
}
