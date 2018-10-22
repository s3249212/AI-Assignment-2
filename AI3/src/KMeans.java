import java.util.*;

import static java.lang.Math.pow;

public class KMeans extends ClusteringAlgorithm
{
	// Number of clusters
	private int k;

	// Dimensionality of the vectors
	private int dim;
	
	// Threshold above which the corresponding html is prefetched
	private double prefetchThreshold;
	
	// Array of k clusters, class cluster is used for easy bookkeeping
	private Cluster[] clusters;
	
	// This class represents the clusters, it contains the prototype (the mean of all it's members)
	// and memberlists with the ID's (which are Integer objects) of the datapoints that are member of that cluster.
	// You also want to remember the previous members so you can check if the clusters are stable.
	static class Cluster
	{
		float[] prototype;

		Set<Integer> currentMembers;
		Set<Integer> previousMembers;
		  
		public Cluster()
		{
			currentMembers = new HashSet<Integer>();
			previousMembers = new HashSet<Integer>();
		}
	}
	// These vectors contains the feature vectors you need; the feature vectors are float arrays.
	// Remember that you have to cast them first, since vectors return objects.
	private Vector<float[]> trainData;
	private Vector<float[]> testData;

	// Results of test()
	private double hitrate;
	private double accuracy;
	
	public KMeans(int k, Vector<float[]> trainData, Vector<float[]> testData, int dim)
	{
		this.k = k;
		this.trainData = trainData;
		this.testData = testData; 
		this.dim = dim;
		prefetchThreshold = 0.5;
		
		// Here k new cluster are initialized
		clusters = new Cluster[k];
		for (int ic = 0; ic < k; ic++) {
			clusters[ic] = new Cluster();
			clusters[ic].prototype = new float[dim];
		}
	}


	public boolean train()
	{
	 	//implement k-means algorithm here:
		// Step 1: Select an initial random partioning with k clusters
		Random rand = new Random();
		for(int i = 0; i < trainData.size(); i++) {
			clusters[rand.nextInt(k)].currentMembers.add(i);
		}

		for(int i = 0; i < k; i++) {
			/*for (int j = 0; j < dim; j++) {
				clusters[i].prototype[j] = 0;
			}
			for (int index : clusters[i].currentMembers) {
				for (int j = 0; j < dim; j++) {
					clusters[i].prototype[j] += trainData.get(index)[j] / clusters[i].currentMembers.size();
				}
			}*/
			for (int j = 0; j < dim; j++) {
				clusters[i].prototype[j] = 0;
				for (int index : clusters[i].currentMembers) {
					clusters[i].prototype[j] += trainData.get(index)[j] / clusters[i].currentMembers.size();
				}
			}
		}
		boolean go_on = true;
		while(go_on) {
			// Step 2: Generate a new partition by assigning each datapoint to its closest cluster center
			for (Cluster cluster : clusters) {
				cluster.previousMembers = cluster.currentMembers;
				cluster.currentMembers = new HashSet<Integer>();
			}
			for (int j = 0; j < trainData.size(); j++) {
				float[] datapoint = trainData.get(j);
				double minDistance = 100000000;
				Cluster winner = null;
				for (Cluster cluster : clusters) {
					double distance = 0;
					for (int i = 0; i < dim; i++) {
						distance += pow((cluster.prototype[i] - datapoint[i]), 2);
					}
					if (distance < minDistance) {
						minDistance = distance;
						winner = cluster;
					}
				}
				winner.currentMembers.add(j);
			}
			// Step 3: recalculate cluster centers
			for (int i = 0; i < k; i++) {
				/*for (int j = 0; j < dim; j++) {
					clusters[i].prototype[j] = 0;
				}
				for (int index : clusters[i].currentMembers) {
					for (int j = 0; j < dim; j++) {
						clusters[i].prototype[j] += trainData.get(index)[j] / clusters[i].currentMembers.size();
					}
				}*/
				for (int j = 0; j < dim; j++) {
					clusters[i].prototype[j] = 0;
					for (int index : clusters[i].currentMembers) {
						clusters[i].prototype[j] += trainData.get(index)[j] / clusters[i].currentMembers.size();
					}
				}
			}
			// Step 4: repeat until clustermembership stabilizes
			go_on = false;
			for (Cluster cluster : clusters) {
				go_on = go_on || cluster.currentMembers.equals(cluster.previousMembers);
			}
		}
		return false;
	}

	public boolean test()
	{
		double nHits = 0;
		double nPrefetchedHtmls = 0;
		double nRequests = 0;
		// iterate along all clients. Assumption: the same clients are in the same order as in the testData
		for(int i = 0; i < testData.size(); i++) {
			// for each client find the cluster of which it is a member
			Cluster cluster = null;
			for (Cluster c : clusters) {
				if(c.currentMembers.contains(i)) {
					cluster = c;
					break;
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
			// get the actual testData (the vector) of this client
			// iterate along all dimensions
			// and count prefetched htmls
			// count number of hits
			// count number of requests
		}
		// set the global variables hitrate and accuracy to their appropriate value

		hitrate = nHits / nRequests;
		//double correctRejection = n
		accuracy = nHits / nPrefetchedHtmls;
		System.out.println(k + " " + prefetchThreshold + " " + hitrate + " " + accuracy);
		return true;
	}


	// The following members are called by RunClustering, in order to present information to the user
	public void showTest()
	{
		System.out.println("Prefetch threshold=" + this.prefetchThreshold);
		System.out.println("Hitrate: " + this.hitrate);
		System.out.println("Accuracy: " + this.accuracy);
		System.out.println("Hitrate+Accuracy=" + (this.hitrate + this.accuracy));
	}
	
	public void showMembers()
	{
		for (int i = 0; i < k; i++)
			System.out.println("\nMembers cluster["+i+"] :" + clusters[i].currentMembers);
	}
	
	public void showPrototypes()
	{
		for (int ic = 0; ic < k; ic++) {
			System.out.print("\nPrototype cluster["+ic+"] :");
			
			for (int ip = 0; ip < dim; ip++)
				System.out.print(clusters[ic].prototype[ip] + " ");
			
			System.out.println();
		 }
	}

	// With this function you can set the prefetch threshold.
	public void setPrefetchThreshold(double prefetchThreshold)
	{
		this.prefetchThreshold = prefetchThreshold;
	}
}
