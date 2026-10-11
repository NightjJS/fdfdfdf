package com.nightjjs.starfall.client;

import com.nightjjs.starfall.client.particle.StarlightParticle;
import com.nightjjs.starfall.client.render.StarGuardianRenderer;
import com.nightjjs.starfall.client.render.StarWispRenderer;
import com.nightjjs.starfall.registry.ModBlocks;
import com.nightjjs.starfall.registry.ModEntities;
import com.nightjjs.starfall.registry.ModParticles;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.BlockRenderLayer;

public class StarfallClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(ModEntities.STAR_GUARDIAN, StarGuardianRenderer::new);
        EntityRendererRegistry.register(ModEntities.STAR_WISP, StarWispRenderer::new);

        ParticleFactoryRegistry.getInstance().register(ModParticles.STARLIGHT, StarlightParticle.Factory::new);

        BlockRenderLayerMap.putBlock(ModBlocks.STARBLOOM, BlockRenderLayer.CUTOUT);
        BlockRenderLayerMap.putBlock(ModBlocks.STAR_CRYSTAL, BlockRenderLayer.CUTOUT);
        BlockRenderLayerMap.putBlock(ModBlocks.ASTRAL_LEAVES, BlockRenderLayer.CUTOUT_MIPPED);
        BlockRenderLayerMap.putBlock(ModBlocks.STAR_GATE, BlockRenderLayer.TRANSLUCENT);
        BlockRenderLayerMap.putBlock(ModBlocks.STAR_LANTERN, BlockRenderLayer.TRANSLUCENT);
    }
}
