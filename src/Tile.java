package com.sudoku.game;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

public class Tile {

	private Rectangle region;
	private int number;
	private int note;
	private int row;
	private int col;
	private int testNum;
	private boolean isFilled;
	private Sprite numSprite;
	private Sprite noteSprite;
	private boolean resetAfterSolve;

	public Tile(Rectangle region, int row, int col, int number, boolean isClue) {
		setRegion(region);
		this.row = row;
		this.col = col;
		resetAfterSolve = false;
		setNumber(number);
		isFilled = isClue;
		if (isClue) {
			testNum = this.number;
			numSprite.setAlpha(1);
		}
	}

	public Rectangle getRegion() {
		return region;
	}

	public void setRegion(Rectangle region) {
		this.region = region;
	}

	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
		String spritePath = number + ".png";
		numSprite = new Sprite(new Texture(spritePath));
		numSprite.setPosition(getX() + 16, getY() + 16);
		if (!isFilled) {
		numSprite.setAlpha(0);
		}
	}

	public int getNote() {
		return note;
	}

	public void setNote(int note) {
		this.note = note;
		String spritePath = note + ".png";
		noteSprite = new Sprite(new Texture(spritePath));
		noteSprite.setOrigin(0, 0);
		noteSprite.setScale(0.4f);
		noteSprite.setPosition(getX() + 4, getY() + 40);
	}

	public void eraseNote() {
		if (noteSprite != null) {
			noteSprite.setAlpha(0);
		}
	}

	public float getX() {
		return region.getX();
	}

	public float getY() {
		return region.getY();
	}

	public int[] getPos() {
		int[] returnVal = { row, col };
		return returnVal;
	}

	public void showNumber() {
		numSprite.setAlpha(1);
		isFilled = true;
	}

	private void hideNumber() {
		numSprite.setAlpha(0);
		isFilled = false;
	}

	public boolean isFilled() {
		return isFilled;
	}

	public void setHideAfterSolve(boolean reset) {
		resetAfterSolve = reset;
	}

	public void hideAfterSolve() {
		if (resetAfterSolve) {
			hideNumber();
		}
		resetAfterSolve = false;
	}
	
	public void setTestNum(int testNum) {
		this.testNum = testNum;
	}
	
	public int getTestNum() {
		return testNum;
	}

	public void draw(SpriteBatch batch) {
		numSprite.draw(batch);
		if (noteSprite != null) {
			noteSprite.draw(batch);
		}
	}
}
