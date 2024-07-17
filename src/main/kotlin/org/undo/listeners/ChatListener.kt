package org.undo.listeners

import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.undo.BuildProtectionPlugin
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class ChatListener(private val plugin: BuildProtectionPlugin) : Listener {

    private val lastMessageTimes: MutableMap<String, Long> = ConcurrentHashMap()

    @EventHandler
    fun onPlayerChat(event: AsyncPlayerChatEvent) {
        val player = event.player
        val uuid = player.uniqueId
        val chatInput = event.message

        val waiting = plugin.waitingForPermission.remove(uuid) ?: return

        val (action, startTime) = waiting
        if (System.currentTimeMillis() - startTime > 30000) {
            sendMessageWithCooldown(player.uniqueId, "permission_timeout", plugin.config.getString("messages.permission_timeout", "시간 초과되었습니다."))
            return
        }

        event.isCancelled = true

        when (action.toLowerCase()) {
            "give" -> {
                val targetPlayer = getTargetPlayer(chatInput)
                if (targetPlayer != null) {
                    plugin.buildZoneManager.addAdmin(player, targetPlayer)
                } else {
                    sendMessageWithCooldown(player.uniqueId, "player_not_found", plugin.config.getString("messages.player_not_found", "플레이어를 찾을 수 없습니다."))
                }
            }
            else -> {
                sendMessageWithCooldown(player.uniqueId, "unsupported_action", "지원하지 않는 작업입니다.")
            }
        }
    }

    private fun getTargetPlayer(playerName: String): org.bukkit.entity.Player? {
        return Bukkit.getOnlinePlayers().firstOrNull { it.name.equals(playerName, ignoreCase = true) }
    }

    private fun sendMessageWithCooldown(playerUUID: UUID, messageType: String, message: String?) {
        val currentTime = System.currentTimeMillis()
        val lastMessageTime = lastMessageTimes["$playerUUID-$messageType"]

        if (lastMessageTime == null || currentTime - lastMessageTime > 5000) {
            lastMessageTimes["$playerUUID-$messageType"] = currentTime
            message?.let { Bukkit.getPlayer(playerUUID)?.sendMessage(it) }
        }
    }
}


