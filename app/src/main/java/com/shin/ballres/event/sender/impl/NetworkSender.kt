package com.shin.ballres.event.sender.impl

import com.shin.ballres.event.Event
import com.shin.ballres.event.sender.EventSender
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class NetworkSender : EventSender {
    override fun send(event: Event) {
        println("[Network] POST /api/event - ${event.name}")
        simulateNetworkRequest(event)
    }

    //应使用Retrofit替代,为了简单,我先这么做了,delay也封装好了协程
    private fun simulateNetworkRequest(event: Event) {
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            delay(1000)
            println("[Network] Response: OK for event ${event.name}")
        }
    }
}