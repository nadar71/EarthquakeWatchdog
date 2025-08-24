package com.indiewalk.watchdog.earthquake.domain.model.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

// 1 row per Feature (earthquake).
// Linked to its feed snapshot by feedGenerated (FK to FeedSnapshotEntity.generated).
@Entity(
    tableName = "earthquakes",
    foreignKeys = [
        ForeignKey(
            entity = FeedSnapshotEntity::class,
            parentColumns = ["generated"],
            childColumns = ["feed_generated"],
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("feed_generated"),
        Index("time"),
        Index("mag"),
        Index("place"),
        Index(value = ["latitude", "longitude"]) // helps bbox queries
    ]
)
data class EQEntity(
    @PrimaryKey val id: String,                     // feature.properties.id
    @ColumnInfo(name = "feed_generated") val feedGenerated: Long?, // FK links to FeedSnapshotEntity.generated

    // properties
    val mag: Double?,
    val place: String?,
    val time: Long?,
    val updated: Long?,
    val tz: Int?,
    val url: String?,
    val detail: String?,
    val felt: Int?,
    val cdi: Double?,
    val mmi: Double?,
    val alert: String?,
    val status: String?,
    val tsunami: Int?,
    val sig: Int?,
    val net: String?,
    val code: String?,
    val ids: String?,
    val sources: String?,
    val types: String?,
    val nst: Int?,
    val dmin: Double?,
    val rms: Double?,
    val gap: Double?,
    val magType: String?,
    @ColumnInfo(name = "event_type") val eventType: String?, // properties.type

    // geometry (flattened)
    @ColumnInfo(name = "geometry_type", defaultValue = "'Point'")
    val geometryType: String = "Point",
    val longitude: Double?,
    val latitude: Double?,
    @ColumnInfo(name = "depth_km") val depthKm: Double?,

    // custom
    val distanceFromUser: Int?
)
