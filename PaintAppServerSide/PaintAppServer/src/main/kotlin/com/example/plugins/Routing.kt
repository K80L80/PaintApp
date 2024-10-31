package com.example.plugins

import com.example.Drawings
import com.example.SharedImage
import com.example.User
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
            // Process the drawing (e.g., store it in a database)
            println("client making post request to upload route")

            handleFileUpload(call)
        }

        post("/login/{ownerID}") {
            println("Calling getlist")
            val ownerID = call.parameters["ownerID"] ?: error("Missing ownerID parameter")

            transaction {
                User.insert { user ->
                    user[uID] = ownerID
                }
            }
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

        post("/unshare/{ownerID}/{drawingId}") {
//            val parameters = call.receiveParameters()
//            println(parameters);
            val ownerID = call.parameters["ownerID"]
            val drawingID = call.parameters["drawingId"]?.toLongOrNull()

            println("ownerID: $ownerID, drawingID: $drawingID");
            if (ownerID == null || drawingID == null) {
                call.respond(HttpStatusCode.BadRequest, "missing")
                return@post
            }

            // Delete from the database and file system
            val success = deleteDrawingAndFile(ownerID, drawingID)
            if (success) {
                call.respond(HttpStatusCode.OK, "Successfully unshare\n")
            } else {
                call.respond(HttpStatusCode.InternalServerError, "Unshare failure\n")
            }
        }







        get("drawing/download/{ownerID}/{drawingID}.png") {
            println("..........hitting the download route..............")
            val ownerID = call.parameters["ownerID"]
            val drawingID = call.parameters["drawingID"]


            val fileName = "user-$ownerID-drawing-$drawingID.png"

            println("getting file from : uploads/$fileName")
            val file = File("uploads/$fileName")
            if (file.exists()) {
                println("the file does exist")
                call.response.header(HttpHeaders.ContentDisposition, "attachment; filename=$fileName")
                println("set content disposition:  attachment; filename=$fileName")
                println("...........sending file")
                call.respondFile(file)

            } else {
                call.respond(HttpStatusCode.NotFound, "File not on cloud")
            }
        }

        get("drawing/download/{ownerID}/{drawingId}") {
            println("..........hitting the download route DIFFERENT ONE THOUGH..............")
            val ownerID = call.parameters["ownerID"]
            val drawingId = call.parameters["drawingId"]?.toLong()
            //responds to client with JSON Drawing Object and the file
            getDrawing(ownerID, drawingId, call)
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

//suspend fun getDrawing(ownerID: String?, drawingID: Long?, call: ApplicationCall) {
//
//    // Endpoint to g a drawing file by ownerID and drawingId
//    if (ownerID == null || drawingID == null) {
//        println("Server Side –– ownerID and drawingID is null ")
//        call.respond(HttpStatusCode.BadRequest, "Invalid owner ID or drawing ID")
//        return
//    }
//
//    //Get Drawing add it to the body  of response
//    val drawing = getDrawingByOwnerAndId(ownerID,drawingID) // Default to an empty list if null
//    if(drawing==null) {
//        println("Server Side –– there is no drawing with that ID ")
//        call.respond(HttpStatusCode.BadRequest, "no drawing for that user and drawing id")
//        return
//    }
//
//    //Drawing JSON
//    call.respond(HttpStatusCode.OK, drawing)
//    println("Server Side –– responding HttpStatusCode.OK $drawing" )
//
//    // Construct the file path based on the naming convention
//    val filePath = "uploads/user-$ownerID-drawing-$drawingID.png"
//    println("Server Side –– retrieving file....${filePath}")
//
//    val file = File(filePath)
//    if (file.exists()) {
//        // Serve the file as a response
//        println("Server Side  –– Responding with file....${filePath}")
//        call.respondFile(file)
//    } else {
//        // Return a 404 if the file doesn't exist
//        println("Server Side  –– that file does not exist")
//        call.respond(HttpStatusCode.NotFound, "File not found")
//    }
//}

suspend fun deleteDrawingAndFile(ownerID: String, drawingID: Long): Boolean {
    return try {
        // Deletes a drawing entry from the database
        newSuspendedTransaction {
            Drawings.deleteWhere {
                (Drawings.ownerID eq ownerID) and (Drawings.dID eq drawingID)
            }
        }

        // Construct the file path and delete the file
        val filePath = "uploads/user-$ownerID-drawing-$drawingID.png"
        val file = File(filePath)
        if (file.exists()) {
            val fileDeleted = file.delete()
            if (!fileDeleted) {
                println("Error deleting file at $filePath")
                return false
            }
        }
        true // Return true if both operations succeed
    } catch (e: Exception) {
        println("Error deleting drawing or file: ${e.message}")
        false // If an error occurs, false is returned
    }
}

suspend fun getDrawing(ownerID: String?, drawingID: Long?, call: ApplicationCall) {
    if (ownerID == null || drawingID == null) {
        println("Server Side –– ownerID and drawingID is null ")
        call.respond(HttpStatusCode.BadRequest, "Invalid owner ID or drawing ID")
        return
    }

    // Get the drawing metadata
    val drawing = getDrawingByOwnerAndId(ownerID, drawingID)
    if (drawing == null) {
        println("Server Side –– there is no drawing with that ID ")
        call.respond(HttpStatusCode.BadRequest, "No drawing for that user and drawing id")
        return
    }

    // Construct the file path
    val filePath = "uploads/user-$ownerID-drawing-$drawingID.png"
    val file = File(filePath)
    if (!file.exists()) {
        println("Server Side –– that file does not exist")
        call.respond(HttpStatusCode.NotFound, "File not found")
        return
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
                    "DrawingID" -> {
                        drawingId = part.value.toLong()
                        println("receiving meta data........DrawingID: ${drawingId}")
                    }
                    "ImageTitle" -> {
                        imageTitle = part.value
                        println("receiving meta data........imageTitle: ${imageTitle}")
                    }
                    "fileName" -> {
                        fileName = part.value
                        println("receiving meta data.......fileName: ${fileName}")
                    }

                    "ownerID" ->{
                        ownerID = part.value
                        println("receiving meta data........ownerID: ${ownerID}")
                    }
                }
            }
            // File part for image
            is PartData.FileItem -> {
                fileName = part.originalFileName as String
                println("receiving file Item.........fileName: $fileName")
                fileBytes = part.provider().readRemaining().readByteArray()

            }
            else -> Unit // Ignore other parts
        }
        part.dispose() // Release resources for each part
    }

    val userExists = transaction {
        ownerID?.let {
            User.selectAll().where { User.uID eq it }.count() > 0
        } ?: false
    }

    //TODO: probably add a token check here?
    // Check that the required data was received

    if (drawingId == null || imageTitle == null || fileBytes == null || ownerID == null || !userExists) {
        call.respond(HttpStatusCode.BadRequest, "Missing data in multipart form")
        return
    }

    //Save file and add metadata info to database
    try {
        //if saving file sucessed add it to the database
        println(".......UPLOADING...............")
        println(".......UPLOADING...............")
        val fileName1 = "user-$ownerID-drawing-$drawingId.png"
        val dir = "uploads/"
        println("writing file do $fileName1 in director $dir")
        File(dir+fileName1).writeBytes(fileBytes!!)
        println("DATABASE(drawingID = ${drawingId}, ownerID = $ownerID, fileName = $fileName1, imageTitle= $imageTitle")
        addDrawing(drawingId!!, ownerID!!, fileName1!!, imageTitle!!)
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
        Drawings.upsert { drawing ->
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

fun onDuplicateKeyUpdate(ownerID: String, drawingID: Long){
    transaction {
        // Check if the record with the same dID and uID exists

    }
}
@Serializable
data class Drawing(
    val id: Long?,
    val fileName: String?,  // Full path of the file
    var imageTitle: String?, // User-chosen name for display purposes
    val ownerID: String//user that owner the drawing
)