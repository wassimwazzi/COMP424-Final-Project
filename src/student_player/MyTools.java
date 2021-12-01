package student_player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import boardgame.Board;
import pentago_twist.PentagoBoardState;
import pentago_twist.PentagoBoardState.Piece;
import pentago_twist.PentagoCoord;
import pentago_twist.PentagoMove;


public class MyTools {
    public static double getSomething() {
        return Math.random();
    }
}

class Node {
	private PentagoBoardState state;
	private ArrayList<Node> children;
	private Node parent;
	private int numVisits=0;
	private double numWins=0;
	private PentagoMove move;
	

	public Node(PentagoBoardState state, Node parent) {
		this.state = state;
		this.parent = parent;
		ArrayList <Node> children = new ArrayList<Node>();
	}
	public Node(Node node) {
		this.state = (PentagoBoardState) node.getState().clone();
	}
	public PentagoBoardState getState() {
		return this.state;
	}
	public ArrayList<Node> getChildren(){
		return this.children;
	}
	public void setChildren(ArrayList<Node> children) {
		this.children=children;
	}
	public Node getRandomChild() {
		if (this.getChildren().size()==0) this.addChildren();
		int allMoves = this.getChildren().size();
		int randMove = (int) (Math.random() * allMoves);
		return this.children.get(randMove);
	}
	public Node getParent() {
		return this.parent;
	}
	public int getVisits() {
		return this.numVisits;
	}
	public double getWins(){
		return this.numWins;
	}
	public void incrementVisits() {
		this.numVisits++;
	}
	public void incrementWins() {
		this.numWins++;
	}
	public void incrementWinsIfDraw() {
		this.numWins+=0.4;
	}
	public PentagoMove getMove() {
		return this.move;
	}
	public void setMove(PentagoMove move) {
		this.move=move;
	}
	public Node getChildWMaxVisits() {
		return Collections.max(this.children, Comparator.comparing(x -> {
			return x.getVisits();
		}));
	}
	// get child with max visits or child that guarantees a win
	public Node getBestChild(int myPlayer) {
		  Node bestchild = this.getChildWMaxVisits();
	        for(Node node: this.getChildren()) {
	        	if (node.getState().getWinner()==myPlayer) bestchild=node;
	        }
	        return bestchild;
	}

	// Fill children array
	public void addChildren(){
		ArrayList<PentagoMove> all_possible_moves = this.state.getAllLegalMoves();
		ArrayList<Node> children = new ArrayList<Node>();
		for(PentagoMove move: all_possible_moves) {
			PentagoBoardState new_state = (PentagoBoardState) this.state.clone();
			new_state.processMove(move);
			Node child = new Node(new_state, this);//Each new child has parent this
			child.setMove(move);
			children.add(child);
			//this.setChildren(children);
		}
		this.setChildren(children);
	}
}
class MCTS{
	// Recursive function to traverse tree until leaf is reached
	public static Node selectPromisingNode(Node root){
		if (root.getChildren()==null) return root;
		return selectPromisingNode(UCT.childWithBestUCT(root));
	}
	/*public static int simPlayout(Node node, boolean withHeuristic) {
		Node cur = new Node(node);
		PentagoBoardState cur_state = cur.getState();
		int winner = cur_state.getWinner();
		while (winner==Board.NOBODY) {
			
			PentagoMove move = (PentagoMove) cur_state.getRandomMove();
			cur_state.processMove(move);
			winner = cur_state.getWinner();
		}
		return winner;
	}
	*/
	
	//Simulating random game starting from a node
	public static int simRandomGame(Node node) {
		Node cur = new Node(node);
		PentagoBoardState cur_state = cur.getState();
		int winner = cur_state.getWinner();
		while (winner==Board.NOBODY) {
			PentagoMove move = (PentagoMove) cur_state.getRandomMove();
			cur_state.processMove(move);
			winner = cur_state.getWinner();
		}
		return winner;
	}
	// was used for heuristic function
	/*public static int simSmartPlayout(Node node) {
		Node cur = new Node(node); // clone the node
		PentagoBoardState cur_state = cur.getState(); // clone state
		int winner = cur_state.getWinner();
		while (winner==Board.NOBODY) {
			PentagoMove move =  Heuristic.getSmartMove(cur_state);
			cur_state.processMove(move);
			winner = cur_state.getWinner();
		}
		return winner;
	}
	*/
	// recursively propagate the results up the tree
	public static void backPropagate(Node nodeExplored, boolean won, boolean draw) {
		if(nodeExplored==null) return;
		nodeExplored.incrementVisits();
		if(won) nodeExplored.incrementWins();
		if(draw) nodeExplored.incrementWinsIfDraw();
		backPropagate(nodeExplored.getParent(), won, draw);
	}
	public static void buildTree(Node node, int player, boolean withHeuristic) {
    	Node mostPromising =selectPromisingNode(node);
    	mostPromising.addChildren();
    	int winner;
    	Node toSim = mostPromising.getRandomChild();
    	//if (!withHeuristic) {
    	winner = simRandomGame(toSim);
    	//}
    	//else {
    	//winner = simSmartPlayout(toSim);
    	//}
    	boolean won = winner==player;
    	boolean draw = winner==Board.DRAW;
    	backPropagate(toSim, won, draw);
	}
}
class UCT {
	// Compute UCT Score
    public static double uctScore(int parentVisits, double nodeWins, int nodeVisits) {
        if (nodeVisits == 0) {
            return Integer.MAX_VALUE;
        }
        return ((double) nodeWins/ (double) nodeVisits) 
          + Math.sqrt(2) * Math.sqrt(Math.log(parentVisits) / (double) nodeVisits);
    }
    
    //Returns child with highest UCT Score
    public static Node childWithBestUCT(Node node) {
        int parentVisits = node.getVisits();
        double maxUCT = 0;
        Node bestChild = null;
        ArrayList<Node> children = node.getChildren();
        for(Node child: children) {
            double nodeWins = child.getWins();
            int nodeVisits = child.getVisits();
            double childUCT= uctScore(parentVisits, nodeWins, nodeVisits);
        	if (childUCT>maxUCT) {
        		maxUCT=childUCT;
        		bestChild = child;
        	}
        }
        return bestChild;
    }
}

		/*int player=state.getTurnPlayer();
		int opponent=1-player;
		int maxScore=0;
		PentagoCoord coord = move.getMoveCoord();
		int row = coord.getX();
		int col = coord.getY();
		ArrayList<Integer> ar = new ArrayList<Integer>(Arrays.asList(0,1,2,3,4,5));
		
	}
	*/

