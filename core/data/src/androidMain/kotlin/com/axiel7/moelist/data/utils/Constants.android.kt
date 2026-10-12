package com.axiel7.moelist.data.utils

import MoeList.core.data.BuildConfig

// KirinMaL only: injected from the owner's MAL_CLIENT_ID; no inherited client ID.
actual val CLIENT_ID: String = BuildConfig.MAL_CLIENT_ID
actual val AUTH_REDIRECT_URI: String = "kirinmal://auth/"
actual val DEFAULT_DATA_STORE_FILE: String = "default"
actual val NOTIFICATIONS_DATA_STORE_FILE: String = "notifications"