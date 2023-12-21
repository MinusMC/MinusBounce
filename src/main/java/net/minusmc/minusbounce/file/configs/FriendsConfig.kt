/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.minusmc.minusbounce.file.configs

import com.google.gson.*
import net.minusmc.minusbounce.file.FileConfig
import net.minusmc.minusbounce.file.FileManager
import net.minusmc.minusbounce.utils.ClientUtils.logger
import java.io.*

class FriendsConfig
/**
 * Constructor of config
 *
 * @param file of config
 */
    (file: File?) : FileConfig(file!!) {
    val friends = ArrayList<Friend>()

    /**
     * Load config from file
     *
     * @throws IOException beo
     */
    @Throws(IOException::class)
    public override fun loadConfig() {
        clearFriends()
        try {
            val jsonElement = JsonParser().parse(BufferedReader(FileReader(file)))
            if (jsonElement is JsonNull) return
            for (friendElement in jsonElement.asJsonArray) {
                val friendObject = friendElement.asJsonObject
                addFriend(friendObject["playerName"].asString, friendObject["alias"].asString)
            }
        } catch (ex: JsonSyntaxException) {
            //When the JSON Parse fail, the client try to load and update the old config
            logger.info("[FileManager] Try to load old Friends config...")
            val bufferedReader = BufferedReader(FileReader(file))
            var line: String
            while (bufferedReader.readLine().also { line = it } != null) {
                if (!line.contains("{") && !line.contains("}")) {
                    line = line.replace(" ", "").replace("\"", "").replace(",", "")
                    if (line.contains(":")) {
                        val data = line.split(":".toRegex()).dropLastWhile { it.isEmpty() }
                            .toTypedArray()
                        addFriend(data[0], data[1])
                    } else addFriend(line)
                }
            }
            bufferedReader.close()
            logger.info("[FileManager] Loaded old Friends config...")

            //Save the friends into a new valid JSON file
            saveConfig()
            logger.info("[FileManager] Saved Friends to new config...")
        } catch (ex: IllegalStateException) {
            logger.info("[FileManager] Try to load old Friends config...")
            val bufferedReader = BufferedReader(FileReader(file))
            var line: String
            while (bufferedReader.readLine().also { line = it } != null) {
                if (!line.contains("{") && !line.contains("}")) {
                    line = line.replace(" ", "").replace("\"", "").replace(",", "")
                    if (line.contains(":")) {
                        val data = line.split(":".toRegex()).dropLastWhile { it.isEmpty() }
                            .toTypedArray()
                        addFriend(data[0], data[1])
                    } else addFriend(line)
                }
            }
            bufferedReader.close()
            logger.info("[FileManager] Loaded old Friends config...")
            saveConfig()
            logger.info("[FileManager] Saved Friends to new config...")
        }
    }

    /**
     * Save config to file
     *
     * @throws IOException
     */
    @Throws(IOException::class)
    public override fun saveConfig() {
        val jsonArray = JsonArray()
        for (friend in getFriends()) {
            val friendObject = JsonObject()
            friendObject.addProperty("playerName", friend.playerName)
            friendObject.addProperty("alias", friend.alias)
            jsonArray.add(friendObject)
        }
        val printWriter = PrintWriter(FileWriter(file))
        printWriter.println(FileManager.PRETTY_GSON.toJson(jsonArray))
        printWriter.close()
    }
    /**
     * Add friend to config
     *
     * @param playerName of friend
     * @param alias      of friend
     * @return of successfully added friend
     */
    /**
     * Add friend to config
     *
     * @param playerName of friend
     * @return of successfully added friend
     */
    @JvmOverloads
    fun addFriend(playerName: String, alias: String? = playerName): Boolean {
        if (isFriend(playerName)) return false
        friends.add(Friend(playerName, alias))
        return true
    }

    /**
     * Remove friend from config
     *
     * @param playerName of friend
     */
    fun removeFriend(playerName: String): Boolean {
        if (!isFriend(playerName)) return false
        friends.removeIf { friend: Friend -> friend.playerName == playerName }
        return true
    }

    /**
     * Check is friend
     *
     * @param playerName of friend
     * @return is friend
     */
    fun isFriend(playerName: String): Boolean {
        for (friend in friends) if (friend.playerName == playerName) return true
        return false
    }

    /**
     * Clear all friends from config
     */
    fun clearFriends() {
        friends.clear()
    }

    /**
     * Get friends
     *
     * @return list of friends
     */
    fun getFriends(): List<Friend> {
        return friends
    }

    inner class Friend internal constructor(
        /**
         * @return name of friend
         */
        val playerName: String,
        /**
         * @return alias of friend
         */
        var alias: String?
    ) {

        /**
         * @param playerName of friend
         * @param alias      of friend
         */
        init {
            alias = alias
        }
    }
}