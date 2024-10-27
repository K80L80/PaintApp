package com.example

import org.jetbrains.exposed.sql.Table

object User: Table(){
    val email = varchar("email", 100)
    val uID = varchar("uID",100)
    override val primaryKey = PrimaryKey(uID)
}

object SharedImage: Table(){
    val email = reference("email", User.email)
    val sharedDate = long("sharedDate")
    val fileName = varchar("fileName", 250)
    val imageTitle = varchar("imageTitle", 250)
    override val primaryKey = PrimaryKey(arrayOf(User.email,fileName))
}