import java.util.Arrays;

public class PlayerSkeleton {
	public static final int COLS = 10;
	public static final int ROWS = 21;
	public static final int N_PIECES = 7;
	//indices for legalMoves
	public static final int ORIENT = 0;
	public static final int SLOT = 1;

	//completeLines, aggregateHeight, bumpiness, holes
	public double[] heuristicWeights;

	//possible orientations for a given piece type
	protected static int[] pOrients = {1,2,4,4,4,2,2};

	//the next several arrays define the piece vocabulary in detail
	//width of the pieces [piece ID][orientation]
	protected static int[][] pWidth = {
			{2},
			{1,4},
			{2,3,2,3},
			{2,3,2,3},
			{2,3,2,3},
			{3,2},
			{3,2}
	};

	//all legal moves - first index is piece type - then a list of 2-length arrays
	protected static int[][][] allLegalMoves = new int[N_PIECES][][];

	//initialize legalMoves
	{
		//for each piece type
		for(int i = 0; i < N_PIECES; i++) {
			//figure number of legal moves
			int n = 0;
			for(int j = 0; j < pOrients[i]; j++) {
				//number of locations in this orientation
				n += COLS+1-pWidth[i][j];
			}
			//allocate space
			allLegalMoves[i] = new int[n][2];
			//for each orientation
			n = 0;
			for(int j = 0; j < pOrients[i]; j++) {
				//for each slot
				for(int k = 0; k < COLS+1-pWidth[i][j];k++) {
					allLegalMoves[i][n][ORIENT] = j;
					allLegalMoves[i][n][SLOT] = k;
					n++;
				}
			}
		}

	}

	public PlayerSkeleton(double[] hW){
		heuristicWeights = hW;
	}

	//implement this function to have a working system
	public int pickMove(State s, int[][] legalMoves) {
		int bestMove;
		bestMove = depthTwoSearch(new Node(heuristicWeights, s.getField(), s.getTop(), 0, false), legalMoves, s.getNextPiece());
		return bestMove;
	}

	/**
	 * Assigns a score to each legal move which is the average of optimal scores for every possible piece playable
	 * after the legal move is played.
	 * Optimal score for each piece is obtained by exhaustively searching every orient and position.
	 * Returns the move corresponding to the best average score among legal moves.
	 * ref: http://www.diva-portal.org/smash/get/diva2:815662/FULLTEXT01.pdf
	 * @param s state to apply depth-2 search on
	 * @param legalMoves of the nextPiece
	 * @param nextPiece integer representing the next piece
	 * @return best move to be played at depth-1
	 */
	public int depthTwoSearch(Node s, int[][] legalMoves, int nextPiece) {
		double bestAvg = Integer.MIN_VALUE;
		int bestDepthOneMove = 0;
		double[] averages = new double[legalMoves.length];

		for(int i = 0; i < legalMoves.length; i++) {
			Node depthOneNode = s.simulateMove(legalMoves[i], nextPiece);
			double[] optimalScores = new double[N_PIECES];
			for(int j = 0; j < N_PIECES; j++) {
				double bestScore = Integer.MIN_VALUE;
				for(int k = 0; k < allLegalMoves[j].length; k++) {
					Node depthTwoNode = depthOneNode.simulateMove(allLegalMoves[j][k], j);
					double newScore = depthTwoNode.getScore();
					if (newScore > bestScore) {
						bestScore = newScore;
					}
				}
				optimalScores[j] = bestScore;
			}

			double avg = 0;
			for(int l = 0; l < N_PIECES; l++) {
				avg += optimalScores[l];
			}
			avg /= N_PIECES;
			averages[i] = avg;
		}

		for(int i = 0; i < averages.length; i++) {
			if(averages[i] > bestAvg) {
				bestAvg = averages[i];
				bestDepthOneMove = i;
			}
		}

		return bestDepthOneMove;
	}

	public static void main(String[] args) {
		State s = new State();
//		new TFrame(s);
		PlayerSkeleton p = new PlayerSkeleton(new double[]{0.1636736030816534, -0.11117594223369093, -0.20390418721234355, -0.9501423421384158, -0.12846584618379997});
		while(!s.hasLost()) {
			s.makeMove(p.pickMove(s,s.legalMoves()));
//			s.draw();
//			s.drawNext(0,0);
//			try {
//				Thread.sleep(300);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
			System.out.println("You have completed "+s.getRowsCleared()+" rows.");
		}
		System.out.println("Has lost: " + s.hasLost());
		System.out.println("You have completed "+s.getRowsCleared()+" rows.");
	}

	// Plays the game and returns the number of rows cleared
	public int playGame() {
		State s = new State();
		while(!s.hasLost()) {
			s.makeMove(pickMove(s, s.legalMoves()));
		}
		return s.getRowsCleared();
	}
}

class Node {
	public static final int COLS = 10;
	public static final int ROWS = 21;
	//indices for legalMoves
	public static final int ORIENT = 0;
	public static final int SLOT = 1;

	private boolean gameEnded;
	private int[][] originalField;
	private int[] originalTop;
	private double score;

	//completeLines, aggregateHeight, bumpiness, holes
	public double[] heuristicWeights;

	//the next several arrays define the piece vocabulary in detail
	//width of the pieces [piece ID][orientation]
	protected static int[][] pWidth = {
			{2},
			{1,4},
			{2,3,2,3},
			{2,3,2,3},
			{2,3,2,3},
			{3,2},
			{3,2}
	};
	//height of the pieces [piece ID][orientation]
	private static int[][] pHeight = {
			{2},
			{4,1},
			{3,2,3,2},
			{3,2,3,2},
			{3,2,3,2},
			{2,3},
			{2,3}
	};
	private static int[][][] pBottom = {
			{{0,0}},
			{{0},{0,0,0,0}},
			{{0,0},{0,1,1},{2,0},{0,0,0}},
			{{0,0},{0,0,0},{0,2},{1,1,0}},
			{{0,1},{1,0,1},{1,0},{0,0,0}},
			{{0,0,1},{1,0}},
			{{1,0,0},{0,1}}
	};
	private static int[][][] pTop = {
			{{2,2}},
			{{4},{1,1,1,1}},
			{{3,1},{2,2,2},{3,3},{1,1,2}},
			{{1,3},{2,1,1},{3,3},{2,2,2}},
			{{3,2},{2,2,2},{2,3},{1,2,1}},
			{{1,2,2},{3,2}},
			{{2,2,1},{2,3}}
	};

	public Node(double[] hW, int[][] originalField, int[] originalTop, int rowsCleared, boolean gameEnded) {
		this.heuristicWeights = hW;
		this.originalField = originalField;
		this.originalTop = originalTop;
		this.gameEnded = gameEnded;

		calculateScore(rowsCleared);
	}

	// Similar logic to makeMove() in State, simulates a single move based on an original state
	// @param s - original state
	// @param move - move to simulate
	// @return field - a duplicated field with the new outcome based on the move
	public Node simulateMove(int[] move, int nextPiece) {
		int turn = 1;
		int orient = move[ORIENT];
		int slot = move[SLOT];
		boolean hasGameEnded = gameEnded;

		// Copy into a new field and top array
		// Otherwise it will refer to the same field, top arrays for each simulation
		int[][] field = new int[ROWS][COLS];
		int[] top = new int[COLS];
		for (int i = 0; i < ROWS; i++) {
			for (int j = 0; j < COLS; j++) {
				field[i][j] = originalField[i][j];
			}
		}
		for (int i = 0; i < COLS; i++) {
			top[i] = originalTop[i];
		}

		//height if the first column makes contact
		int height = top[slot]-pBottom[nextPiece][orient][0];
		//for each column beyond the first in the piece
		for(int c = 1; c < pWidth[nextPiece][orient];c++) {
			height = Math.max(height,top[slot+c]-pBottom[nextPiece][orient][c]);
		}

		//check if game ended
		if(height+pHeight[nextPiece][orient] >= ROWS) {
			hasGameEnded = true;
			return new Node(heuristicWeights, field, top, 0, hasGameEnded);
		}


		//for each column in the piece - fill in the appropriate blocks
		for(int i = 0; i < pWidth[nextPiece][orient]; i++) {

			//from bottom to top of brick
			for(int h = height+pBottom[nextPiece][orient][i]; h < height+pTop[nextPiece][orient][i]; h++) {
				field[h][i+slot] = turn;
			}
		}

		//adjust top
		for(int c = 0; c < pWidth[nextPiece][orient]; c++) {
			top[slot+c]=height+pTop[nextPiece][orient][c];
		}

		int linesCompleted = 0;

		//check for full rows - starting at the top
		for(int r = height+pHeight[nextPiece][orient]-1; r >= height; r--) {
			//check all columns in the row
			boolean full = true;
			for(int c = 0; c < COLS; c++) {
				if(field[r][c] == 0) {
					full = false;
					break;
				}
			}
			//if the row was full - remove it and slide above stuff down
			if(full) {
				linesCompleted++;
				//for each column
				for(int c = 0; c < COLS; c++) {

					//slide down all bricks
					for(int i = r; i < top[c]; i++) {
						field[i][c] = field[i+1][c];
					}
					//lower the top
					top[c]--;
					while(top[c]>=1 && field[top[c]-1][c]==0)	top[c]--;
				}
			}
		}

		return new Node(heuristicWeights, field, top, linesCompleted, hasGameEnded);
	}

	//Must call simulateMove before calling this method
	public void calculateScore(int rowsCleared) {
		int bumpiness = bumpinessHeuristic(originalTop);
		int completeLines = rowsCleared;
		int aggregateHeight = aggregateHeightHeuristic(originalTop);
		int holes = holesHeuristic(originalField);
		int wellSum = wellSumHeuristic(originalTop);
		if (!gameEnded) {
			score = heuristicWeights[0] * completeLines + heuristicWeights[1] * aggregateHeight + heuristicWeights[2] * bumpiness + heuristicWeights[3] * holes + heuristicWeights[4] * wellSum;
		} else {
			score = Integer.MIN_VALUE;
		}
	}

	public int aggregateHeightHeuristic(int[] top) {
		int aggregateHeight = 0;

		for(int i=0; i<COLS; i++) {
			aggregateHeight += top[i];
		}

		return aggregateHeight;
	}

	public int holesHeuristic(int[][] field) {
		int numHoles = 0;

		for (int j=0; j<field[j].length; j++) {
			int startIdx = 0;
			int endIdx = 0;
			for(int i=0; i<field.length; i++) {
				if(field[i][j] != 0) {
					numHoles += (endIdx - startIdx);
					startIdx = i+1;
					endIdx = i+1;
				} else {
					endIdx++;
				}
			}
		}

		return numHoles;
	}

	public int bumpinessHeuristic(int[] top){
		int bumpiness = 0;

		for(int i=0; i<COLS - 1; i++){
			//System.out.print(depths[i] + " ");
			bumpiness += Math.abs(top[i] - top[i+1]);
		}

		return bumpiness;
	}

	// sum of all well heights
	public int wellSumHeuristic(int[] top) {
		int wellSum = 0;

		for(int j = 0; j < COLS; j++) {
			if (j == 0) {
				if (top[j] < top[j+1]) {
					int wellHeight = top[j+1] - top[j];
					wellSum += wellHeight * (wellHeight+1) / 2;
				}
			} else if (j == COLS-1) {
				if (top[j] < top[j-1]) {
					int wellHeight = top[j-1] - top[j];
					wellSum += wellHeight * (wellHeight+1) / 2;
				}
			} else if (top[j] < top[j-1] && top[j] < top[j+1]) {
				int wellHeight = Math.min(top[j-1], top[j+1]) - top[j];
				wellSum += wellHeight * (wellHeight+1) / 2;
			}
		}
		return wellSum;
	}

	//unused
	public int blockadeHeuristic(int[][] field) {
		int numBlockades = 0;

		for (int j = 0; j < COLS; j++) {
			boolean countingBlockades = false;
			for (int i = 0; i < ROWS; i++) {
				if (countingBlockades) {
					if (field[i][j] != 0) {
						numBlockades++;
					}
				} else {
					if (field[i][j] == 0) {
						countingBlockades = true;
					}
				}
			}
		}
		return numBlockades;
	}

	public double getScore() {
		return score;
	}

}