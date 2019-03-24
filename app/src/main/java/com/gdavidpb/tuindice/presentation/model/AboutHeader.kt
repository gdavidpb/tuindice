package com.gdavidpb.tuindice.presentation.model

data class AboutHeader(
        val title: String,
        val onClick: () -> Unit = { }
) : AboutBase {
    override fun onClick() = onClick.invoke()
}