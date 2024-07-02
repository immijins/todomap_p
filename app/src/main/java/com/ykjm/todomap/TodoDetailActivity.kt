package com.ykjm.todomap

import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import com.vanniktech.emoji.EmojiManager
import com.vanniktech.emoji.EmojiPopup
import com.vanniktech.emoji.google.GoogleEmojiProvider
import com.ykjm.todomap.databinding.TodoDetailBinding
import com.ykjm.todomap.db.MyApp
import com.ykjm.todomap.db.ToDoEntity
import com.ykjm.todomap.viewmodels.TodoViewModel
import com.ykjm.todomap.viewmodels.TodoViewModelFactory
import java.util.*

class TodoDetailActivity : AppCompatActivity() {
    private lateinit var binding: TodoDetailBinding
    private lateinit var viewModel: TodoViewModel
    private var todoId: Int = -1
    private lateinit var emojiPopup: EmojiPopup

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = TodoDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // MyApp에서 TodoRepository와 TodoViewModelFactory를 가져오기
        val application = application as MyApp
        val repository = MyApp.todoRepository
        val factory = TodoViewModelFactory(application, repository)
        viewModel = ViewModelProvider(this, factory).get(TodoViewModel::class.java)

        // Intent에서 todoId 가져오기
        todoId = intent.getIntExtra("TODO_ID", -1)

        // todoId가 유효한 경우, ViewModel에서 해당 TodoEntity를 가져와 UI 업데이트
        if (todoId != -1) {
            viewModel.getTodoById(todoId)
            viewModel.todo.observe(this) { todo ->
                todo?.let {
                    updateUI(todo)
                }
            }
        } else {
            // todoId가 유효하지 않으면 예외 처리 또는 Activity 종료
            finish()
        }

        // Fragment 결과 리스너 설정
        supportFragmentManager.setFragmentResultListener("requestKey", this) { _, bundle ->
            val selectedLocationName = bundle.getString("selectedLocationName")
            selectedLocationName?.let {
                // 선택된 위치 정보가 유효한 경우 EditText에 설정
                binding.todoMap.setText(selectedLocationName)
            } ?: run {
                // 선택된 위치 정보가 유효하지 않은 경우 사용자에게 메시지를 표시
                Snackbar.make(binding.root, "유효하지 않은 위치입니다.", Snackbar.LENGTH_LONG).show()
                // 또는 기타 처리를 수행할 수 있습니다.
            }
        }

        initUI()

        // Fragment 백스택 변경 리스너 추가
        supportFragmentManager.addOnBackStackChangedListener {
            if (supportFragmentManager.backStackEntryCount == 0) {
                // BackStack이 비어있으면 버튼 보이기
                binding.btnSave.visibility = View.VISIBLE
            }
        }
    }

    private fun initUI() {

        binding.btnSave.setOnClickListener {
            saveTodoDetails()
        }

        binding.btnBack.setOnClickListener {
            onBackPressed()
        }

        binding.btnCategory.setOnClickListener {
            showCategoryList()
        }

        binding.btnCalendar.setOnClickListener {
            showDatePickerDialog()
        }

        binding.btnMap.setOnClickListener {
            // MapFragment를 추가하여 표시
            supportFragmentManager.commit {
                setReorderingAllowed(true)
                add(R.id.fragment_container, MapFragment::class.java, null)
                addToBackStack(null) // 백 스택에 추가하여 뒤로가기 시 제거 가능
            }

            binding.btnSave.visibility = View.GONE // MapFragment 열리면 버튼 숨기기
        }

        binding.btnAlarm.setOnClickListener {
            showAlarm(todoId)
        }

        binding.btnEmoticon.setOnClickListener {
            EmojiManager.install(GoogleEmojiProvider())

            // EmojiPopup 설정
            emojiPopup = EmojiPopup.Builder.fromRootView(binding.root)
                .setOnEmojiBackspaceClickListener {
                    // 이모지 입력란에서 백스페이스 이벤트 처리
                    binding.todoEmoticon.dispatchKeyEvent(
                        KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL)
                    )
                }
                .setOnEmojiClickListener { _, _ ->
                    // 이모지 선택 후 팝업 닫기
                    emojiPopup.dismiss()
                }
                .build(binding.todoEmoticon)

            // EmojiPopup 토글
            if (!emojiPopup.isShowing) {
                emojiPopup.toggle()
            }
        }
    }

    private fun updateUI(todo: ToDoEntity) {
        binding.apply {
            todoTitle.setText(todo.title)
            todoDate.setText(todo.date)
            todoCategory.setText(todo.category)
            todoMemo.setText(todo.memo)
            todoMap.setText(todo.map)
            todoEmoticon.setText(todo.emoticon)

            val context = root.context
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

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            R.style.CreateTheme,
            { _, selectedYear, selectedMonth, selectedDay ->
                val date = String.format(Locale.getDefault(), "%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
                binding.todoDate.setText(date)
            }, year, month, day
        )

        datePickerDialog.show()

        datePickerDialog.getButton(DatePickerDialog.BUTTON_POSITIVE)
            .setTextColor(ContextCompat.getColor(this, R.color.color_point))
        datePickerDialog.getButton(DatePickerDialog.BUTTON_NEGATIVE)
            .setTextColor(ContextCompat.getColor(this, R.color.color_txt))
    }

    private fun showCategoryList() {
        val categories = resources.getStringArray(R.array.category)
        val builder = AlertDialog.Builder(this)
        builder.setTitle("카테고리 선택")
        builder.setItems(categories) { dialog, which ->
            val selectedCategory = categories[which]
            binding.todoCategory.setText(selectedCategory)

            when (selectedCategory) {
                "기타" -> binding.categoryBg.backgroundTintList =
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.etc))
                "중요" -> binding.categoryBg.backgroundTintList =
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.important))
                "쇼핑" -> binding.categoryBg.backgroundTintList =
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.shopping))
                "공부" -> binding.categoryBg.backgroundTintList =
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.study))
                "운동" -> binding.categoryBg.backgroundTintList =
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.workout))
                "예약" -> binding.categoryBg.backgroundTintList =
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.reserve))
                "업무" -> binding.categoryBg.backgroundTintList =
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.work))
                "여행" -> binding.categoryBg.backgroundTintList =
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.travel))
                "일정" -> binding.categoryBg.backgroundTintList =
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.schedule))

                else -> binding.categoryBg.backgroundTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(
                        this,
                        android.R.color.transparent
                    )
                )
            }
        }
        builder.show()
    }

    private fun showAlarm(todoId: Int) {
        val currentTime = Calendar.getInstance()
        val timePickerDialog = TimePickerDialog(
            this,
            R.style.CreateTheme,
            { _, hourOfDay, minute ->
                val selectedTime = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, hourOfDay)
                    set(Calendar.MINUTE, minute)
                    set(Calendar.SECOND, 0)
                }

                // 현재 시간 이후의 시간을 선택하지 않은 경우
                if (selectedTime.before(currentTime)) {
                    Toast.makeText(this, "현재 시간 이후의 시간을 선택하세요.", Toast.LENGTH_SHORT).show()
                    return@TimePickerDialog
                }

                // 유효한 시간을 선택한 경우 알림 설정
                val alarmTime = String.format("%02d:%02d", hourOfDay, minute)
                Toast.makeText(this, "${alarmTime}에 알림이 설정되었습니다! ", Toast.LENGTH_SHORT).show()
                setAlarm(todoId, hourOfDay, minute)
            },
            currentTime.get(Calendar.HOUR_OF_DAY),
            currentTime.get(Calendar.MINUTE),
            true
        )
        timePickerDialog.show()
    }

    private fun setAlarm(todoId: Int, hourOfDay: Int, minute: Int) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, AlarmReceiver::class.java).apply {
            putExtra("TODO_ID", todoId)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            todoId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, hourOfDay)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
        }

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )
    }

    private fun saveTodoDetails() {
        val title = binding.todoTitle.text.toString()
        val date = binding.todoDate.text.toString()
        val category = binding.todoCategory.text.toString()
        val memo = binding.todoMemo.text.toString()
        val map = binding.todoMap.text.toString()
        val emoticon = binding.todoEmoticon.text.toString()

        if (todoId != -1) {
            val updatedTodo = ToDoEntity(todoId, title, date, category, memo, map, emoticon)
            viewModel.updateTodo(updatedTodo)
            Toast.makeText(this, "저장되었습니다.", Toast.LENGTH_SHORT).show()
            onBackPressed()
        }
    }
}