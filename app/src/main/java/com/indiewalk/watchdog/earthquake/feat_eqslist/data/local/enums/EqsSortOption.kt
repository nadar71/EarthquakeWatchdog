package com.indiewalk.watchdog.earthquake.feat_eqslist.data.local.enums

enum class EqsSortOption {
    MAG_DESC,   // greatest -> smallest magnitude
    MAG_ASC,    // smallest -> greatest magnitude
    DATE_ASC,   // oldest -> newest
    DATE_DESC,  // newest -> oldest
    DIST_ASC,   // nearest -> furthest
    DIST_DESC;  // furthest -> nearest

    companion object {
        fun fromNameString(s: String?): EqsSortOption =
            runCatching { valueOf(s ?: "") }.getOrDefault(DATE_DESC)
    }
}
