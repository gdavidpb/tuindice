package com.gdavidpb.tuindice.data.source.secure

// Control negativo: el seam de prueba del proveedor AEAD vive junto a su implementación privada, y la exención por
// archivo de data-source-no-interface lo deja fuera.
internal interface AndroidSecureAeadProvider {
	fun aead(): Any
	fun clear()
}

private class TinkAndroidSecureAeadProvider : AndroidSecureAeadProvider {
	override fun aead(): Any = Any()

	override fun clear() = Unit
}
