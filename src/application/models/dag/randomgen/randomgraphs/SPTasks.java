package application.models.dag.randomgen.randomgraphs;

import java.util.LinkedList;

import application.common.structs.TasksParam;
import application.models.dag.Node;
import application.models.dag.Task;

public class SPTasks extends AbsGraphCreator
{
	final double p_term = 0.4; // //  0.7; // 
	final double addProb = 0.1;

	final int maxParBranches = 6; // //  3; // ???    n_par
	// final int maxNodes = 50; // 
    final int minC = 1;
    final int maxC = 50;
    final int rec_depth =   3; // //  2; //  
        
    public SPTasks(long seed)
    {
    	super(seed);
	}
    
    public Task[] generate(TasksParam params, int ntask)
    {
    	int task_id = 1;
		Task[] tasks = new Task[ntask];
		for (int i = 0 ; i < ntask; i++)
		{
			tasks[i] = gen(task_id);
			task_id++;
		}	
		
		return tasks;
    }

	public Task gen(int task_id)
	{
		LinkedList<V> v = new LinkedList<V>();		
		expandTaskSeriesParallel(v, -1, -1, rec_depth, 0);	
		V[] vs = V.toArray(v);
		assignWCET(vs, minC, maxC);
		makeItDAG(vs);
		
		return toTask(vs, task_id);	    
	}
	
	private void assignWCET(V[] v, int Cmin, int Cmax) 
	{
		for (V node : v)
			node.wcet = randi(Cmin, Cmax);
	}

	private Task toTask(V[] v, int task_id) 
	{
		Task t = new Task(10, "" + task_id);
		Node[] nodes = new Node[v.length];
		
		// Creating nodes
		for (int i = 0; i < v.length; i++)
		{
			nodes[i] = CPUNode(v[i].wcet, i+1, task_id); 
			t.addNode(nodes[i]);
		}
		
		// Adding edges
		for (int i = 0; i < v.length; i++)
			for (int j : v[i].pred)
				nodes[i].addPred(nodes[j]);	
		
		return t;
	}

	private void expandTaskSeriesParallel(LinkedList<V> v, int source, int sink, int depth, int numBranches)
	{
		// double horSpace = Math.pow(maxParBranches, depth);
	    
		if (source == -1 && sink == -1)
		{
			v.addLast(new V(depth));
			v.addLast(new V(-depth));
			int parBranches = randi(2, maxParBranches);
			expandTaskSeriesParallel(v, 0, 1, depth - 1, parBranches);
		}
		else
		{	       
	        for(int i = 1; i <= numBranches; i++)
    		{
	        	//if (length(v) >= maxNodes)
	    		//	return;
	    		
	        	int current = last_index(v);
	        	int x = 3;	            
	            if (depth != 0)
	                if (rand() > p_term) 
	                	x = 2;

	            if (x == 3) //   % terminal vertex
	            {
	            	V t = new V(depth);
                    t.pred.add(source);
                    t.succ.add(sink);
                    
                    v.get(source).succ.add(current + 1);
                    v.get(sink).pred.add(current + 1);
                    v.add(t);
	            }
	            else // x == 2   % parallel subgraph
	            {
	            	//if (length(v) >= maxNodes - 1)
	        		//	return;
	        		
	            	V t1 = new V(depth); 
	            	V t2 = new V(-depth); 
	            	t1.pred.add(source);                    
	                v.get(source).succ.add(current + 1);
	                
	                t2.succ.add(sink);	                
	                v.get(sink).pred.add(current + 2);
	                v.add(t1);
	                v.add(t2);
	                
	                int parBranches = randi(2, maxParBranches);
	                expandTaskSeriesParallel(v, current + 1, current + 2, depth - 1, parBranches); 	            	
	            }
    		}
		}
	}
	
	private void makeItDAG(V[] v)
	{
		for (int i = 0; i < v.length; i++)
	        for (int j = 0; j < v.length; j++)
	        	if (v[i].depth > v[j].depth)   // % the depth of the source is greater than that of the destination
		              if( !v[i].succ.contains(j) &&  // % j is not already a successor of i     
	        			randi(1, 100) <= addProb * 100)                      // % the probability of this event respects the given probability value		                    
				{
					v[i].succ.add(j);
                    v[j].pred.add(i);
				}
	}
	
	private double rand() 
	{
		return random.nextDouble();
	}

	private int last_index(LinkedList<V> v) {
		return v.size() - 1; // the index of the last leement
	}

	// return a random int between a and b (inclusive)
	private int randi(int a, int b)
	{
		return a + random.nextInt(b+1-a);	
	}
}

class V 
{
	public V(int dep) 
	{
		depth = dep;
		//width = wid;
	}
	
	public static V[] toArray(LinkedList<V> list) 
	{
		V[] arr = new V[list.size()];
		int ind = 0;
		for (V v : list)
		{
			arr[ind] = v;
			ind++;
		}
		return arr;
	}

	int wcet;
	int depth = 0;
	//double width;
	LinkedList<Integer> succ = new LinkedList<Integer>();
	LinkedList<Integer> pred = new LinkedList<Integer>();	
}
