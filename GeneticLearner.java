import java.util.*;

public class GeneticLearner {
	
	public static void main(String[] args) {
		int initialSize = 1000;
		double cutoff = 0.3;
		int numGenerations = 0;
		int cutoffGenerations = 50;
		
		Population p = new Population(initialSize);
		
		// Run this for a fixed number (cutoffGenerations) of generations
		while(numGenerations < cutoffGenerations) {
			// A single generation producing offspring
			while(p.offspringProduced < initialSize*cutoff) {
				p.crossover();
			}
			
			System.out.print("Generation " + (numGenerations+1) + ": ");
			p.getFittest();
			
			// Once this generation produces a certain percentage of offspring, purge the population
			p.purge();
			numGenerations++;
		}
	}

}

class Population {
	public static final int HEURISTICS = 4;
	public int originalSize;
	public int offspringProduced;
	public PriorityQueue<Vector> vectors;
	Comparator<Vector> comparator = new Comparator<Vector>() {
		public int compare(Vector a, Vector b) {
			return Integer.compare(b.fitness, a.fitness); 
		}
	};
	
	/**
	 * Constructs the population with given population size
	 * 	- Creates the given number of vectors and calculates their fitness
	 *  - Adds the vectors into the max heap of vectors (by fitness)
	 */
	public Population(int populationSize) {
		originalSize = populationSize;
		
		vectors = new PriorityQueue<Vector>(populationSize, comparator);
		
		for (int i = 0; i < populationSize; i++) {
			Vector v = new Vector();
			vectors.add(v);
		}
		
		offspringProduced = 0;
		System.out.println("Initial population created.");
	}

	/**
	 * Randomly samples 10% of the population
	 * Chooses the two vectors with the highest fitness
	 * Crosses the two most fit vectors by the formula:
	 * 		v1*fitness(v1) + v2*fitness(v2)
	 * Adds this new vector into the population
	 */
	public void crossover() {
		PriorityQueue<Vector> sample = samplePopulation();

		// Takes the 2 fittest vectors
		Vector a = sample.poll();
		Vector b = sample.poll();
		if (a == null || b == null) {
			return;
		}
		
		double[] newWeights = new double[HEURISTICS];
		for (int i = 0; i < HEURISTICS; i++) {
			newWeights[i] = a.weights[i]*a.fitness + b.weights[i]*b.fitness;
		}

		Vector v = new Vector(newWeights);
		
		addOffspring(v);
	}
	
	/**
	 * Adds a new vector to the population
	 * Increments the number of offspring produced
	 */
	public void addOffspring(Vector v) {
		vectors.add(v);
		offspringProduced += 1;
	}
	
	public static final double sampleProp = 0.1;
	
	/**
	 * Return a max heap with a sample of 10% of the current population
	 */
	public PriorityQueue<Vector> samplePopulation() {
		int sampleSize = new Double(originalSize*sampleProp).intValue();
		PriorityQueue<Vector> sample = new PriorityQueue<Vector>(sampleSize, comparator);
		Vector[] p = new Vector[sampleSize];
		p = vectors.toArray(new Vector[sampleSize]);
		List<Vector> population = Arrays.asList(p);
		// Shuffles the population to simulate randomness in selection
		Collections.shuffle(population);

		for (int i = 0; i < sampleSize; i++) {
			Vector v = population.get(i);
			sample.add(v);
		}
		
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
		PriorityQueue<Vector> q = new PriorityQueue<Vector>(originalSize, comparator);

		while(q.size() < originalSize) {
			q.add(vectors.poll());
		}

		vectors = q;
		offspringProduced = 0;
	}
	
	/**
	 * Returns the fitness of the best vector in this current population
	 */
	public int getFittest() {
		Vector v = vectors.peek();
		System.out.print(Arrays.toString(v.weights) + ", fitness: " + v.fitness + "\n");
		return v.fitness;
	}
}

class Vector {
	public static final int HEURISTICS = 4; 
	public double[] weights;
	public int fitness;
	
	public static final double randomMin = -1;
	public static final double randomMax = 1;

	/**
	 * Generates a vector with random weights
	 */
	public Vector() {
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
	public Vector(double[] w) {
		weights = new double[HEURISTICS];
		for (int i = 0; i < HEURISTICS; i++) {
			weights[i] = w[i];
		}
		
		mutate();
		calculateFitness();
	}
	
	/**
	 * Plays the game and sets the score to the fitness
	 */
	public void calculateFitness() {
		PlayerSkeleton p = new PlayerSkeleton(weights);
		fitness = p.playGame();
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