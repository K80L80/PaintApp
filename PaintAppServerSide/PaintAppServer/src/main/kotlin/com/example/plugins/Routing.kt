package com.example.plugins

import io.ktor.http.*
import io.ktor.resources.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.resources.*
import io.ktor.server.resources.Resources
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

fun Application.configureRouting() {
    install(Resources)
    routing {
        //listen for when client types /books into browser and send back text "fetching all books"
        get("/drawing") {
            call.respondText("Fetching all drawings")
        }

        //client listens for when user types ie /drawing/8 into their browser and responds by sending complex object (drawing) back to client
        get("/drawing/{id}") {
            // Example book object
            val drawing = Drawing(
                id = call.parameters["id"]?.toIntOrNull() ?: 1,
            )
            // Respond with the book object serialized as JSON
            call.respond(drawing)
        }
        //client sends post request to add book to server resource
        post("/drawing") {
            // Receive the Book object from the request body
            val newDrawing = call.receive<Drawing>()

            // Process the book (e.g., store it in a database)
            println("Received drawing: ${newDrawing.id}!!!!")

            // Respond with a success message
            call.respond(HttpStatusCode.Created, "creating new drawing")
        }
    }
}


@Serializable
class Drawing(val id: Int? = null)