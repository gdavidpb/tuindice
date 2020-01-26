package com.gdavidpb.tuindice.presentation.model

data class AboutHeaderItem(
        val title: String,
        val onClick: () -> Unit = { }
) : AboutItemBase {
    override fun onClick() = onClick.invoke()
}