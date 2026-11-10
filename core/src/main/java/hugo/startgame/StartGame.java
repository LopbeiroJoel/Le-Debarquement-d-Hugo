package hugo.startgame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.video.VideoPlayer;
import com.badlogic.gdx.video.VideoPlayerCreator;
import hugo.JeuHugo;
import hugo.map.EcranCarte;

import java.io.FileNotFoundException;

public final class StartGame extends ScreenAdapter {
    private static final String ASSET_FOLDER = "Start Game/";
    private final JeuHugo game;
    private final FitViewport viewport = new FitViewport(1280, 720);
    private final Rectangle startButton = new Rectangle(520, 100, 240, 65);
    private final Vector2 pointer = new Vector2();
    private final GlyphLayout label = new GlyphLayout();
    private SpriteBatch batch;
    private Texture logo;
    private Texture pixel;
    private BitmapFont font;
    private VideoPlayer video;
    private volatile boolean introFinished;

    public StartGame(JeuHugo game) {
        this.game = game;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        logo = new Texture(Gdx.files.internal(ASSET_FOLDER + "logo.png"));
        font = new BitmapFont();
        font.getData().setScale(2f);

        Pixmap image = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        image.setColor(1, 1, 1, 1);
        image.fill();
        pixel = new Texture(image);
        image.dispose();

        try {
            video = VideoPlayerCreator.createVideoPlayer();
            if (video != null && video.load(Gdx.files.internal(ASSET_FOLDER + "intro.webm"))) {
                video.setOnCompletionListener(file -> introFinished = true);
                video.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
                video.play();
            } else {
                Gdx.app.log("StartGame", "Intro could not be loaded");
                introFinished = true;
            }
        } catch (FileNotFoundException | RuntimeException | LinkageError error) {
            Gdx.app.error("StartGame", "Intro could not be played", error);
            introFinished = true;
        }
    }

    @Override
    public void render(float delta) {
        if (!introFinished && Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            introFinished = true;
        }

        if (introFinished) {
            closeVideo();
            drawMenu();
            return;
        }

        ScreenUtils.clear(0, 0, 0, 1);
        viewport.apply();
        video.update();
        Texture frame = video.getTexture();
        if (frame != null) {
            batch.setProjectionMatrix(viewport.getCamera().combined);
            batch.begin();
            batch.draw(frame, 0, 0, 1280, 720, 0, 0,
                video.getVideoWidth(), video.getVideoHeight(), false, false);
            batch.end();
        }
    }

    private void drawMenu() {
        ScreenUtils.clear(0.08f, 0.10f, 0.08f, 1);
        viewport.apply();
        viewport.unproject(pointer.set(Gdx.input.getX(), Gdx.input.getY()));
        boolean hovered = startButton.contains(pointer);

        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)
                || (hovered && Gdx.input.isButtonJustPressed(Input.Buttons.LEFT))) {
            game.setScreen(new EcranCarte(false));
            dispose();
            return;
        }

        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();
        float logoHeight = 430f;
        float logoWidth = logoHeight * logo.getWidth() / logo.getHeight();
        batch.draw(logo, (1280 - logoWidth) / 2f, 210, logoWidth, logoHeight);
        if (hovered) {
            batch.setColor(0.38f, 0.40f, 0.22f, 1);
        } else {
            batch.setColor(0.28f, 0.31f, 0.16f, 1);
        }
        batch.draw(pixel, startButton.x, startButton.y, startButton.width, startButton.height);
        batch.setColor(1, 1, 1, 1);
        label.setText(font, "Start");
        font.draw(batch, label, startButton.x + (startButton.width - label.width) / 2f,
            startButton.y + (startButton.height + label.height) / 2f);
        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        if (width > 0 && height > 0) {
            viewport.update(width, height, true);
        }
    }

    @Override
    public void pause() {
        if (video != null && !introFinished) {
            video.pause();
        }
    }

    @Override
    public void resume() {
        if (video != null && !introFinished) {
            video.play();
        }
    }

    private void closeVideo() {
        if (video != null) {
            video.dispose();
            video = null;
        }
    }

    @Override
    public void dispose() {
        closeVideo();
        batch.dispose();
        logo.dispose();
        pixel.dispose();
        font.dispose();
    }
}
