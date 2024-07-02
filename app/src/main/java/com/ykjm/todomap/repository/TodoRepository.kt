package com.ykjm.todomap.repository

import androidx.lifecycle.LiveData
import com.ykjm.todomap.db.ToDoDao
import com.ykjm.todomap.db.ToDoEntity

class TodoRepository(private val todoDao: ToDoDao) {  // 데이터베이스와 통신하는 중개자 역할

    fun getAllTodos(): LiveData<List<ToDoEntity>> {
        return todoDao.getAllLiveData()
    }

    suspend fun insert(todo: ToDoEntity) {
        todoDao.insertTodo(todo)
    }

    suspend fun delete(todo: ToDoEntity) {
        todoDao.deleteTodo(todo)
    }

    suspend fun update(todo: ToDoEntity) {
        todoDao.updateTodo(todo)
    }

    fun getTodoById(id: Int): LiveData<ToDoEntity> {
        return todoDao.getTodoById(id)
    }

    // 주어진 날짜에 해당하는 ToDoEntity 목록을 LiveData<List<ToDoEntity>>로 가져오는 메서드
    fun getTodosByDate(date: String): LiveData<List<ToDoEntity>> {
        return todoDao.getTodosByDate(date)
    }
}