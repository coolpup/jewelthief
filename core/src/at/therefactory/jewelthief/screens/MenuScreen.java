/*
 * Copyright (C) 2016  Christian DeTamble
 *
 * This file is part of Jewel Thief.
 *
 * Jewel Thief is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Jewel Thief is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Jewel Thief.  If not, see <http://www.gnu.org/licenses/>.
 */

package at.therefactory.jewelthief.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.I18NBundle;
import com.badlogic.gdx.utils.viewport.FitViewport;

import at.therefactory.jewelthief.JewelThief;
import at.therefactory.jewelthief.constants.I18NKeys;
import at.therefactory.jewelthief.constants.PrefsKeys;
import at.therefactory.jewelthief.input.MenuScreenInputAdapter;
import at.therefactory.jewelthief.misc.Utils;
import at.therefactory.jewelthief.ui.buttons.GrayButton;
import at.therefactory.jewelthief.ui.buttons.GrayStateButton;

import static at.therefactory.jewelthief.constants.Config.HIGHSCORES_LINE_HEIGHT;
import static at.therefactory.jewelthief.constants.Config.INITIAL_SCROLLBAR_POSITION_Y;
import static at.therefactory.jewelthief.constants.Config.MENU_SCREEN_NUM_STARS;
import static at.therefactory.jewelthief.constants.Config.PLUS_ONE_MAN_INTERVAL;
import static at.therefactory.jewelthief.constants.Config.WINDOW_HEIGHT;
import static at.therefactory.jewelthief.constants.Config.WINDOW_WIDTH;
import static at.therefactory.jewelthief.constants.I18NKeys.ABOUT;
import static at.therefactory.jewelthief.constants.I18NKeys.ABOUT_TEXT;
import static at.therefactory.jewelthief.constants.I18NKeys.FETCHING;
import static at.therefactory.jewelthief.constants.I18NKeys.HIGHSCORES;
import static at.therefactory.jewelthief.constants.I18NKeys.IS;
import static at.therefactory.jewelthief.constants.I18NKeys.LICENSE;
import static at.therefactory.jewelthief.constants.I18NKeys.LICENSE_TEXT;
import static at.therefactory.jewelthief.constants.I18NKeys.MUSIC;
import static at.therefactory.jewelthief.constants.I18NKeys.OFF;
import static at.therefactory.jewelthief.constants.I18NKeys.ON;
import static at.therefactory.jewelthief.constants.I18NKeys.PLAYERNAME;
import static at.therefactory.jewelthief.constants.I18NKeys.RESET_HIGHSCORE;
import static at.therefactory.jewelthief.constants.I18NKeys.SETTINGS;
import static at.therefactory.jewelthief.constants.I18NKeys.SINGLEPLAYER;
import static at.therefactory.jewelthief.constants.I18NKeys.SOUND;
import static at.therefactory.jewelthief.constants.I18NKeys.UPDATE;

public class MenuScreen extends ScreenAdapter {

    public GrayStateButton buttonToggleSound;
    public GrayStateButton buttonToggleMusic;
    public GrayStateButton buttonChangeLanguage;
    public GrayButton buttonResetHighscore;
    public GrayButton buttonChangePlayername;
    public GrayButton buttonStartSinglePlayerGame;
    public GrayButton buttonShowHighscores;
    public GrayButton buttonShowSettings;
    public GrayButton buttonShowAbout;
    public GrayButton buttonUpdateHighscores;
    public final GrayButton buttonExitToMainMenu;
    public GrayButton buttonShowLicense;

    private final SpriteBatch batch;
    private final ShapeRenderer shapeRenderer;
    private final float borderSize;
    private final BitmapFont font;
    private int showLicenseYOffset = 0;
    private float scrollbarPositionY;

    private Sprite spriteTitle;
    private Sprite spritePlayer;
    private Sprite spriteRedPlayer;
    private Sprite spriteBluePlayer;
    private Sprite spritePearl;
    private Sprite spriteSoldier;
    private Sprite spriteSettings;
    private Sprite spriteSkyline;
    private Sprite spriteThere;
    private Sprite spriteFactory;
//    private Sprite spriteBadge;

    private Sprite[] spritesStars;
    private float[] starSpeeds;

    private final OrthographicCamera camera;
    private final FitViewport viewport;

    private final Preferences prefs;
    private MenuState state;
    private MenuScreenInputAdapter inputHandler;
    private I18NBundle bundle;

    private String[] highscores;
    private boolean fetchingHighscores;
    private String aboutText;

    public MenuScreen(SpriteBatch batch, ShapeRenderer shapeRenderer, FitViewport viewport, OrthographicCamera camera) {

        // initialize member variables
        prefs = JewelThief.getInstance().getPreferences();
        this.batch = batch;
        this.shapeRenderer = shapeRenderer;
        this.viewport = viewport;
        this.camera = camera;
        this.bundle = JewelThief.getInstance().getBundle();
        this.highscores = new String[0];
        font = JewelThief.getInstance().getFont();
        updateAboutText(bundle);

        initSprites();
        initStars();
        borderSize = (WINDOW_WIDTH - spriteSkyline.getWidth()) / 2;

        short spaceBetweenButtons = 13; // in pixels
        short buttonWidth = 110, buttonHeight = 90;
        int borderDistance = (WINDOW_WIDTH - 4 * buttonWidth - 3 * spaceBetweenButtons) / 2;
        initButtonsSettings();
        initButtonsMainMenu(buttonWidth, buttonHeight, borderDistance, spaceBetweenButtons);
        initButtonsHighscores(buttonWidth, buttonHeight, borderDistance);

        // button for returning back to main menu when in submenu
        buttonExitToMainMenu = new GrayButton("X", WINDOW_WIDTH - buttonWidth / 2 - borderDistance,
                WINDOW_HEIGHT - buttonHeight / 2 - borderDistance, buttonWidth / 2, buttonHeight / 2);

        positionImagesOnButtons();
        setState(MenuState.ShowMenu);
    }

    private void updateAboutText(I18NBundle bundle) {
        aboutText = bundle.format(ABOUT_TEXT, PLUS_ONE_MAN_INTERVAL, JewelThief.getInstance().getVersionName()); // calls to format in update method cause memory leak
    }

    private void initButtonsHighscores(short buttonWidth, short buttonHeight, int borderDist) {
        // button for fetching highscores
        buttonUpdateHighscores = new GrayButton(bundle.get(UPDATE), borderDist, WINDOW_HEIGHT
                - buttonHeight / 2 - borderDist, buttonWidth / 2 + 5, buttonHeight / 2, true);

        // button for showing license
        buttonShowLicense = new GrayButton(bundle.get(LICENSE), borderDist, WINDOW_HEIGHT - buttonHeight
                / 2 - borderDist, buttonWidth / 2 + 5, buttonHeight / 2, true);
    }

    private void initButtonsMainMenu(short buttonWidth, short buttonHeight, int borderDist, short spaceBetweenButtons) {
        buttonStartSinglePlayerGame = new GrayButton(bundle.get(SINGLEPLAYER), borderDist, borderDist, buttonWidth,
                buttonHeight);
        buttonShowHighscores = new GrayButton(bundle.get(HIGHSCORES), buttonStartSinglePlayerGame.getX()
                + buttonStartSinglePlayerGame.getWidth() + spaceBetweenButtons, buttonStartSinglePlayerGame.getY(), buttonWidth,
                buttonHeight);
        buttonShowSettings = new GrayButton(bundle.get(SETTINGS), buttonShowHighscores.getX()
                + buttonShowHighscores.getWidth() + spaceBetweenButtons, buttonStartSinglePlayerGame.getY(), buttonWidth,
                buttonHeight);
        buttonShowAbout = new GrayButton(bundle.get(ABOUT), buttonShowSettings.getX() + buttonShowSettings.getWidth()
                + spaceBetweenButtons, buttonStartSinglePlayerGame.getY(), buttonWidth, buttonHeight);
        buttonStartSinglePlayerGame.setCaptionOffsetY(25);
        buttonShowHighscores.setCaptionOffsetY(buttonStartSinglePlayerGame.getCaptionOffsetY());
        buttonShowSettings.setCaptionOffsetY(buttonStartSinglePlayerGame.getCaptionOffsetY());
        buttonShowAbout.setCaptionOffsetY(buttonStartSinglePlayerGame.getCaptionOffsetY());
    }

    private void initButtonsSettings() {
        buttonToggleSound = new GrayStateButton(new String[]{
                bundle.get(SOUND) + " " + bundle.get(IS) + " " + bundle.get(OFF),
                bundle.get(SOUND) + " " + bundle.get(IS) + " " + bundle.get(ON)},
                new String[]{"checkbox_unchecked", "checkbox_checked"}, (short) (prefs.getBoolean(PrefsKeys.ENABLE_SOUND) ? 1 : 0),
                false, 16, 66, 130, 40);
        buttonToggleMusic = new GrayStateButton(new String[]{
                bundle.get(MUSIC) + " " + bundle.get(IS) + " " + bundle.get(OFF),
                bundle.get(MUSIC) + " " + bundle.get(IS) + " " + bundle.get(ON)},
                new String[]{"checkbox_unchecked", "checkbox_checked"}, (short) (prefs.getBoolean(PrefsKeys.ENABLE_MUSIC) ? 1 : 0),
                false, buttonToggleSound.getX(), 16, buttonToggleSound.getWidth(), buttonToggleSound.getHeight());
        buttonChangePlayername = new GrayButton(bundle.get(PLAYERNAME), 155, buttonToggleSound.getY(), 100,
                buttonToggleSound.getHeight(), true);
        buttonChangeLanguage = new GrayStateButton(new String[]{"English", "Deutsch"}, new String[]{"flag_usa",
                "flag_austria"}, (short) (prefs.getString("language").equals("en") ? 0 : 1), true,
                buttonChangePlayername.getX(), 16, 100, 40);
        buttonResetHighscore = new GrayButton(bundle.get(RESET_HIGHSCORE), 264, 16, 100, 40, true);
    }

    private void initStars() {
        spritesStars = new Sprite[MENU_SCREEN_NUM_STARS];
        starSpeeds = new float[MENU_SCREEN_NUM_STARS];
        for (int i = 0; i < MENU_SCREEN_NUM_STARS; i++) {
            Sprite star = JewelThief.getInstance().getTextureAtlas().createSprite("star");
            star.setPosition(
                    Utils.randomWithin(borderSize + star.getWidth(), WINDOW_WIDTH - borderSize - star.getWidth()),
                    Utils.randomWithin(borderSize, WINDOW_HEIGHT - star.getHeight()));
            spritesStars[i] = star;
            starSpeeds[i] = Utils.randomWithin(.01f, .2f);
        }
    }

    private void initSprites() {
        spriteThere = new Sprite(new Texture("there.png"));
        spriteFactory = new Sprite(new Texture("factory.png"));

        spriteTitle = new Sprite(new Texture("title.png"));
        spriteTitle.setPosition(WINDOW_WIDTH / 2 - spriteTitle.getWidth() / 2, WINDOW_HEIGHT / 2 + 70
                + showLicenseYOffset);

        spriteSkyline = new Sprite(new Texture("skyline.png"));
        spriteSkyline.setPosition(borderSize, 115);

        spritePlayer = JewelThief.getInstance().getTextureAtlas().createSprite("WhitePlayer");
        spriteRedPlayer = JewelThief.getInstance().getTextureAtlas().createSprite("RedPlayer");
        spriteBluePlayer = JewelThief.getInstance().getTextureAtlas().createSprite("BluePlayer");
        spritePearl = JewelThief.getInstance().getTextureAtlas().createSprite("Pearl");
        spriteSoldier = JewelThief.getInstance().getTextureAtlas().createSprite("Soldier");
        spriteSettings = JewelThief.getInstance().getTextureAtlas().createSprite("settings");
//        spriteBadge = new Sprite(new Texture("google-play-badge.png"));

        spritePlayer.setFlip(true, false);
        spriteRedPlayer.setFlip(true, false);
        spriteBluePlayer.setFlip(true, false);
    }

    @Override
    public void show() {
        inputHandler = new MenuScreenInputAdapter(this, viewport);
        Gdx.input.setInputProcessor(inputHandler);
        Gdx.input.setCatchBackKey(true);
        scrollbarPositionY = INITIAL_SCROLLBAR_POSITION_Y;

        // play background music
        if (prefs.getBoolean(PrefsKeys.ENABLE_MUSIC)) {
            JewelThief.getInstance().playMusicFile(false);
        }
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    @Override
    public void dispose() {
        super.dispose();
        shapeRenderer.dispose();
    }

    private void update(float delta) {
        inputHandler.update(delta);

        // move spritesStars
        for (int i = 0; i < spritesStars.length; i++) {
            Sprite star = spritesStars[i];
            if (star.getY() < borderSize) {
                star.setY(WINDOW_HEIGHT - star.getHeight());
            } else {
                star.setY(star.getY() - starSpeeds[i]);
            }
        }
    }

    private void positionImagesOnButtons() {
        spritePlayer.setPosition(buttonStartSinglePlayerGame.getX() + buttonStartSinglePlayerGame.getWidth() / 2 - spritePlayer.getWidth() / 2
                + buttonStartSinglePlayerGame.getPressedOffset(), buttonStartSinglePlayerGame.getY() + buttonStartSinglePlayerGame.getHeight() / 2
                - 5 - buttonStartSinglePlayerGame.getPressedOffset());
        spriteRedPlayer.setPosition(buttonShowHighscores.getX() + buttonShowHighscores.getWidth() / 2 - spriteRedPlayer.getWidth() / 2
                + buttonShowHighscores.getPressedOffset(), buttonShowHighscores.getY() + buttonShowHighscores.getHeight() / 2 - 3
                - buttonShowHighscores.getPressedOffset());
        spriteBluePlayer.setPosition(spriteRedPlayer.getX() - 5, spriteRedPlayer.getY() - 5);
        spriteSettings.setPosition(buttonShowSettings.getX() + buttonShowSettings.getWidth() / 2 - spriteSettings.getWidth() / 2
                + buttonShowSettings.getPressedOffset(), 52 - buttonShowSettings.getPressedOffset());
        spriteSoldier.setPosition(buttonShowAbout.getX() + buttonShowAbout.getWidth() / 2 - spriteSoldier.getWidth() / 2
                        + buttonShowAbout.getPressedOffset() - 1, 63 - buttonShowAbout.getPressedOffset());
        spritePearl.setPosition(buttonShowAbout.getX() + buttonShowAbout.getWidth() / 2 - spriteSoldier.getWidth() / 2
                        + buttonShowAbout.getPressedOffset(), 50 - buttonShowAbout.getPressedOffset());
    }

    @Override
    public void render(float delta) {

        // clear the screen
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // simulate world
        update(delta);

        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeType.Filled);
        shapeRenderer.setColor(0, 0, 0.7f, 1); // dark blue background color
//        shapeRenderer.rect(0, spriteSkyline.getY(), WINDOW_WIDTH, WINDOW_HEIGHT - spriteSkyline.getY());
        shapeRenderer.rect(0, spriteSkyline.getY(), WINDOW_WIDTH, WINDOW_HEIGHT - spriteSkyline.getY());
        shapeRenderer.end();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        for (Sprite star : spritesStars) {
            star.draw(batch);
        }

        spriteSkyline.draw(batch);

        if (!state.equals(MenuState.ShowHighscores) && showLicenseYOffset == 0)
            spriteTitle.draw(batch);
        batch.end();

        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeType.Filled);
        shapeRenderer.setColor(Color.BLACK);
        shapeRenderer.rect(0, 0, WINDOW_WIDTH, state.equals(MenuState.ShowHighscores) ? WINDOW_HEIGHT - 70 : spriteSkyline.getY());

//        if (state.equals(MenuState.ShowPromo)) {
//            shapeRenderer.end();
//            batch.begin();
//            batch.draw(spriteBadge, 0, 0, (int) (646 / 2.1), (int) (250 / 2.1));
//            font.setColor(Color.WHITE);
//            font.draw(batch, Config.EMAIL, 300, 45);
//            batch.draw(spriteTheRefactory, 310, 70, 180, 14);
//            batch.end();
//        } else {
            // buttons
            if (state.equals(MenuState.ShowAbout) || state.equals(MenuState.ShowHighscores) || state.equals(MenuState.ShowSettings)) {
                buttonExitToMainMenu.renderShape(shapeRenderer);
                switch (getState()) {
                    case ShowHighscores:
                        buttonUpdateHighscores.renderShape(shapeRenderer);
                        break;
                    case ShowSettings:
                        buttonToggleSound.renderShape(shapeRenderer);
                        buttonToggleMusic.renderShape(shapeRenderer);
                        buttonChangePlayername.renderShape(shapeRenderer);
                        buttonChangeLanguage.renderShape(shapeRenderer);
                        buttonResetHighscore.renderShape(shapeRenderer);
                        break;
                    case ShowAbout:
                        buttonShowLicense.renderShape(shapeRenderer);
                        break;
                }
            } else {
                buttonStartSinglePlayerGame.renderShape(shapeRenderer);
                buttonShowHighscores.renderShape(shapeRenderer);
                buttonShowSettings.renderShape(shapeRenderer);
                buttonShowAbout.renderShape(shapeRenderer);
            }
            shapeRenderer.end();

            batch.begin();
            if (state.equals(MenuState.ShowAbout) || state.equals(MenuState.ShowHighscores) ||state.equals(MenuState.ShowSettings)) {
                buttonExitToMainMenu.renderCaption(batch);
            }
            switch (getState()) {
                case ShowHighscores:
                    buttonUpdateHighscores.setCaption(bundle.get(UPDATE));
                    buttonUpdateHighscores.renderCaption(batch);
                    if (fetchingHighscores) {
                        font.setColor(Color.WHITE);
                        font.draw(batch, bundle.get(FETCHING) + "...", 15, 205);
                    } else {
                        if (highscores != null) {

                            // lines of highscores
                            for (int i = 0; i < highscores.length; i++) {
                                font.setColor(i == getMyRank() ? Color.GREEN : Color.WHITE);
                                float yOfHighscoreLine = (205 - i * HIGHSCORES_LINE_HEIGHT + inputHandler.getDeltaY());
                                if (yOfHighscoreLine < spriteSkyline.getY() // lines disappear when above spriteSkyline sprite
                                		&& yOfHighscoreLine > 0) { // lines disappear when outside of the viewport
                                    font.draw(batch, highscores[i], 15, yOfHighscoreLine);
                                }
                            }

                            // scrollbar
                            if (highscores.length > 0) {
                                font.draw(batch, "^", WINDOW_WIDTH - 20, INITIAL_SCROLLBAR_POSITION_Y + 5);
                                font.draw(batch, "#", WINDOW_WIDTH - 20, Math.min(INITIAL_SCROLLBAR_POSITION_Y, scrollbarPositionY));
                                font.getData().setScale(1, -1);
                                font.draw(batch, "^", WINDOW_WIDTH - 20, 10);
                                font.getData().setScale(1, 1);
                            }
                        }
                    }
                    break;
                case ShowSettings:
                    // playername
                    buttonChangePlayername.setCaption(prefs.getString(PrefsKeys.PLAYER_NAME).trim().length() == 0 ? "<"
                            + bundle.get(PLAYERNAME) + ">" : bundle.get(PLAYERNAME) + ": " + prefs.getString(PrefsKeys.PLAYER_NAME));
                    buttonChangePlayername.renderCaption(batch);
                    font.setColor(Color.WHITE);

                    // sound
                    buttonToggleSound.setCaption(bundle.get(SOUND) + " " + bundle.get(IS) + " "
                            + (prefs.getBoolean(PrefsKeys.ENABLE_SOUND) ? bundle.get(ON) : bundle.get(OFF)));
                    buttonToggleSound.renderCaption(batch);

                    // music
                    buttonToggleMusic.setCaption(bundle.get(MUSIC) + " " + bundle.get(IS) + " "
                            + (prefs.getBoolean(PrefsKeys.ENABLE_MUSIC) ? bundle.get(ON) : bundle.get(OFF)));
                    buttonToggleMusic.renderCaption(batch);

                    // language
                    buttonChangeLanguage.renderCaption(batch);

                    // reset highscore
                    buttonResetHighscore.setCaption(bundle.get(RESET_HIGHSCORE));
                    buttonResetHighscore.renderCaption(batch);
                    break;
                case ShowAbout:
                    font.setColor(Color.WHITE);
                    font.draw(batch, aboutText, 15, 100 + showLicenseYOffset);
                    if (showLicenseYOffset > 0) {
                        font.draw(batch, bundle.get(LICENSE_TEXT), 15, showLicenseYOffset + 2);
                    }
                    batch.draw(spriteThere, 145, showLicenseYOffset + 19, spriteThere.getWidth()/3, spriteThere.getHeight()/3);
                    batch.draw(spriteFactory, 200, showLicenseYOffset + 19, spriteFactory.getWidth()/3, spriteFactory.getHeight()/3);
                    buttonShowLicense.setCaption(bundle.get(LICENSE));
                    buttonShowLicense.renderCaption(batch);
                    break;
                default:
                    // buttons' icons
                    spritePlayer.draw(batch);
                    spriteRedPlayer.draw(batch);
                    spriteBluePlayer.draw(batch);
                    spritePearl.draw(batch);
                    spriteSoldier.draw(batch);
                    spriteSettings.draw(batch);

                    // buttons themselves
                    buttonStartSinglePlayerGame.setCaption(bundle.get(SINGLEPLAYER));
                    buttonShowHighscores.setCaption(bundle.get(HIGHSCORES));
                    buttonShowSettings.setCaption(bundle.get(SETTINGS));
                    buttonShowAbout.setCaption(bundle.get(ABOUT));
                    buttonStartSinglePlayerGame.renderCaption(batch);
                    buttonShowHighscores.renderCaption(batch);
                    buttonShowSettings.renderCaption(batch);
                    buttonShowAbout.renderCaption(batch);
                    break;
            }
            batch.end();
//        }
    }

    public void releaseAllButtons() {
        buttonStartSinglePlayerGame.release();
        buttonShowHighscores.release();
        buttonShowSettings.release();
        buttonShowAbout.release();
        buttonExitToMainMenu.release();
        buttonUpdateHighscores.release();
        buttonToggleSound.release();
        buttonToggleMusic.release();
        buttonChangePlayername.release();
        buttonResetHighscore.release();
        buttonShowLicense.release();
    }

    public void setFetchingHighscores(boolean fetchingHighscores) {
        this.fetchingHighscores = fetchingHighscores;
    }

    public int getMyRank() {
        return prefs.contains(PrefsKeys.MY_RANK) ? prefs.getInteger(PrefsKeys.MY_RANK) : -1;
    }

    public void setMyRank(int myRank) {
        prefs.putInteger(PrefsKeys.MY_RANK, myRank);
        prefs.flush();
    }

    public void setScrollbarPositionY(float scrollbarPositionY) {
        this.scrollbarPositionY = scrollbarPositionY;
    }

    public Sprite getSpriteTitle() {
        return spriteTitle;
    }

    public MenuState getState() {
        return state;
    }

    public void setState(MenuState state) {
        this.state = state;
        if (state.equals(MenuState.ShowMenu)) {
            spriteSkyline.setY(115);
        } else if (state.equals(MenuState.ShowHighscores)) {
            spriteSkyline.setY(WINDOW_HEIGHT - 75);
            if (prefs.getString(PrefsKeys.PLAYER_NAME) == null || prefs.getString(PrefsKeys.PLAYER_NAME).length() == 0) {
                JewelThief.getInstance().toast(bundle.get(I18NKeys.SET_YOUT_PLAYER_NAME_TO_SEE_HIGHSCORES), true);
            }
        }
    }

    public String[] getHighscores() {
        return highscores;
    }

    public void setHighscores(String[] string) {
        highscores = string;
    }

    public int getShowLicenseYOffset() {
        return showLicenseYOffset;
    }

    public void setShowLicenseYOffset(int showLicenseYOffset) {
        this.showLicenseYOffset = showLicenseYOffset;
        if (showLicenseYOffset > 0) {
            spriteSkyline.setY(WINDOW_HEIGHT - 173 + showLicenseYOffset);
        } else {
            spriteSkyline.setY(115);
        }
    }

    public void handleTouchOnStars(Vector3 touchCoordinates) {
        if (touchCoordinates.y > spriteSkyline.getY()) {
            for (Sprite star : spritesStars) {
                if (Utils.within(touchCoordinates.x, star.getX(), star.getX() + star.getWidth())
                        && Utils.within(touchCoordinates.y, star.getY(), star.getY() + star.getHeight())) {
                    JewelThief.getInstance().playCymbalSound();
                    star.setPosition(star.getX(), WINDOW_HEIGHT + star.getHeight() * 3);
                }
            }
        }
    }

    public void pressOrReleaseButtons(Vector3 screenCoord) {
        switch (getState()) {
            case ShowAbout:
                Utils.pressOrReleaseButtons(screenCoord, buttonExitToMainMenu, buttonShowLicense);
                break;
            case ShowHighscores:
                Utils.pressOrReleaseButtons(screenCoord, buttonExitToMainMenu, buttonUpdateHighscores);
                break;
            case ShowSettings:
                Utils.pressOrReleaseButtons(screenCoord, buttonExitToMainMenu, buttonToggleSound,
                        buttonToggleMusic, buttonChangePlayername, buttonChangeLanguage, buttonResetHighscore);
                break;
            default:
                Utils.pressOrReleaseButtons(screenCoord, buttonStartSinglePlayerGame, buttonShowHighscores, buttonShowSettings, buttonShowAbout);
                break;
        }
        positionImagesOnButtons();
    }

    public void setBundle(I18NBundle bundle) {
        this.bundle = bundle;
        updateAboutText(bundle);
    }

    public enum MenuState {
        ShowMenu,
        ShowSettings,
        ShowAbout,
        ShowHighscores,
        //ShowPromo
    }

}
