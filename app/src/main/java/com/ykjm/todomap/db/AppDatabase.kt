package com.ykjm.todomap.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.ykjm.todomap.todomap.db.ToDoDao
import com.ykjm.todomap.todomap.db.ToDoEntity

@Database(entities = [ToDoEntity::class], version = 1,  exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun todoDao(): ToDoDao

    companion object { // 인스턴스의 중복 생성을 방지하기 위하여 싱글톤 패턴으로 생성
        @Volatile
        private var Instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase? {
            if (Instance == null) {
                synchronized(AppDatabase::class) {
                    Instance = Room.databaseBuilder(
                        context,
                        AppDatabase::class.java,
                        "db_todo"
                    ).build()
                }
            }
            return Instance
        }
    }
}