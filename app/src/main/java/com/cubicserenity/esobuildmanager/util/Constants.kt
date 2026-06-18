package com.cubicserenity.esobuildmanager.util

val ESO_CLASSES = listOf(
    "Arcanist", "Dragonknight", "Necromancer", "Nightblade",
    "Sorcerer", "Templar", "Warden",
)

val ROLES = listOf("Healer", "Hybrid", "MagDPS", "StamDPS", "Tank")

val CONTENT_TYPES = listOf("Dungeon", "Overland", "PvP", "Solo", "Trial")

val GEAR_SLOTS = listOf(
    "Head", "Shoulder", "Chest", "Hands", "Waist", "Legs", "Feet",
    "Neck", "Ring 1", "Ring 2",
    "Main Hand", "Off Hand", "Backup Main", "Backup Off",
)

val ARMOR_WEIGHTS = listOf("Heavy", "Light", "Medium")
val OFF_HAND_WEIGHTS = listOf("—", "N/A", "Heavy", "Light", "Medium")
val OFF_HAND_SLOTS = setOf("Off Hand", "Backup Off")

val ARMOR_TRAITS = listOf(
    "Divines", "Infused", "Impenetrable", "Reinforced", "Sturdy",
    "Training", "Well-Fitted", "Nirnhoned", "Invigorating",
)
val WEAPON_TRAITS = listOf(
    "Charged", "Defending", "Infused", "Nirnhoned", "Precise",
    "Sharpened", "Training", "Powered", "Decisive",
)
val JEWELRY_TRAITS = listOf(
    "Arcane", "Bloodthirsty", "Harmony", "Healthy", "Infused",
    "Protective", "Robust", "Swift", "Triune",
)
val JEWELRY_SLOTS = setOf("Neck", "Ring 1", "Ring 2")
val WEAPON_SLOTS = setOf("Main Hand", "Off Hand", "Backup Main", "Backup Off")

val QUALITY_TIERS = listOf("Normal", "Fine", "Superior", "Epic", "Legendary")

val MUNDUS_STONES = listOf(
    "The Apprentice", "The Atronach", "The Lady", "The Lord", "The Lover",
    "The Mage", "The Ritual", "The Serpent", "The Shadow", "The Steed",
    "The Thief", "The Tower", "The Warrior",
)

val GAME_PATCHES = listOf(
    "U35", "U36", "U37", "U38", "U39", "U40",
    "U41", "U42", "U43", "U44", "U45", "U46",
    "U47", "U48", "U49", "U50",
)

const val CP_SLOT_COUNT = 12
val CP_TREE_LABELS = listOf("Craft", "Warfare", "Fitness")
