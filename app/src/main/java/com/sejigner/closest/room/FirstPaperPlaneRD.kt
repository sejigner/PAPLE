package com.sejigner.closest.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "firstPaperPlaneTable")
data class FirstPaperPlaneRD(
    @PrimaryKey
    val userId: String?,

    @ColumnInfo(name = "fromId")
    val fromId : String?,

    @ColumnInfo(name = "message")
    val message : String?,

    @ColumnInfo(name = "flightDistance")
    val flightDistance : Double,

    @ColumnInfo(name = "timestamp")
    val timestamp : Long
)
