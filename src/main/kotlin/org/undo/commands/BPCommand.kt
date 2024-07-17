package org.undo.commands

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import org.undo.BuildProtectionPlugin

class BPCommand(private val plugin: BuildProtectionPlugin) : CommandExecutor, TabCompleter {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.isEmpty()) {
            sender.sendMessage("사용 가능한 명령어: /bp <setitem|reload|buildzone>")
            return true
        }

        when (args[0].toLowerCase()) {
            "setitem" -> {
                if (sender is Player) {
                    plugin.buildZoneManager.setBuildZoneItem(sender)
                } else {
                    sender.sendMessage("이 명령어는 플레이어만 사용할 수 있습니다.")
                }
            }
            "reload" -> {
                plugin.buildZoneManager.reloadConfig()
                sender.sendMessage("플러그인 설정이 리로드되었습니다.")
            }
            "buildzone" -> {
                if (sender is Player) {
                    plugin.buildZoneManager.giveBuildZoneItem(sender)
                } else {
                    sender.sendMessage("이 명령어는 플레이어만 사용할 수 있습니다.")
                }
            }
            else -> {
                sender.sendMessage("알 수 없는 명령어입니다. 사용 가능한 명령어: /bp <setitem|reload|buildzone>")
            }
        }
        return true
    }

    override fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<out String>): List<String> {
        return if (args.size == 1) {
            listOf("setitem", "reload", "buildzone").filter { it.startsWith(args[0], ignoreCase = true) }
        } else {
            emptyList()
        }
    }
}
