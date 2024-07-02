package com.ykjm.todomap

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.ykjm.todomap.databinding.FragmentMainBinding
import com.ykjm.todomap.todomap.MapFragment
import com.ykjm.todomap.todomap.TodoCalendarFragment
import com.ykjm.todomap.todomap.TodoListFragment
import com.ykjm.todomap.todomap.TodoSystemFragment


class MainFragment : Fragment() {
    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!
    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        val view = binding.root

        bottomNavigationView = binding.bottomNavigation
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.list_bottom -> {
                    // 목록 화면
                    childFragmentManager.beginTransaction()
                        .replace(R.id.fragment_bottom_container, TodoListFragment())
                        .commit()
                    true
                }
                R.id.cal_bottom -> {
                    // 달력 화면
                    childFragmentManager.beginTransaction()
                        .replace(R.id.fragment_bottom_container, TodoCalendarFragment())
                        .commit()
                    true
                }
                R.id.add_bottom -> {
                    // 추가 화면
                    AddTodoFragment().show(childFragmentManager, "AddTodoFragment")
                    true
                }
                R.id.map_bottom -> {
                    // 위치 화면
                    childFragmentManager.beginTransaction()
                        .replace(R.id.fragment_bottom_container, MapFragment())
                        .commit()
                    true
                }
                R.id.sys_bottom -> {
                    // 설정 화면
                    childFragmentManager.beginTransaction()
                        .replace(R.id.fragment_bottom_container, TodoSystemFragment())
                        .commit()
                    true
                }
                else -> false
            }
        }
        // 기본 페이지
        if (savedInstanceState == null) {
            childFragmentManager.beginTransaction()
                .replace(R.id.fragment_bottom_container, TodoListFragment())
                .commit()
        }

        return view
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}