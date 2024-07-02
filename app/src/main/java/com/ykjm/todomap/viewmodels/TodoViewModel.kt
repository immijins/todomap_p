package com.ykjm.todomap.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.ykjm.todomap.db.ToDoEntity
import com.ykjm.todomap.repository.TodoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TodoViewModel(application: Application, private val repository: TodoRepository) :
    AndroidViewModel(application) {
    // 비동기적으로 데이터를 처리

    // 사용할 데이터베이스 메서드들을 repository에서 직접 호출
    fun getAllTodos(): LiveData<List<ToDoEntity>> {
        return repository.getAllTodos()
    }

    fun insertTodo(todo: ToDoEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insert(todo)
        }
    }

    fun deleteTodo(todo: ToDoEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.delete(todo)
        }
    }

    fun updateTodo(todo: ToDoEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.update(todo)
        }
    }

    private val _todo = MutableLiveData<ToDoEntity>()
    val todo: LiveData<ToDoEntity>
        get() = _todo

    fun getTodoById(id: Int) {
        viewModelScope.launch {
            repository.getTodoById(id).observeForever { todo ->
                _todo.value = todo
            }
        }
    }

    fun getTodosByDate(date: String): LiveData<List<ToDoEntity>> {
        return repository.getTodosByDate(date)
    }

}