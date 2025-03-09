package com.mucheng.mucute.client.game.module.combat

import com.mucheng.mucute.client.game.InterceptablePacket
import com.mucheng.mucute.client.game.Module
import com.mucheng.mucute.client.game.ModuleCategory
import com.mucheng.mucute.client.game.entity.*
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket

class CrystalSmashModule : Module("crystal_smash", ModuleCategory.Combat) {

    private var rangeValue by floatValue("range", 4.0f, 0f..20f)
    private var attackInterval by intValue("delay", 5, 1..100)
    private var cpsValue by intValue("cps", 10, 1..1000)
    private var packets by intValue("packets", 1, 1..100)

    private var lastAttackTime = 0L

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) return

        val packet = interceptablePacket.packet
        if (packet is PlayerAuthInputPacket) {
            val currentTime = System.currentTimeMillis()
            val minAttackDelay = 1000L / cpsValue

            if (packet.tick % attackInterval == 0L && (currentTime - lastAttackTime) >= minAttackDelay) {
                val crystals = searchForCrystals()

                if (crystals.isNotEmpty()) {
                    crystals.forEach { crystal ->
                        repeat(packets) {
                            session.localPlayer.attack(crystal)
                        }
                    }
                    lastAttackTime = currentTime
                }
            }
        }
    }

    private fun searchForCrystals(): List<Entity> {
        return session.level.entityMap.values
            .filter { entity ->
                entity.distance(session.localPlayer) < rangeValue &&
                        entity is EntityUnknown &&
                        entity.identifier == "minecraft:end_crystal"
            }
    }
}
