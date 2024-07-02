package com.ykjm.todomap


import android.content.Intent
import android.graphics.Canvas
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.ykjm.todomap.adapter.TodoRecyclerViewAdapter
import com.ykjm.todomap.databinding.TodoListBinding
import com.ykjm.todomap.db.MyApp
import com.ykjm.todomap.viewmodels.TodoViewModel
import com.ykjm.todomap.viewmodels.TodoViewModelFactory
import java.text.SimpleDateFormat
import java.util.*
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.ykjm.todomap.db.ToDoEntity
import java.text.ParseException

enum class ButtonState {
    GONE,
    DELETE_VISIBLE
}

class TodoListFragment : Fragment(), OnItemClickListener {

    private var _binding: TodoListBinding? = null
    private val binding get() = _binding!!

    private lateinit var todoViewModel: TodoViewModel
    private lateinit var adapter: TodoRecyclerViewAdapter

    private var isCompletionDialogShown = false // 완료 알림이 표시된 여부를 저장하는 변수
    private var buttonShowedState: ButtonState = ButtonState.GONE

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = TodoListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val myApp = requireActivity().application as MyApp
        val repository = MyApp.todoRepository
        val factory = TodoViewModelFactory(myApp, repository)
        todoViewModel = ViewModelProvider(this, factory).get(TodoViewModel::class.java)

        /*
        binding.bottomNavigation.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.add_bottom -> {
                    val addTodoFragment = AddTodoFragment()
                    addTodoFragment.show(parentFragmentManager, "AddTodoFragment")
                    true
                }
                else -> false
            }
        }*/

        // 오늘 날짜 연동
        val currentDate = SimpleDateFormat("MM.dd", Locale.getDefault()).format(Date())
        binding.todoDate.text = currentDate

        setupRecyclerView()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        // 어댑터 초기화
        adapter = TodoRecyclerViewAdapter(arrayListOf(), this,
            { todo ->
                todoViewModel.deleteTodo(todo)
                Snackbar.make(binding.recyclerView, "삭제되었습니다.", Snackbar.LENGTH_LONG)
                    .setAction("실행 취소") {
                        todoViewModel.insertTodo(todo)
                    }.show()
            },
            { todo -> // 추가: 체크 상태 변경 콜백 처리
                todoViewModel.updateTodo(todo) // Room 데이터베이스에 변경된 항목 저장
            }
        )
        // 어댑터 및 레이아웃 매니저 설정
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(
            0, ItemTouchHelper.LEFT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                // 항목 이동을 처리하지 않기 때문에 false를 반환
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                // 항목이 스와이프될 때 호출
                if (buttonShowedState != ButtonState.DELETE_VISIBLE) {
                    buttonShowedState = ButtonState.DELETE_VISIBLE
                    adapter.notifyItemChanged(viewHolder.adapterPosition)
                } else {
                    buttonShowedState = ButtonState.GONE
                    adapter.notifyItemChanged(viewHolder.adapterPosition)
                }
            }

            // 항목이 그려질 때 호출
            override fun onChildDraw(
                c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
                dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean
            ) {
                val itemView = viewHolder.itemView
                val deleteIcon = itemView.findViewById<ImageView>(R.id.deleteIcon)
                val itemContent = itemView.findViewById<ConstraintLayout>(R.id.itemContent)

                // 스와이프 중일 때 휴지통 아이콘을 표시하고 이동 효과를 줌
                if (buttonShowedState == ButtonState.DELETE_VISIBLE) {
                    deleteIcon.visibility = View.VISIBLE
                    val translationX = if (dX < -200) -200f else dX
                    itemContent.translationX = translationX
                } else {
                    deleteIcon.visibility = View.GONE
                    itemContent.translationX = dX
                }

                super.onChildDraw(
                    c,
                    recyclerView,
                    viewHolder,
                    dX,
                    dY,
                    actionState,
                    isCurrentlyActive
                )
            }
        }

        // ItemTouchHelper 초기화 및 연결
        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(binding.recyclerView)
    }

    private fun observeViewModel() {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val currentDate = dateFormat.format(Date())

        todoViewModel.getAllTodos().observe(viewLifecycleOwner, Observer { todos ->
            todos?.let {
                // 오늘 날짜의 항목만 필터링하여 어댑터에 전달
                val filteredTodos = it.filter { todo ->
                    try {
                        val todoDate = dateFormat.parse(todo.date)
                        val today = dateFormat.parse(currentDate)
                        todoDate != null && today != null && todoDate == today
                    } catch (e: ParseException) {
                        false // 날짜 형식 변환 오류 시 false 반환
                    }
                }
                adapter.updateData(filteredTodos)
                binding.noneMessage.visibility =
                    if (filteredTodos.isEmpty()) View.VISIBLE else View.GONE

                checkAllCompleted(filteredTodos) // 완료 체크 확인
            }
        })
    }

    private fun checkAllCompleted(todoList: List<ToDoEntity>) {
        // 모든 항목이 체크된 상태인지 확인
        val allCompleted = todoList.isNotEmpty() && todoList.all { it.isChecked }

        // 한 번만 알림창 뜨도록 처리
        if (allCompleted) {
            showCompletionDialog()
        }
    }

    private fun showCompletionDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder
            .setMessage("오늘의 할 일이 모두 완료되었습니다!")
            .setPositiveButton("확인") { _, _ ->
                // 확인 버튼을 누르면 완료 알림이 표시된 상태를 초기화
                isCompletionDialogShown = false
            }
            .show()
    }


    override fun onClick(position: Int) {
        val builder = AlertDialog.Builder(requireContext())
        builder
            .setMessage("상세화면으로 이동하시겠습니까?")
            .setNegativeButton("취소", null)
            .setPositiveButton("네") { dialog, which ->
                val intent = Intent(requireContext(), TodoDetailActivity::class.java)
                intent.putExtra("TODO_ID", adapter.getTodoId(position))
                startActivity(intent)
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}