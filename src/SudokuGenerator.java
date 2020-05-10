package com.sudoku.game;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.util.Stack;
import java.util.Vector;

public class SudokuGenerator {

	private static final int WIDTH = 9;
	private static final int MAX_TRIES = 20;
	private static final boolean PRINT = false;
	private static int[][] board = new int[WIDTH][WIDTH];

	static int[][] generateBoard() {
		shuffle3x3s();
		for (int i = 0; i < WIDTH; i++) {
			if (PRINT) {
				for (int j = 0; j < 5; j++) {
					System.out.println("-------------------------------");
				}
				System.out.println("Fixing Row " + i);
				printBoard();
			}
			fixRow(i);
			if (PRINT) {
				for (int j = 0; j < 5; j++) {
					System.out.println("-------------------------------");
				}
				System.out.println("Fixing Column " + i);
				printBoard();
			}
			fixColumn(i);
		}
		if (verifyBoard()) {
			return board;
		} else {
			return new int[9][9];
		}
	}

	private static void fixRow(int row) {

		if ((row + 1) % 3 == 0) {
			return;
		}
		int rowStart = row + 1;
		int rowEnd = ((row / 3) + 1) * 3;
		Set<Vector<Integer>> dontSwapPos = new HashSet<Vector<Integer>>();
		int countTries = 0;

		while (true) {
			if (countTries > MAX_TRIES) {
				System.out.println("TERMINATING PROGRAM!");
				System.exit(0);
			}
			if (PRINT) {
				printBoard();
			}
			LinkedList<Integer> thisRow = new LinkedList<Integer>();
			Set<Integer> matchCols = new HashSet<Integer>();
			Set<Integer> prioritySwapNums = new HashSet<Integer>();

			// Populate thisRow and duplicateCols
			scanRow(row, thisRow, matchCols);

			// First check if the missing number(s) can swap out a number that is in the
			// same column as a duplicate

			// Start by retrieving the numbers that are in the same columns (and 3x3s) as
			// matches
			for (int col : matchCols) {
				for (int r = rowStart; r < rowEnd; r++) {
					prioritySwapNums.add(board[r][col]);
				}
			}
			if (swapR1(row, thisRow, matchCols, prioritySwapNums, dontSwapPos, rowStart, rowEnd)) {
				countTries++;
				continue;
			}
			// Second, try to find a missing number in any square that is not in dontSwapPos
			// and swap it in
			if (swapR2(row, thisRow, dontSwapPos, rowStart, rowEnd)) {
				countTries++;
			} else { // If the second method cannot find a value to swap, the row is fixed
				break;
			}
		}
	}

	private static void fixColumn(int col) {

		if ((col + 1) % 3 == 0) {
			return;
		}
		int colStart = col + 1;
		int colEnd = ((col / 3) + 1) * 3;
		Set<Vector<Integer>> dontSwapPos = new HashSet<Vector<Integer>>();
		int countTries = 0;

		while (true) {
			if (countTries > MAX_TRIES) {
				System.out.println("TERMINTATING PROGRAM!");
				System.exit(0);
			}
			if (PRINT) {
				printBoard();
			}
			LinkedList<Integer> thisCol = new LinkedList<Integer>();
			Set<Integer> matchRows = new HashSet<Integer>();
			Set<Integer> prioritySwapNums = new HashSet<Integer>();

			// Populate thisCol and matchRows
			scanCol(col, thisCol, matchRows);

			// First check if the missing number can swap out a number that is in the same
			// row as a duplicate

			// Start by retrieving the numbers that are in the same rows (and 3x3s) as
			// matches
			for (int row : matchRows) {
				for (int c = colStart; c < colEnd; c++) {
					prioritySwapNums.add(board[row][c]);
				}
			}
			if (swapC1(col, thisCol, matchRows, prioritySwapNums, dontSwapPos, colStart, colEnd)) {
				countTries++;
				continue;
			}
			// Second, try to find a missing number in any square that is not in dontSwapPos
			// and swap it in
			if (swapC2(col, thisCol, dontSwapPos, colStart, colEnd)) {
				countTries++;
			} else { // If the second method cannot find a value to swap, the column is fixed
				break;
			}
		}
	}

	private static boolean swapR1(int row, LinkedList<Integer> thisRow, Set<Integer> matchCols, Set<Integer> swapNums,
			Set<Vector<Integer>> dontSwapPos, int rowStart, int rowEnd) {

		boolean swapped = false;
		int swappedNum = 0;
		for (int c = 0; c < WIDTH; c++) {
			if (swapNums.contains(board[row][c])) {
				for (int r = rowStart; r < rowEnd; r++) {
					if (!thisRow.contains(board[r][c])) {
						swappedNum = board[row][c];
						swap(row, c, r, c);
						Vector<Integer> add = new Vector<Integer>(2);
						add.add(r);
						add.add(c);
						dontSwapPos.add(add);
						swapped = true;
						matchCols.remove(c);
						break;
					}
				}
			}
			if (swapped)
				break;
		}
		if (PRINT) {
			printBoard();
		}
		for (int col : matchCols) {
			for (int r = rowStart; r < rowEnd; r++) {
				if (board[r][col] == swappedNum) {
					swap(row, col, r, col);
					Vector<Integer> add = new Vector<Integer>(2);
					add.add(r);
					add.add(col);
					dontSwapPos.add(add);
					return true;
				}
			}
		}
		return false;
	}

	private static boolean swapR2(int row, LinkedList<Integer> thisRow, Set<Vector<Integer>> dontSwapPos, int rowStart,
			int rowEnd) {

		for (int r = rowStart; r < rowEnd; r++) {
			for (int c = 0; c < WIDTH; c++) {
				Vector<Integer> containsTest = new Vector<Integer>(2);
				containsTest.add(r);
				containsTest.add(c);
				if (dontSwapPos.contains(containsTest)) {
					continue;
				}
				if (!thisRow.contains(board[r][c])) {
					LinkedList<Vector<Integer>> removeList = new LinkedList<Vector<Integer>>();
					Iterator<Vector<Integer>> it = dontSwapPos.iterator();
					while (it.hasNext()) {
						Vector<Integer> coords = it.next();
						if (board[coords.firstElement()][coords.lastElement()] == board[row][c]) {
							removeList.add(coords);
						}
					}
					for (Vector<Integer> v : removeList) {
						dontSwapPos.remove(v);
					}
					swap(row, c, r, c);
					Vector<Integer> add = new Vector<Integer>(2);
					add.add(r);
					add.add(c);
					dontSwapPos.add(add);
					return true;
				}
			}
		}
		return false;
	}

	private static boolean swapC1(int col, LinkedList<Integer> thisCol, Set<Integer> matchRows, Set<Integer> swapNums,
			Set<Vector<Integer>> dontSwapPos, int colStart, int colEnd) {

		boolean swapped = false;
		int swappedNum = 0;
		for (int r = 0; r < WIDTH; r++) {
			if (swapNums.contains(board[r][col])) {
				for (int c = colStart; c < colEnd; c++) {
					if (!thisCol.contains(board[r][c])) {
						swappedNum = board[r][col];
						swap(r, col, r, c);
						Vector<Integer> add = new Vector<Integer>();
						add.add(r);
						add.add(c);
						dontSwapPos.add(add);
						swapped = true;
						matchRows.remove(r);
						break;
					}
				}
			}
			if (swapped) {
				break;
			}
		}
		if (PRINT) {
			printBoard();
		}
		for (int row : matchRows) {
			for (int c = colStart; c < colEnd; c++) {
				if (board[row][c] == swappedNum) {
					swap(row, col, row, c);
					Vector<Integer> add = new Vector<Integer>();
					add.add(row);
					add.add(c);
					dontSwapPos.add(add);
					return true;
				}
			}
		}
		return false;
	}

	private static boolean swapC2(int col, LinkedList<Integer> thisCol, Set<Vector<Integer>> dontSwapPos, int colStart,
			int colEnd) {

		for (int c = colStart; c < colEnd; c++) {
			for (int r = 0; r < WIDTH; r++) {
				Vector<Integer> containsTest = new Vector<Integer>();
				containsTest.add(r);
				containsTest.add(c);
				if (dontSwapPos.contains(containsTest)) {
					continue;
				}
				if (!thisCol.contains(board[r][c])) {
					LinkedList<Vector<Integer>> removeList = new LinkedList<Vector<Integer>>();
					Iterator<Vector<Integer>> it = dontSwapPos.iterator();
					while (it.hasNext()) {
						Vector<Integer> coords = it.next();
						if (board[coords.firstElement()][coords.lastElement()] == board[r][col]) {
							removeList.add(coords);
						}
					}
					for (Vector<Integer> v : removeList) {
						dontSwapPos.remove(v);
					}
					swap(r, col, r, c);
					Vector<Integer> add = new Vector<Integer>();
					add.add(r);
					add.add(c);
					dontSwapPos.add(add);
					return true;
				}
			}
		}
		return false;
	}

	private static void scanRow(int row, LinkedList<Integer> thisRow, Set<Integer> matchCols) {
		// Populate thisRow and matchCols

		for (int c = 0; c < WIDTH; c++) {
			int check = board[row][c];
			if (thisRow.contains(check)) {
				matchCols.add(thisRow.lastIndexOf(check));
				matchCols.add(c);
			}
			thisRow.add(check);
		}
	}

	private static void scanCol(int col, LinkedList<Integer> thisCol, Set<Integer> matchRows) {
		// Populate thisCol and matchRows

		for (int r = 0; r < WIDTH; r++) {
			int check = board[r][col];
			if (thisCol.contains(check)) {
				matchRows.add(thisCol.lastIndexOf(check));
				matchRows.add(r);
			}
			thisCol.add(check);
		}
	}

	private static void swap(int r1, int c1, int r2, int c2) {
		int temp = board[r1][c1];
		board[r1][c1] = board[r2][c2];
		board[r2][c2] = temp;
	}

	private static void shuffle3x3s() {

		Stack<Integer> entries;
		for (int x = 0; x < 3; x++) {
			for (int y = 0; y < 3; y++) {
				int rowStart = x * 3, rowEnd = (x + 1) * 3;
				int colStart = y * 3, colEnd = (y + 1) * 3;
				entries = new Stack<Integer>();
				for (int n = 1; n <= 9; n++) {
					entries.push(n);
				}
				Collections.shuffle(entries);
				for (int i = rowStart; i < rowEnd; i++) {
					for (int j = colStart; j < colEnd; j++) {
						board[i][j] = entries.pop();
					}
				}
			}
		}
	}

	private static boolean verifyBoard() {
		Set<Integer> check = new HashSet<Integer>();
		// Check the rows
		for (int r = 0; r < WIDTH; r++) {
			for (int c = 0; c < WIDTH; c++) {
				if (check.contains(board[r][c]))
					return false; // Found a duplicate number in this row
				check.add(board[r][c]);
			}
			check.clear();
		}
		// Check the columns
		for (int c = 0; c < WIDTH; c++) {
			for (int r = 0; r < WIDTH; r++) {
				if (check.contains(board[r][c]))
					return false; // Found a duplicate number in this column
				check.add(board[r][c]);
			}
			check.clear();
		}
		// Check the sectors
		// Begin by setting the vector to sector (0, 0)
		Vector<Integer> sec = new Vector<Integer>(2);
		sec.add(0);
		sec.add(0);
		while (sec.firstElement() < 3) { // Break before reaching a 4th row of 3x3 sectors
			int rowStart = sec.firstElement() * 3, rowEnd = (sec.firstElement() + 1) * 3;
			int colStart = sec.lastElement() * 3, colEnd = (sec.lastElement() + 1) * 3;
			for (int r = rowStart; r < rowEnd; r++) {
				for (int c = colStart; c < colEnd; c++) {
					if (check.contains(board[r][c]))
						return false; // Found a duplicate number in this sector
					check.add(board[r][c]);
				}
			}
			check.clear();
			if (sec.lastElement() >= 2) { // Move to next row of 3x3s
				sec.set(0, sec.get(0) + 1);
				sec.set(1, 0);
			} else { // Move to the 3x3 to the right
				sec.set(1, sec.get(1) + 1);
			}
		}
		return true; // Every row, column, and 3x3 has the numbers 1-9
	}

	private static void printBoard() {
		if (PRINT) {
			System.out.println("---------------------\n---------------------");
			for (int i = 0; i < WIDTH; i++) {
				if (i % 3 == 0 && i != 0)
					System.out.println("---------------------");
				for (int j = 0; j < WIDTH; j++) {
					if (j % 3 == 0 && j != 0)
						System.out.print("| ");
					if (j == WIDTH - 1)
						System.out.println(board[i][j]);
					else
						System.out.print(board[i][j] + " ");
				}
			}
		}
	}
}