package com.example

import com.example.plugins.*
import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.FileInputStream

//defines a database with two tables User and SharedImage
object DBSettings {
    val db by lazy { Database.connect("jdbc:h2:mem:test;MODE=MYSQL;DB_CLOSE_DELAY=-1", driver = "org.h2.Driver")}

    fun init() {
        transaction(db) {
            SchemaUtils.create(User, SharedImage, Drawings)
            //TODO: Remove test user once user sign up is done
            addTestUser("AAA")
        }
    }
}

//sets up a server that listens on port 8080
fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

//Configures plugins and features such as routing and serialization
fun Application.module() {
    DBSettings.init() //sets up the database defined
    configureSerialization()
    configureRouting()
}

fun addTestUser(uID: String) {
    transaction {
        //TODO: remove test user after real user sign-up is done
        User.insert {
            it[User.uID] = uID
        }
    }
}