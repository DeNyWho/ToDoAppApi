package com.example.routes

import com.example.auth.MySession
import com.example.repository.TodoRepository
import com.example.repository.UserRepository
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.sessions.*

fun Route.todoRoute(
    userDb: UserRepository,
    todoDb: TodoRepository
) {

    authenticate("jwt") {

        post("/v1/todo") {

            val parameter = call.receive<Parameters>()

            val todo = parameter["todo"] ?: return@post call.respondText(
                "Missing parameter",
                status = HttpStatusCode.Unauthorized
            )

            val done = parameter["done"] ?: return@post call.respondText(
                "Missing parameter",
                status = HttpStatusCode.Unauthorized
            )

            val user = call.sessions.get<MySession>()?.let {
                userDb.findUserById(it.userId)
            }

            if (user == null) {
                call.respondText("pblm getting user...")
            }

            try {

                val currentTodo = todoDb.createTodo(user!!.userId, todo, done.toBoolean())
                currentTodo?.id?.let {
                    call.respond(currentTodo)
                }


            } catch (e: Throwable) {
                call.respondText("pblm creating todos...")
            }

        }
    }

    get("/v1/todo"){
        val user = call.sessions.get<MySession>()?.let {
            userDb.findUserById(it.userId)
        }

        if (user == null){
            call.respondText ("pblm getting user...")
        }


        try {

            val allTodo = todoDb.getAllTodo(user!!.userId)
            if(allTodo.isNotEmpty()){
                call.respond(allTodo)
            }

        } catch (e: Throwable){
            call.respondText ("pblm creating todos...")
        }
    }


    delete("/v1/todo/{id}"){

        val id = call.parameters["id"]

        val user = call.sessions.get<MySession>()?.let {
            userDb.findUserById(it.userId)
        }

        if (user == null){
            call.respondText ("pblm getting user...")
        }

        try {

            val allTodo = todoDb.getAllTodo(user!!.userId)

            allTodo.forEach{
                if(it.id == id!!.toInt())
                {
                    todoDb.deleteTodo(id.toInt())
                    call.respondText("deleted")
                } else {
                    call.respondText("getting pblm..")
                }
            }

        } catch (e: Throwable){
            call.respondText("pblm creating todos...")
        }
    }


    delete("/v1/todo"){

        val user = call.sessions.get<MySession>()?.let {
            userDb.findUserById(it.userId)
        }

        if (user == null){
            call.respondText ("pblm getting user...")
        }

        try {
            val allTodo = user?.userId?.let {todoDb.deleteAllTodo(it) }
            if(allTodo != null ){
                if(allTodo > 0){
                    call.respondText("deleted successfully")
                } else {
                    call.respondText("pblm getting user..")
                }

            }
        } catch (e: Throwable){
            call.respondText("pblm creating todos..")
        }
    }

    put("/v1/todo"){

        val id = call.parameters["id"]

        val user = call.sessions.get<MySession>()?.let {
            userDb.findUserById(it.userId)
        }

        if (user == null){
            call.respondText ("pblm getting user...")
        }

        val parameter = call.receive<Parameters>()

        val todo = parameter["todo"] ?: return@put call.respondText(
            "missing parameter",
            status = HttpStatusCode.Unauthorized
        )

        val done = parameter["done"] ?: return@put call.respondText(
            "missing parameter",
            status = HttpStatusCode.Unauthorized
        )

        try {

            val allTodo = user?.userId?.let { todoDb.getAllTodo(it) }

            allTodo!!.forEach {
                if(it.id == id!!.toInt()){
                    todoDb.updateTodo(id.toInt(), todo, done.toBoolean())
                    call.respond("updated.. successfully")
                } else {
                    call.respondText("getting pblm")
                }
            }

        } catch (e: Throwable){
            call.respondText ("pblm creating todos...")
        }

    }


}