package net.backupcup.hexed

import net.backupcup.hexed.api.HexedEvents.PostMine
import net.backupcup.hexed.listener.ServerListener
import net.backupcup.hexed.loot.ModifyLootTables
import net.backupcup.hexed.register.*
import net.backupcup.hexed.register.RegisterEnchantments.AMPLIFY_HEX
import net.backupcup.hexed.register.RegisterEnchantments.OVERBURDEN_HEX
import net.backupcup.hexed.register.RegisterEnchantments.RUINOUS_HEX
import net.backupcup.hexed.register.RegisterStatusEffects.OVERBURDEN
import net.backupcup.hexed.util.HexHelper
import net.backupcup.hexed.util.HexHelper.entityMultiplyingEffect
import net.backupcup.hexed.util.HexHelper.hasFullRobes
import net.backupcup.hexed.util.HexHelper.stackHasEnchantment
import net.backupcup.hexed.util.HexRandom.nextDouble
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.fabricmc.fabric.api.resource.ResourceManagerHelper
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener
import net.minecraft.block.BlockState
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.resource.ResourceManager
import net.minecraft.resource.ResourceType
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.stat.Stats
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

object Hexed : ModInitializer {
	const val MOD_ID: String = "hexed"

	val LOGGER: Logger? = LoggerFactory.getLogger(MOD_ID)

	val SYNC_CONFIG_PACKET = Identifier.of(MOD_ID, "sync_config")
	private var config: Config? = null

	fun getConfig(): Config? {
		return config
	}

	fun setConfig(config: Config) {
		Hexed.config = config
	}

	override fun onInitialize() {
		//Config Sync
		ResourceManagerHelper.get(ResourceType.SERVER_DATA)
			.registerReloadListener(object : SimpleSynchronousResourceReloadListener {
				override fun getFabricId(): Identifier {
					return Identifier.of(MOD_ID, "config")!!
				}

				override fun reload(manager: ResourceManager) {
					config = Config.load()
				}
			})

		ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register(ServerLifecycleEvents.SyncDataPackContents { player: ServerPlayerEntity, _: Boolean ->
			val buf = PacketByteBufs.create()
			config?.writeToClient(buf)
			ServerPlayNetworking.send(player, SYNC_CONFIG_PACKET, buf)
			if (Config.lastError != null) {
				player.sendMessage(
					Text.literal("[${MOD_ID.uppercase()}]: ")
						.append(Config.lastError).formatted(Formatting.RED)
				)
			}
		})

		RegisterItems.registerItems()
		RegisterArmor.registerArmor()
		RegisterScreenHandlers.registerScreenHandlers()
		RegisterBlocks.registerBlocks()
		RegisterBlockEntities.registerBlockEntities()
		RegisterDecoCandles.registerDecoCandles()
		RegisterSounds.registerSounds()
		RegisterStatusEffects.registerStatusEffects()
		RegisterEnchantments.registerHexes()
		RegisterTags.registerTags()
		//RegisterWorldGen.registerWorldGen()
		registerAllGroups()
		RegisterStats.registerStats()
		RegisterEntities.registerEntities()

		RegisterTimedEvents.registerServerTick()
		RegisterPackets.registerServerPackets()
		ServerListener.registerServerListeners()

		ModifyLootTables.registerLootModifiers()

		registerPostMine()
	}

	private fun registerPostMine() {
		PostMine.POST_MINE.register { tool, world, state, pos, player ->
			if(!tool.item.isSuitableFor(state)) return@register

			amplifyMine(tool, world, pos, state, player)
			overburdenedStack(tool, world, pos, state, player)
			ruinousExplode(tool, world, pos, state, player)
		}
	}

	private fun ruinousExplode(tool: ItemStack, world: World, pos: BlockPos, state: BlockState, miner: PlayerEntity) {
		if (stackHasEnchantment(tool, RUINOUS_HEX)) {
			var entity: Entity? = null
			if (hasFullRobes(miner)) entity = miner

			world.createExplosion(
				entity,
				pos.x + nextDouble(-0.25, 1.25),
				pos.y + nextDouble(-0.25, 1.25),
				pos.z + nextDouble(-0.25, 1.25),
				if (getConfig() != null) getConfig()!!.ruinousHex.explosionPower else 1.25f,
				World.ExplosionSourceType.BLOCK
			)
		}
	}

	private fun overburdenedStack(tool: ItemStack, world: World, pos: BlockPos, state: BlockState, miner: PlayerEntity) {
		if (stackHasEnchantment(tool, OVERBURDEN_HEX)) {
			if((miner.getStatusEffect(OVERBURDEN)?.amplifier ?: 0) < 256) {
				entityMultiplyingEffect(miner, OVERBURDEN, config?.overburdenHex?.buffDuration ?: 200, if (hasFullRobes(miner)) 0 else 1)
			}
		}
	}

	private fun amplifyMine(tool: ItemStack, world: World, pos: BlockPos, state: BlockState, player: PlayerEntity) {
		if (!player.isSneaking && HexHelper.stackHasEnchantment(tool, AMPLIFY_HEX)) {
			val cameraPosVec = player.getCameraPosVec(1.0f)
			val blockCenter = Vec3d(pos.x + 0.5, pos.y + 0.5, pos.z + 0.5)

			val dotProduct = Vec3d(
				(blockCenter.x - cameraPosVec.x) * player.rotationVector.x,
				(blockCenter.y - cameraPosVec.y) * player.rotationVector.y,
				(blockCenter.z - cameraPosVec.z) * player.rotationVector.z
			)

			val maxDot = max(abs(dotProduct.x), max(abs(dotProduct.y), abs(dotProduct.z)))

			var breakBox = Box(pos, pos)

			if (maxDot == abs(dotProduct.x)) breakBox = breakBox.expand(0.0, 1.0, 1.0)
			else if (maxDot == abs(dotProduct.y)) breakBox = breakBox.expand(1.0, 0.0, 1.0)
			else if (maxDot == abs(dotProduct.z)) breakBox = breakBox.expand(1.0, 1.0, 0.0)

			iterateOverBox(breakBox, world, player, tool, state)
		}
	}

	private fun iterateOverBox(box: Box, world: World, player: PlayerEntity, tool: ItemStack, state: BlockState) {
		val minPos = BlockPos(box.minX.toInt(), box.minY.toInt(), box.minZ.toInt())
		val maxPos = BlockPos(box.maxX.toInt(), box.maxY.toInt(), box.maxZ.toInt())

		for (x in minPos.x..maxPos.x) {
			for (y in minPos.y..maxPos.y) {
				for (z in minPos.z..maxPos.z) {
					val pos = BlockPos(x, y, z)

					if (tool.isSuitableFor(world.getBlockState(pos)) && !world.getBlockState(pos).isAir && world.getBlockState(
							pos
						).block.hardness <= state.block.hardness + 2
					) {
						var shouldDrop = if (getConfig() != null) nextDouble(
							0.0,
							1.0
						) >= getConfig()!!.amplifyHex.dropChance else nextDouble(0.0, 1.0) >= 0.5
						if (hasFullRobes(player.armorItems)) {
							shouldDrop = true
						}

						player.incrementStat(Stats.MINED.getOrCreateStat(world.getBlockState(pos).block))
						world.breakBlock(pos, shouldDrop, player)

						player.incrementStat(Stats.USED.getOrCreateStat(tool.item))
						if (!world.isClient) tool.damage(
							if (getConfig() != null) getConfig()!!.amplifyHex.toolDamage else 1,
							player
						) { entity: PlayerEntity -> entity.sendToolBreakStatus(player.activeHand) }

						if (getConfig() != null) player.addExhaustion(getConfig()!!.amplifyHex.exhaustionAmount.toFloat())
					}
				}
			}
		}
	}

}