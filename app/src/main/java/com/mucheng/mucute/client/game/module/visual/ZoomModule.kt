package com.mucheng.mucute.client.game.module.visual

import com.mucheng.mucute.client.game.InterceptablePacket
import com.mucheng.mucute.client.game.Module
import com.mucheng.mucute.client.game.ModuleCategory
import org.cloudburstmc.protocol.bedrock.data.Ability
import org.cloudburstmc.protocol.bedrock.data.AbilityLayer
import org.cloudburstmc.protocol.bedrock.data.PlayerPermission
import org.cloudburstmc.protocol.bedrock.data.command.CommandPermission
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket
import org.cloudburstmc.protocol.bedrock.packet.UpdateAbilitiesPacket

class ZoomModule : Module("zoom", ModuleCategory.Visual) {

    private var zoomAmount by floatValue("zoom", 0.1f, 0.05f..0.5f)
    private val defaultWalkSpeed = 0.1f

    private val abilitiesPacket = UpdateAbilitiesPacket().apply {
        playerPermission = PlayerPermission.OPERATOR
        commandPermission = CommandPermission.OWNER
        abilityLayers.add(AbilityLayer().apply {
            layerType = AbilityLayer.Type.BASE
            abilitiesSet.addAll(Ability.entries)
            abilityValues.addAll(arrayOf(
                Ability.BUILD,
                Ability.MINE,
                Ability.DOORS_AND_SWITCHES,
                Ability.OPEN_CONTAINERS,
                Ability.ATTACK_PLAYERS,
                Ability.ATTACK_MOBS,
                Ability.OPERATOR_COMMANDS
            ))
        })
    }

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        val packet = interceptablePacket.packet
        if (packet is PlayerAuthInputPacket) {
            updateZoom()
        }
    }

    private fun updateZoom() {
        abilitiesPacket.uniqueEntityId = session.localPlayer.uniqueEntityId
        val calculatedZoom = if (isEnabled) {
            zoomAmount
        } else {
            defaultWalkSpeed
        }
        abilitiesPacket.abilityLayers[0].walkSpeed = calculatedZoom
        session.clientBound(abilitiesPacket)
    }
}