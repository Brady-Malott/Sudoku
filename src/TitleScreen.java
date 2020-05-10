package com.sudoku.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.sudoku.game.Sudoku.difficulty;

public class TitleScreen implements Screen, InputProcessor {

	private Sudoku game;
	private SpriteBatch batch;
	
	private Sprite background;
	
	private difficulty[] difficulties;
	private difficulty currDiff;
	private BitmapFont selectDiffPrompt;
	private String prompt;

	public TitleScreen(Sudoku game, SpriteBatch batch) {
		this.game = game;
		this.batch = batch;
	}

	@Override
	public void show() {
		background = new Sprite(new Texture("titleScreen.png"));
		difficulties = difficulty.values();
		currDiff = difficulty.EASY;
		prompt = "Use the up and down arrow keys to increase or\ndecrease the difficulty\nCurrent difficulty: "
				+ currDiff.name();
		selectDiffPrompt = new BitmapFont();
		selectDiffPrompt.setColor(Color.SKY);
		Gdx.input.setInputProcessor(this);
	}

	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(1, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		batch.begin();
		background.draw(batch);
		selectDiffPrompt.draw(batch, prompt, 536, 730);
		batch.end();
	}

	@Override
	public void resize(int width, int height) {

	}

	@Override
	public void pause() {

	}

	@Override
	public void resume() {

	}

	@Override
	public void hide() {

	}

	@Override
	public void dispose() {
		selectDiffPrompt.dispose();
	}

	@Override
	public boolean keyDown(int keycode) {
		if (keycode == Keys.SPACE) {
			dispose();
			game.setScreen(new GameScreen(game, batch, currDiff));
		}
		int diffIndex = currDiff.ordinal();
		if (keycode == Keys.UP && diffIndex < difficulties.length - 1) {
			currDiff = difficulties[++diffIndex];
			prompt = "Use the up and down arrow keys to increase or\ndecrease the difficulty\nCurrent difficulty: "
					+ currDiff.name();
		}
		if (keycode == Keys.DOWN && diffIndex > 0) {
			currDiff = difficulties[--diffIndex];
			prompt = "Use the up and down arrow keys to increase or\ndecrease the difficulty\nCurrent difficulty: "
					+ currDiff.name();
		}
		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		return false;
	}

	@Override
	public boolean keyTyped(char character) {
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		return false;
	}

	@Override
	public boolean scrolled(int amount) {
		return false;
	}

}
