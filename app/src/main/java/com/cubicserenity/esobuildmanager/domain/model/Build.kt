package com.cubicserenity.esobuildmanager.domain.model

data class Build(
    val id: Long = 0,
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
    val cpSlots: List<String> = List(12) { "" },
    val classMasteries: List<String> = emptyList(),
    val notes: String = "",
    val updatedAt: String = "",
    val skills: List<Skill> = emptyList(),
    val gear: List<GearPiece> = emptyList(),
)

data class Skill(
    val id: Long = 0,
    val buildId: Long = 0,
    val bar: Int = 0,
    val slot: Int = 0,
    val name: String = "",
)

data class GearPiece(
    val id: Long = 0,
    val buildId: Long = 0,
    val slot: String = "",
    val setName: String = "",
    val weight: String = "",
    val trait: String = "",
    val enchant: String = "",
    val quality: String = "Epic",
)
