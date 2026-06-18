package com.cubicserenity.esobuildmanager.data.local.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(tableName = "builds")
data class BuildEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String = "New Build",
    val esoClass: String = "",
    val subclass1: String = "",
    val subclass2: String = "",
    val role: String = "",
    val content: String = "",
    val gamePatch: String = "U50",
    val source: String = "",
    val mundusStone: String = "",
    val foodBuff: String = "",
    val attributeHealth: Int = 0,
    val attributeMagicka: Int = 0,
    val attributeStamina: Int = 64,
    val championPoints: String = "",
    val cpSlotsJson: String = "[]",
    val classMasteriesJson: String = "[]",
    val notes: String = "",
    val updatedAt: String = "",
)

@Entity(
    tableName = "skills",
    foreignKeys = [ForeignKey(
        entity = BuildEntity::class,
        parentColumns = ["id"],
        childColumns = ["buildId"],
        onDelete = ForeignKey.CASCADE,
    )],
)
data class SkillEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val buildId: Long = 0,
    val bar: Int = 0,
    val slot: Int = 0,
    val name: String = "",
)

@Entity(
    tableName = "gear",
    foreignKeys = [ForeignKey(
        entity = BuildEntity::class,
        parentColumns = ["id"],
        childColumns = ["buildId"],
        onDelete = ForeignKey.CASCADE,
    )],
)
data class GearEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val buildId: Long = 0,
    val slot: String = "",
    val setName: String = "",
    val weight: String = "",
    val trait: String = "",
    val enchant: String = "",
    val quality: String = "Epic",
)

data class BuildWithDetails(
    @Embedded val build: BuildEntity,
    @Relation(parentColumn = "id", entityColumn = "buildId")
    val skills: List<SkillEntity>,
    @Relation(parentColumn = "id", entityColumn = "buildId")
    val gear: List<GearEntity>,
)
