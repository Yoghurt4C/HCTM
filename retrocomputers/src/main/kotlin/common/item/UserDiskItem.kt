package therealfarfetchd.retrocomputers.common.item

import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.server.world.ServerWorld
import net.minecraft.text.StringTextComponent
import net.minecraft.text.TextComponent
import therealfarfetchd.retrocomputers.common.item.ext.ItemDisk
import java.io.RandomAccessFile
import java.nio.file.Files
import java.nio.file.Path
import java.util.*

class UserDiskItem : Item(Item.Settings().stackSize(1)), ItemDisk {

  override fun getTranslatedNameTrimmed(stack: ItemStack): TextComponent {
    if (hasLabel(stack)) return StringTextComponent(getLabel(stack))

    return super.getTranslatedNameTrimmed(stack)
  }

  override fun getLabel(stack: ItemStack): String {
    return if (hasLabel(stack)) stack.orCreateTag.getString("disk_name") else "Disk"
  }

  override fun setLabel(stack: ItemStack, str: String) {
    stack.orCreateTag.putString("disk_name", str)
  }

  fun hasLabel(stack: ItemStack): Boolean = stack.tag?.containsKey("disk_name") ?: false

  override fun getUuid(stack: ItemStack): UUID {
    val tag = stack.orCreateTag
    if (!tag.hasUuid("uuid")) tag.putUuid("uuid", UUID.randomUUID())
    return tag.getUuid("uuid")
  }

  override fun sector(stack: ItemStack, world: ServerWorld, index: Int): Sector? {
    if (index !in 0 until 2048) return null
    val path = world.saveHandler.worldDir.toPath().resolve("rcdisks").resolve(getUuid(stack).toString())
    Files.createDirectories(path.parent)
    return Sector(path, index)
  }

  class Sector(path: Path, val sector: Int) : ItemDisk.Sector {

    override val data = ByteArray(128)

    private val raf = RandomAccessFile(path.toFile(), "rw")

    private val csum = Arrays.hashCode(data)

    init {
      if (raf.length() >= (sector + 1) * 128) {
        raf.seek(sector * 128L)
        raf.read(data)
      }
    }

    override fun isEmpty() = raf.length() <= sector * 128L

    override fun close() {
      val hashCode = Arrays.hashCode(data)
      if (hashCode != csum) {
        val emptyHash = -474025983

        raf.seek(sector * 128L)
        raf.write(data)

        if (hashCode == emptyHash) {
          val buf = ByteArray(128)
          for (i in sector - 1 downTo 0) {
            raf.seek(i * 128L)
            raf.read(buf)
            if (Arrays.hashCode(buf) != emptyHash) {
              raf.setLength((i + 1) * 128L)
              break
            }
          }
        }
      }
      raf.close()
    }
  }

}