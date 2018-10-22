import java.util.*;

import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

public class Kohonen extends ClusteringAlgorithm
{
	// Size of clustersmap
	private int n;

	// Number of epochs
	private int epochs;
	
	// Dimensionality of the vectors
	private int dim;
	
	// Threshold above which the corresponding html is prefetched
	private double prefetchThreshold;

	private double initialLearningRate;

	private double initialR;
	
	// This class represents the clusters, it contains the prototype (the mean of all it's members)
	// and a memberlist with the ID's (Integer objects) of the datapoints that are member of that cluster.  
	private Cluster[][] clusters;

	// Vector which contains the train/test data
	private Vector<float[]> trainData;
	private Vector<float[]> testData;
	
	// Results of test()
	private double hitrate;
	private double accuracy;
	
	static class Cluster
	{
			float[] prototype;

			Set<Integer> currentMembers;

			public Cluster()
			{
				currentMembers = new HashSet<Integer>();
			}
	}
	
	public Kohonen(int n, int epochs, Vector<float[]> trainData, Vector<float[]> testData, int dim)
	{
		this.n = n;
		this.epochs = epochs;
		prefetchThreshold = 0.5;
		initialLearningRate = 0.8;
		initialR = 0.5;
		this.trainData = trainData;
		this.testData = testData; 
		this.dim = dim;       
		
		Random rnd = new Random();
		boolean print = true;
		// Here n*n new cluster are initialized
		clusters = new Cluster[n][n];
		for (int i = 0; i < n; i++)  {
			for (int i2 = 0; i2 < n; i2++) {
				clusters[i][i2] = new Cluster();
				clusters[i][i2].prototype = new float[dim];
				if(print) {
					System.out.println("Prototype");
				}
				for (int i3 = 0; i3 < dim; i3++) {
					clusters[i][i2].prototype[i3] = rnd.nextFloat();
					if(print) {
						System.out.println(clusters[i][i2].prototype[i3]);
					}
				}
				if(print) {
					//print = false;
				}
			}
		}
	}

	
	public boolean train()
	{
		// Step 1: initialize map with random vectors (A good place to do this, is in the initialisation of the clusters)
		// Repeat 'epochs' times:
		for(int t = 0; t < epochs; t++) {
			// Step 2: Calculate the squareSize and the learningRate, these decrease lineary with the number of epochs.

			// Step 3: Every input vector is presented to the map (always in the same order)
			// For each vector its Best Matching Unit is found, and :
			for(Cluster[] ca: clusters) {
				for(Cluster cluster: ca) {
					cluster.currentMembers = new HashSet<>();
				}
			}
			for (int j = 0; j < trainData.size(); j++) {
				float[] datapoint = trainData.get(j);
				double minDistance = 100000000;
				Cluster bmu = null;
				for (int i = 0; i < n; i++) {
					for (int i2 = 0; i2 < n; i2++) {
						Cluster cluster = clusters[i][i2];
						double distance = 0;
						for (int k = 0; k < dim; k++) {
							distance += pow((cluster.prototype[k] - datapoint[k]), 2);
						}
						//System.out.println("Client " + j + " distance " + distance);
						if (distance < minDistance) {
							minDistance = distance;
							bmu = cluster;
						}
					}
				}
				bmu.currentMembers.add(j);
			}
			double sump = 0;
			boolean print = true;
			double r = (float) n / 2 * (1 - (float)t / epochs);
			double eta = initialLearningRate * (1 - (float)t / epochs);
			for(int i = 0; i < n; i++) {
				for(int i2 = 0; i2 < n; i2++) {
					Cluster bmu = clusters[i][i2];
					if(print) {
						//System.out.println("Prototype:");
						for (float p : bmu.prototype) {
							//System.out.println(p + " ");
							sump += p;
						}
						//System.out.println("");
					}
					for(int j: bmu.currentMembers) {
						float[] datapoint = trainData.get(j);
						for(int i3 = (int)Math.ceil(i - r); i3 < i + r; i3++) {
							for(int i4 = (int)Math.ceil(i2 - r); i4 < i2 + r ; i4++) {
								if(i3 >= 0 && i3 < n && i4 >= 0 && i4 < n) {
									for (int k = 0; k < dim; k++) {
										bmu.prototype[k] = (float) ((1 - eta) * clusters[i3][i4].prototype[k] + eta * datapoint[k]);
									}
								}
							}
						}
						/*for (int i = 0; i < n; i++) {
							for (int i2 = 0; i2 < n; i2++) {
								Cluster cluster = clusters[i][i2];
								double distance = 0;
								for (int k = 0; k < dim; k++) {
									distance = pow((cluster.prototype[k] - bmu.prototype[k]), 2);
								}
								if (sqrt(distance) < r) {
									for (int k = 0; k < dim; k++) {
										bmu.prototype[k] = (float) ((1 - eta) * cluster.prototype[k] + eta * datapoint[k]);
									}
								}
							}
						}*/
					}
				}
				/**/
			}
			double avgp = sump / (dim * n * n);
			System.out.println("Is it random? " + avgp);
				// Step 4: All nodes within the neighbourhood of the BMU are changed, you don't have to use distance relative learning.
		}
			// Since training kohonen maps can take quite a while, presenting the user with a progress bar would be nice
		return true;
	}
	
	public boolean test()
	{
		for(Cluster ca[]: clusters) {
			for(Cluster c: ca) {
				//System.out.println(c.currentMembers);
			}
		}
		double nPrefetchedHtmls = 0;
		double nHits = 0;
		int nRequests = 0;
		int nCorrect = 0;
		// iterate along all clients
		for(int i = 0; i < testData.size(); i++) {
			// for each client find the cluster of which it is a member
			Cluster cluster = null;
			for(Cluster ca[]: clusters) {
				for(Cluster c: ca) {
					//System.out.println(c.currentMembers);
					if (c.currentMembers.contains(i)) {
						//System.out.println("Contains " + i);
						//System.out.println("");
						cluster = c;
						break;
					}
				}
			}
			// get the actual testData (the vector) of this client
			float[] data = testData.get(i);
			// iterate along all dimensions
			for(int j = 0; j < dim; j++) {
				// and count prefetched htmls
				if(cluster.prototype[j] >= prefetchThreshold) {
					nPrefetchedHtmls++;
				}
				if((cluster.prototype[j] >= prefetchThreshold) && (data[j] == 1)) {
					nHits++;
				}
				if(data[j] == 1){
					nRequests++;
				}
				// count number of hits
				// count number of requests
			}
			// set the global variables hitrate and accuracy to their appropriate value
		}
		hitrate = nHits / nRequests;
		//double correctRejection = n
		accuracy = nHits / nPrefetchedHtmls;
		System.out.println(n + " " + prefetchThreshold + " " + hitrate + " " + accuracy);
		return true;
	}


	public void showTest()
	{
		System.out.println("Initial learning Rate=" + initialLearningRate);
		System.out.println("Prefetch threshold=" + prefetchThreshold);
		System.out.println("Hitrate: " + hitrate);
		System.out.println("Accuracy: " + accuracy);
		System.out.println("Hitrate+Accuracy=" + (hitrate + accuracy));
	}
 
 
	public void showMembers()
	{
		for (int i = 0; i < n; i++)
			for (int i2 = 0; i2 < n; i2++)
				System.out.println("\nMembers cluster["+i+"]["+i2+"] :" + clusters[i][i2].currentMembers);
	}

	public void showPrototypes()
	{
		for (int i = 0; i < n; i++) {
			for (int i2 = 0; i2 < n; i2++) {
				System.out.print("\nPrototype cluster["+i+"]["+i2+"] :");
				
				for (int i3 = 0; i3 < dim; i3++)
					System.out.print(" " + clusters[i][i2].prototype[i3]);
				
				System.out.println();
			}
		}
	}

	public void setPrefetchThreshold(double prefetchThreshold)
	{
		this.prefetchThreshold = prefetchThreshold;
	}
}

