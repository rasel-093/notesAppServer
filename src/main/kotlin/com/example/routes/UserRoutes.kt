package com.example.routes

import com.example.authentication.JwtService
import com.example.data.model.LoginRequest
import com.example.data.model.RegisterRequest
import com.example.data.model.SimpleResponse
import com.example.data.model.User
import com.example.repository.UserRepo
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.locations.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlin.math.log

const val API_VERSION = "/v1"
const val USERS = "$API_VERSION/users"
const val REGISTER_REQUEST = "$USERS/register"
const val LOGIN_REQUEST = "$USERS/login"

@Location(REGISTER_REQUEST)
data class UserRegisterRoute(
    val name: String
)
@Location(LOGIN_REQUEST)
class UserLoginRoute

fun Route.userRoutes(
    db: UserRepo,
    jwtService: JwtService,
    hashFunction: (String)->String
){
    post(REGISTER_REQUEST) {
        val registerRequest = try {
            call.receive<RegisterRequest>()
        }catch (e: Exception){
            call.respond(
                HttpStatusCode.BadRequest,
                SimpleResponse(false, "Missing some fields")
            )
            return@post
        }

        try {
            val user = User(
                email = registerRequest.email,
                hashPassword = hashFunction(registerRequest.password),
                userName = registerRequest.name
            )
            db.addUser(user = user )
            call.respond(
                HttpStatusCode.OK,
                SimpleResponse(true, jwtService.generateToken(user))
            )
        }catch (e: Exception){
            call.respond(
                HttpStatusCode.Conflict,
                SimpleResponse(false, e.localizedMessage ?: "Something went wrong")
            )
        }
    }

    post(LOGIN_REQUEST){
        val loginRequest = try {
            call.receive<LoginRequest>()
        }catch (e: Exception){
            call.respond(
                HttpStatusCode.Conflict,
                SimpleResponse(false, e.localizedMessage ?: "Something went wrong")
            )
            return@post
        }

        try {
            val user = db.findUserByEmail(email = loginRequest.email)
            if (user == null){
                call.respond(
                    HttpStatusCode.BadRequest,
                    SimpleResponse(false, "User not found")
                )
            }else{
                if (user.hashPassword == hashFunction(loginRequest.password)){
                    call.respond(
                        HttpStatusCode.OK,
                        SimpleResponse(true, jwtService.generateToken(user))
                    )
                }else{
                    call.respond(
                            HttpStatusCode.BadRequest,
                            SimpleResponse(false, "Wrong password")
                        )
                }
            }
        }catch (e: Exception){
            call.respond(
                HttpStatusCode.Conflict,
                e.message ?: "Something went wrong"
            )
        }
    }
}