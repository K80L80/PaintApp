package com.CS6018PaintAppProject

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Table

object User: Table(){
    val email = varchar("email", 100)
    val password = varchar("password",100)
    override val primaryKey = PrimaryKey(email)
}

object SharedImage: Table(){
    val email = reference("email", User.email)
    val sharedDate = long("sharedDate")
    val fileName = varchar("fileName", 250)
    val imageTitle = varchar("imageTitle", 250)
    override val primaryKey = PrimaryKey(arrayOf(User.email,fileName))
}