import java.util.*;
import java.util.concurrent.CountDownLatch;

/**
 * This is the code for our Genetic Algorithm. This file is not necessary to run PlayerSkeleton.java.
 */
public class GeneticLearner {
	
	public static void main(String[] args) {
		int initialSize = 1000;
		double cutoff = 0.3;
		int numGenerations = 0;
		int cutoffGenerations = Integer.MAX_VALUE;
		
		StopWatch sw = new StopWatch();
		
		try {
			sw.start();
			Population p = new Population(initialSize);
			System.out.println("------------------------------------------------------");
			System.out.println("PROFILE: Population created in " + sw.getTime() + "ms");
			System.out.println("------------------------------------------------------");
			sw.start();
			// Run this for a fixed number (cutoffGenerations) of generations
			while(numGenerations < cutoffGenerations) {
				
				// A single generation producing offspring
				while(p.offspringProduced < initialSize*cutoff) {
					p.crossover();
				}

				System.out.println("---------------GENERATION PROFILE------------------");
				System.out.print("Generation " + (numGenerations+1) + ": ");
				p.getFittest();
				
				// Once this generation produces a certain percentage of offspring, purge the population
				p.purge();
				

				p.profile();
				System.out.println("Total time elapsed: " + sw.getTime());
				System.out.println("---------------------------------------------------");
				
				numGenerations++;
			}
			
			
			
		} catch (Exception e) {
			System.out.println("error: " + e);
		}
		

	}

}

class Population {
	public static final int HEURISTICS = 5;
	public int originalSize;
	public int offspringProduced;
	public PriorityQueue<WeightVector> vectors;
	
	// Used to profile each generation
	public StopWatch purgeClock;
	public StopWatch sampleClock;
	public StopWatch crossClock;
	
	
	Comparator<WeightVector> comparator = new Comparator<WeightVector>() {
		public int compare(WeightVector a, WeightVector b) {
			return Double.compare(b.fitness, a.fitness);
		}
	};
	
	/**
	 * Constructs the population with given population size
	 * 	- Creates the given number of vectors and calculates their fitness
	 *  - Adds the vectors into the max heap of vectors (by fitness)
	 */
	public Population(int populationSize) {
		originalSize = populationSize;
		int checkpointSize = 10;
		int checkpoint = populationSize/checkpointSize;
		vectors = new PriorityQueue<WeightVector>(populationSize, comparator);
		System.out.println("Initializing population...");
		for (int i = 0; i < populationSize; i++) {
			if ((i+1) % checkpoint == 0) {
				System.out.println("..." + ((i+1)*100/populationSize) + "%");
				System.out.print("Current best: ");
				getFittest();
			}
			WeightVector v = new WeightVector();
			vectors.add(v);
		}
		
		offspringProduced = 0;
		System.out.println("\nInitial population created.");
		
		purgeClock = new StopWatch();
		sampleClock = new StopWatch();
		crossClock = new StopWatch();
	}

	/**
	 * Randomly samples 10% of the population
	 * Chooses the two vectors with the highest fitness
	 * Crosses the two most fit vectors by the formula:
	 * 		v1*fitness(v1) + v2*fitness(v2)
	 * Adds this new vector into the population
	 */
	public void crossover() {
		PriorityQueue<WeightVector> sample = samplePopulation();
		
		crossClock.start();
		// Takes the 2 fittest vectors
		WeightVector a = sample.poll();
		WeightVector b = sample.poll();
		if (a == null || b == null) {
			return;
		}
		
		double[] newWeights = new double[HEURISTICS];
		for (int i = 0; i < HEURISTICS; i++) {
			newWeights[i] = a.weights[i]*a.fitness + b.weights[i]*b.fitness;
		}

		WeightVector v = new WeightVector(newWeights);
		crossClock.clock();
		addOffspring(v);
	}
	
	/**
	 * Adds a new vector to the population
	 * Increments the number of offspring produced
	 */
	public void addOffspring(WeightVector v) {
		vectors.add(v);
		offspringProduced += 1;
	}
	
	public static final double sampleProp = 0.1;
	
	/**
	 * Return a max heap with a sample of 10% of the current population
	 */
	public PriorityQueue<WeightVector> samplePopulation() {
		sampleClock.start();
		int sampleSize = new Double(originalSize*sampleProp).intValue();
		PriorityQueue<WeightVector> sample = new PriorityQueue<WeightVector>(sampleSize, comparator);
		
		WeightVector[] p = new WeightVector[sampleSize];
		p = vectors.toArray(new WeightVector[sampleSize]);
		List<WeightVector> population = Arrays.asList(p);
		// Shuffles the population to simulate randomness in selection
		Collections.shuffle(population);

		for (int i = 0; i < sampleSize; i++) {
			WeightVector v = population.get(i);
			sample.add(v);
		}
		
		sampleClock.clock();
		return sample;
		
	}
	
	/**
	 * Gets the size of the population
	 */
	public int size() {
		return vectors.size();
	}
	
	/**
	 * Creates a new max heap of vectors and pushes the top (originalSize) vectors
	 * Sets this heap to be the new heap of the population
	 * Essentially removes the least fit vectors until we get back the original population size
	 * Resets the number of offspring produced to 0.
	 */
	public void purge() {
		purgeClock.start();
		PriorityQueue<WeightVector> q = new PriorityQueue<WeightVector>(originalSize, comparator);

		while(q.size() < originalSize) {
			q.add(vectors.poll());
		}

		vectors = q;
		offspringProduced = 0;
		purgeClock.clock();
	}
	
	/**
	 * Returns the fitness of the best vector in this current population
	 */
	public double getFittest() {
		WeightVector v = vectors.peek();
		System.out.print(Arrays.toString(v.weights) + ", fitness: " + v.fitness + "\n");
		return v.fitness;
	}
	
	/**
	 * Prints the accumulated time of purge(), samplePopulation() and (crossing and creating a new vector)
	 */
	public void profile() {
		System.out.println("Sample total elapsed: " + sampleClock.getElapsedTime() + "ms");
		System.out.println("Purge total elapsed: " + purgeClock.getElapsedTime() + "ms");
		System.out.println("Crossing total elapsed: " + crossClock.getElapsedTime() + "ms");
	}
}

class WeightVector {
	public static final int HEURISTICS = 5;
	public double[] weights;
	public double fitness;
	
	public static final double randomMin = -1;
	public static final double randomMax = 1;

	/**
	 * Generates a vector with random weights
	 */
	public WeightVector() {
		weights = new double[HEURISTICS];
		for (int i = 0; i < HEURISTICS; i++) {
			weights[i] = randomMin + (randomMax-randomMin)*Math.random();
		}
		
		normalize();
		calculateFitness();
	}
	
	/**
	 * Generates a vector with the specified weights
	 */
	public WeightVector(double[] w) {
		weights = new double[HEURISTICS];
		for (int i = 0; i < HEURISTICS; i++) {
			weights[i] = w[i];
		}

		normalize();
		mutate();
		calculateFitness();
	}
	
	/**
	 * Plays the game and sets the score to the fitness
	 */
	public void calculateFitness() {
		int numGames = 10;
		PlayerSkeleton p = new PlayerSkeleton(weights);
		Vector<Integer> scores = new Vector<>();
		CountDownLatch completionSignal = new CountDownLatch(numGames);
		
		try {
			for (int i = 0; i < numGames; i++) {
				Thread t = new Thread(new GameRunner(completionSignal, scores, p));
				t.start();
			}
			
			// Wait for all threads to complete.
			completionSignal.await();
		} catch (Exception e) {
			System.out.println("Thread Interrupted");
		}
		
		Integer[] s = new Integer[numGames];
		double total = 0;
		scores.toArray(s);
		for (int i = 0; i < numGames; i++) {
			total += s[i];
		}
		
		this.fitness = total/numGames;
	}
	
	public static final double mutationThreshold = 0.2;
	public static final double mutationChance = 0.05;

	/**
	 * This is called when the vector is initialized with weights
	 * (after being crossed by two parents)
	 * It has a 5% chance of mutating a random weight to up to +/- 0.2
	 * The vector is then normalized
	 */
	public void mutate() {
		if (Math.random() <= mutationChance) {
			Random rand = new Random();
			int randomIndex = rand.nextInt(HEURISTICS);
			double mutationAmount = Math.random()*(mutationThreshold*2) - mutationThreshold;
			weights[randomIndex] += mutationAmount;
		}
		
		normalize();
	}
	
	/**
	 * Normalizes the vector based on its magnitude
	 */
	public void normalize() {
		double magnitude = 0;
		for (int i = 0; i < HEURISTICS; i++) {
			magnitude += Math.pow(weights[i], 2);
		}
		magnitude = Math.sqrt(magnitude);
		
		for (int i = 0; i < HEURISTICS; i++) {
			weights[i] /= magnitude;
		}
	}
}

class GameRunner implements Runnable {
	// Require thread-safe list
	private Vector<Integer> scores;
	private PlayerSkeleton player;
	private CountDownLatch doneSignal;
	
	public GameRunner(CountDownLatch doneSignal, Vector<Integer> scores, PlayerSkeleton player) {
		this.doneSignal = doneSignal;
		this.scores = scores;
		this.player = player;
	}
	
	// Plays the game and adds the score to scores.
	public void run() {
		int score = player.playGame();
		scores.add(score);
		doneSignal.countDown();
	}
}

/**
 * Simple stopwatch class
 * @author tanzh
 *
 */
class StopWatch {
	long startTime;
	long elapsedTime;
	
	public StopWatch() {
		startTime = 0;
		elapsedTime = 0;
	}
	
	public void start() {
		startTime = System.nanoTime();
	}
	
	public void reset() {
		elapsedTime = 0;
	}
	
	// Used for simple start-stop functionality.
	// Returns the time elapsed in ms.
	public long getTime() {
		return (System.nanoTime() - startTime)/1000000;
	}
	
	// Keeps track of the accumulated time.
	public void clock() {
		elapsedTime += System.nanoTime() - startTime;
	}
	
	// Gets the time elapsed
	public long getElapsedTime() {
		return elapsedTime/1000000;
	}
}

