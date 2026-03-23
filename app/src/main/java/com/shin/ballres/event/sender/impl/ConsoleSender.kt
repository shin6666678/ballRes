package com.shin.ballres.event.sender.impl

import com.shin.ballres.event.Event
import com.shin.ballres.event.sender.EventSender

class ConsoleSender : EventSender {
    override fun send(event: Event) {
        println("[Event] name=${event.name}, params=${event.params}, timestamp=${event.timestamp}")
    }
}