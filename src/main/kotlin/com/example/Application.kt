package com.example

import com.example.authentication.JwtService
import com.example.authentication.hash
import com.example.data.model.User
import com.example.plugins.*
import com.example.repository.DatabaseFactory
import com.example.repository.UserRepo
import com.example.routes.noteRoutes
import com.example.routes.userRoutes
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    DatabaseFactory.init()
    val db = UserRepo()
    val jwtService = JwtService()
    val hashFunction = {
        s: String -> hash(s)
    }
    configureSerialization()
    configureSecurity(jwtService, db)
    configureRouting()
    configureLocation()

    routing {
        userRoutes(db,jwtService,hashFunction)
        noteRoutes(db, hashFunction)




        get("/") { call.respondText("Hello") }
        get("/token"){
            val email = call.request.queryParameters["email"]
            val password = call.request.queryParameters["password"]
            val userName = call.request.queryParameters["userName"]
            if (email!=null && password!=null && userName!=null){
                val user = User(
                    email = email,
                    hashPassword = password,
                    userName = userName
                )
                call.respond(jwtService.generateToken(user))
            }
        }
    }
}
