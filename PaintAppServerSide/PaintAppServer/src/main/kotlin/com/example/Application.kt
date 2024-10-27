package com.example

import com.example.plugins.* //allowing the wildcard import (import com.example.plugins.*) to cover all the functions within that package.
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

fun main() {
    println("Server starting...")  // Should print when the server starts
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    configureRouting()
    configureSerialization()
}
