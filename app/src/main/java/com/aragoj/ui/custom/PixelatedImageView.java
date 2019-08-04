package com.aragoj.ui.custom;

import com.sun.javafx.sg.prism.NGImageView;
import com.sun.javafx.sg.prism.NGNode;
import com.sun.prism.Graphics;
import com.sun.prism.Texture;
import com.sun.prism.impl.BaseResourceFactory;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * A somewhat hackish way to accomplish pixelated image views in JavaFX.
 * Should be refactored later if JavaFX devs decide to give a more sane and approachable way.
 *
 * See: https://stackoverflow.com/a/39294505
 */
@SuppressWarnings("restriction")
public class PixelatedImageView extends ImageView {

    public PixelatedImageView(Image image){
        super(image);
    }

    @Override protected NGNode impl_createPeer() {
        return new NGImageView() {
            private com.sun.prism.Image image;

            @Override public void setImage(Object img) {
                super.setImage(img);
                image = (com.sun.prism.Image) img;
            }

            @Override protected void renderContent(Graphics g) {
                BaseResourceFactory factory = (BaseResourceFactory) g.getResourceFactory();
                Texture tex = factory.getCachedTexture(image, Texture.WrapMode.CLAMP_TO_EDGE);
                tex.setLinearFiltering(false);
                tex.unlock();
                super.renderContent(g);
            }
        };
    }
}