package com.indiewalk.watchdog.earthquake.core.domain.model

sealed interface AppError {
    data class Network(val message: String? = null) : AppError
    data class Storage(val message: String? = null) : AppError
    data class Location(val message: String? = null) : AppError
    data class Unknown(val message: String? = null) : AppError
}

