package com.vitalyk.insight.main

import java.sql.DriverManager

fun main(args: Array<String>) {
    Class.forName("org.h2.Driver")
    val connection = DriverManager.getConnection("jdbc:h2:~/insight/test", "test", "test")
    val statement = connection.createStatement()
    statement.execute("create table pawn(name varchar(20))")
    connection.close()
}