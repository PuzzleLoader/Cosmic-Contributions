package com.github.puzzle.game.engine.items;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.github.puzzle.core.Identifier;
import com.github.puzzle.core.resources.PuzzleGameAssetLoader;
import com.github.puzzle.core.resources.ResourceLocation;
import com.github.puzzle.game.engine.items.model.IPuzzleItemModel;
import com.github.puzzle.game.engine.shaders.ItemShader;
import com.github.puzzle.game.items.IModItem;
import com.github.puzzle.game.items.data.DataTagManifest;
import com.github.puzzle.game.items.data.attributes.IdentifierDataAttribute;
import com.github.puzzle.game.items.data.attributes.PairAttribute;
import com.github.puzzle.game.items.data.attributes.ResourceLocationDataAttribute;
import com.github.puzzle.game.items.puzzle.ItemThingWrapper;
import com.github.puzzle.game.util.DataTagUtil;
import com.llamalad7.mixinextras.lib.apache.commons.tuple.ImmutablePair;
import com.llamalad7.mixinextras.lib.apache.commons.tuple.Pair;
import finalforeach.cosmicreach.blocks.BlockPosition;
import finalforeach.cosmicreach.entities.Entity;
import finalforeach.cosmicreach.gamestates.InGame;
import finalforeach.cosmicreach.items.Item;
import finalforeach.cosmicreach.items.ItemStack;
import finalforeach.cosmicreach.items.ItemThing;
import finalforeach.cosmicreach.rendering.MeshData;
import finalforeach.cosmicreach.rendering.RenderOrder;
import finalforeach.cosmicreach.rendering.shaders.GameShader;
import finalforeach.cosmicreach.ui.UI;
import finalforeach.cosmicreach.world.Sky;
import finalforeach.cosmicreach.world.Zone;

import java.lang.ref.WeakReference;
import java.util.HashMap;

public class ItemThingItemModel implements IPuzzleItemModel {

    Mesh mesh;
    Texture texture;

    GameShader program = new MeshData(ItemShader.DEFAULT_ITEM_SHADER, RenderOrder.FULLY_TRANSPARENT).shader;

    static Matrix4 noRotMtrx = new Matrix4();
    static Camera itemCam2 = new OrthographicCamera(100F, 100F);

    static {
        noRotMtrx.setTranslation(0, -1f, 0);

        itemCam2.position.set(0, 0, 2);
        itemCam2.lookAt(0, 0, 0);
        ((OrthographicCamera) itemCam2).zoom = 0.027F;
        itemCam2.update();
    }

    ItemThingWrapper item;

    public ItemThingItemModel(ItemThingWrapper item) {
        this.item = item;
        String texturePath = (String) ((ItemThing) item.getParentItem()).itemProperties.get("texture");
        this.texture = PuzzleGameAssetLoader.LOADER.loadSync(texturePath, Texture.class);
//        texture.getTextureData().prepare();
        texture = ItemModelBuilder.flip(texture);
        mesh = ItemModelBuilder.build2_5DMesh(texture);
    }

    static final Color tintColor = new Color();
    static final BlockPosition tmpBlockPos = new BlockPosition(null, 0, 0, 0);

    public void renderGeneric(Vector3 pos, ItemStack stack, Camera cam, Matrix4 tmpMatrix, boolean isSlot) {
        if (isSlot) {
            tintColor.set(Color.WHITE);
        } else {
            Zone zone = InGame.getLocalPlayer().getZone(InGame.world);
            try {
                Entity.setLightingColor(zone, pos, Sky.currentSky.currentAmbientColor, tintColor, tmpBlockPos, tmpBlockPos);
            } catch (Exception e) {
                tintColor.set(Color.WHITE);
            }
        }

        program.bind(cam);
        program.bindOptionalMatrix4("u_projViewTrans", cam.combined);
        program.bindOptionalMatrix4("u_modelMat", tmpMatrix);
        program.bindOptionalUniform4f("tintColor", tintColor);
        program.bindOptionalTexture("texDiffuse", texture, 0);
        mesh.render(program.shader, GL20.GL_TRIANGLES);
        program.unbind();
    }

    @Override
    public void renderInSlot(Vector3 pos, ItemStack stack, Camera slotCamera, Matrix4 tmpMatrix, boolean useAmbientLighting) {
        renderGeneric(new Vector3(0, 0, 0), stack, slotCamera, noRotMtrx, true);
    }

    @Override
    public void dispose(WeakReference<Item> itemRef) {
        texture.dispose();
        mesh.dispose();
    }

    @Override
    public Camera getItemSlotCamera() {
        return itemCam2;
    }

    static final PerspectiveCamera heldItemCamera = new PerspectiveCamera();

    @Override
    public void renderAsHeldItem(Vector3 pos, ItemStack stack2, Camera handCam, float popUpTimer, float maxPopUpTimer, float swingTimer, float maxSwingTimer) {
        Matrix4 tmpHeldMat4 = new Matrix4();
        ItemStack stack = UI.hotbar.getSelectedItemStack();

        heldItemCamera.fieldOfView = 50.0F;
        heldItemCamera.viewportHeight = handCam.viewportHeight;
        heldItemCamera.viewportWidth = handCam.viewportWidth;
        heldItemCamera.near = handCam.near;
        heldItemCamera.far = handCam.far;
        heldItemCamera.update();
        tmpHeldMat4.idt();
        float swing;
        if (popUpTimer > 0.0F) {
            swing = (float)Math.pow(popUpTimer / maxPopUpTimer, 2.0);
            tmpHeldMat4.translate(0.0F, -1.0F * swing, 0.0F);
        }

        tmpHeldMat4.translate(1.65F, -1.25F, -2.5F);
        tmpHeldMat4.rotate(Vector3.Y, -75.0F);
        tmpHeldMat4.translate(-0.25F, -0.25F, -0.25F);
        if (swingTimer > 0.0F) {
            swing = swingTimer / maxSwingTimer;
            swing = 1.0F - (float)Math.pow(swing - 0.5F, 2.0) / 0.25F;
            tmpHeldMat4.rotate(Vector3.Z, 90.0F * swing);
            float st = -swing;
            tmpHeldMat4.translate(st * 2.0F, st, 0.0F);
        }

        if (item.isTool()) {
            tmpHeldMat4.translate(.6f,0, 0);
            tmpHeldMat4.translate(0,-.2f, 0);
            tmpHeldMat4.rotate(new Vector3(0, 0, 1), 20);
            tmpHeldMat4.rotate(new Vector3(1, 0, 0), 15);
        }

        Gdx.gl.glDisable(2929);
        renderGeneric(pos, stack, heldItemCamera, tmpHeldMat4, false);
        Gdx.gl.glEnable(2929);
    }

    @Override
    public void renderAsEntity(Vector3 pos, ItemStack stack, Camera entityCam, Matrix4 tmpMatrix) {
        tmpMatrix.translate(0.5F, 0.2F, 0.5F);
        tmpMatrix.scale(0.7f, 0.7f, 0.7f);
        renderGeneric(pos, stack, entityCam, tmpMatrix, false);
    }
}
