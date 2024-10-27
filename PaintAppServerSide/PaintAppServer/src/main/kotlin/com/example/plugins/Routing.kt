package com.example.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.resources.Resources
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

fun Application.configureRouting() {
    install(Resources)
    routing {
        //listen for when client types /drawing into browser and send back text "fetching all drawings"
        get("/drawing") {
            call.respondText("Fetching all drawings")
        }

        //client listens for when user types ie /drawing/8 into their browser and responds by sending complex object (drawing) back to client
        get("/drawings/{id}") {
            val id = call.parameters["id"]?.toLongOrNull()
            if (id == null) {
                call.respondText("Invalid drawing ID", status = HttpStatusCode.BadRequest)
                return@get
            }

            // Retrieve the drawing based on ID (mocking here with sample data)
            val drawing = Drawing(
                id = id,
                fileName = "path/to/file_$id.png",
                imageTitle = "My Artwork #$id"
            )

            // Send the drawing data back to the client
            call.respond(drawing)
        }


        //client sends post request to add drawing to server resource
        post("/drawing") {
            // Receive the Book object from the request body
            val newDrawing = call.receive<Drawing>()

            // Process the drawing (e.g., store it in a database)
            println("Received drawing: id: ${newDrawing.id}, fileName:${newDrawing.fileName} title: ${newDrawing.imageTitle}, ")

            // Respond with a success message
            call.respond(HttpStatusCode.Created, "creating new drawing")
        }
    }
}

@Serializable
data class Drawing(
    val id: Long?,

    val fileName: String?,  // Full path of the file

    var imageTitle: String? // User-chosen name for display purposes
)