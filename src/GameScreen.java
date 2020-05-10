package com.sudoku.game;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Scanner;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.sudoku.game.Sudoku.difficulty;

public class GameScreen implements Screen, InputProcessor {

	private Sudoku game;
	private SpriteBatch batch;

	private static final int WIDTH = 9;
	private static final float TILE_WIDTH = 64;
	private difficulty dif;

	private Sprite boardSprite;
	private Sprite selectOverlay;
	private LinkedList<Sprite> strikeSprites;

	private int[] selectedTile = { -1, -1 };
	private int emptyTiles;
	private int strikesLeft = 3;

	private BitmapFont resetPrompt;
	private String resetString;
	private boolean gameFinished = false;
	private BitmapFont highScoresDisplay;
	private String[] highScores;
	private StringBuilder highScoresText;

	private long startTime;

	private Tile[][] tiles;

	public GameScreen(Sudoku game, SpriteBatch batch, difficulty dif) {
		this.game = game;
		this.batch = batch;
		this.dif = dif;
	}

	@Override
	public void show() {
		boardSprite = new Sprite(new Texture("board.png"));
		// Center the board sprite vertically and horizontally
		boardSprite.setPosition(209, 109);

		selectOverlay = new Sprite(new Texture("selectOverlay.png"));
		selectOverlay.setAlpha(0);
		strikeSprites = new LinkedList<Sprite>();
		resetPrompt = new BitmapFont();
		resetPrompt.setColor(Color.RED);
		resetString = "";
		highScoresDisplay = new BitmapFont();
		highScoresDisplay.setColor(Color.LIME);
		highScores = new String[10];
		highScoresText = new StringBuilder();
		highScoresText.append("High Scores:\n\n");

		setupBoard();
		try {
			readHighScores();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		Gdx.input.setInputProcessor(this);
		startTime = System.currentTimeMillis();
	}

	private void setupBoard() {
		/**
		 * Starts by calling pickClueSquares to determine which squares will be used as
		 * clues. Then generateBoard from SudokuGenerator is called to create an int[][]
		 * representaion of a valid sudoku board, where every number is in the range
		 * 1-9. Lastly, a tile object is created for each square and added to the tiles
		 * matrix
		 */

		// Determine which squares will be used as clues
		boolean[][] showAtStart = pickClueSquares();

		// Get a complete board with every number filled
		int[][] board = SudokuGenerator.generateBoard();

		// Create the actual board that will be used for the game
		tiles = new Tile[WIDTH][WIDTH];
		emptyTiles = 0;
		for (int i = 0; i < WIDTH; i++) {
			for (int j = 0; j < WIDTH; j++) {

				// Get the position for the number sprite
				float xPos = (boardSprite.getX() + 2) + (TILE_WIDTH * j) + ((j / 3) * 2);
				float yPos = (Gdx.graphics.getHeight() - boardSprite.getY() - TILE_WIDTH) - (TILE_WIDTH * i)
						- ((i / 3) * 2);

				// Create a tileRegion which will be used to check when this tile is clicked
				Rectangle tileRegion = new Rectangle(xPos, yPos, TILE_WIDTH, TILE_WIDTH);

				// Create the tile and add it to the board
				Tile tile = new Tile(tileRegion, i, j, board[i][j], showAtStart[i][j]);
				if (!showAtStart[i][j]) {
					emptyTiles++;
				}
				tiles[i][j] = tile;
			}
		}
	}

	private boolean[][] pickClueSquares() {
		/**
		 * First, the number of clues is generated randomly from a range that
		 * corresponds to the selected difficulty. Next, an int[] of length 2 is created
		 * for each square's position on the sudoku board, and a LinkedList named coords
		 * stores these positions. Then, a 9x9 boolean matrix named showAtStart is
		 * created to represent whether each square is or is not a clue. Remove numClues
		 * coordinate pairs from the coords list and set that position in the boolean
		 * matrix to true
		 * 
		 * @return showAtStart
		 */

		// Get the number of clues based on the difficulty
		int numClues = 0;
		switch (dif) {
		case EASY: // 35-40
			numClues = (int) (Math.random() * 6) + 35;
			break;
		case INTERMEDIATE: // 30-34
			numClues = (int) (Math.random() * 5) + 30;
			break;
		case ADVANCED: // 25-29
			numClues = (int) (Math.random() * 5) + 25;
			break;
		case EXPERT: // 20-24
			numClues = (int) (Math.random() * 5) + 20;
			break;
		default:
			break;
		}

		// Add all of the square's positions to a list
		LinkedList<int[]> coords = new LinkedList<int[]>();
		for (int r = 0; r < WIDTH; r++) {
			for (int c = 0; c < WIDTH; c++) {
				int[] coord = { r, c };
				coords.add(coord);
			}
		}

		// Every value starts as false
		boolean[][] showAtStart = new boolean[WIDTH][WIDTH];

		// For the number of clues, randomly select an index from coords and make that
		// square a clue
		for (; numClues > 0; numClues--) {
			int selectIndex = (int) (Math.random() * coords.size());
			int r = coords.get(selectIndex)[0];
			int c = coords.get(selectIndex)[1];
			showAtStart[r][c] = true;
			coords.remove(selectIndex);
		}
		return showAtStart;
	}

	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(1, 1, 1, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		drawSprites();
	}

	private void onGameEnd(boolean solvedByPlayer) {
		resetString += "Press Enter to return to the title screen.\nPress Escape to exit the game.";
		gameFinished = true;
		int elapsedTimeSeconds = (int) ((System.currentTimeMillis() - startTime) / 1000);

		try {
			writeHighScores(elapsedTimeSeconds, solvedByPlayer);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private boolean backtrackSolve(boolean show) {
		/**
		 * First, set every unfilled square's number to 0 to ensure the integrity of the
		 * backtrackSolve algorithm. Then, obtain the coordinates of an empty square; if
		 * no empty square is found, the board is solved. Check the numbers 1-9; when
		 * you find a number that can occupy this blank square (w.r.t the current state
		 * of the board), update the square's number and recursively call this function.
		 * If false is returned up the recursion stack, set this number back to 0 and
		 * try another number from 1-9. This function will finish and return true when
		 * recursive call cannot find an empty square; it will then return true all the
		 * way up the stack.
		 * 
		 * @param show: whether to hide the squares that were solved for after solving
		 */

		setEmptyToZero();
		int[] empty = findEmpty();
		if (empty == null) {
			return true;
		}
		int row = empty[0], col = empty[1];

		for (int i = 1; i <= WIDTH; i++) {
			if (valid(row, col, i)) {
				tiles[row][col].setNumber(i);
				tiles[row][col].showNumber();
				if (!show) {
					tiles[row][col].setHideAfterSolve(true);
				}
				if (backtrackSolve(show)) {
					// If any tiles were flagged to not be shown after solving, hide them
					for (Tile[] tileRow : tiles) {
						for (Tile tile : tileRow) {
							tile.hideAfterSolve();
						}
					}
					return true;
				}
				tiles[row][col].setNumber(0);
			}
		}
		return false;
	}

	private void setEmptyToZero() {
		for (Tile[] row : tiles) {
			for (Tile tile : row) {
				if (!tile.isFilled()) {
					tile.setNumber(0);
				}
			}
		}
	}

	private boolean valid(int row, int col, int num) {
		/**
		 * Returns whether num can occupy the square located in the sudoku board at
		 * (row, col)
		 */
		// Check row
		for (int c = 0; c < WIDTH; c++) {
			Tile tile = tiles[row][c];
			if (c != col && tile.getNumber() != 0) {
				if (tile.getNumber() == num) {
					return false;
				}
			}
		}
		// Check column
		for (int r = 0; r < WIDTH; r++) {
			Tile tile = tiles[r][col];
			if (r != row && tile.getNumber() != 0) {
				if (tile.getNumber() == num) {
					return false;
				}
			}
		}
		// Check 3x3 sectors
		int rowStart = (row / 3) * 3, rowEnd = rowStart + 3;
		int colStart = (col / 3) * 3, colEnd = colStart + 3;
		for (int r = rowStart; r < rowEnd; r++) {
			for (int c = colStart; c < colEnd; c++) {
				Tile tile = tiles[r][c];
				if ((r != row || c != col) && tile.getNumber() != 0) {
					if (tile.getNumber() == num) {
						return false;
					}
				}
			}
		}
		return true;
	}

	private int[] findEmpty() {
		/**
		 * Traverses the board row by row, and returns the position as an int[] of the
		 * first empty square. Returns null if no empty square is found.
		 */
		for (int r = 0; r < WIDTH; r++) {
			for (int c = 0; c < WIDTH; c++) {
				if (tiles[r][c].getNumber() == 0) {
					int[] returnVal = { r, c };
					return returnVal;
				}
			}
		}
		return null;
	}

	public void drawSprites() {
		batch.begin();
		for (Sprite strike : strikeSprites) {
			strike.draw(batch);
		}
		boardSprite.draw(batch);
		if (tiles != null) {
			for (Tile[] tileRow : tiles) {
				for (Tile tile : tileRow) {
					tile.draw(batch);
				}
			}
		}
		selectOverlay.draw(batch);
		resetPrompt.draw(batch, resetString, 0, Gdx.graphics.getHeight() - 30, Gdx.graphics.getWidth(), 1, false);
		highScoresDisplay.draw(batch, highScoresText.toString(), 850, Gdx.graphics.getHeight() - 30, 80, -1, false);
		batch.end();
	}

	public void checkTile(int inputNum) {
		/**
		 * Called when the player tries to input a number into an empty square. First,
		 * hide the selectOverlay. Then, reveal the number on the board if the input is
		 * correct, otherwise add a strike and display a strike sprite. If the player
		 * just got their last strike or filled the last empty square, the game is over.
		 */
		Tile checkTile = tiles[selectedTile[0]][selectedTile[1]];
		selectOverlay.setAlpha(0);
		selectedTile[0] = -1;
		selectedTile[1] = -1;
		if (checkTile.getNumber() == inputNum) {
			checkTile.showNumber();
			checkTile.eraseNote();
			emptyTiles--;
		} else {
			strikesLeft--;
			addStrikeSprite();
			if (strikesLeft == 0) {
				resetString += "GAME OVER --- You got three strikes\n";
				onGameEnd(false); // false ensures the solve time will not be added to the high scores file
			}
		}
		if (emptyTiles == 0) {
			resetString += "Congratulations, you solved the puzzle!\n";
			onGameEnd(true); // true ensures the solve time will be compared to the previous high scores and
								// added to the file if appropriate
		}
	}

	public void setNote(int note) {
		/**
		 * Called when the player tries to add a note number to an empty square. First,
		 * hide the select overlay. Then, check if the tile is empty (it will be since
		 * this method is only called if that is the case; this check is just to be
		 * safe). Set the note to the input number.
		 */
		Tile tile = tiles[selectedTile[0]][selectedTile[1]];
		selectOverlay.setAlpha(0);
		selectedTile[0] = -1;
		selectedTile[1] = -1;
		if (!tile.isFilled()) {
			tile.setNote(note);
		}
	}

	public void addStrikeSprite() {
		Sprite strike = new Sprite(new Texture("strike.png"));
		if (strikeSprites.size() == 0) {
			strike.setPosition(16, Gdx.graphics.getHeight() - strike.getHeight() - 16);
			strikeSprites.add(strike);
		} else {
			strike.setPosition(16, strikeSprites.peekLast().getY() - strike.getHeight() - 16);
			strikeSprites.add(strike);
		}
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
		resetPrompt.dispose();
		highScoresDisplay.dispose();
	}

	@Override
	public boolean keyDown(int keycode) {

		// First, check input that is not related to inputting numbers or notes

		// Return to TitleScreen
		if (keycode == Keys.ENTER && gameFinished) {
			dispose();
			game.setScreen(new TitleScreen(game, batch));
		}

		// Solve the board
		if (keycode == Keys.SPACE && !gameFinished) {
			if (backtrackSolve(true)) {
				resetString += "The puzzle is solved.\n";
				onGameEnd(false); // false ensures the solve time will not be added to the high scores file
			}
			return false;
		}

		// Close the game (if finished) or stop selecting a tile (if not finished)
		if (keycode == Keys.ESCAPE) {
			if (gameFinished) {
				dispose();
				game.dispose();
				Gdx.app.exit();
			} else {
				selectedTile[0] = -1;
				selectedTile[1] = -1;
				selectOverlay.setAlpha(0);
			}
		}

		// All other input only matters when a tile is selected and the game is not
		// finished
		if (selectedTile[0] == -1 || gameFinished) {
			return false;
		}

		// Then, check input that is related to inputting numbers or notes

		int input = -1;
		int note = -1;
		switch (keycode) {
		case Keys.NUM_1:
			input = 1;
			break;
		case Keys.NUM_2:
			input = 2;
			break;
		case Keys.NUM_3:
			input = 3;
			break;
		case Keys.NUM_4:
			input = 4;
			break;
		case Keys.NUM_5:
			input = 5;
			break;
		case Keys.NUM_6:
			input = 6;
			break;
		case Keys.NUM_7:
			input = 7;
			break;
		case Keys.NUM_8:
			input = 8;
			break;
		case Keys.NUM_9:
			input = 9;
			break;
		case Keys.NUMPAD_1:
			note = 1;
			break;
		case Keys.NUMPAD_2:
			note = 2;
			break;
		case Keys.NUMPAD_3:
			note = 3;
			break;
		case Keys.NUMPAD_4:
			note = 4;
			break;
		case Keys.NUMPAD_5:
			note = 5;
			break;
		case Keys.NUMPAD_6:
			note = 6;
			break;
		case Keys.NUMPAD_7:
			note = 7;
			break;
		case Keys.NUMPAD_8:
			note = 8;
			break;
		case Keys.NUMPAD_9:
			note = 9;
			break;
		default:
			break;
		}
		if (input != -1) {
			checkTile(input);
		}
		if (note != -1) {
			setNote(note);
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
		float mouseX = screenX, mouseY = Gdx.graphics.getHeight() - screenY;
		System.out.println(mouseX + "-" + mouseY);
		// No need to do anything after a mouse click if the game is already finished
		if (!gameFinished) {
			for (Tile[] tileRow : tiles) {
				for (Tile tile : tileRow) {
					if (tile.getRegion().contains(mouseX, mouseY) && !tile.isFilled()) {
						selectedTile = tile.getPos();
						selectOverlay.setPosition(tile.getX() + 1, tile.getY() + 1);
						selectOverlay.setAlpha(1);
						return false;
					}
				}
			}
			// If a tile was not clicked, then remove a current tile selection
			selectedTile[0] = -1;
			selectedTile[1] = -1;
			selectOverlay.setAlpha(0);
		}
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

	private void readHighScores() throws FileNotFoundException {
		// Each highscore timestamp is of the form hh:mm:ss

		// Read through the highscores file and store each as a string in the String[]
		// highScores
		File read = new File("filepath");
		Scanner sc = new Scanner(read);
		int c = 0;
		while (sc.hasNextLine() && c < 10) {
			highScores[c] = sc.nextLine();
			highScoresText.append(highScores[c] + "\n");
			c++;
		}
		sc.close();
	}

	private void writeHighScores(int elapsedTimeSeconds, boolean solvedByPlayer) throws IOException {

		// If the player didn't solve the board themselves, no changes need to be made

		if (solvedByPlayer) {
			// Each highscore timestamp is of the form hh:mm:ss

			// Add this game's time to high scores if it is in the top 10
			String[] newHighScores = addToHighScores(elapsedTimeSeconds);

			FileWriter write = new FileWriter("filepath");
			for (String line : newHighScores) {
				if (line != null) {
					write.write(line);
				}
			}
			write.close();
		}
	}

	private String[] addToHighScores(int elapsedTimeSeconds) {

		LinkedList<Integer> timesInSeconds = new LinkedList<Integer>();
		for (String str : highScores) {
			if (str != null) {
				int hours = Integer.parseInt(str.substring(0, 2));
				int minutes = Integer.parseInt(str.substring(3, 5));
				int seconds = Integer.parseInt(str.substring(6));
				int timeInSeconds = seconds + (minutes * 60) + (hours * 3600);
				timesInSeconds.add(timeInSeconds);
			}
		}
		boolean inserted = false;
		for (int i = 0; i < timesInSeconds.size(); i++) {
			if (elapsedTimeSeconds < timesInSeconds.get(i)) {
				timesInSeconds.add(i, elapsedTimeSeconds);
				inserted = true;
				break;
			}
		}
		// If this game's time is longer than every past high score, add it to the end
		if (!inserted) {
			timesInSeconds.addLast(elapsedTimeSeconds);
		}
		// If there are more than 10 times in this list, remove the last one (the
		// longest time)
		if (timesInSeconds.size() > 10) {
			timesInSeconds.removeLast();
		}

		String[] newHighScores = new String[10];
		for (int i = 0; i < timesInSeconds.size(); i++) {
			// Construct the timestamp from each high score

			int timeInSeconds = timesInSeconds.get(i);
			String timestamp = createTimestamp(timeInSeconds);
			newHighScores[i] = timestamp;
		}
		return newHighScores;
	}

	private String createTimestamp(int timeInSeconds) {

		// Make hours string
		int hours = timeInSeconds / 3600;
		String hoursStr = String.valueOf(hours);
		if (hours < 10) {
			hoursStr = "0" + hoursStr;
		}
		timeInSeconds -= hours * 3600;

		// Make minutes string
		int minutes = timeInSeconds / 60;
		String minutesStr = String.valueOf(minutes);
		if (minutes < 10) {
			minutesStr = "0" + minutesStr;
		}
		timeInSeconds -= minutes * 60;

		// Make seconds string
		int seconds = timeInSeconds;
		String secondsStr = String.valueOf(seconds);
		if (seconds < 10) {
			secondsStr = "0" + secondsStr;
		}

		// Return the full timestamp
		return hoursStr + ":" + minutesStr + ":" + secondsStr + "\n";
	}
}
