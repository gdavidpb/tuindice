package com.gdavidpb.tuindice;

/* Java static code for constants is more clearly than Kotlin */
public final class Constants {
    /* Default */
    public final static int DEFAULT_CONNECTION_RETRY = 3;

    /* Result codes */
    public final static int RES_UPDATE = 0;
    public final static int RES_RESPONSE = 1;

    /* Notification channels */
    public final static String CHANNEL_SERVICE = "CHANNEL_SERVICE";

    /* Preferences keys */
    public final static String KEY_FIRST_RUN = "KEY_FIRST_RUN";
    public final static String KEY_CONNECTION_RETRY = "KEY_CONNECTION_RETRY";

    /* Extras */
    public final static String EXTRA_UPDATE = "EXTRA_UPDATE";
    public final static String EXTRA_ACCOUNT = "EXTRA_ACCOUNT";
    public final static String EXTRA_RESPONSE = "EXTRA_RESPONSE";
    public final static String EXTRA_RECEIVER = "EXTRA_RECEIVER";

    /* Google Play */
    public static final String GOOGLE_PLAY_INTENT = "market://details?id=%s";
    public static final String GOOGLE_PLAY_URL = "https://play.google.com/store/apps/details?id=%s";

    /* Creative Commons */
    public static final String CREATIVE_COMMONS_LICENSE = "https://creativecommons.org/licenses/by-nc/4.0/";

    /* Contact information */
    public static final String CONTACT_EMAIL = "gerardoprado93@gmail.com";
    public static final String CONTACT_SUBJECT = "TuIndice - Contacto";
}
