package com.nightjjs.starfall.client.render;

import com.nightjjs.starfall.Starfall;
import com.nightjjs.starfall.entity.StarWispEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.BlazeEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

/**
 * Star Wisps reuse the blaze model at a small scale with a soft glowing texture.
 */
public class StarWispRenderer extends MobEntityRenderer<StarWispEntity, LivingEntityRenderState, BlazeEntityModel> {
    private static final Identifier TEXTURE = Starfall.id("textures/entity/star_wisp.png");
    private static final float SCALE = 0.45f;

    public StarWispRenderer(EntityRendererFactory.Context context) {
        super(context, new BlazeEntityModel(context.getPart(EntityModelLayers.BLAZE)), 0.25f);
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
    protected int getBlockLight(StarWispEntity entity, BlockPos pos) {
        return 15;
    }
}
