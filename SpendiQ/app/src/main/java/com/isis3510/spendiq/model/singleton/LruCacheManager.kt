package com.isis3510.spendiq.model.singleton

import android.util.LruCache

object LruCacheManager {
    val cache = LruCache<String, Any>(5)
}
