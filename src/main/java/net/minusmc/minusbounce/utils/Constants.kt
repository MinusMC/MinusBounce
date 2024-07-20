package net.minusmc.minusbounce.utils

import net.minecraft.network.handshake.client.*
import net.minecraft.network.login.client.*
import net.minecraft.network.login.server.*
import net.minecraft.network.play.client.*
import net.minecraft.network.play.client.C03PacketPlayer.*
import net.minecraft.network.play.server.*
import net.minecraft.network.play.server.S14PacketEntity.*
import net.minecraft.network.status.client.*
import net.minecraft.network.status.server.*

object Constants: MinecraftInstance() {

	val clientPacketClasses = listOf(
		C00PacketKeepAlive::class.java, C01PacketChatMessage::class.java, 
		C02PacketUseEntity::class.java, C03PacketPlayer::class.java, 
		C03PacketPlayer.C04PacketPlayerPosition::class.java, C03PacketPlayer.C05PacketPlayerLook::class.java, 
		C03PacketPlayer.C06PacketPlayerPosLook::class.java, C07PacketPlayerDigging::class.java, 
		C08PacketPlayerBlockPlacement::class.java, C09PacketHeldItemChange::class.java, 
		C0APacketAnimation::class.java, C0BPacketEntityAction::class.java, 
		C0CPacketInput::class.java, C0DPacketCloseWindow::class.java, 
		C0EPacketClickWindow::class.java, C0FPacketConfirmTransaction::class.java, 
		C10PacketCreativeInventoryAction::class.java, C11PacketEnchantItem::class.java, 
		C12PacketUpdateSign::class.java, C13PacketPlayerAbilities::class.java, 
		C14PacketTabComplete::class.java, C15PacketClientSettings::class.java, 
		C16PacketClientStatus::class.java, C17PacketCustomPayload::class.java, 
		C18PacketSpectate::class.java, C19PacketResourcePackStatus::class.java)

	val serverPacketClasses = listOf(
		S00PacketKeepAlive::class.java, S01PacketJoinGame::class.java, 
		S02PacketChat::class.java, S03PacketTimeUpdate::class.java, 
		S04PacketEntityEquipment::class.java, S05PacketSpawnPosition::class.java, 
		S06PacketUpdateHealth::class.java, S07PacketRespawn::class.java, 
		S08PacketPlayerPosLook::class.java, S09PacketHeldItemChange::class.java, 
		S0APacketUseBed::class.java, S0BPacketAnimation::class.java, 
		S0CPacketSpawnPlayer::class.java, S0DPacketCollectItem::class.java, 
		S0EPacketSpawnObject::class.java, S0FPacketSpawnMob::class.java, 
		S10PacketSpawnPainting::class.java, S11PacketSpawnExperienceOrb::class.java, 
		S12PacketEntityVelocity::class.java, S13PacketDestroyEntities::class.java, 
		S14PacketEntity::class.java, S14PacketEntity.S15PacketEntityRelMove::class.java, 
		S14PacketEntity.S16PacketEntityLook::class.java, S14PacketEntity.S17PacketEntityLookMove::class.java, 
		S18PacketEntityTeleport::class.java, S19PacketEntityHeadLook::class.java, 
		S19PacketEntityStatus::class.java, S1BPacketEntityAttach::class.java, 
		S1CPacketEntityMetadata::class.java, S1DPacketEntityEffect::class.java, 
		S1EPacketRemoveEntityEffect::class.java, S1FPacketSetExperience::class.java, 
		S20PacketEntityProperties::class.java, S21PacketChunkData::class.java, 
		S22PacketMultiBlockChange::class.java, S23PacketBlockChange::class.java, 
		S24PacketBlockAction::class.java, S25PacketBlockBreakAnim::class.java, 
		S26PacketMapChunkBulk::class.java, S27PacketExplosion::class.java, 
		S28PacketEffect::class.java, S29PacketSoundEffect::class.java, 
		S2APacketParticles::class.java, S2BPacketChangeGameState::class.java, 
		S2CPacketSpawnGlobalEntity::class.java, S2DPacketOpenWindow::class.java, 
		S2EPacketCloseWindow::class.java, S2FPacketSetSlot::class.java, 
		S30PacketWindowItems::class.java, S31PacketWindowProperty::class.java, 
		S32PacketConfirmTransaction::class.java, S33PacketUpdateSign::class.java, 
		S34PacketMaps::class.java, S35PacketUpdateTileEntity::class.java, 
		S36PacketSignEditorOpen::class.java, S37PacketStatistics::class.java, 
		S38PacketPlayerListItem::class.java, S39PacketPlayerAbilities::class.java, 
		S3APacketTabComplete::class.java, S3BPacketScoreboardObjective::class.java, 
		S3CPacketUpdateScore::class.java, S3DPacketDisplayScoreboard::class.java, 
		S3EPacketTeams::class.java, S3FPacketCustomPayload::class.java, 
		S40PacketDisconnect::class.java, S41PacketServerDifficulty::class.java, 
		S42PacketCombatEvent::class.java, S43PacketCamera::class.java, 
		S44PacketWorldBorder::class.java, S45PacketTitle::class.java, 
		S46PacketSetCompressionLevel::class.java, S47PacketPlayerListHeaderFooter::class.java, 
		S48PacketResourcePackSend::class.java, S49PacketUpdateEntityNBT::class.java
	)

	val clientOtherPacketClasses = listOf(
		C00Handshake::class.java,
		C00PacketLoginStart::class.java, C00PacketServerQuery::class.java, 
		C01PacketEncryptionResponse::class.java, C01PacketPing::class.java
	)

	val serverOtherPacketClasses = listOf(
		S00PacketDisconnect::class.java, S01PacketEncryptionRequest::class.java, 
		S02PacketLoginSuccess::class.java, S03PacketEnableCompression::class.java, 
		S00PacketServerInfo::class.java, S01PacketPong::class.java,
	)

	val moveKeys = listOf(mc.gameSettings.keyBindForward, mc.gameSettings.keyBindRight, 
		mc.gameSettings.keyBindBack, mc.gameSettings.keyBindLeft)

	const val GROUND_ACCELERATION = 0.1299999676734952 - 0.12739998266255503 + 1E-7 - 1E-8
    const val AIR_ACCELERATION = 0.025999999334873708 - 0.025479999685988748 - 1E-8

}