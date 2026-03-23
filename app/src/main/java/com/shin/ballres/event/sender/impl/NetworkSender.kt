package com.shin.ballres.event.sender.impl

import com.shin.ballres.event.Event
import com.shin.ballres.event.sender.EventSender

class NetworkSender : EventSender {
    override fun send(event: Event) {
        println("[Network] POST /api/event - ${event.name}")
        simulateNetworkRequest(event)
    }

    //应使用Retrofit挂起替代
    private fun simulateNetworkRequest(event: Event) {
        Thread {
            Thread.sleep(100)
            println("[Network] Response: OK for event ${event.name}")
        }.start()
    }
}