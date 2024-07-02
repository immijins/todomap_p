package com.ykjm.todomap.adapter

import android.content.res.ColorStateList
import android.graphics.Paint
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.ykjm.todomap.R
import com.ykjm.todomap.databinding.ItemTodoBinding
import com.ykjm.todomap.todomap.OnItemClickListener
import com.ykjm.todomap.todomap.db.ToDoEntity

class TodoRecyclerViewAdapter(
    private var todoList: ArrayList<ToDoEntity>,
    private val listener: OnItemClickListener,
    private val onDeleteClick: (ToDoEntity) -> Unit,
    private val onCheckedChange: (ToDoEntity) -> Unit
) : RecyclerView.Adapter<TodoRecyclerViewAdapter.MyViewHolder>() {

    inner class MyViewHolder(private val binding: ItemTodoBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(todoData: ToDoEntity) {
            // 할 일의 제목 설정
            binding.todo.text = todoData.title

            // 체크 상태에 따라 UI 업데이트
            binding.btnCheckbox.setOnCheckedChangeListener(null) // 초기화 필요
            binding.btnCheckbox.isChecked = todoData.isChecked // 체크 상태 설정

            binding.btnCheckbox.setOnCheckedChangeListener { buttonView, isChecked ->
                // 데이터 모델 업데이트
                todoData.isChecked = isChecked
                onCheckedChange(todoData) // 체크 상태 변경 콜백 호출

                // 취소선 설정
                if (isChecked) {
                    binding.todo.paintFlags = binding.todo.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                } else {
                    binding.todo.paintFlags = binding.todo.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                }
                // UI 업데이트
                binding.todo.requestLayout() // 취소선이 올바르게 표시될 수 있도록 레이아웃 요청
            }

            // 취소선 초기 설정
            if (todoData.isChecked) {
                binding.todo.paintFlags = binding.todo.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            } else {
                binding.todo.paintFlags = binding.todo.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            }


            // 중요도에 따라 배경색 설정
            when (todoData.category) {
                "기타" -> binding.categoryBg.backgroundTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(
                        binding.root.context,
                        R.color.etc
                    )
                )
                "중요" -> binding.categoryBg.backgroundTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(
                        binding.root.context,
                        R.color.important
                    )
                )
                "쇼핑" -> binding.categoryBg.backgroundTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(
                        binding.root.context,
                        R.color.shopping
                    )
                )
                "공부" -> binding.categoryBg.backgroundTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(
                        binding.root.context,
                        R.color.study
                    )
                )
                "운동" -> binding.categoryBg.backgroundTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(
                        binding.root.context,
                        R.color.workout
                    )
                )
                "예약" -> binding.categoryBg.backgroundTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(
                        binding.root.context,
                        R.color.reserve
                    )
                )
                "업무" -> binding.categoryBg.backgroundTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(
                        binding.root.context,
                        R.color.work
                    )
                )
                "여행" -> binding.categoryBg.backgroundTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(
                        binding.root.context,
                        R.color.travel
                    )
                )
                "일정" -> binding.categoryBg.backgroundTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(
                        binding.root.context,
                        R.color.schedule
                    )
                )
                else -> binding.categoryBg.backgroundTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(
                        binding.root.context,
                        android.R.color.transparent
                    )
                )
            }

            // 아이템 클릭 리스너 설정
            binding.root.setOnClickListener {
                listener.onClick(adapterPosition)
                true // true로 반환하여 이벤트 소비를 명시적으로 표시
            }

            // 휴지통 아이콘 클릭 리스너 설정
            binding.deleteIcon.setOnClickListener {
                onDeleteClick(todoData)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        // item_todo.xml 뷰 바인딩 객체 생성
        val binding: ItemTodoBinding =
            ItemTodoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(todoList[position])
    }

    override fun getItemCount(): Int {
        // 리사이클러뷰 아이템 개수는 할 일 리스트 크기
        return todoList.size
    }

    fun updateData(newList: List<ToDoEntity>) {
        // RecyclerView의 데이터를 새로운 리스트로 교체하고, 변경 사항을 RecyclerView에 알리는 역할
        todoList.clear()
        todoList.addAll(newList)
        notifyDataSetChanged()
    }

    // 특정 위치의 할 일 ID 반환
    fun getTodoId(position: Int): Int {
        return todoList[position].id ?: -1 // 만약 ID가 null이면 -1을 반환하도록 처리
    }
}