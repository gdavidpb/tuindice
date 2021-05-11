package com.gdavidpb.tuindice.data.source.service.responses

import com.google.gson.annotations.SerializedName

data class DstUsbIdCheckResponse(
        @SerializedName("sid") val usbId: String,
        @SerializedName("pid") val id: String,
        @SerializedName("gname") val firstNames: String,
        @SerializedName("sname") val lastNames: String,
        @SerializedName("userType") val userType: String,
        @SerializedName("career") val careerName: String,
        @SerializedName("userClass") val userClass: String
)