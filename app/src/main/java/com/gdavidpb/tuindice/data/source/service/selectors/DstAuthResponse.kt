package com.gdavidpb.tuindice.data.source.service.selectors

import pl.droidsonroids.jspoon.annotation.Selector

data class DstAuthResponse(
        @Selector("#status")
        var invalidCredentialsMessage: String = "",
        @Selector("p:contains(Usted no está inscrito para ninguno de los períodos configurados)")
        var notEnrolledMessage: String = "",
        @Selector("p:contains(Ha ocurrido un error o el tiempo de la sesión se ha terminado)")
        var expiredSessionMessage: String = ""
)