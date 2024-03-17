package com.example.routes

import com.example.data.model.Note
import com.example.data.model.SimpleResponse
import com.example.data.model.User
import com.example.repository.UserRepo
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

const val NOTES = "$API_VERSION/notes"
const val CREATE_NOTES = "$NOTES/create"
const val UPDATE_NOTES = "$NOTES/update"
const val DELETE_NOTES = "$NOTES/delete"

fun Routing.noteRoutes(
    db: UserRepo,
    hashFunction: (String) -> String
){

    authenticate("jwt") {
        post(CREATE_NOTES) {
            val note = try {
                call.receive<Note>()
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    SimpleResponse(false, "Missing fields")
                )
                return@post
            }

            try {
                val email = call.principal<User>()!!.email
                db.addNote(note, email)
                call.respond(
                        HttpStatusCode.OK,
                        SimpleResponse(true, "Note added  successfully")
                    )
            }catch (e: Exception){
                call.respond(
                    HttpStatusCode.Conflict,
                    SimpleResponse(false, e.message ?: "Something went wrong")
                )
            }
        }

        get(NOTES){
            try {
                val email = call.principal<User>()!!.email
                val notes = db.getAllNotes(email)
                call.respond(
                    HttpStatusCode.OK,
                    notes
                )
            }catch (e: Exception){
                call.respond(
                    HttpStatusCode.Conflict,
                    emptyList<Note>()
                )
            }
        }

        post(UPDATE_NOTES){
            val note = try {
                call.receive<Note>()
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    SimpleResponse(false, "Missing fields")
                )
                return@post
            }

            try {
                val email = call.principal<User>()!!.email
                db.updateNote(note, email)
                call.respond(
                    HttpStatusCode.OK,
                    SimpleResponse(true, "Note updated  successfully")
                )
            }catch (e: Exception){
                call.respond(
                    HttpStatusCode.Conflict,
                    SimpleResponse(false, e.message ?: "Something went wrong")
                )
            }
        }

        delete(DELETE_NOTES){
            val noteId = try {
                call.request.queryParameters["id"]!!
            }catch (e: Exception){
                call.respond(
                    HttpStatusCode.BadRequest,
                    "Query parameter id is not present"
                )
                return@delete
            }
            try {
                val email = call.principal<User>()!!.email
                db.deleteNote(noteId, email)
                call.respond(HttpStatusCode.OK,
                    SimpleResponse(true, "Note deleted successfully"))
            }catch (e: Exception){
                call.respond(
                    HttpStatusCode.Conflict,
                    SimpleResponse(false, "Something went wrong")
                )
            }
        }
    }
}