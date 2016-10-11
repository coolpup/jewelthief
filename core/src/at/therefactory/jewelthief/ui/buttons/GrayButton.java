package at.therefactory.jewelthief.ui.buttons;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import at.therefactory.jewelthief.JewelThief;

import static at.therefactory.jewelthief.constants.Colors.BUTTON;
import static at.therefactory.jewelthief.constants.Colors.BUTTON_BORDER_DARK;
import static at.therefactory.jewelthief.constants.Colors.BUTTON_BORDER_LIGHT;
import static at.therefactory.jewelthief.constants.Colors.BUTTON_PRESSED;
import static at.therefactory.jewelthief.constants.Colors.BUTTON_PRESSED_BORDER_DARK;
import static at.therefactory.jewelthief.constants.Config.FONT_OFFSET_ON_BUTTON_PRESS;

/**
 * Created by Christian on 08.06.2016.
 */
public class GrayButton {

    final float x;
    final float y;

    private float width;
    final float height;
    float pressedOffset;
    float xCaptionOffset;
    float yCaptionOffset;

	private boolean adaptWidthToCaption;
    private final GlyphLayout layout;
    final BitmapFont font;
    private Color captionColor;
    private short borderSize;
    private String caption;

    public GrayButton(String caption, float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.caption = caption;
        font = JewelThief.getInstance().getFont();
        captionColor = Color.DARK_GRAY;
        borderSize = 3;

        // horizontally and vertically align to center
        layout = new GlyphLayout(font, caption);
        xCaptionOffset = width/2 - layout.width/2;
        yCaptionOffset = height/2 + layout.height/2;
    }
    
    public GrayButton(String caption, float x, float y, float width, float height, boolean adaptWidthToCaption) {
    	this(caption, x, y, width, height);
    	this.adaptWidthToCaption = adaptWidthToCaption;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    public void renderShape(ShapeRenderer sr) {
        if (isPressed()) {
            sr.setColor(BUTTON_PRESSED_BORDER_DARK);
            sr.rect(x, y, borderSize, height);
            sr.rect(x + borderSize, y + height - borderSize, width - borderSize, borderSize);
            sr.setColor(BUTTON_PRESSED);
            sr.rect(x + borderSize, y, width - borderSize, height - borderSize);
        } else {
            sr.setColor(BUTTON_BORDER_LIGHT);
            sr.rect(x, y, borderSize, height);
            sr.rect(x + borderSize, y + height - borderSize, width - borderSize, borderSize);
            sr.setColor(BUTTON);
            sr.rect(x + borderSize, y + borderSize, width - borderSize * 2, height - borderSize * 2);
            sr.setColor(BUTTON_BORDER_DARK);
            sr.rect(x + borderSize, y, width - borderSize, borderSize);
            sr.rect(x + width - borderSize, y + borderSize, borderSize, height - borderSize * 2);
        }
    }

    public void renderCaption(SpriteBatch batch) {
        font.setColor(captionColor);
        font.draw(batch, caption, x + xCaptionOffset + pressedOffset, y + yCaptionOffset - pressedOffset);
    }

    public void release() {
        pressedOffset = 0;
    }

    public void press() {
        pressedOffset = FONT_OFFSET_ON_BUTTON_PRESS;
    }

    public boolean isPressed() {
        return (pressedOffset == FONT_OFFSET_ON_BUTTON_PRESS);
    }

    public void setCaption(String caption) {
        this.caption = caption;
        layout.setText(font, caption);
        xCaptionOffset = width/2 - layout.width/2;
    	if (adaptWidthToCaption) {
	        width = layout.width + 20 + borderSize *2;
    	}
    }

    public float getPressedOffset() {
        return pressedOffset;
    }

    public void setBorderSize(short borderSize) {
        this.borderSize = borderSize;
    }

    public void setCaptionOffsetY(float yOffset) {
        this.yCaptionOffset = yOffset;
    }

    public float getCaptionOffsetY() {
        return yCaptionOffset;
    }

}
