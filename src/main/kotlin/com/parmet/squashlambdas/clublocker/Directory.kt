package com.parmet.squashlambdas.clublocker

import com.parmet.squashlambdas.activity.Player

internal class Directory(
    private val client: ClubLockerClient,
) {
    private val directory by lazy { client.directory() }

    fun idForPlayer(player: Player): Int? =
        directory
            .filter { it.fullName == player.name }
            .map { it.id }
            .firstOrNull()
}
