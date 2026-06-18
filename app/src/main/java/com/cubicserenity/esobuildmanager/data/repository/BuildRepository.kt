package com.cubicserenity.esobuildmanager.data.repository

import com.cubicserenity.esobuildmanager.data.local.dao.BuildDao
import com.cubicserenity.esobuildmanager.data.local.entity.BuildEntity
import com.cubicserenity.esobuildmanager.data.local.entity.BuildWithDetails
import com.cubicserenity.esobuildmanager.data.local.entity.GearEntity
import com.cubicserenity.esobuildmanager.data.local.entity.SkillEntity
import com.cubicserenity.esobuildmanager.domain.model.Build
import com.cubicserenity.esobuildmanager.domain.model.GearPiece
import com.cubicserenity.esobuildmanager.domain.model.Skill
import com.cubicserenity.esobuildmanager.util.GEAR_SLOTS
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BuildRepository @Inject constructor(
    private val dao: BuildDao,
    private val gson: Gson,
) {
    val builds: Flow<List<Build>> = dao.observeAll().map { list ->
        list.map { it.toDomain(emptyList(), emptyList()) }
    }

    suspend fun getBuild(id: Long): Build? =
        dao.getBuildWithDetails(id)?.toDomain()

    suspend fun getAllBuilds(): List<Build> =
        dao.getAllWithDetails().map { it.toDomain() }

    suspend fun saveBuild(build: Build): Long {
        val now = Instant.now().toString()
        val entity = build.toEntity(now)
        val id = if (build.id == 0L) {
            dao.insertBuild(entity)
        } else {
            dao.updateBuild(entity.copy(id = build.id))
            build.id
        }
        saveSkillsAndGear(id, build)
        return id
    }

    suspend fun deleteBuild(id: Long) {
        val entity = dao.getBuildWithDetails(id)?.build ?: return
        dao.deleteBuild(entity)
    }

    suspend fun upsertFromSync(build: Build): Long {
        val existing = dao.findByName(build.name)
        val id: Long
        if (existing != null) {
            val existingTs = existing.updatedAt
            val remoteTs = build.updatedAt
            if (remoteTs <= existingTs) return existing.id
            dao.updateBuild(build.toEntity(remoteTs).copy(id = existing.id))
            id = existing.id
        } else {
            id = dao.insertBuild(build.toEntity(build.updatedAt.ifBlank { Instant.now().toString() }))
        }
        saveSkillsAndGear(id, build)
        return id
    }

    private suspend fun saveSkillsAndGear(buildId: Long, build: Build) {
        dao.deleteSkillsForBuild(buildId)
        dao.insertSkills(build.skills.map {
            SkillEntity(buildId = buildId, bar = it.bar, slot = it.slot, name = it.name)
        })

        dao.deleteGearForBuild(buildId)
        val gearBySlot = build.gear.associateBy { it.slot }
        dao.insertGear(GEAR_SLOTS.map { slot ->
            val g = gearBySlot[slot]
            GearEntity(
                buildId = buildId,
                slot = slot,
                setName = g?.setName ?: "",
                weight = g?.weight ?: "",
                trait = g?.trait ?: "",
                enchant = g?.enchant ?: "",
                quality = g?.quality ?: "Epic",
            )
        })
    }

    private fun BuildWithDetails.toDomain(): Build = build.toDomain(
        skills.sortedWith(compareBy({ it.bar }, { it.slot })),
        gear.sortedBy { GEAR_SLOTS.indexOf(it.slot) },
    )

    private fun BuildEntity.toDomain(
        skills: List<SkillEntity>,
        gear: List<GearEntity>,
    ): Build {
        val cpType = object : TypeToken<List<String>>() {}.type
        return Build(
            id = id,
            name = name,
            esoClass = esoClass,
            subclass1 = subclass1,
            subclass2 = subclass2,
            role = role,
            content = content,
            gamePatch = gamePatch,
            source = source,
            mundusStone = mundusStone,
            foodBuff = foodBuff,
            attributeHealth = attributeHealth,
            attributeMagicka = attributeMagicka,
            attributeStamina = attributeStamina,
            championPoints = championPoints,
            cpSlots = runCatching { gson.fromJson<List<String>>(cpSlotsJson, cpType) }.getOrDefault(List(12) { "" }).let {
                val padded = it.toMutableList()
                while (padded.size < 12) padded.add("")
                padded
            },
            classMasteries = runCatching { gson.fromJson<List<String>>(classMasteriesJson, cpType) }.getOrDefault(emptyList()),
            notes = this.notes,
            updatedAt = updatedAt,
            skills = skills.map { Skill(it.id, it.buildId, it.bar, it.slot, it.name) },
            gear = gear.map { GearPiece(it.id, it.buildId, it.slot, it.setName, it.weight, it.trait, it.enchant, it.quality) },
        )
    }

    private fun Build.toEntity(ts: String): BuildEntity = BuildEntity(
        id = id,
        name = name,
        esoClass = esoClass,
        subclass1 = subclass1,
        subclass2 = subclass2,
        role = role,
        content = content,
        gamePatch = gamePatch,
        source = source,
        mundusStone = mundusStone,
        foodBuff = foodBuff,
        attributeHealth = attributeHealth,
        attributeMagicka = attributeMagicka,
        attributeStamina = attributeStamina,
        championPoints = championPoints,
        cpSlotsJson = gson.toJson(cpSlots),
        classMasteriesJson = gson.toJson(classMasteries),
        notes = notes,
        updatedAt = ts,
    )
}
