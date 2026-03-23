package com.shin.ballres.event

data class Event(
    val name: String,
    val params: Map<String, Any> = emptyMap(),
    val timestamp: Long = System.currentTimeMillis()
)
