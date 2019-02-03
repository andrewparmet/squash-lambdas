package com.parmet.squashlambdas.reserve

import com.google.common.util.concurrent.Service
import com.parmet.squashlambdas.activity.Player

internal class DirectoryService(
    private val client: ClubLockerClient
) : Service by client {
    private val directory by lazy { client.directory() }

    fun idForPlayer(player: Player): Int? =
        directory
            .filter { it.email == player.email }
            .map { it.id }
            .firstOrNull()
}
