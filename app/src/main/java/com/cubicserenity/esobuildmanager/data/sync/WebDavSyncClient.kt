package com.cubicserenity.esobuildmanager.data.sync

import com.cubicserenity.esobuildmanager.data.preferences.PreferencesRepository
import com.cubicserenity.esobuildmanager.data.repository.BuildRepository
import com.cubicserenity.esobuildmanager.di.NetworkModule
import com.cubicserenity.esobuildmanager.domain.model.Build
import com.cubicserenity.esobuildmanager.domain.model.GearPiece
import com.cubicserenity.esobuildmanager.domain.model.Skill
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.xml.sax.InputSource
import java.io.StringReader
import javax.inject.Inject
import javax.inject.Singleton
import javax.xml.parsers.DocumentBuilderFactory

private const val REMOTE_DIR = "ESO-Builds"

data class SyncResult(val uploaded: Int, val downloaded: Int, val errors: List<String>)

@Singleton
class WebDavSyncClient @Inject constructor(
    private val prefs: PreferencesRepository,
    private val repo: BuildRepository,
    private val gson: Gson,
) {
    private fun dirUrl(serverUrl: String, username: String) =
        "${serverUrl.trimEnd('/')}/remote.php/dav/files/$username/$REMOTE_DIR/"

    private fun fileUrl(serverUrl: String, username: String, filename: String) =
        "${serverUrl.trimEnd('/')}/remote.php/dav/files/$username/$REMOTE_DIR/$filename"

    suspend fun sync(): SyncResult = withContext(Dispatchers.IO) {
        val config = prefs.serverConfig.first()
        if (config.serverUrl.isBlank() || config.username.isBlank()) {
            return@withContext SyncResult(0, 0, listOf("Nextcloud not configured"))
        }

        val client = NetworkModule.buildClient(config)
        val errors = mutableListOf<String>()

        // Ensure directory exists
        runCatching {
            client.newCall(
                Request.Builder()
                    .url(dirUrl(config.serverUrl, config.username))
                    .method("MKCOL", ByteArray(0).toRequestBody(null))
                    .build()
            ).execute().close()
        }

        val localBuilds = repo.getAllBuilds()

        // Upload all local builds
        val uploadedSlugs = mutableSetOf<String>()
        var uploaded = 0
        for (build in localBuilds) {
            val slug = makeSlug(build.name)
            val json = buildToJson(build)
            runCatching {
                client.newCall(
                    Request.Builder()
                        .url(fileUrl(config.serverUrl, config.username, "$slug.json"))
                        .put(json.toRequestBody("application/json".toMediaType()))
                        .build()
                ).execute().close()
                uploadedSlugs.add("$slug.json")
                uploaded++
            }.onFailure { errors.add("Upload '${build.name}': ${it.message}") }
        }

        // Discover remote builds not in uploadedSlugs
        val remoteFiles = runCatching {
            val response = client.newCall(
                Request.Builder()
                    .url(dirUrl(config.serverUrl, config.username))
                    .method("PROPFIND", ByteArray(0).toRequestBody(null))
                    .header("Depth", "1")
                    .build()
            ).execute()
            if (!response.isSuccessful) return@runCatching emptyList()
            val body = response.body?.string() ?: return@runCatching emptyList()
            parsePropfindFilenames(body)
        }.getOrDefault(emptyList<String>())

        var downloaded = 0
        val localNameSet = localBuilds.map { it.name.lowercase() }.toSet()

        for (filename in remoteFiles) {
            if (filename in uploadedSlugs) continue
            runCatching {
                val response = client.newCall(
                    Request.Builder()
                        .url(fileUrl(config.serverUrl, config.username, filename))
                        .get()
                        .build()
                ).execute()
                if (!response.isSuccessful) return@runCatching
                val body = response.body?.string() ?: return@runCatching
                val build = jsonToBuild(body) ?: return@runCatching
                if (build.name.isBlank()) return@runCatching

                val remoteTs = build.updatedAt
                val existing = localBuilds.find { it.name.lowercase() == build.name.lowercase() }
                if (existing == null || remoteTs > existing.updatedAt) {
                    repo.upsertFromSync(build)
                    if (build.name.lowercase() !in localNameSet) downloaded++
                }
            }.onFailure { errors.add("Download '$filename': ${it.message}") }
        }

        SyncResult(uploaded, downloaded, errors)
    }

    private fun makeSlug(name: String): String =
        name.replace(Regex("[^\\w\\s-]"), "").trim().replace(Regex("\\s+"), "_").ifBlank { "build" }

    private fun buildToJson(build: Build): String {
        val obj = JsonObject()
        obj.addProperty("_eso_build_manager_version", 1)
        obj.addProperty("_sync_updated_at", build.updatedAt)
        obj.addProperty("name", build.name)
        obj.addProperty("eso_class", build.esoClass)
        obj.addProperty("subclass_1", build.subclass1)
        obj.addProperty("subclass_2", build.subclass2)
        obj.addProperty("role", build.role)
        obj.addProperty("content", build.content)
        obj.addProperty("game_patch", build.gamePatch)
        obj.addProperty("source", build.source)
        obj.addProperty("mundus_stone", build.mundusStone)
        obj.addProperty("food_buff", build.foodBuff)
        obj.addProperty("attribute_health", build.attributeHealth)
        obj.addProperty("attribute_magicka", build.attributeMagicka)
        obj.addProperty("attribute_stamina", build.attributeStamina)
        obj.addProperty("champion_points", build.championPoints)
        obj.add("cp_slots", gson.toJsonTree(build.cpSlots))
        obj.add("class_masteries", gson.toJsonTree(build.classMasteries))
        obj.addProperty("notes", build.notes)
        obj.add("skills", gson.toJsonTree(build.skills.filter { it.name.isNotBlank() }.map { s ->
            JsonObject().apply {
                addProperty("bar", s.bar); addProperty("slot", s.slot); addProperty("name", s.name)
            }
        }))
        obj.add("gear", gson.toJsonTree(build.gear.map { g ->
            JsonObject().apply {
                addProperty("slot", g.slot); addProperty("set_name", g.setName)
                addProperty("weight", g.weight); addProperty("trait", g.trait)
                addProperty("enchant", g.enchant); addProperty("quality", g.quality)
            }
        }))
        return gson.toJson(obj)
    }

    private fun jsonToBuild(json: String): Build? = runCatching {
        val obj = gson.fromJson(json, JsonObject::class.java)
        val strType = object : TypeToken<List<String>>() {}.type

        val cpSlots = runCatching {
            gson.fromJson<List<String>>(obj.get("cp_slots"), strType)
        }.getOrDefault(List(12) { "" }).let {
            val m = it.toMutableList(); while (m.size < 12) m.add(""); m.toList()
        }
        val classMasteries = runCatching {
            gson.fromJson<List<String>>(obj.get("class_masteries"), strType)
        }.getOrDefault(emptyList())

        val skills = obj.getAsJsonArray("skills")?.mapNotNull { el ->
            val s = el.asJsonObject
            Skill(
                bar = s.get("bar")?.asInt ?: return@mapNotNull null,
                slot = s.get("slot")?.asInt ?: return@mapNotNull null,
                name = s.get("name")?.asString ?: "",
            )
        } ?: emptyList()

        val gear = obj.getAsJsonArray("gear")?.map { el ->
            val g = el.asJsonObject
            GearPiece(
                slot = g.get("slot")?.asString ?: "",
                setName = g.get("set_name")?.asString ?: "",
                weight = g.get("weight")?.asString ?: "",
                trait = g.get("trait")?.asString ?: "",
                enchant = g.get("enchant")?.asString ?: "",
                quality = g.get("quality")?.asString ?: "Epic",
            )
        } ?: emptyList()

        Build(
            name = obj.get("name")?.asString ?: "",
            esoClass = obj.get("eso_class")?.asString ?: "",
            subclass1 = obj.get("subclass_1")?.asString ?: "",
            subclass2 = obj.get("subclass_2")?.asString ?: "",
            role = obj.get("role")?.asString ?: "",
            content = obj.get("content")?.asString ?: "",
            gamePatch = obj.get("game_patch")?.asString ?: "U50",
            source = obj.get("source")?.asString ?: "",
            mundusStone = obj.get("mundus_stone")?.asString ?: "",
            foodBuff = obj.get("food_buff")?.asString ?: "",
            attributeHealth = obj.get("attribute_health")?.asInt ?: 0,
            attributeMagicka = obj.get("attribute_magicka")?.asInt ?: 0,
            attributeStamina = obj.get("attribute_stamina")?.asInt ?: 64,
            championPoints = obj.get("champion_points")?.asString ?: "",
            cpSlots = cpSlots,
            classMasteries = classMasteries,
            notes = obj.get("notes")?.asString ?: "",
            updatedAt = obj.get("_sync_updated_at")?.asString ?: "",
            skills = skills,
            gear = gear,
        )
    }.getOrNull()

    private fun parsePropfindFilenames(xml: String): List<String> = runCatching {
        val doc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
            .parse(InputSource(StringReader(xml)))
        val hrefs = doc.getElementsByTagNameNS("DAV:", "href")
        (0 until hrefs.length)
            .map { hrefs.item(it).textContent.trim() }
            .map { it.trimEnd('/').substringAfterLast('/') }
            .filter { it.endsWith(".json") }
    }.getOrDefault(emptyList())
}
