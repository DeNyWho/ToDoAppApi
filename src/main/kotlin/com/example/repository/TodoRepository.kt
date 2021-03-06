package com.example.repository

import com.example.data.Todo
import com.example.data.dao.TodoDao
import com.example.data.table.TodoTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.InsertStatement

class TodoRepository: TodoDao {
    override suspend fun createTodo(userId: Int, todoData: String, done: Boolean): Todo? {
        var statement: InsertStatement<Number>? = null

        DataBaseFactory.dbQuery {

            statement = TodoTable.insert { todo ->
                todo[TodoTable.todo] = todoData
                todo[TodoTable.done] = done

            }

        }

        return rowToTodo(statement?.resultedValues?.get(0))

    }

    override suspend fun getAllTodo(userId: Int): List<Todo> = DataBaseFactory.dbQuery {
        TodoTable.select{ TodoTable.userId.eq(userId)}
            .mapNotNull {
                rowToTodo(it)
            }
    }

    override suspend fun getTodo(id: Int): Todo? = DataBaseFactory.dbQuery {
        TodoTable.select { TodoTable.id.eq(id)}
            .map{
                rowToTodo(it)
            }.singleOrNull()
    }

    override suspend fun deleteTodo(id: Int): Int = DataBaseFactory.dbQuery {
        TodoTable.deleteWhere { TodoTable.id.eq(id) }
    }

    override suspend fun deleteAllTodo(userId: Int): Int = DataBaseFactory.dbQuery {
        TodoTable.deleteWhere { TodoTable.userId.eq(userId) }
    }

    override suspend fun updateTodo(id: Int, todoData: String, done: Boolean): Int = DataBaseFactory.dbQuery {
        TodoTable.update ({TodoTable.id.eq(id)}){ todo ->
            todo[TodoTable.done] = done
            todo[TodoTable.todo] = todoData

        }
    }

    private fun rowToTodo(row: ResultRow?): Todo?{
        if(row == null){
            return null
        }

        return Todo(

            id = row[TodoTable.id],
            userId = row[TodoTable.userId],
            todo = row[TodoTable.todo],
            done = row[TodoTable.done]

        )

    }

}