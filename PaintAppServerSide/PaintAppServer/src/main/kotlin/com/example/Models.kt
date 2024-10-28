package com.example

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Table

object User: Table(){
    val uID = long("uID")
    val email = varchar("email", 100)
    override val primaryKey = PrimaryKey(uID)
}

object Drawings: Table(){
    val dID = long("dID")
    val fileName = varchar("fileName", 250) // Full path of the file
    val imageTitle = varchar("imageTitle", 250)// User-chosen name for display purposes
    val ownerID = reference("uID", User.uID) // specify who owns the drawing
}


object SharedImage: Table(){
    val uID = reference("uID", User.uID)
    val sharedDate = long("sharedDate")
    val fileName = varchar("fileName", 250)
    val imageTitle = varchar("imageTitle", 250)
    override val primaryKey = PrimaryKey(arrayOf(User.uID,fileName))
}