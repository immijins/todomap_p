package com.ykjm.todomap.db

import android.app.Application
import androidx.room.Room
import com.ykjm.todomap.todomap.repository.TodoRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MyApp : Application() {

    companion object {
        lateinit var database: AppDatabase
            private set

        lateinit var todoRepository: TodoRepository
            private set
    }

    override fun onCreate() {
        super.onCreate()

        // Initialize the database
        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "db_todo"
        ).build()

        // Initialize the repository
        todoRepository = TodoRepository(database.todoDao())
    }
}