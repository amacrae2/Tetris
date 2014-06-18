package tetris;

import static org.junit.Assert.*;

import org.junit.*;

public class BoardTest {
	Board b, b2;
	Piece pyr1, pyr2, pyr3, pyr4, s, sRotated, stick;

	// This shows how to build things in setUp() to re-use
	// across tests.
	
	// In this case, setUp() makes shapes,
	// and also a 3X6 board, with pyr placed at the bottom,
	// ready to be used by tests.
	@Before
	public void setUp() throws Exception {
		b = new Board(3, 6);
		b2 = new Board(3, 6);
		
		pyr1 = new Piece(Piece.PYRAMID_STR);
		pyr2 = pyr1.computeNextRotation();
		pyr3 = pyr2.computeNextRotation();
		pyr4 = pyr3.computeNextRotation();
		
		s = new Piece(Piece.S1_STR);
		sRotated = s.computeNextRotation();
		
		stick = new Piece(Piece.STICK_STR);
		
		b.place(pyr1, 0, 0);
	}
	
	// Check the basic width/height/max after the one placement
	@Test
	public void testSample1() {
		assertEquals(1, b.getColumnHeight(0));
		assertEquals(2, b.getColumnHeight(1));
		assertEquals(2, b.getMaxHeight());
		assertEquals(3, b.getRowWidth(0));
		assertEquals(1, b.getRowWidth(1));
		assertEquals(0, b.getRowWidth(2));
	}
	
	// Place sRotated into the board, then check some measures
	@Test
	public void testSample2() {
		b.commit();
		int result = b.place(sRotated, 1, 1);
		assertEquals(Board.PLACE_OK, result);
		assertEquals(1, b.getColumnHeight(0));
		assertEquals(4, b.getColumnHeight(1));
		assertEquals(3, b.getColumnHeight(2));
		assertEquals(4, b.getMaxHeight());
	}
	
	// Place sRotated into the board, then check some placement issues
	@Test
	public void testSample3() {
		int result = b2.place(sRotated, 1, 1);
		assertEquals(Board.PLACE_OK, result);
		b2.commit();
		int result2 = b2.place(sRotated, 1, 2);
		assertEquals(Board.PLACE_BAD, result2);
		b2.commit();
		int result3 = b2.place(sRotated, 2, 1);
		assertEquals(Board.PLACE_OUT_BOUNDS, result3);
		b2.commit();
		int result4 = b2.place(sRotated, 0, 7);
		assertEquals(Board.PLACE_OUT_BOUNDS, result4);
		b2.commit();
		int result5 = b2.place(sRotated, -1, 2);
		assertEquals(Board.PLACE_OUT_BOUNDS, result5);
	}
	
	// Place pyr2 to see if the board performs correctly (testing clearRows). 
	@Test
	public void testSample4() {
		b.commit();
		int result = b.place(pyr2, 1, 1);
		assertEquals(Board.PLACE_OK, result);
		assertEquals(1, b.getColumnHeight(0));
		assertEquals(3, b.getColumnHeight(1));
		assertEquals(4, b.getColumnHeight(2));
		assertEquals(4, b.getMaxHeight());
		assertEquals(3, b.getRowWidth(0));
		assertEquals(2, b.getRowWidth(1));
		assertEquals(2, b.getRowWidth(2));
		assertEquals(1, b.getRowWidth(3));
		
		int rowsCleared = b.clearRows();
		assertEquals(1, rowsCleared);
		assertEquals(0, b.getColumnHeight(0));
		assertEquals(2, b.getColumnHeight(1));
		assertEquals(3, b.getColumnHeight(2));
		assertEquals(3, b.getMaxHeight());
		assertEquals(2, b.getRowWidth(0));
		assertEquals(2, b.getRowWidth(1));
		assertEquals(1, b.getRowWidth(2));
		assertEquals(0, b.getRowWidth(3));
	}
	
	// Place pyr3 to see if the board performs correctly (testing clearRows). 
		@Test
		public void testSample5() {
			b.commit();
			int result = b.place(pyr3, 0, 2);
			assertEquals(Board.PLACE_ROW_FILLED, result);
			assertEquals(4, b.getColumnHeight(0));
			assertEquals(4, b.getColumnHeight(1));
			assertEquals(4, b.getColumnHeight(2));
			assertEquals(4, b.getMaxHeight());
			assertEquals(3, b.getRowWidth(0));
			assertEquals(1, b.getRowWidth(1));
			assertEquals(1, b.getRowWidth(2));
			assertEquals(3, b.getRowWidth(3));
			
			int rowsCleared = b.clearRows();
			assertEquals(2, rowsCleared);
			assertEquals(0, b.getColumnHeight(0));
			assertEquals(2, b.getColumnHeight(1));
			assertEquals(0, b.getColumnHeight(2));
			assertEquals(2, b.getMaxHeight());
			assertEquals(1, b.getRowWidth(0));
			assertEquals(1, b.getRowWidth(1));
			assertEquals(0, b.getRowWidth(2));
			assertEquals(0, b.getRowWidth(3));
		}
	
		
		// Place pyr2 to see if the board performs correctly (testing dropHeight). 
		@Test
		public void testSample6() {
			b.commit();
			int result = b.dropHeight(pyr2, 1);
			assertEquals(1, result);
			
			b.place(pyr2, 1, 1);
			b.clearRows();
			result = b.dropHeight(pyr2, 1);
			assertEquals(3, result);
		}
		
		// Place pyr3 to see if the board performs correctly (testing dropHeight). 
		@Test
		public void testSample7() {
			b.commit();
			int result = b.dropHeight(pyr3, 0);
			assertEquals(2, result);
			
			b.place(pyr3, 0, 2);
			b.clearRows();
			result = b.dropHeight(pyr3, 0);
			assertEquals(2, result);
		}
		
		// Test the undo() method. 
		@Test
		public void testSample8() {
			b2.commit();
			b2.place(pyr2, 1, 1);
			b2.undo();
			assertEquals(0, b2.getColumnHeight(0));
			assertEquals(0, b2.getColumnHeight(1));
			assertEquals(0, b2.getMaxHeight());
			assertEquals(0, b2.getRowWidth(0));
			assertEquals(0, b2.getRowWidth(1));
			assertEquals(false, b2.getGrid(2, 1));
			
			b.commit();
			b.place(pyr2, 1, 1);
			b.undo();
			b.undo();
			assertEquals(1, b.getColumnHeight(0));
			assertEquals(2, b.getColumnHeight(1));
			assertEquals(2, b.getMaxHeight());
			assertEquals(3, b.getRowWidth(0));
			assertEquals(1, b.getRowWidth(1));
			assertEquals(false, b.getGrid(2, 1));			
		}
		
		// Test the undo() method further. 
		@Test
		public void testSample9() {
			b.commit();
			b.clearRows();
			b.undo();
			assertEquals(1, b.getColumnHeight(0));
			assertEquals(2, b.getColumnHeight(1));
			assertEquals(2, b.getMaxHeight());
			assertEquals(3, b.getRowWidth(0));
			assertEquals(1, b.getRowWidth(1));
			assertEquals(false, b.getGrid(2, 1));
			
			b.place(pyr2, 1, 1);
			b.clearRows();
			b.undo();
			assertEquals(1, b.getColumnHeight(0));
			assertEquals(2, b.getColumnHeight(1));
			assertEquals(2, b.getMaxHeight());
			assertEquals(3, b.getRowWidth(0));
			assertEquals(1, b.getRowWidth(1));
			assertEquals(false, b.getGrid(2, 1));	
		}
		
		// Test the undo() method further. 
		@Test
		public void testSample10() {
			b.commit();
			b.place(pyr2, 1, 1);
			b.clearRows();
			b.commit();
			b.undo();
			assertEquals(0, b.getColumnHeight(0));
			assertEquals(2, b.getColumnHeight(1));
			assertEquals(3, b.getColumnHeight(2));
			assertEquals(3, b.getMaxHeight());
			assertEquals(2, b.getRowWidth(0));
			assertEquals(2, b.getRowWidth(1));
			assertEquals(1, b.getRowWidth(2));
			assertEquals(0, b.getRowWidth(3));
			assertEquals(true, b.getGrid(2, 1));	
			
			b2.undo();
			assertEquals(0, b2.getColumnHeight(0));
			assertEquals(0, b2.getColumnHeight(1));
			assertEquals(0, b2.getMaxHeight());
			assertEquals(0, b2.getRowWidth(0));
			assertEquals(0, b2.getRowWidth(1));
			assertEquals(false, b2.getGrid(2, 1));	
		}

		
	// Make  more tests, by putting together longer series of 
	// place, clearRows, undo, place ... checking a few col/row/max
	// numbers that the board looks right after the operations.
	
	
}
