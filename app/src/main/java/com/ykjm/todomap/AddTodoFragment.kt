package com.ykjm.todomap

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.ykjm.todomap.databinding.ActivityAddTodoBinding
import com.ykjm.todomap.db.MyApp
import com.ykjm.todomap.db.ToDoEntity
import com.ykjm.todomap.viewmodels.TodoViewModel
import com.ykjm.todomap.viewmodels.TodoViewModelFactory
import java.text.SimpleDateFormat
import java.util.*

class AddTodoFragment : DialogFragment() {
    private var _binding: ActivityAddTodoBinding? = null
    private val binding get() = _binding!!

    private lateinit var todoViewModel: TodoViewModel
    private var selectedDate: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = ActivityAddTodoBinding.inflate(inflater, container, false)
        val view = binding.root

        // ViewModel 초기화
        val application = requireActivity().application as MyApp
        val repository = MyApp.todoRepository
        val factory = TodoViewModelFactory(application, repository)
        todoViewModel = ViewModelProvider(this, factory).get(TodoViewModel::class.java)

        // Spinner 어댑터 설정
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.category,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.addCate.adapter = adapter
        }

        // 오늘 날짜로 초기화
        setDateToToday()

        // 날짜 선택
        binding.addCal.setOnClickListener {
            showDatePicker()
        }

        // 닫기 버튼 선택
        binding.btnClose.setOnClickListener {
            dismiss()
        }

        // 추가 버튼 선택
        binding.addTodo.setOnClickListener {
            val todoText = binding.addText.text.toString()
            val category = binding.addCate.selectedItem.toString()
            val memo = "" // 메모 초기값
            val map = "" // 위치 초기값
            val emoticon = "" // 이모티콘 초기값

            val categoryId = when (category) {
                "기타" -> 1
                "중요" -> 2
                "쇼핑" -> 3
                "공부" -> 4
                "운동" -> 5
                "예약" -> 6
                "업무" -> 7
                "여행" -> 8
                "일정" -> 9
                else -> -1
            }

            if (todoText.isBlank() || selectedDate == null || categoryId == -1) {
                Toast.makeText(requireContext(), "할 일, 카테고리, 날짜를 모두 선택하세요.", Toast.LENGTH_SHORT).show()
            }
            else {
                val todoEntity = ToDoEntity(null, todoText, selectedDate!!, category, memo, map, emoticon, isChecked=false)
                todoViewModel.insertTodo(todoEntity)
                //Toast.makeText(context, "추가되었습니다: $todoText", Toast.LENGTH_SHORT).show()
                Toast.makeText(context, "추가되었습니다!", Toast.LENGTH_SHORT).show()
                binding.addText.text.clear()
                dismiss()
            }
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // 날짜 선택 다이얼로그 표시
    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            R.style.CreateTheme,
            { _, year, month, dayOfMonth ->
                // 선택한 날짜를 문자열로 변환하여 할당
                selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth)
                // 선택한 날짜를 텍스트로 표시
                binding.selectedDateText.text = selectedDate
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }


    // 오늘 날짜로 초기화
    private fun setDateToToday() {
        val today = Calendar.getInstance().time
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        selectedDate = dateFormat.format(today)
        binding.selectedDateText.text = selectedDate // 초기 날짜를 텍스트로 표시
    }
}