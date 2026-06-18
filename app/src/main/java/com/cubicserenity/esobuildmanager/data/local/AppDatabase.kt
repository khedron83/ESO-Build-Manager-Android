package com.cubicserenity.esobuildmanager.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.cubicserenity.esobuildmanager.data.local.dao.BuildDao
import com.cubicserenity.esobuildmanager.data.local.entity.BuildEntity
import com.cubicserenity.esobuildmanager.data.local.entity.GearEntity
import com.cubicserenity.esobuildmanager.data.local.entity.SkillEntity

@Database(
    entities = [BuildEntity::class, SkillEntity::class, GearEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun buildDao(): BuildDao
}
