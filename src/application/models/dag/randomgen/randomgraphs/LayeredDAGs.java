package application.models.dag.randomgen.randomgraphs;

import application.common.structs.TasksParam;
import application.models.dag.Node;
import application.models.dag.Task;

public class LayeredDAGs extends AbsGraphCreator {

	public LayeredDAGs(long seed) 
	{
		super(seed);
	}	
	
	// maxDegree is the maximum degree of parallelism
	// critical path is the longest path from a root node to an end node
	public Task[] generate(TasksParam params, int ntask)
	{
		int maxCritPath = params.layers;
		int minNodes    = params.minNodes;
		int maxNodes    = params.maxNodes;
		double p = params.prob;
		int minC = params.minWCET;
		int maxC = params.maxWCET;
		
		int task_id = 1;
		Task[] tasks = new Task[ntask];
		int i = 0;
		while (i < ntask)
		{
			int n    = random.nextInt(1+maxNodes-minNodes) + minNodes;				
			tasks[i] = layerByLayerTask(maxCritPath, n, p, minC, maxC, task_id++);
			i++;
		}
		return tasks;
	}
	
	/**
	 * Cordeiro 2010 (Random graph generation for scheduling simulations)
	 * @return
	 */
	private Task layerByLayerTask(int crit, int n, double prob, int minC, int maxC, int task_id)
	{
		Task t       = new Task(task_id);
		int[] layer  = new int[n]; // layer[i] is the layer of the i-th node
		Node[] nodes = new Node[n];
		
		// Creating nodes
		for (int i = 0; i < n; i++)
		{
			int wcet = random.nextInt(1+maxC-minC) + minC; //  20; 	// 20 us			
			layer[i] = random.nextInt(crit);
			nodes[i] = CPUNode(wcet, i+1, task_id); 
			t.addNode(nodes[i]);
		}
		
		// Adding edges
		for (int i = 0; i < n; i++)
			for (int j = 0; j < n; j++)
				if (layer[j] > layer[i])
					if (random.nextDouble() < prob)
						nodes[j].addPred(nodes[i]);	

		return t;
	}
}
