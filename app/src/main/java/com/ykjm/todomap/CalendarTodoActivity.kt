package com.ykjm.todomap

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.KeyEvent
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vanniktech.emoji.EmojiManager
import com.vanniktech.emoji.EmojiPopup
import com.vanniktech.emoji.google.GoogleEmojiProvider
import com.ykjm.todomap.R
import com.ykjm.todomap.adapter.TodoCalendarListAdapter
import com.ykjm.todomap.db.AppDatabase
import com.ykjm.todomap.repository.TodoRepository
import com.ykjm.todomap.todomap.SharedPreferencesHelper
import com.ykjm.todomap.viewmodels.TodoViewModel
import com.ykjm.todomap.viewmodels.TodoViewModelFactory
import java.text.SimpleDateFormat
import java.util.*

class CalendarTodoActivity : AppCompatActivity() {

    private lateinit var dateTextView: TextView
    private lateinit var todoViewModel: TodoViewModel
    private lateinit var backButton: ImageButton
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TodoCalendarListAdapter
    private lateinit var emoButton: ImageButton
    private lateinit var emojiEditText: EditText
    private lateinit var emojiPopup: EmojiPopup
    private lateinit var dateButton: ImageButton

    private var selectedDate: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.calendar_todo)

        dateTextView = findViewById(R.id.dateTextView)
        backButton = findViewById(R.id.backButton)
        recyclerView = findViewById(R.id.todoList)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = TodoCalendarListAdapter(emptyList())
        recyclerView.adapter = adapter
        emoButton = findViewById(R.id.emoButton)
        emojiEditText = findViewById(R.id.emojiEditText)
        dateButton = findViewById(R.id.dateButton)

        selectedDate = intent.getLongExtra("SELECTED_DATE", 0)
        displaySelectedDate(selectedDate)

        // ViewModel 초기화
        val viewModelFactory = TodoViewModelFactory(
            application,
            TodoRepository(AppDatabase.getInstance(application)!!.todoDao())
        )
        todoViewModel = ViewModelProvider(this, viewModelFactory).get(TodoViewModel::class.java)

        // 날짜에 따른 Todo 목록 관찰
        observeTodosByDate(selectedDate)

        // 저장된 이모티콘 불러오기
        val savedEmoji = SharedPreferencesHelper.getEmojiForDate(this, getFormattedDate(selectedDate))
        emojiEditText.setText(savedEmoji)

        backButton.setOnClickListener {
            finish()
        }

        emoButton.setOnClickListener {
            EmojiManager.install(GoogleEmojiProvider())

            // EmojiPopup 설정
            emojiPopup = EmojiPopup.Builder.fromRootView(findViewById(android.R.id.content))
                .setOnEmojiBackspaceClickListener {
                    emojiEditText.dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL))
                }
                .build(emojiEditText)

            // EmojiPopup 토글
            if (!emojiPopup.isShowing) {
                emojiPopup.toggle()
            }
        }

        dateButton.setOnClickListener {
            showDatePickerDialog()
        }
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = selectedDate

        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            R.style.CreateTheme,
            { _, selectedYear, selectedMonth, selectedDay ->
                val newDate = Calendar.getInstance()
                newDate.set(selectedYear, selectedMonth, selectedDay)
                selectedDate = newDate.timeInMillis
                displaySelectedDate(selectedDate)
                observeTodosByDate(selectedDate)
                val savedEmoji = SharedPreferencesHelper.getEmojiForDate(this, getFormattedDate(selectedDate))
                emojiEditText.setText(savedEmoji)
            }, year, month, day
        )

        datePickerDialog.show()

        datePickerDialog.getButton(DatePickerDialog.BUTTON_POSITIVE)
            .setTextColor(ContextCompat.getColor(this, R.color.color_point))
        datePickerDialog.getButton(DatePickerDialog.BUTTON_NEGATIVE)
            .setTextColor(ContextCompat.getColor(this, R.color.color_txt))
    }


    private fun displaySelectedDate(selectedDate: Long) {
        val formattedDate = getFormattedDate(selectedDate)
        dateTextView.text = formattedDate
    }

    private fun getFormattedDate(dateMillis: Long): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(dateMillis))
    }

    private fun observeTodosByDate(selectedDate: Long) {
        val formattedDate = getFormattedDate(selectedDate)

        todoViewModel.getTodosByDate(formattedDate).observe(this, Observer { todos ->
            todos?.let {
                adapter.updateTodos(it)
            }
        })
    }

    override fun onPause() {
        super.onPause()
        // 이모티콘 저장
        val emojiText = emojiEditText.text.toString()
        SharedPreferencesHelper.saveEmojiForDate(this, getFormattedDate(selectedDate), emojiText)
    }
}