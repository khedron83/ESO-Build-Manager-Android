package com.cubicserenity.esobuildmanager.util

import android.content.Context
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import java.security.MessageDigest

object SkillData {
    private val _skillNames = mutableListOf<String>()
    private val _skillLineMap = mutableMapOf<String, String>()
    private var initialized = false

    val skillNames: List<String> get() = _skillNames

    private val GRIMOIRE_ICONS = mapOf(
        "Banner Bearer" to "ON-icon-book-grimoire-Support.png",
        "Elemental Explosion" to "ON-icon-book-grimoire-Destruction Staff.png",
        "Mender's Bond" to "ON-icon-book-grimoire-Restoration Staff.png",
        "Shield Throw" to "ON-icon-book-grimoire-1-Handed.png",
        "Smash" to "ON-icon-book-grimoire-2-Handed.png",
        "Soul Burst" to "ON-icon-book-grimoire-Soul Magic 02.png",
        "Torchbearer" to "ON-icon-book-grimoire-Fighters Guild.png",
        "Trample" to "ON-icon-book-grimoire-Assault.png",
        "Traveling Knife" to "ON-icon-book-grimoire-Dual Wield.png",
        "Ulfsild's Contingency" to "ON-icon-book-grimoire-Mages Guild.png",
        "Vault" to "ON-icon-book-grimoire-Bow.png",
        "Wield Soul" to "ON-icon-book-grimoire-Soul Magic 01.png",
    )

    fun init(context: Context) {
        if (initialized) return
        val gson = Gson()
        val names = mutableSetOf<String>()

        runCatching {
            val json = context.assets.open("skills.json").bufferedReader().readText()
            val arr = gson.fromJson(json, JsonArray::class.java)
            for (catEl in arr) {
                val cat = catEl.asJsonObject
                val line = cat.get("line")?.asString ?: continue
                val skills = cat.getAsJsonArray("skills") ?: continue
                for (skillEl in skills) {
                    val skill = skillEl.asJsonObject
                    val base = skill.get("base")?.asString ?: continue
                    names.add(base)
                    _skillLineMap[base] = line
                    skill.getAsJsonArray("morphs")?.forEach { morphEl ->
                        val morph = morphEl.asString
                        names.add(morph)
                        _skillLineMap[morph] = line
                    }
                }
            }
        }

        // Fallback: skill_ids.json fills line map gaps (keys are "Category::LineName")
        runCatching {
            val json = context.assets.open("skill_ids.json").bufferedReader().readText()
            val obj = gson.fromJson(json, JsonObject::class.java)
            for ((lineKey, skillsEl) in obj.entrySet()) {
                if (!skillsEl.isJsonObject) continue
                val sep = lineKey.indexOf("::")
                val line = if (sep >= 0) lineKey.substring(sep + 2) else ""
                for ((name, _) in skillsEl.asJsonObject.entrySet()) {
                    names.add(name)
                    if (line.isNotEmpty() && !_skillLineMap.containsKey(name)) {
                        _skillLineMap[name] = line
                    }
                }
            }
        }

        _skillNames.addAll(names.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it }))
        initialized = true
    }

    fun skillIconUrl(skillName: String): String? {
        val grimoireFile = GRIMOIRE_ICONS[skillName]
        if (grimoireFile != null) return uesp(grimoireFile)
        val line = _skillLineMap[skillName] ?: return null
        if (line == "Grimoires") return null
        return uesp("ON-icon-skill-$line-$skillName.png")
    }

    private fun uesp(filename: String): String {
        val fname = filename.replace(' ', '_')
        val md5 = MessageDigest.getInstance("MD5").digest(fname.toByteArray())
            .joinToString("") { "%02x".format(it) }
        return "https://images.uesp.net/${md5[0]}/${md5.substring(0, 2)}/$fname"
    }
}
