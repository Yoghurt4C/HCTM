package net.dblsaiko.rswires.common.init

import net.dblsaiko.hctm.common.util.delegatedNotNull
import net.dblsaiko.hctm.common.util.flatten
import net.dblsaiko.rswires.ModID
import net.dblsaiko.rswires.common.block.BundledCableBlock
import net.dblsaiko.rswires.common.block.InsulatedWireBlock
import net.dblsaiko.rswires.common.block.RedAlloyWireBlock
import net.fabricmc.fabric.api.block.FabricBlockSettings
import net.minecraft.block.Block
import net.minecraft.block.Material
import net.minecraft.util.DyeColor
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry
import kotlin.properties.ReadOnlyProperty

object Blocks {

  private val tasks = mutableListOf<() -> Unit>()

  private val wireSettings = FabricBlockSettings.of(Material.STONE)
    .breakByHand(true)
    .noCollision()
    .strength(0.05f, 0.05f)
    .build()

  val RedAlloyWire by create("red_alloy_wire", RedAlloyWireBlock(wireSettings))
  val InsulatedWires by DyeColor.values().associate { it to create("${it.getName()}_insulated_wire", InsulatedWireBlock(wireSettings, it)) }.flatten()
  val UncoloredBundledCable by create("bundled_cable", BundledCableBlock(wireSettings, null))
  val ColoredBundledCables by DyeColor.values().associate { it to create("${it.getName()}_bundled_cable", BundledCableBlock(wireSettings, it)) }.flatten()

  private fun <T : Block> create(name: String, block: T): ReadOnlyProperty<Blocks, T> {
    var regBlock: T? = null
    tasks += { regBlock = Registry.register(Registry.BLOCK, Identifier(ModID, name), block) }
    return delegatedNotNull { regBlock }
  }

  internal fun register() {
    tasks.forEach { it() }
    tasks.clear()
  }

}