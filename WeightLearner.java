import java.util.Arrays;

public class WeightLearner {

	public static final int NUMHEURISTICS = 4;
	public static double delta = 0.1;
	
	// Returns an array of vectors where changes have been applied onto each weight
	public static double[][] generateNewVectors(double[][] newVectors, double[] vector, boolean reduceDelta) {
		double d = calculateDelta(reduceDelta);

		for (int i = 0; i < NUMHEURISTICS*2; i++) {
			double[] newVector = new double[NUMHEURISTICS];
			// Deduct instead of add
			if (i == NUMHEURISTICS) {
				d *= -1;
			}
			
			for (int j = 0; j < NUMHEURISTICS; j++) {
				// Change a single weight
				if (i % NUMHEURISTICS == j) {
					newVector[j] = vector[j] + d;
				} else {
					newVector[j] = vector[j];
				}
			}
			
			newVectors[i] = newVector;
		}

		return newVectors;
	}
	
	// Reduce delta by half
	public static double calculateDelta(boolean reduceDelta) {
		if (reduceDelta) {
			delta /= 2;
		}
		return delta;
	}
	
	
	public static final double randomMin = -1;
	public static final double randomMax = 1; 

	// Generates a random vector to run the checks
	public static double[] generateRandomVector() {
		double[] vector = new double[NUMHEURISTICS];
		for (int i = 0; i < NUMHEURISTICS; i++) {
			vector[i] = randomMin + (randomMax-randomMin)*Math.random();
		}
		return vector;
	}
	
	public static void main(String[] args) {
		boolean continueClimb = true;
		boolean reduceDelta = false;
		double[] randomVector = generateRandomVector();
		double[][] newVectors = new double[NUMHEURISTICS*2][NUMHEURISTICS];
		generateNewVectors(newVectors, randomVector, reduceDelta);
		int bestVector = 0;
		int bestScore = Integer.MIN_VALUE;
		
		while(continueClimb) {
			for (int i = 0; i < NUMHEURISTICS*2; i++) {
				PlayerSkeleton p = new PlayerSkeleton(newVectors[i]);
				int score = p.playGame();
				if (bestScore < score) {
					bestVector = i;
				}
				System.out.println("Score: " + score);
			}
			double[] bestVec = newVectors[bestVector].clone();
			System.out.println(Arrays.toString(bestVec));
			generateNewVectors(newVectors, bestVec, reduceDelta);
		}
		
	}
	
	
}
