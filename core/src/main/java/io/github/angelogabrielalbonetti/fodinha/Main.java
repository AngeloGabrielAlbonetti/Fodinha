package io.github.angelogabrielalbonetti.fodinha;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import java.io.IOException;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends Game {
    Servidor servidor = new Servidor();

    @Override
    public void create() {
        try {
            servidor.ServidorIniciar();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


