package com.cubicserenity.esobuildmanager.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.cubicserenity.esobuildmanager.data.local.entity.BuildEntity
import com.cubicserenity.esobuildmanager.data.local.entity.BuildWithDetails
import com.cubicserenity.esobuildmanager.data.local.entity.GearEntity
import com.cubicserenity.esobuildmanager.data.local.entity.SkillEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BuildDao {

    @Query("SELECT * FROM builds ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<BuildEntity>>

    @Transaction
    @Query("SELECT * FROM builds WHERE id = :id")
    suspend fun getBuildWithDetails(id: Long): BuildWithDetails?

    @Transaction
    @Query("SELECT * FROM builds ORDER BY updatedAt DESC")
    suspend fun getAllWithDetails(): List<BuildWithDetails>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBuild(build: BuildEntity): Long

    @Update
    suspend fun updateBuild(build: BuildEntity)

    @Delete
    suspend fun deleteBuild(build: BuildEntity)

    @Query("DELETE FROM skills WHERE buildId = :buildId")
    suspend fun deleteSkillsForBuild(buildId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSkills(skills: List<SkillEntity>)

    @Query("DELETE FROM gear WHERE buildId = :buildId")
    suspend fun deleteGearForBuild(buildId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGear(gear: List<GearEntity>)

    @Query("SELECT * FROM builds WHERE name = :name LIMIT 1")
    suspend fun findByName(name: String): BuildEntity?
}
