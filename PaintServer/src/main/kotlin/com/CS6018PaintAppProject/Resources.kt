package com.CS6018PaintAppProject

import io.ktor.http.*
import io.ktor.resources.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.resources.*
import io.ktor.server.response.*
import io.ktor.server.routing.routing

import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.io.File

fun Application.configureResources() {
    routing {
        get<SharedImages.Download> { passedInVals ->
            val fileExistsAndHasAccess = newSuspendedTransaction(Dispatchers.IO) {
                SharedImage.selectAll()
                    .where { (SharedImage.email eq passedInVals.email) and (SharedImage.fileName eq passedInVals.fileName) }.count() > 0
            }

            if (fileExistsAndHasAccess) {
                val file = File("uploads/${passedInVals.fileName}")
                if (file.exists()) {
                    call.response.header(HttpHeaders.ContentDisposition, "attachment; filename=${passedInVals.fileName}")
                    call.respondFile(file)
                } else {
                    call.respond(HttpStatusCode.NotFound, "File does not exist/not found")
                }
            } else {
                call.respond(HttpStatusCode.Forbidden, "Issue with username or file on server")
            }
        }
    }
}

@Serializable
@Resource("/sharedImage")
class SharedImages {

    @Resource("download")
    class Download(val email: String, val fileName: String)
}