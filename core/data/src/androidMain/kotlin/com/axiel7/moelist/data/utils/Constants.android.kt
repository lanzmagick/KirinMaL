package com.axiel7.moelist.data.utils

import MoeList.core.data.BuildConfig

// KirinMaL's own Client ID and OAuth redirect. KirinNime has a different scheme.
actual val CLIENT_ID: String = BuildConfig.MAL_CLIENT_ID
actual val AUTH_REDIRECT_URI: String = "kirinmal://oauth/mal"
actual val DEFAULT_DATA_STORE_FILE: String = "default"
actual val NOTIFICATIONS_DATA_STORE_FILE: String = "notifications"