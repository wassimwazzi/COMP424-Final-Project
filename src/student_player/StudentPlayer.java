package student_player;

import boardgame.Move;

import pentago_twist.PentagoPlayer;
import pentago_twist.PentagoBoardState;
import boardgame.Board;
/** A player file submitted by a student. */
public class StudentPlayer extends PentagoPlayer {

	/**
	 * You must modify this constructor to return your student number. This is
	 * important, because this is what the code that runs the competition uses to
	 * associate you with your agent. The constructor should do nothing else.
	 */
	public StudentPlayer() {
		super("260825559");
	}

	/**
	 * This is the primary method that you need to implement. The ``boardState``
	 * object contains the current state of the game, which your agent must use to
	 * make decisions.
	 */
	public Move chooseMove(PentagoBoardState boardState) {
		//return boardState.getRandomMove();
		
		long timer = System.currentTimeMillis() + 2000;
        Node rootNode = new Node(boardState, null);
        int myPlayer = boardState.getTurnPlayer();
        while (System.currentTimeMillis()<timer) {
        	MCTS.buildTree(rootNode, myPlayer,true);
        }
        Node nodeToMove = rootNode.getBestChild(myPlayer); // get best move as a node
        return nodeToMove.getMove(); // convert to move
	}
}