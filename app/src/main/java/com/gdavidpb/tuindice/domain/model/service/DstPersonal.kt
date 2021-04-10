package com.gdavidpb.tuindice.domain.model.service

data class DstPersonal(
        val usbId: String,
        val id: String,
        val firstNames: String,
        val lastNames: String,
        val careerCode: Int,
        val careerName: String,
        val scholarship: Boolean
)