package com.ykjm.todomap.adapter

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.ykjm.todomap.R
import com.ykjm.todomap.todomap.db.ToDoEntity

class TodoCalendarListAdapter(private var todos: List<ToDoEntity>) :
    RecyclerView.Adapter<TodoCalendarListAdapter.ToDoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ToDoViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_todo, parent, false)
        return ToDoViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ToDoViewHolder, position: Int) {
        val currentTodo = todos[position]
        holder.bind(currentTodo)
    }

    override fun getItemCount(): Int {
        return todos.size
    }

    fun updateTodos(newTodos: List<ToDoEntity>) {
        todos = newTodos
        notifyDataSetChanged()
    }

    inner class ToDoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.todo)
        private val categoryBg: View = itemView.findViewById(R.id.category_bg)

        fun bind(todo: ToDoEntity) {
            titleTextView.text = todo.title

            // 카테고리 색상 설정
            val context = itemView.context
            val categoryColor = when (todo.category) {
                "기타" -> ContextCompat.getColor(context, R.color.etc)
                "중요" -> ContextCompat.getColor(context, R.color.important)
                "쇼핑" -> ContextCompat.getColor(context, R.color.shopping)
                "공부" -> ContextCompat.getColor(context, R.color.study)
                "운동" -> ContextCompat.getColor(context, R.color.workout)
                "예약" -> ContextCompat.getColor(context, R.color.reserve)
                "업무" -> ContextCompat.getColor(context, R.color.work)
                "여행" -> ContextCompat.getColor(context, R.color.travel)
                "일정" -> ContextCompat.getColor(context, R.color.schedule)
                else -> ContextCompat.getColor(context, android.R.color.transparent)
            }
            categoryBg.backgroundTintList = ColorStateList.valueOf(categoryColor)
        }
    }
}