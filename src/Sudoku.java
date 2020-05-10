package com.sudoku.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Sudoku extends Game {

	SpriteBatch batch;
	static enum difficulty {
		EASY, INTERMEDIATE, ADVANCED, EXPERT
	}
	
	@Override
	public void create() {
		batch = new SpriteBatch();
		setScreen(new TitleScreen(this, batch));
	}
	
	@Override
	public void render() {
		super.render();
	}
}