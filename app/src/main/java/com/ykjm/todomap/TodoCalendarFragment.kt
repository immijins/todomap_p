package com.ykjm.todomap

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.prolificinteractive.materialcalendarview.*
import com.prolificinteractive.materialcalendarview.spans.DotSpan
import com.ykjm.todomap.db.AppDatabase
import com.ykjm.todomap.repository.TodoRepository
import com.ykjm.todomap.viewmodels.TodoViewModel
import com.ykjm.todomap.viewmodels.TodoViewModelFactory
import java.text.SimpleDateFormat
import java.util.*

class TodoCalendarFragment : Fragment() {
    private lateinit var calendarView: MaterialCalendarView
    private var selectedDate: Long = 0
    private lateinit var todoViewModel: TodoViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.calendar, container, false)
        calendarView = view.findViewById(R.id.mcCalendarView)

        // ViewModel 초기화
        val viewModelFactory = TodoViewModelFactory(requireActivity().application, TodoRepository(AppDatabase.getInstance(requireActivity().application)!!.todoDao()))
        todoViewModel = ViewModelProvider(this, viewModelFactory).get(TodoViewModel::class.java)

        // 날짜 클릭 리스너 설정
        calendarView.setOnDateChangedListener(OnDateSelectedListener { widget, date, selected ->
            selectedDate = date.date.time
            navigateToCalendarTodoActivity()
        })

        // ToDo 데이터를 관찰하고 달력에 점을 표시
        observeTodos()

        return view
    }

    private fun navigateToCalendarTodoActivity() {
        val intent = Intent(requireContext(), CalendarTodoActivity::class.java).apply {
            putExtra("SELECTED_DATE", selectedDate)
        }
        startActivity(intent)
    }

    private fun observeTodos() {
        todoViewModel.getAllTodos().observe(viewLifecycleOwner, Observer { todos ->
            todos?.let {
                val datesWithTodos = mutableSetOf<CalendarDay>()
                it.forEach { todo ->
                    val date = parseDate(todo.date)
                    datesWithTodos.add(CalendarDay.from(date))
                }
                calendarView.addDecorator(EventDecorator(resources.getColor(R.color.purple_200), datesWithTodos))
            }
        })
    }

    private fun parseDate(dateString: String): Date {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateString) ?: Date()
    }
}

class EventDecorator(private val color: Int, dates: Collection<CalendarDay>) : DayViewDecorator {
    private val dates: HashSet<CalendarDay> = HashSet(dates)

    override fun shouldDecorate(day: CalendarDay?): Boolean {
        return dates.contains(day)
    }

    override fun decorate(view: DayViewFacade?) {
        view?.addSpan(DotSpan(5F, color))
    }
}