package knightminer.ceramics.items;

import knightminer.ceramics.Ceramics;
import knightminer.ceramics.Registration;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.util.LazyLoadedValue;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.function.Supplier;

public enum ArmorMaterials implements ArmorMaterial {
  // name, durability, protection, enchantability, toughness
  CLAY("clay", 4, new int[]{1, 2, 3, 1}, 7, 0.0f, 0.0f, () -> Ingredient.of(Registration.CLAY_PLATE));

  // borrowed from vanilla
  private static final int[] MAX_DAMAGE_ARRAY = new int[]{13, 15, 16, 11};

  // armor fields
  private final String name;
  private final int durabilityFactor;
  private final int[] protection;
  private final int enchantability;
  private final float toughness;
  private final float knockbackResistance;
  private final LazyLoadedValue<Ingredient> repairMaterial;

  ArmorMaterials(String name, int durabilityFactor, int[] protection, int enchantability, float toughness, float knockbackResistance, Supplier<Ingredient> ingredient) {
    this.name = Ceramics.MOD_ID + ":" + name;
    this.durabilityFactor = durabilityFactor;
    this.protection = protection;
    this.enchantability = enchantability;
    this.toughness = toughness;
    this.knockbackResistance = knockbackResistance;
    repairMaterial = new LazyLoadedValue<>(ingredient);
  }

  @Override
  public int getDurabilityForSlot(EquipmentSlot slot) {
    return MAX_DAMAGE_ARRAY[slot.getIndex()] * this.durabilityFactor;
  }

  @Override
  public int getDefenseForSlot(EquipmentSlot slot) {
    return this.protection[slot.getIndex()];
  }

  @Override
  public int getEnchantmentValue() {
    return this.enchantability;
  }

  @Override
  public SoundEvent getEquipSound() {
    return SoundEvents.ARMOR_EQUIP_GENERIC;
  }

  @Override
  public Ingredient getRepairIngredient() {
    return repairMaterial.get();
  }

  @OnlyIn(Dist.CLIENT)
  @Override
  public String getName() {
    return this.name;
  }

  @Override
  public float getToughness() {
    return this.toughness;
  }

  @Override
  public float getKnockbackResistance() {
    return knockbackResistance;
  }
}
