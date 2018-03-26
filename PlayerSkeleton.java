
public class PlayerSkeleton {

	//implement this function to have a working system
	public int pickMove(State s, int[][] legalMoves) {
		bumpinessHeuristic(s);
		completeLinesHeuristic(s);
		return 0;
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

	public int bumpinessHeuristic(State s){
		int[][] fields = s.getField();
		int[] depths = new int[fields[0].length];
		int depth = 0;
		int bumpiness = 0;
		boolean debug = false;

		//if field[i][j] is not 0 then it is empty, else it is occupied by a block
		for(int j=0; j<fields[0].length; j++){
			for(int i=fields.length - 1; i>=0; i--){
				if(fields[i][j] != 0 || i == 0){
					depths[j] = depth;
					depth = 0;
					break;
				} else { //i=0; continue going deeper
					depth++;
				}
			}
		}
		
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
