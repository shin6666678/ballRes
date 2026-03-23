package com.shin.ballres.event

import com.shin.ballres.event.sender.EventSender
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch




object EventManager {
    private val senders = mutableListOf<EventSender>()
    private val scope = CoroutineScope(Dispatchers.IO)
    private val eventQueue = mutableListOf<Event>()

    fun registerSender(sender: EventSender) {
        senders.add(sender)
    }

    fun track(event: Event) {
        scope.launch {
            senders.forEach { it.send(event) }
        }
    }

    fun trackEvent(name: String, params: Map<String, Any> = emptyMap()) {
        track(Event(name, params))
    }
}