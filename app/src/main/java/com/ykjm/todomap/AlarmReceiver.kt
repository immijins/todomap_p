package com.ykjm.todomap

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Observer
import com.ykjm.todomap.db.MyApp
import com.ykjm.todomap.db.ToDoEntity
import com.ykjm.todomap.todomap.TodoDetailActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val todoId = intent.getIntExtra("TODO_ID", -1)

        if (todoId != -1) {
            val todoDao = MyApp.database.todoDao()

            // CoroutineScope를 사용하여 백그라운드 작업 수행
            CoroutineScope(Dispatchers.IO).launch {
                // LiveData를 관찰하고 처리하기
                val todoLiveData = todoDao.getTodoById(todoId)

                // 결과를 처리하기 위해 Dispatchers.Main 사용
                withContext(Dispatchers.Main) {
                    todoLiveData.observeForever(object : Observer<ToDoEntity?> {
                        override fun onChanged(todo: ToDoEntity?) {
                            todo?.let {
                                todoLiveData.removeObserver(this)

                                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    val channelId = "todo_channel"
                                    if (notificationManager.getNotificationChannel(channelId) == null) {
                                        val channel = NotificationChannel(
                                            channelId,
                                            "Todo Notifications",
                                            NotificationManager.IMPORTANCE_HIGH
                                        ).apply {
                                            description = "Todo 알림을 위한 채널"
                                            setShowBadge(true)
                                            enableVibration(true)
                                        }
                                        // 시스템에 채널 등록
                                        notificationManager.createNotificationChannel(channel)
                                    }
                                }

                                val notificationIntent = Intent(context, TodoDetailActivity::class.java).apply {
                                    putExtra("TODO_ID", todo.id ?: -1)
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                }

                                val pendingIntent = PendingIntent.getActivity(
                                    context,
                                    todo.id ?: 0,
                                    notificationIntent,
                                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                                )

                                val notification = NotificationCompat.Builder(context, "todo_channel")
                                    .setSmallIcon(R.drawable.icon_add_alarm)
                                    .setContentTitle("오늘의 할일")
                                    .setContentText(todo.title)
                                    .setContentIntent(pendingIntent)
                                    .setAutoCancel(true)  // 알림 클릭 시 사라지도록 설정
                                    .build()

                                notificationManager.notify(todo.id ?: 0, notification)
                            }
                        }
                    })
                }
            }
        }
    }
}