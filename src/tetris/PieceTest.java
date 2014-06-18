package tetris;

import static org.junit.Assert.*;
import java.util.*;

import org.junit.*;

/*
  Unit test for Piece class -- starter shell.
 */
public class PieceTest {
	// You can create data to be used in the your
	// test cases like this. For each run of a test method,
	// a new PieceTest object is created and setUp() is called
	// automatically by JUnit.
	// For example, the code below sets up some
	// pyramid and s pieces in instance variables
	// that can be used in tests.
	private Piece pyr1, pyr2, pyr3, pyr4;
	private Piece s, sRotated, stick, l1, square, pyr;
	private Piece u1, u2, u3, u4;
	private Piece zigZag, snake;

	@Before
	public void setUp() throws Exception {

		pyr1 = new Piece(Piece.PYRAMID_STR);
		pyr2 = pyr1.computeNextRotation();
		pyr3 = pyr2.computeNextRotation();
		pyr4 = pyr3.computeNextRotation();
		
		s = new Piece(Piece.S1_STR);
		sRotated = s.computeNextRotation();
		
		Piece[] pieces = Piece.getPieces();
		stick = pieces[0];
		l1 = pieces[1];
		square = pieces[5];
		pyr = pieces[6];
		
		u1 = new Piece("0 3  0 2  0 1  0 0  1 0  2 0  2 1  2 2  2 3");
		u2 = u1.computeNextRotation();
		u3 = u2.computeNextRotation();
		u4 = u3.computeNextRotation();
		
		zigZag = new Piece("0 0  1 0  1 1  2 1  2 2  3 2  3 3  4 3  4 4");
		snake = new Piece("0 0  0 1  0 2  1 2  2 2  2 1  2 0  3 0  4 0  4 1  4 2");
	}
	
	// Here are some sample tests to get you started
	
	@Test
	public void testSampleSize() {
		// Check size of pyr piece
		assertEquals(3, pyr1.getWidth());
		assertEquals(2, pyr1.getHeight());
		
		// Now try after rotation
		// Effectively we're testing size and rotation code here
		assertEquals(2, pyr2.getWidth());
		assertEquals(3, pyr2.getHeight());
		
		// Now try with some other piece, made a different way
		Piece l = new Piece(Piece.STICK_STR);
		assertEquals(1, l.getWidth());
		assertEquals(4, l.getHeight());
		
		// Now try with some other pieces
		assertEquals(1, stick.getWidth());
		assertEquals(4, stick.getHeight());
		assertEquals(2, l1.getWidth());
		assertEquals(3, l1.getHeight());		
		assertEquals(2, square.getWidth());
		assertEquals(2, square.getHeight());
		
		// Now try with some another piece and all of its rotations
		// Testing size and rotation here
		assertEquals(3, u1.getWidth());
		assertEquals(4, u1.getHeight());
		assertEquals(4, u2.getWidth());
		assertEquals(3, u2.getHeight());
		assertEquals(3, u3.getWidth());
		assertEquals(4, u3.getHeight());
		assertEquals(4, u4.getWidth());
		assertEquals(3, u4.getHeight());
		
		// Now try with some more extreme pieces.
		assertEquals(5, zigZag.getWidth());
		assertEquals(5, zigZag.getHeight());
		assertEquals(5, snake.getWidth());
		assertEquals(3, snake.getHeight());
	}
	
	
	// Test the skirt returned by a few pieces
	@Test
	public void testSampleSkirt() {
		// Note must use assertTrue(Arrays.equals(... as plain .equals does not work
		// right for arrays.
		assertTrue(Arrays.equals(new int[] {0, 0, 0}, pyr1.getSkirt()));
		assertTrue(Arrays.equals(new int[] {1, 0, 1}, pyr3.getSkirt()));
		
		assertTrue(Arrays.equals(new int[] {0, 0, 1}, s.getSkirt()));
		assertTrue(Arrays.equals(new int[] {1, 0}, sRotated.getSkirt()));
		
		assertTrue(Arrays.equals(new int[] {0}, stick.getSkirt()));
		assertTrue(Arrays.equals(new int[] {0, 0, 0, 0}, stick.computeNextRotation().getSkirt()));
		assertTrue(Arrays.equals(new int[] {0, 0}, l1.getSkirt()));
		assertTrue(Arrays.equals(new int[] {0, 0}, square.getSkirt()));
		
		assertTrue(Arrays.equals(new int[] {0, 0, 0}, u1.getSkirt()));
		assertTrue(Arrays.equals(new int[] {0, 0, 0, 0}, u2.getSkirt()));
		assertTrue(Arrays.equals(new int[] {0, 3, 0}, u3.getSkirt()));
		assertTrue(Arrays.equals(new int[] {0, 0, 0, 0}, u4.getSkirt()));
		
		assertTrue(Arrays.equals(new int[] {0, 0, 1, 2, 3}, zigZag.getSkirt()));
		assertTrue(Arrays.equals(new int[] {4, 3, 2, 1, 0}, zigZag.computeNextRotation().getSkirt()));
		assertTrue(Arrays.equals(new int[] {0, 2, 0, 0, 0}, snake.getSkirt()));
		assertTrue(Arrays.equals(new int[] {0, 0, 0}, snake.computeNextRotation().getSkirt()));
	}
	
	
	// Test the equals functionality. 
	// Equals is tested before fastRotation because unit test for fastRotation is dependent on equals functionality.
	@Test
	public void testEquals() {
		assertTrue((new Piece("0 0  0 1  0 2  0 3")).equals(stick));
		assertTrue((new Piece("0 3  0 1  0 0  0 2")).equals(stick));
		assertTrue((new Piece("0 0  0 1  1 0  1 1")).equals(square));
		assertTrue((new Piece("0 1  1 1  0 0  1 0")).equals(square));
		assertTrue((new Piece("0 0  1 0  2 0  1 1")).equals(pyr1));
		assertFalse((new Piece("0 0  0 1  0 2  0 3")).equals(pyr1));
	}
	
	// Test the fastRotation functionality is working correctly. 
	@Test
	public void testFastRotation() {
		Piece stickRotate = stick.fastRotation();
		assertEquals(stickRotate, stickRotate);
		assertTrue((new Piece("0 0  1 0  2 0  3 0")).equals(stickRotate));
		assertTrue((new Piece("0 0  0 1  0 2  0 3")).equals(stickRotate.fastRotation()));
		assertTrue((new Piece("0 0  1 0  0 1  1 1")).equals(square.fastRotation()));
		assertTrue((new Piece("0 1  1 0  1 1  1 2")).equals(pyr.fastRotation()));
		assertTrue((new Piece("0 1  1 0  1 1  2 1")).equals(pyr.fastRotation().fastRotation()));
		assertTrue((new Piece("0 0  0 1  1 1  0 2")).equals(pyr.fastRotation().fastRotation().fastRotation()));
		assertTrue((new Piece("0 0  1 0  1 1  2 0")).equals(pyr.fastRotation().fastRotation().fastRotation().fastRotation()));
		assertFalse((new Piece("0 1  1 0  1 1  1 2")).equals(pyr1.fastRotation()));

	}
	
}
