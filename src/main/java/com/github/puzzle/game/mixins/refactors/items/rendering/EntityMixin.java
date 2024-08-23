package com.github.puzzle.game.mixins.refactors.items.rendering;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.github.puzzle.game.engine.items.InstanceModelWrapper;
import com.github.puzzle.game.engine.items.model.ItemModelWrapper;
import com.github.puzzle.game.util.Reflection;
import finalforeach.cosmicreach.entities.Entity;
import finalforeach.cosmicreach.entities.ItemEntity;
import finalforeach.cosmicreach.items.ItemStack;
import finalforeach.cosmicreach.rendering.entities.IEntityModel;
import finalforeach.cosmicreach.rendering.items.ItemEntityModel;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class EntityMixin {

    @Shadow public IEntityModel model;

    @Shadow @Final protected static Matrix4 tmpModelMatrix;

    @Shadow public Vector3 position;

    @Inject(method = "renderModelAfterMatrixSet", at = @At(value = "INVOKE", target = "Lfinalforeach/cosmicreach/rendering/entities/IEntityModel;render(Lfinalforeach/cosmicreach/entities/Entity;Lcom/badlogic/gdx/graphics/Camera;Lcom/badlogic/gdx/math/Matrix4;)V", shift = At.Shift.BEFORE), cancellable = true)
    private void render(Camera worldCamera, CallbackInfo ci) {
        if (model instanceof ItemEntityModel) {
            if (Reflection.getFieldContents(model, "model") instanceof InstanceModelWrapper m0) {
                if (m0.getModel() instanceof ItemModelWrapper m) {
                    ItemStack stack = Reflection.getFieldContents(this, "itemStack");
                    m.renderAsEntity(position, stack, worldCamera, tmpModelMatrix);
                    ci.cancel();
                }
            }
            if (Reflection.getFieldContents(model, "model") instanceof ItemModelWrapper m) {
                ItemStack stack = Reflection.getFieldContents(this, "itemStack");
                m.renderAsEntity(position, stack, worldCamera, tmpModelMatrix);
                ci.cancel();
            }
        }
    }

}
