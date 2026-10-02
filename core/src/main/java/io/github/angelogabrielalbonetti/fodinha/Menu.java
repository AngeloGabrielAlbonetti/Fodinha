package io.github.angelogabrielalbonetti.fodinha;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class Menu implements Screen {
    SpriteBatch batch;
    Texture fundo;
    Viewport viewport;
    OrthographicCamera orthographicCamera;



    @Override
    public void show() {
        batch = new SpriteBatch();
        fundo = new Texture("imagem/pixellab-Create-a-2D-first-person-pixel-1789694938852.png");

        orthographicCamera = new OrthographicCamera();
        viewport = new FitViewport(800f, 225f, orthographicCamera);
        viewport.apply();
        orthographicCamera.position.set(800f / 2, 225f / 2, 0);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0, 1);

        orthographicCamera.update();
        batch.setProjectionMatrix(orthographicCamera.combined);

        batch.begin();
        batch.draw(fundo, 0, 0, 800f, 225f);
        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}

    @Override
    public void dispose() {
        batch.dispose();
        fundo.dispose();
    }
}

