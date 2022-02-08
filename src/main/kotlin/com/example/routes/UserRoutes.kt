package com.example.routes

import com.example.auth.JwtService
import com.example.auth.MySession
import com.example.repository.TodoRepository
import com.example.repository.UserRepository
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.sessions.*

fun Route.userRoute(
    userDb: UserRepository,
    todoDb: TodoRepository,
    jwtService: JwtService,
    hash: (String) -> String
){

    post("/v1/create"){
        val parameter = call.receive<Parameters>()

        val name = parameter["name"] ?: return@post call.respondText (
            "missing data",
            status = HttpStatusCode.Unauthorized,
        )

        val email = parameter["email"] ?: return@post call.respondText (
            "missing data",
            status = HttpStatusCode.Unauthorized,
        )

        val password = parameter["password"] ?: return@post call.respondText (
            "missing data",
            status = HttpStatusCode.Unauthorized,
        )

        val hashPassword = hash(password)
        val currentUser = userDb.createUser(name, email, hashPassword)

        try {

            currentUser?.userId?.let {

                call.sessions.set(MySession(it))
                call.respondText (
                    jwtService.generateToken(currentUser),
                    status = HttpStatusCode.Created
                )

            }

        } catch (e: Throwable){
            call.respondText ("pblm creating user..")
        }
    }

    post ("/v1/login"){

        val parameter = call.receive<Parameters>()

        val email = parameter["email"] ?: return@post call.respondText (
            "missing data",
            status = HttpStatusCode.Unauthorized,
        )

        val password = parameter["password"] ?: return@post call.respondText (
            "missing data",
            status = HttpStatusCode.Unauthorized,
        )

        val hashPassword = hash(password)

        try {
            val currentUser = userDb.findUserByEmail(email)
            currentUser?.userId?.let {
                if(currentUser.password == hashPassword){
                    call.sessions.set(MySession(it))
                    call.respondText (
                        jwtService.generateToken(currentUser)
                            )
                }
            }
        } catch (e: Throwable){
            call.respondText("plbm creating user..")
        }
    }

    delete("/v1/user"){

        val user = call.sessions.get<MySession>()?.let {
            userDb.findUserById(it.userId)
        }

        if(user == null) {
            call.respondText(
                "plbm creating user",
                status = HttpStatusCode.BadRequest
            )
        }

        try {

            user?.userId?.let { todoDb.deleteAllTodo(it) }
            val currentUser = user?.userId?.let { userDb.deleteUser(it) }
            if(currentUser == 1){
                call.respondText ("user deleted..")
            } else {
                call.respond("getting plbm..")
            }

        } catch (e: Throwable){
            call.respond("plbm creating user..")
        }
    }


    put("/v1/user"){

        val parameter = call.receive<Parameters>()

        val name = parameter["name"] ?: return@put call.respondText (
            "missing data",
            status = HttpStatusCode.Unauthorized,
        )

        val email = parameter["email"] ?: return@put call.respondText (
            "missing data",
            status = HttpStatusCode.Unauthorized,
        )

        val password = parameter["password"] ?: return@put call.respondText (
            "missing data",
            status = HttpStatusCode.Unauthorized,
        )

        val user = call.sessions.get<MySession>()?.let {
            userDb.findUserById(it.userId)
        }

        if(user == null){
            call.respondText (
                "pblm getting user",
                status = HttpStatusCode.BadRequest
            )
        }

        val hash = hash(password)
        try {
            val currentUser = userDb.updateUser(user!!.userId, name, email, hash)
            if(currentUser == 1){
                call.respondText("updated successfully...")
            } else {
                call.respondText ("getting pblm...")
            }
        } catch (e: Throwable){
            call.respondText("pblm creating user...")
        }

    }


}