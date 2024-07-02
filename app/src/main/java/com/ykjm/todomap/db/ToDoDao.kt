package com.ykjm.todomap.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.ykjm.todomap.todomap.db.ToDoEntity

@Dao
interface ToDoDao {

    @Query("SELECT * FROM ToDoEntity")
    fun getAllLiveData(): LiveData<List<ToDoEntity>> // LiveData로 변경

    @Query("SELECT * FROM ToDoEntity WHERE id = :id")
    fun getTodoById(id: Int): LiveData<ToDoEntity> // LiveData로 변경

    @Query("SELECT * FROM ToDoEntity WHERE date = :date")
    fun getTodosByDate(date: String): LiveData<List<ToDoEntity>> // 날짜에 따른 ToDoEntity 가져오기

    @Insert
    fun insertTodo(todo: ToDoEntity)

    @Delete
    fun deleteTodo(todo: ToDoEntity)

    @Update
    fun updateTodo(todo: ToDoEntity)

}