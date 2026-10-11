package com.nightjjs.starfall.registry;

import com.nightjjs.starfall.Starfall;
import net.minecraft.item.equipment.ArmorMaterial;
import net.minecraft.item.equipment.EquipmentAsset;
import net.minecraft.item.equipment.EquipmentAssetKeys;
import net.minecraft.item.equipment.EquipmentType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.sound.SoundEvents;

import java.util.Map;

public final class ModArmorMaterials {
    private ModArmorMaterials() {
    }

    /** Equipment asset: assets/starfall/equipment/starforged.json. */
    public static final RegistryKey<EquipmentAsset> STARFORGED_ASSET =
            RegistryKey.of(EquipmentAssetKeys.REGISTRY_KEY, Starfall.id("starforged"));

    /** A touch above netherite, with higher enchantability. */
    public static final ArmorMaterial STARFORGED = new ArmorMaterial(
            40,
            Map.of(
                    EquipmentType.HELMET, 4,
                    EquipmentType.CHESTPLATE, 9,
                    EquipmentType.LEGGINGS, 7,
                    EquipmentType.BOOTS, 4,
                    EquipmentType.BODY, 11
            ),
            20,
            SoundEvents.ITEM_ARMOR_EQUIP_NETHERITE,
            3.5f,
            0.15f,
            ModTags.STAR_TOOL_MATERIALS,
            STARFORGED_ASSET
    );
}
