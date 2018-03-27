import java.util.Arrays;

public class PlayerSkeleton {
	public static final int COLS = 10;
	public static final int ROWS = 21;
	//indices for legalMoves
	public static final int ORIENT = 0;
	public static final int SLOT = 1;
	
	// Used for each simulated turn
	public boolean gameEnded = false;
	public int rowsCleared;
	
	//implement this function to have a working system
	public int pickMove(State s, int[][] legalMoves) {
		int bestMove = 0;
		double bestScore = Integer.MIN_VALUE;
		for (int i = 0; i < legalMoves.length; i++) {
			double score = 0;
			gameEnded = false;
			int[] move = legalMoves[i];
			int[][] newField = simulateMove(s, move);
			int bumpiness = bumpinessHeuristic(newField);
			int completeLines = rowsCleared;
			int aggregateHeight = aggregateHeightHeuristic(newField);
			int holes = holesHeuristic(newField);
			if (!gameEnded) {
				// TODO: How to weight heuristics?
				score = 0.760666*completeLines + (-0.510066)*aggregateHeight + (-0.184483)*bumpiness + (-0.5)*holes;
			} else {
				// TODO: This move results in the game ending, how to handle?
				score = Integer.MIN_VALUE;
			}
			if (score > bestScore) {
				bestMove = i;
				bestScore = score;
			}
		}
		System.out.println("Best Move: " + Arrays.toString(legalMoves[bestMove]));
		System.out.println("Best Score: " + bestScore);
		return bestMove;
	}
	


	public int aggregateHeightHeuristic(int[][] field) {
		int aggregateHeight = 0;
		int[] heights = getColumnHeights(field);

		for(int i=0; i<heights.length; i++) {
			aggregateHeight += heights[i];
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

	public int bumpinessHeuristic(int[][] field){
		int bumpiness = 0;
		boolean debug = false;

		int[] depths = getColumnHeights(field);
		
		if(debug){
			System.out.println("Start Bumpiness Heuristic Printout");
			for(int i=0; i<field.length; i++){
				for(int j=0; j<field[i].length; j++){
					System.out.print(field[i][j]);
				}
				System.out.println();
			}
			System.out.println("End Bumpiness Heuristic Printout");


			System.out.println("Start Depth Score Printout");	
		}
		
		for(int i=0; i<depths.length - 1; i++){
			//System.out.print(depths[i] + " ");
			bumpiness += Math.abs(depths[i] - depths[i+1]);
		}
//		if(debug){
//			System.out.println("Bumpiness is " + bumpiness);
//			System.out.println("End Depth Score Printout");	
//		}

		return bumpiness;
	}

	//returns an int array of column heights from column 0 to column 9, left to right
	public int[] getColumnHeights(int[][] field) {
		int[] heights = new int[field[0].length];
		int maxHeight = field.length;

		//if field[i][j] is not 0 then it is empty, else it is occupied by a block
		for(int j=0; j<field[j].length; j++) {
			for(int i=maxHeight; i>0; i--) {
				if(field[i-1][j] != 0) {
					heights[j] = i;
					break;
				}
			}
		}

		return heights;
	}
	
	// Similar logic to makeMove() in State, simulates a single move based on an original state
	// @param s - original state
	// @param move - move to simulate
	// @return field - a duplicated field with the new outcome based on the move
	public int[][] simulateMove(State s, int[] move) {
		int[][] originalField = s.getField();
		int[] originalTop = s.getTop();
		int[][] pWidth = State.getpWidth();
		int[][] pHeight = State.getpHeight();
		int[][][] pBottom = State.getpBottom();
		int[][][] pTop = State.getpTop();
		int nextPiece = s.getNextPiece();
		int turn = s.getTurnNumber();
		int orient = move[ORIENT];
		int slot = move[SLOT];
		
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
			gameEnded = true;
			return field;
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
		
		rowsCleared = 0;
		
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
				rowsCleared++;
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

		return field;
	}
	
	public static void main(String[] args) {
		State s = new State();
		new TFrame(s);
		PlayerSkeleton p = new PlayerSkeleton();
		int turnCount = 0;
		while(!s.hasLost()) {
			System.out.println("Hello turn num " + turnCount);
			turnCount++;
//			int[][] legalMoves = s.legalMoves();
//			for (int i = 0; i < legalMoves.length; i++) {
//				System.out.println(Arrays.toString(legalMoves[i]));
//			}
			s.makeMove(p.pickMove(s,s.legalMoves()));
			s.draw();
			s.drawNext(0,0);
			try {
				Thread.sleep(300);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			System.out.println("You have completed "+s.getRowsCleared()+" rows.");
		}
		System.out.println("Has lost: " + s.hasLost());
		System.out.println("You have completed "+s.getRowsCleared()+" rows.");
	}
	
}
