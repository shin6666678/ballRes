package com.shin.ballres.event.sender

import com.shin.ballres.event.Event

interface EventSender {
    fun send(event: Event)
}