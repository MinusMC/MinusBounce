// package net.minusmc.minusbounce.features.module.modules.combat.killaura.blocking

// import net.minusmc.minusbounce.features.module.modules.combat.killaura.KillAuraBlocking
// import net.minusmc.minusbounce.value.*

// class CustomBlocking: KillAuraBlocking("Custom") {
//     private val blockingPacket = ListValue("BlockingPacket", arrayOf("C08", "C08BlockPos", "UseItemKey"), "C08")
//     private val blockingOnEvent = ListValue("BlockingEvent", arrayOf("PreMotion", "PostMotion", "PreUpdate", "PreAttack", "PostAttack"))
//     private val blockingQueue = BoolValue("BlockingQueue", true)

//     private val releasePacket = ListValue("ReleasePacket", arrayOf("C07", "C07BlockPos", "C09", "UseItemKey"))
//     private val releaseQueue = BoolValue("ReleaseQueue", true)
// }