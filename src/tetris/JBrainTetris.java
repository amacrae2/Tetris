package tetris;

import java.awt.Dimension;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.UIManager;

@SuppressWarnings("serial")
public class JBrainTetris extends JTetris{
	
	private static String OK_TEXT = "ok";
	private static String OK_STAR_TEXT = "*ok*";
	private static String STATUS_TEXT = "Status: ";
	
	private static int ADVERSARY_SLIDER_LENGTH = 100;
		
	private JCheckBox brainMode;
	private JSlider adversary;
	
	private JLabel status = new JLabel(STATUS_TEXT + OK_TEXT);

	private DefaultBrain brain = new DefaultBrain();

	JBrainTetris(int pixels) {
		super(pixels);
	}
	
	/**
	 Creates the panel of UI controls -- controls wired
	 up to call methods on the JTetris. Adds a brain active checkbox.
	*/
	public JComponent createControlPanel() {
		JPanel panel = (JPanel) super.createControlPanel();
		
		panel.add(new JLabel("Brain:"));
		brainMode = new JCheckBox("Brain active"); 
		panel.add(brainMode);
		
		// make a little panel, put a JSlider in it. JSlider responds to getValue() 
		JPanel little = new JPanel();
		little.add(new JLabel("Adversary:"));
		adversary = new JSlider(0, 100, 0); // min, max, current 
		adversary.setPreferredSize(new Dimension(100,15)); 
		little.add(adversary);
		// now add little to panel of controls
		panel.add(little);
		panel.add(status);		
		
		return panel;
	}
	
	/**
	 Called to change the position of the current piece.
	 Each key press calls this once with the verbs
	 LEFT RIGHT ROTATE DROP for the user moves,
	 and the timer calls it with the verb DOWN to move
	 the piece down one square.

	 Before this is called, the piece is at some location in the board.
	 This advances the piece to be at its next location.
	 
	 Overriden by the brain when it plays.
	*/
	public void tick(int verb) {
		if (!gameOn) return;
		
		if (currentPiece != null) {
			board.undo();	// remove the piece from its old position
		}
		
		if (verb == DOWN && brainMode.isSelected()) {
			board.undo();
			Brain.Move bestMove = brain.bestMove(board, currentPiece, HEIGHT+TOP_SPACE, null);
			movePieceIfNeeded(bestMove.x,currentX);
			rotatePieceIfNeeded(bestMove.piece,currentPiece);
		}
		
		super.tick(verb);
	}
	
	/**
	 Selects the next piece to use using the random generator
	 set in startGame().
	*/
	public Piece pickNextPiece() {
		int randIndex = (int) (ADVERSARY_SLIDER_LENGTH * random.nextDouble());
		Piece piece = null;
		
		if (randIndex >= adversary.getValue()) {
			status.setText(STATUS_TEXT + OK_TEXT);
			piece = super.pickNextPiece();
		} else {
			// Finds the worst piece with highest best score (worst best score).
			status.setText(STATUS_TEXT + OK_STAR_TEXT);
			Piece worstPiece = null;
			double worstScore = 0;
			for (Piece candidatePiece: pieces) {
				Brain.Move bestMove = brain.bestMove(board, candidatePiece, HEIGHT+TOP_SPACE, null);
				if (bestMove.score > worstScore) {
					worstScore = bestMove.score;
					worstPiece = candidatePiece;
				}
			}
			piece = worstPiece;
		}
		
		return(piece);
	}
	
	private void movePieceIfNeeded(int targetX, int currentX) {
		if (targetX < currentX) {
			super.tick(LEFT);
		} else if (targetX > currentX) {
			super.tick(RIGHT);
		}
	}

	private void rotatePieceIfNeeded(Piece targetPiece, Piece currentPiece) {
		if (!targetPiece.equals(currentPiece)) {
			super.tick(ROTATE);
		}
	}

	/**
	 Creates a frame with a JBrainTetris.
	*/
	public static void main(String[] args) {
		// Set GUI Look And Feel Boilerplate.
		// Do this incantation at the start of main() to tell Swing
		// to use the GUI LookAndFeel of the native platform. It's ok
		// to ignore the exception.
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception ignored) { }
		
		JBrainTetris tetris = new JBrainTetris(16);
		JFrame frame = JTetris.createFrame(tetris);
		frame.setVisible(true);
	}

}
