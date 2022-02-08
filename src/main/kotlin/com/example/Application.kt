package com.example

import com.example.plugins.configureRouting
import com.example.plugins.configureSecurity
import com.example.plugins.configureSerialization
import com.example.repository.DataBaseFactory
import io.ktor.server.engine.*
import io.ktor.server.netty.*

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
        DataBaseFactory.init()
        configureRouting()
        configureSecurity()
        configureSerialization()
    }.start(wait = true)
}
