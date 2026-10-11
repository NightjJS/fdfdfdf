package com.nightjjs.starfall.client.render;

import com.nightjjs.starfall.Starfall;
import com.nightjjs.starfall.entity.StarGuardianEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.BlazeEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

/**
 * The Star Guardian is drawn with the vanilla blaze model (a core cube orbited by rods), scaled up,
 * fully bright, with its own crystalline star texture.
 */
public class StarGuardianRenderer extends MobEntityRenderer<StarGuardianEntity, LivingEntityRenderState, BlazeEntityModel> {
    private static final Identifier TEXTURE = Starfall.id("textures/entity/star_guardian.png");
    private static final float SCALE = 2.2f;

    public StarGuardianRenderer(EntityRendererFactory.Context context) {
        super(context, new BlazeEntityModel(context.getPart(EntityModelLayers.BLAZE)), 1.1f);
    }

    @Override
    public LivingEntityRenderState createRenderState() {
        return new LivingEntityRenderState();
    }

    @Override
    public Identifier getTexture(LivingEntityRenderState state) {
        return TEXTURE;
    }

    @Override
    protected void scale(LivingEntityRenderState state, MatrixStack matrices) {
        matrices.scale(SCALE, SCALE, SCALE);
    }

    @Override
    protected int getBlockLight(StarGuardianEntity entity, BlockPos pos) {
        return 15;
    }
}
