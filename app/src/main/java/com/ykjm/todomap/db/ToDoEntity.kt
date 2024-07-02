package com.ykjm.todomap.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

@Entity
class ToDoEntity(
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "date")
    val date: String,

    @ColumnInfo(name = "category")
    val category: String,

    @ColumnInfo(name = "memo")
    val memo: String,

    @ColumnInfo(name = "map")
    val map: String,

    @ColumnInfo(name = "emoticon")
    val emoticon: String,

    @ColumnInfo(name = "isChecked")
    var isChecked: Boolean = false

) {
    override fun toString(): String {
        return "ToDoEntity(id=$id, title='$title', date='$date', " +
                "category='$category', memo='$memo',  map='$map', " +
                "emoticon='$emoticon', isChecked=$isChecked)"
    }

}