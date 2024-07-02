package com.ykjm.todomap

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.preference.PreferenceManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.ykjm.todomap.databinding.FragmentSystemBinding
import com.ykjm.todomap.db.MyApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TodoSystemFragment : Fragment() {
    private var _binding: FragmentSystemBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSystemBinding.inflate(inflater, container, false)
        val view = binding.root

        // 할 일 목록 버튼 클릭
        binding.listButton.setOnClickListener {
            val bottomNavigationView = activity?.findViewById<BottomNavigationView>(R.id.bottomNavigation)
            bottomNavigationView?.selectedItemId = R.id.list_bottom
            parentFragmentManager.commit {
                replace(R.id.fragment_bottom_container, TodoListFragment())
            }
        }

        // 달력 버튼 클릭
        binding.calButton.setOnClickListener {
            val bottomNavigationView = activity?.findViewById<BottomNavigationView>(R.id.bottomNavigation)
            bottomNavigationView?.selectedItemId = R.id.cal_bottom
            parentFragmentManager.commit {
                replace(R.id.fragment_bottom_container, TodoCalendarFragment())
            }
        }

        // 위치 버튼 클릭
        binding.mapButton.setOnClickListener {
            val bottomNavigationView = activity?.findViewById<BottomNavigationView>(R.id.bottomNavigation)
            bottomNavigationView?.selectedItemId = R.id.map_bottom
            parentFragmentManager.commit {
                replace(R.id.fragment_bottom_container, MapFragment())
            }
        }

        // 카테고리 버튼 클릭
        binding.cateButton.setOnClickListener {
            val intent = Intent(activity, CategoryActivity::class.java)
            startActivity(intent)
        }

        // 앱 테마 설정
        binding.themaButton.setOnClickListener {
            val themeOptions = arrayOf("기본 모드", "다크 모드")
            AlertDialog.Builder(requireContext())
                .setTitle("테마 선택")
                .setItems(themeOptions) { dialog, which ->
                    val selectedTheme = if (which == 0) "default" else "dark"
                    val preferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
                    preferences.edit().putString("theme", selectedTheme).apply()
                    // 테마 변경 후 Activity 재시작
                    AppCompatDelegate.setDefaultNightMode(
                        if (selectedTheme == "dark") AppCompatDelegate.MODE_NIGHT_YES
                        else AppCompatDelegate.MODE_NIGHT_NO
                    )
                    // 액티비티 재시작
                    requireActivity().recreate()
                }
                .show()
        }

        // 알림 설정
        binding.alermButton.setOnClickListener {
            // 시스템 알림 설정 화면으로 이동
            val intent = Intent().apply {
                action = "android.settings.APP_NOTIFICATION_SETTINGS"
                putExtra("app_package", requireContext().packageName)
                putExtra("app_uid", requireContext().applicationInfo.uid)
                // for Android 8 and above
                putExtra("android.provider.extra.APP_PACKAGE", requireContext().packageName)
            }
            startActivity(intent)
        }

        // 초기화
        binding.resetButton.setOnClickListener {
            showResetConfirmationDialog()
        }


        // 앱 정보
        binding.infoButton.setOnClickListener {
            val intent = Intent(activity, AppInfo::class.java)
            startActivity(intent)
        }

        return view
    }

    private fun showResetConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("초기화 확인")
            .setMessage("정말로 앱을 초기화하시겠습니까? 이 작업은 되돌릴 수 없습니다.")
            .setPositiveButton("예") { _, _ ->
                resetApp()
            }
            .setNegativeButton("아니오", null)
            .show()
    }

    private fun resetApp() {
        // SharedPreferences 초기화
        val preferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        preferences.edit().clear().apply()

        // 데이터베이스 파일 삭제 및 재생성
        CoroutineScope(Dispatchers.IO).launch {
            val dbFile = requireContext().getDatabasePath("db_todo")
            if (dbFile.exists()) {
                dbFile.delete()
            }

            // 재생성
            val database = MyApp.database
            database.clearAllTables() // 필요한 경우 테이블 초기화

            withContext(Dispatchers.Main) {
                Toast.makeText(requireContext(), "앱이 초기화되었습니다.", Toast.LENGTH_SHORT).show()
                // 앱 재시작
                val intent = requireActivity().baseContext.packageManager
                    .getLaunchIntentForPackage(requireActivity().baseContext.packageName)
                intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}