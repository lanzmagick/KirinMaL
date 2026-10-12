package com.axiel7.moelist.data.utils

import MoeList.core.data.BuildConfig

// Injected from the owner's MAL_CLIENT_ID, never hard-code upstream credentials.
actual val CLIENT_ID: String = BuildConfig.MAL_CLIENT_ID
actual val AUTH_REDIRECT_URI: String = "moelist://moelist.page.link/"
actual val DEFAULT_DATA_STORE_FILE: String = "default"
actual val NOTIFICATIONS_DATA_STORE_FILE: String = "notifications"