package com.parmet.squashlambdas.clublocker

import com.parmet.squashlambdas.activity.Player

internal class Directory(
    private val client: ClubLockerClient
) {
    private var directory: List<User>? = null

    suspend fun idForPlayer(player: Player): Int? =
        loadDirectory()
            .filter { it.fullName == player.name }
            .map { it.id }
            .firstOrNull()

    private suspend fun loadDirectory(): List<User> =
        directory ?: client.directory().also { directory = it }
}
