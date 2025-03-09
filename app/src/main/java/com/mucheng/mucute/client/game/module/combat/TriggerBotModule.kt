package com.mucheng.mucute.client.game.module.combat

import com.mucheng.mucute.client.game.InterceptablePacket
import com.mucheng.mucute.client.game.Module
import com.mucheng.mucute.client.game.ModuleCategory
import com.mucheng.mucute.client.game.entity.*
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket

class TriggerBotModule : Module("trigger_bot", ModuleCategory.Combat) {

    private var cpsValue by intValue("cps", 12, 1..20)
    private var playersOnly by boolValue("players_only", true)
    private var mobsOnly by boolValue("mobs_only", false)
    private var rangeValue by floatValue("range", 4.0f, 2f..6f)
    private var lastAttackTime = 0L

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) return

        val packet = interceptablePacket.packet
        if (packet is PlayerAuthInputPacket) {
            val currentTime = System.currentTimeMillis()
            val minAttackDelay = 1000L / cpsValue

            if ((currentTime - lastAttackTime) >= minAttackDelay) {
                session.level.entityMap.values
                    .filter { entity ->
                        entity.distance(session.localPlayer) <= rangeValue.toDouble() &&
                                isLookingAt(entity) &&
                                entity.isTarget()
                    }
                    .firstOrNull()?.let { target ->
                        session.localPlayer.attack(target)
                        lastAttackTime = currentTime
                    }
            }
        }
    }

    private fun isLookingAt(entity: Entity): Boolean {
        val playerPos = session.localPlayer.vec3Position
        val targetPos = entity.vec3Position
        val playerRot = session.localPlayer.vec3Rotation

        val dx = targetPos.x.toDouble() - playerPos.x.toDouble()
        val dy = targetPos.y.toDouble() - playerPos.y.toDouble()
        val dz = targetPos.z.toDouble() - playerPos.z.toDouble()
        val distance = Math.sqrt(dx * dx + dy * dy + dz * dz)

        val yawRad = Math.toRadians(playerRot.y.toDouble() + 90.0)
        val pitchRad = Math.toRadians(-playerRot.x.toDouble())

        val lookX = Math.cos(pitchRad) * Math.cos(yawRad)
        val lookY = Math.sin(pitchRad)
        val lookZ = Math.cos(pitchRad) * Math.sin(yawRad)

        val dot = (dx * lookX + dy * lookY + dz * lookZ) / distance
        val angle = Math.toDegrees(Math.acos(dot))

        return angle <= 10.0
    }

    private fun Entity.isTarget(): Boolean {
        return when (this) {
            is LocalPlayer -> false
            is Player -> {
                if (mobsOnly) {
                    false
                } else if (playersOnly) {
                    !this.isBot()
                } else {
                    !this.isBot()
                }
            }
            is EntityUnknown -> {
                if (playersOnly) {
                    false
                } else if (mobsOnly) {
                    this.isMob()
                } else {
                    true
                }
            }
            else -> false
        }
    }

    private fun EntityUnknown.isMob(): Boolean {
        return this.identifier in MobList.mobTypes
    }

    private fun Player.isBot(): Boolean {
        if (this is LocalPlayer) return false
        val playerList = session.level.playerMap[this.uuid] ?: return true
        return playerList.name.isBlank()
    }
}