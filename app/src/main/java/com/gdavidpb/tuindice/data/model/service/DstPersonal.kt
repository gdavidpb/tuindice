package com.gdavidpb.tuindice.data.model.service

data class DstPersonal(
        val usbId: String,
        val id: String,
        val firstNames: String,
        val lastNames: String,
        val career: DstCareer,
        val scholarship: Boolean
)