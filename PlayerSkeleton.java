
public class PlayerSkeleton {

	//implement this function to have a working system
	public int pickMove(State s, int[][] legalMoves) {
		bumpinessHeuristic(s);
		completeLinesHeuristic(s);
		aggregateHeightHeuristic(s);
		holesHeuristic(s);
		return 0;
	}

	public int aggregateHeightHeuristic(State s) {
		int aggregateHeight = 0;
		int[] heights = getColumnHeights(s);

		for(int i=0; i<heights.length; i++) {
			aggregateHeight += heights[i];
		}

		return aggregateHeight;
	}

	public int completeLinesHeuristic(State s) {
		int[][] field = s.getField();
		int completeLines = 0;

		for(int i=0; i<field.length; i++) {
			boolean isComplete = true;
			for(int j=0; j<field[0].length; j++) {
				if(field[i][j] == 0) {
					isComplete = false;
					break;
				}
			}
			if(isComplete) {
				completeLines++;
			}
		}
		return completeLines;
	}

	public int holesHeuristic(State s) {
		int[][] field = s.getField();
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

	public int bumpinessHeuristic(State s){
		int[][] fields = s.getField();
		int bumpiness = 0;
		boolean debug = false;

		int[] depths = getColumnHeights(s);
		
		if(debug){
			System.out.println("Start Bumpiness Heuristic Printout");
			for(int i=0; i<fields.length; i++){
				for(int j=0; j<fields[i].length; j++){
					System.out.print(fields[i][j]);
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
		if(debug){
			System.out.println("Bumpiness is " + bumpiness);
			System.out.println("End Depth Score Printout");	
		}

		return bumpiness;
	}

	//returns an int array of column heights from column 0 to column 9, left to right
	public int[] getColumnHeights(State s) {
		int[][] field = s.getField();
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
	
	public static void main(String[] args) {
		State s = new State();
		new TFrame(s);
		PlayerSkeleton p = new PlayerSkeleton();
		int turnCount = 0;
		while(!s.hasLost()) {
			System.out.print("Hello turn num " + turnCount);
			turnCount++;
			s.makeMove(p.pickMove(s,s.legalMoves()));
			s.draw();
			s.drawNext(0,0);
			try {
				Thread.sleep(300);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		System.out.println("Has lost: " + s.hasLost());
		System.out.println("You have completed "+s.getRowsCleared()+" rows.");
	}
	
}
