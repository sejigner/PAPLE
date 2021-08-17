package com.sejigner.closest.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "first_paper_planes")
data class FirstPaperPlanes(

    @ColumnInfo(name = "fromId")
    val fromId : String?,

    @ColumnInfo(name = "message")
    val message : String?,

    @ColumnInfo(name = "flightDistance")
    val flightDistance : Double,

    @ColumnInfo(name = "timestamp")
    val timestamp : Long
) {
    @PrimaryKey (autoGenerate = true)
    var id: Int? = null
}
