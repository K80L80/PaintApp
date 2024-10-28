package com.example.plugins

import com.example.SharedImage
import io.ktor.http.*
import io.ktor.http.ContentType.Application.Json
import io.ktor.http.content.*
import io.ktor.resources.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.resources.*
import io.ktor.server.resources.Resources
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.routing.routing
import io.ktor.util.cio.*
import io.ktor.utils.io.*
import io.ktor.utils.io.core.*

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.io.readByteArray
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File



fun Application.configureRouting() {
    install(Resources)

    //creates a directory to store all uploads
    val uploadDir = File("uploads")
    if (!uploadDir.exists()) {
        println("creating a uploads directory.........")
        uploadDir.mkdir()  // Create the directory if it doesn't exist
    }
    
    routing {

        //listen for when client want to upload drawing to server
        post("/upload") {
            handleFileUpload(call)
        }

        //listen for when client types /drawing into browser and send back text "fetching all drawings"
        get("/drawing") {
            val checkuID = call.receive<DrawingIn>().uId
            val drawings = transaction {
                SharedImage.selectAll().where{SharedImage.uID eq checkuID}.map { row ->
                    DrawingOut(
                        sharedDate = row[SharedImage.sharedDate],
                        fileName = row[SharedImage.fileName],
                        imageTitle = row[SharedImage.imageTitle]
                    )
                }
            }
            call.respond(drawings)
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
                imageTitle = "My Artwork #$id",
                ownerID =  1L
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

        get("/download/{fileName}") {
            val fileName = call.parameters["fileName"]
            val file = File("uploads/$fileName")

            if (file.exists()) {
                call.response.header(HttpHeaders.ContentDisposition, "attachment; filename=$fileName")
                call.respondFile(file)
            } else {
                call.respond(HttpStatusCode.NotFound, "File not found")
            }
        }
    }
}

// Drawing is sent in two parts
//1) The drawings id, fileName, imageTitle is sent as a JSON object in the body of the post request (calling it meta-data)
//2) The file itself is sent as a file
suspend fun handleFileUpload(call: ApplicationCall) {
    println("Received drawing..........")

    // Initialize variables to hold received parts
    var drawingId: String? = null
    var imageTitle: String? = null
    var fileName: String? = null
    var fileBytes: ByteArray? = null
    var ownerID: String? = null
    println("receiving multipart.........")
    // Process each part in the multipart request
    val multipart = call.receiveMultipart()
    multipart.forEachPart { part ->
        when (part) {
            // Text parts for metadata
            is PartData.FormItem -> {
                println("receiving meta data.........")
                when (part.name) {
                    "DrawingID" -> drawingId = part.value
                    "ImageTitle" -> imageTitle = part.value
                    "fileName" -> fileName = part.value
                    "ownerID" -> ownerID = part.value
                }
            }
            // File part for image
            is PartData.FileItem -> {
                println("receiving file Item.........")
                fileName = part.originalFileName as String
                fileBytes = part.provider().readRemaining().readByteArray()
            }
            else -> Unit // Ignore other parts
        }
        part.dispose() // Release resources for each part
    }

    //TODO: probably add a token check here?
    // Check that the required data was received
    if (drawingId == null || imageTitle == null || fileBytes == null || ownerID == null) {
        call.respond(HttpStatusCode.BadRequest, "Missing data in multipart form")
        return
    }

    // Save the file
    File("uploads/user-$ownerID-drawing-$drawingId.png").writeBytes(fileBytes!!)

    println("Received drawing downloaded to: uploads/user-$ownerID-drawing-${drawingId.toString()}.png")

    // Respond with confirmation
    call.respond(HttpStatusCode.OK, "File uploaded successfully with DrawingID: $drawingId and title: $imageTitle")
}


// Define the metadata data class
@Serializable
data class DrawingMetadata(
    val title: String,
    val description: String,
    val author: String
)

@Serializable
data class Drawing(
    val id: Long?,
    val fileName: String?,  // Full path of the file
    var imageTitle: String?, // User-chosen name for display purposes
    val ownerID: Long//user that owner the drawing
)


@Serializable
data class DrawingOut(
    val sharedDate: Long,
    val fileName: String,
    val imageTitle: String
)

@Serializable
data class DrawingIn(
    val uId: String,
    val sharedDate: Long,
    val fileName: String,
    val imageTitle: String
)