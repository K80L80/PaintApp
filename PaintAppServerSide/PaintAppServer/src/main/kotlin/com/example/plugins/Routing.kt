package com.example.plugins

import com.example.Drawings
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
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
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

//        //listen for when client types /drawing into browser and send back text "fetching all drawings"
//        get("/drawing") {
//            val checkuID = call.receive<DrawingIn>().uId
//            val drawings = transaction {
//                SharedImage.selectAll().where{SharedImage.uID eq checkuID}.map { row ->
//                    DrawingOut(
//                        sharedDate = row[SharedImage.sharedDate],
//                        fileName = row[SharedImage.fileName],
//                        imageTitle = row[SharedImage.imageTitle]
//                    )
//                }
//            }
//            call.respond(drawings)
//        }

        //client listens for when user types ie /drawing/8 into their browser and responds by sending complex object (drawing) back to client
//        get("/drawings/{id}") {
//            val id = call.parameters["id"]?.toLongOrNull()
//            if (id == null) {
//                call.respondText("Invalid drawing ID", status = HttpStatusCode.BadRequest)
//                return@get
//            }
//
//            // Retrieve the drawing based on ID (mocking here with sample data)
//            val drawing = Drawing(
//                id = id,
//                fileName = "path/to/file_$id.png",
//                imageTitle = "My Artwork #$id",
//                ownerID = "spencer2@gmail.com"
//            )
//
//            // Send the drawing data back to the client
//            call.respond(drawing)
//        }


        //client sends post request to add drawing to server resource
        post("/drawing") {
            // Receive the Book object from the request body
            val newDrawing = call.receive<Drawing>()

            // Process the drawing (e.g., store it in a database)
            println("Received drawing: id: ${newDrawing.id}, fileName:${newDrawing.fileName} title: ${newDrawing.imageTitle}, ")

            // Respond with a success message
            call.respond(HttpStatusCode.Created, "creating new drawing")
        }

//        get("/download/{fileURI}") {
//            val fileName = call.parameters["fileName"]
//            val file = File("uploads/$fileName")
//
//            if (file.exists()) {
//                call.response.header(HttpHeaders.ContentDisposition, "attachment; filename=$fileName")
//                call.respondFile(file)
//            } else {
//                call.respond(HttpStatusCode.NotFound, "File not found")
//            }
//        }

        get("/download/{ownerID}/{drawingId}") {
            val ownerID = call.parameters["ownerID"]
            val drawingId = call.parameters["drawingId"]?.toLong()
            getDrawing(ownerID, drawingId, call) //Unresolved reference: getDrawing
        }

        get("/drawings/{ownerID}") {
            println("Calling getlist")
            val ownerID = call.parameters["ownerID"]
            if (ownerID != null) {
                val drawings = getDrawingsByOwner(ownerID) ?: emptyList<Drawing>()

                println("Owner ID: $ownerID")
                println("Drawings: $drawings")

                call.respond(HttpStatusCode.OK, drawings)
            } else {
                println("Error: Owner ID is missing.")
                call.respond(HttpStatusCode.BadRequest, "Owner ID is missing.")
            }
        }
    }
}
suspend fun getDrawing(ownerID: String?, drawingID: Long?, call: ApplicationCall){
        // Endpoint to download a drawing file by ownerID and drawingId
        if (ownerID == null || drawingID == null) {
            call.respond(HttpStatusCode.BadRequest, "Invalid owner ID or drawing ID")
            return
        }

        // Construct the file path based on the naming convention
        val filePath = "uploads/user-$ownerID-drawing-$drawingID.png"
        val file = File(filePath)

        if (file.exists()) {
            // Serve the file as a response
            call.respondFile(file)
        } else {
            // Return a 404 if the file doesn't exist
            call.respond(HttpStatusCode.NotFound, "File not found")
        }
    }

// Drawing is sent in two parts
//1) The drawings id, fileName, imageTitle is sent as a JSON object in the body of the post request (calling it meta-data)
//2) The file itself is sent as a file
suspend fun handleFileUpload(call: ApplicationCall) {
    println("Received drawing..........")

    // Initialize variables to hold received parts
    var drawingId: Long? = null
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
                    "DrawingID" -> drawingId = part.value.toLong()
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

    //Save file and add metadata info to database
    try {
        //if saving file sucessed add it to the database
        File("uploads/user-$ownerID-drawing-$drawingId.png").writeBytes(fileBytes!!)
        addDrawing(drawingId!!, ownerID!!, fileName!!, imageTitle!!)
    }
    catch (e: Exception) {
        // Handle any other unexpected exceptions and respond with a general error
        println("An unexpected error occurred: ${e.message}")
        call.respond(HttpStatusCode.InternalServerError, "Unable to upload file due to an unexpected error")
    }

    //Add Drawing to Database
    println("Received drawing downloaded to: uploads/user-$ownerID-drawing-${drawingId}.png")

    // Respond with confirmation
    call.respond(HttpStatusCode.OK, "File uploaded successfully with DrawingID: $drawingId and title: $imageTitle")
}

fun addDrawing(drawingID: Long, ownerID: String, fileName: String, imageTitle: String): Pair<Long, String> {
    transaction {
        Drawings.insert { drawing ->
            drawing[Drawings.dID] = drawingID
            drawing[Drawings.ownerID] = ownerID
            drawing[Drawings.fileName] = fileName
            drawing[Drawings.imageTitle] = imageTitle
        }
    }
    return Pair(drawingID, ownerID)
}

fun getDrawingsByOwner(ownerID: String): List<Drawing>  {
   return transaction {
       Drawings
           .selectAll()
           .where(Drawings.ownerID eq ownerID)
           .map { row: ResultRow ->
               Drawing(
                   id = row[Drawings.dID],
                   ownerID = row[Drawings.ownerID],
                   fileName = row[Drawings.fileName],
                   imageTitle = row[Drawings.imageTitle]
               )
       }
   }
}


fun getDrawingByOwnerAndId(ownerID: String, drawingID: Long): Drawing? {
    return transaction {
        Drawings
            .selectAll()
            .where((Drawings.ownerID eq ownerID) and (Drawings.dID eq drawingID))
            .map { row :ResultRow->
                Drawing(
                    id = row[Drawings.dID],
                    ownerID = row[Drawings.ownerID],
                    fileName = row[Drawings.fileName],
                    imageTitle = row[Drawings.imageTitle]
                )
            }
        .firstOrNull() // Returns the first result or null if no match is found
    }
}

@Serializable
data class Drawing(
    val id: Long?,
    val fileName: String?,  // Full path of the file
    var imageTitle: String?, // User-chosen name for display purposes
    val ownerID: String//user that owner the drawing
)