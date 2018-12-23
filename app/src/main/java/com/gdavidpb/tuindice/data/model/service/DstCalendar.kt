package com.gdavidpb.tuindice.data.model.service

import java.util.*

data class DstCalendar(
        val startDate: Date?,
        val endDate: Date?,
        val correctionDate: Date?,
        val giveUpDeadline: Date?,
        val degreeRequestDeadline: Date?,
        val graduationStartDate: Date?,
        val graduationEndDate: Date?,
        val documentsRequestDeadline: Date?,
        val nextEnrollmentDate: Date?,
        val minutesDeliveryDeadline: Date?
)