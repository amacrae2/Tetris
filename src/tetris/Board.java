// Board.java
package tetris;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Stack;
import java.util.List;

/**
 CS108 Tetris Board.
 Represents a Tetris board -- essentially a 2-d grid
 of booleans. Supports tetris pieces and row clearing.
 Has an "undo" feature that allows clients to add and remove pieces efficiently.
 Does not do any drawing or have any idea of pixels. Instead,
 just represents the abstract 2-d board.
*/
public class Board	{
	// Some ivars are stubbed out for you:
	private int width;
	private int height;
	private boolean[][] grid;
	private int[] widths;
	private int[] heights;
	private int maxHeight;
	private boolean DEBUG = true;
	boolean committed;
	boolean firstState = true;
	
	// backup ivars
	private boolean[][] xGrid;
	private int[] xWidths;
	private int[] xHeights;
	private int xMaxHeight;
	
	
	
	// Here a few trivial methods are provided:
	
	/**
	 Creates an empty board of the given width and height
	 measured in blocks.
	*/
	public Board(int width, int height) {
		this.width = width;
		this.height = height;
		grid = new boolean[width][height];
		widths = new int[height];
		heights = new int[width];
		maxHeight = 0;
		committed = true;
		
		xGrid = new boolean[width][height];
		xWidths = new int[height];
		xHeights = new int[width];
		xMaxHeight = 0;
	}
	
	
	/**
	 Returns the width of the board in blocks.
	*/
	public int getWidth() {
		return width;
	}
	
	
	/**
	 Returns the height of the board in blocks.
	*/
	public int getHeight() {
		return height;
	}
	
	
	/**
	 Returns the max column height present in the board.
	 For an empty board this is 0.
	*/
	public int getMaxHeight() {	 
		return maxHeight;
	}
	
	
	/**
	 Checks the board for internal consistency -- used
	 for debugging.
	*/
	public void sanityCheck() {
		if (DEBUG) {
			int[] widthsCheck = new int[height];
			int[] heightsCheck = new int[width];
			int maxHeightCheck = 0;
			for (int col = 0; col < grid.length; col ++) {
				for (int row = 0; row < grid[col].length; row++) {
					if (grid[col][row]) {
						widthsCheck[row] += 1;
						if (row > heightsCheck[col]-1) {
							heightsCheck[col] = row+1;
						}
						if (row > maxHeightCheck-1) {
							maxHeightCheck = row+1;
						}
					}
				}
				checkColLengths(col);
			}
			checkWidths(widthsCheck);
			checkHeights(heightsCheck);
			checkMaxHeight(maxHeightCheck);
			checkGridWidth();
		}
	}
	
	/**
	 Given a piece and an x, returns the y
	 value where the piece would come to rest
	 if it were dropped straight down at that x.
	 
	 <p>
	 Implementation: use the skirt and the col heights
	 to compute this fast -- O(skirt length).
	*/
	public int dropHeight(Piece piece, int x) {
		int y = 0;
		for (int i = 0; i < piece.getWidth(); i++) {
			int currY = heights[x+i] - piece.getSkirt()[i];
			if (currY > y) {
				y = currY;
			}
		}
		return y;
	}
	
	
	/**
	 Returns the height of the given column --
	 i.e. the y value of the highest block + 1.
	 The height is 0 if the column contains no blocks.
	*/
	public int getColumnHeight(int x) {
		return heights[x];
	}
	
	
	/**
	 Returns the number of filled blocks in
	 the given row.
	*/
	public int getRowWidth(int y) {
		 return widths[y];
	}
	
	
	/**
	 Returns true if the given block is filled in the board.
	 Blocks outside of the valid width/height area
	 always return true.
	*/
	public boolean getGrid(int x, int y) {
		if (x >= width || y >= height) {
			return true;
		} else {
			return grid[x][y];
		}
	}
	
	
	public static final int PLACE_OK = 0;
	public static final int PLACE_ROW_FILLED = 1;
	public static final int PLACE_OUT_BOUNDS = 2;
	public static final int PLACE_BAD = 3;
	
	/**
	 Attempts to add the body of a piece to the board.
	 Copies the piece blocks into the board grid.
	 Returns PLACE_OK for a regular placement, or PLACE_ROW_FILLED
	 for a regular placement that causes at least one row to be filled.
	 
	 <p>Error cases:
	 A placement may fail in two ways. First, if part of the piece may falls out
	 of bounds of the board, PLACE_OUT_BOUNDS is returned.
	 Or the placement may collide with existing blocks in the grid
	 in which case PLACE_BAD is returned.
	 In both error cases, the board may be left in an invalid
	 state. The client can use undo(), to recover the valid, pre-place state.
	*/
	public int place(Piece piece, int x, int y) {
		// flag !committed problem
		if (!committed) throw new RuntimeException("place commit problem");
			
		backupIvars();
		int result = PLACE_OK;
		for (TPoint point: piece.getBody()) {
			int currX = x + point.x;
			int currY = y + point.y;
			if (currX >= width || currY >= height || currX < 0 || currY < 0) {
				result = PLACE_OUT_BOUNDS;
				break;
			} else if (grid[currX][currY] == true) {
				result = PLACE_BAD;
				break;
			} else {
				grid[currX][currY] = true;
				widths[currY] += 1;
				updateHeights(currX, currY+1);
				if (widths[currY] >= width) {
					result = PLACE_ROW_FILLED;
				}
			}
		}	
		if (result == PLACE_OK || result == PLACE_ROW_FILLED) {
			sanityCheck();
		}
		return result;
	}


	/**
	 Deletes rows that are filled all the way across, moving
	 things above down. Returns the number of rows cleared.
	*/
	public int clearRows() {
		if (committed) {
			backupIvars();
		}
		
		//first checks for empty array.
		if (grid.length < 1) {
			return 0;
		}
		
		// non-empty array
		Stack<Integer> rowsToClear = new Stack<Integer>();
		int rowsCleared = determineRowsToClear(rowsToClear);
		shiftClearedRows(rowsToClear);
		findNewHeights();
		committed = false;
		
		sanityCheck();
		return rowsCleared;
	}

	/**
	 Reverts the board to its state before up to one place
	 and one clearRows();
	 If the conditions for undo() are not met, such as
	 calling undo() twice in a row, then the second undo() does nothing.
	 See the overview docs.
	*/
	public void undo() {
		if (firstState || committed){
			return;
		}
		for (int col = 0; col < width; col++) {
			System.arraycopy(xGrid[col], 0, grid[col], 0, xGrid[col].length);
		}
		System.arraycopy(xHeights, 0, heights, 0, xHeights.length);
		System.arraycopy(xWidths, 0, widths, 0, xWidths.length);
		maxHeight = xMaxHeight;
		sanityCheck();
		committed = true;
	}
	
	/**
	 Puts the board in the committed state.
	*/
	public void commit() {
		committed = true;
	}

	/*
	 Renders the board state as a big String, suitable for printing.
	 This is the sort of print-obj-state utility that can help see complex
	 state change over time.
	 (provided debugging utility) 
	 */
	public String toString() {
		StringBuilder buff = new StringBuilder();
		for (int y = height-1; y>=0; y--) {
			buff.append('|');
			for (int x=0; x<width; x++) {
				if (getGrid(x,y)) buff.append('+');
				else buff.append(' ');
			}
			buff.append("|\n");
		}
		for (int x=0; x<width+2; x++) buff.append('-');
		return(buff.toString());
	}

	/**
	 * Iterates through the grid and finds which rows are completely filled
	 * @param rowsToClear Stack containing the rows that are all true and should be cleared
	 * @return the number of rows that are all true and targeted to be cleared
	 */
	private int determineRowsToClear(Stack<Integer> rowsToClear) {
		int rowsCleared = 0;
		for (int row = 0; row < grid[0].length; row++) {
			for (int col = 0; col < grid.length; col++) {
				if (!grid[col][row]) {
					break;
				}	
				else if (col == grid.length-1) {
					rowsToClear.push(row);
					rowsCleared += 1;
				}
			}
		}
		return rowsCleared;
	}
	
	/**
	 * clears the rows that contain all true booleans,
	 * and shifts rows down accordingly.
	 * @param rowsToClear the rows that contain all true booleans
	 */
	private void shiftClearedRows(Stack<Integer> rowsToClear) {
		while (!rowsToClear.empty()) {
			int startRow = rowsToClear.pop();
			for (int row = startRow; row < grid[0].length; row++) {
				if (row >= maxHeight-1) {
					fillRestOfGridFalse(row);
					widths[row] = 0;
					break;
				} else {
					for (int col = 0; col < grid.length; col++) {
						grid[col][row] = grid[col][row+1];
					}
					widths[row] = widths[row+1];
				}
			}
		}
	}

	/**
	 * resets all the heights to be 0 and then recalculates the heights for each column.
	 * Also updates maxHeight.
	 */
	private void findNewHeights() {
		Arrays.fill(heights, 0);
		maxHeight = 0;
		for (int col = 0; col < heights.length; col++) {
			int last = -1;
			for (int row = 0; row < grid[col].length; row++) {
				if (grid[col][row] == true) {
					last = row;
				}
			}
			updateHeights(col, last+1);
		}
	}
	
	/**
	 * Compares each currHeight to the maxHeight for the given column and updates the height if needed
	 * @param col the column currently being looked at
	 * @param currHeight the height that is checked to see if it should replace any of the previous heights.
	 */
	private void updateHeights(int col, int currHeight) {
		if (currHeight > heights[col]) {
			heights[col] = currHeight;
		}
		if (currHeight > maxHeight) {
			maxHeight = currHeight;
		}
	}

	/**
	 * Fills in the grid from the row just above the highest block with falses.
	 * filling the rest of the grid false only requires changing the row with the previous highest point
	 * @param row the row above the highest block that is true
	 */
	private void fillRestOfGridFalse(int row) {
		for (int col = 0; col < grid.length; col++) {
			grid[col][row] = false;
		}
	}
	
	private void checkWidths(int[] widthsCheck) {
		for (int row = 0; row < widthsCheck.length; row ++) {
			if (widthsCheck[row] != widths[row]) {
				throw new RuntimeException("widths array is inconsistant at row " + row);
			}
		}
	}


	private void checkHeights(int[] heightsCheck) {
		for (int col = 0; col < heightsCheck.length; col ++) {
			if (heightsCheck[col] != heights[col]) {
				throw new RuntimeException("heights array is inconsistant at col " + col);
			}
		}
	}


	private void checkMaxHeight(int maxHeightCheck) {
		if (maxHeightCheck != maxHeight) {
			throw new RuntimeException("maxHeight is inconsistant");
		}
	}


	private void checkGridWidth() {
		if (grid.length != width || grid.length != heights.length) {
			throw new RuntimeException("Width is inconsistant");
		}
	}


	private void checkColLengths(int col) {
		if (grid[col].length != height || grid[col].length != widths.length) {
			throw new RuntimeException("length of col " + col + " is inconsistant");
		}
	}
	
	/**
	 * Backs up Instance variables that would get changed in the place or clearRoes methods.
	 */
	private void backupIvars() {
		committed = false;
		firstState = false;
		for (int col = 0; col < width; col++) {
			System.arraycopy(grid[col], 0, xGrid[col], 0, grid[col].length);
		}
		System.arraycopy(heights, 0, xHeights, 0, heights.length);
		System.arraycopy(widths, 0, xWidths, 0, widths.length);
		xMaxHeight = maxHeight;
	}
	
}






