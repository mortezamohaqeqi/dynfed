package application.models.dag.randomgen.randomgraphs;

import application.common.structs.TasksParam;
import application.models.dag.Node;
import application.models.dag.Task;
import application.util.Util;

public class MiscellaneousGen extends AbsGraphCreator 
{
	public MiscellaneousGen(long seed)
	{
		super(seed);
	}
	
	@Override
	public Task[] generate(TasksParam params, int ntasks) 
	{
		// TODO Auto-generated method stub
		return null;
	}
	
	public Task[] harmonic5G_basic()
	{
		Util.assert_(false, "Obsolete code");
		int pop = 1;
		int wcet = 20;
		Task[] tasks = new Task[pop * 4];
		int i = 0;
		for (; i < pop; i++)
			tasks[i] = singletonTask(125, wcet, i);
				
		for (; i < 2 * pop; i++)
			tasks[i] = singletonTask(250, wcet, i);
		
		for (; i < 3 * pop; i++)
			tasks[i] = singletonTask(500, wcet, i);
		
		for (; i < 4 * pop; i++)
			tasks[i] = singletonTask(1000, wcet, i);
		
		return tasks;
	}
	
	public Task[] harmonic5G_binary()
	{
		Util.assert_(false, "Obsolete code");
		int pop = 30;
		int wcet = 20;
		Task[] tasks = new Task[pop * 4];
		int i = 0;
		for (; i < pop; i++)
			tasks[i] = doubletonTask(125, wcet, i);
				
		for (; i < 2 * pop; i++)
			tasks[i] = doubletonTask(250, wcet, i);
		
		for (; i < 3 * pop; i++)
			tasks[i] = doubletonTask(500, wcet, i);
		
		for (; i < 4 * pop; i++)
			tasks[i] = doubletonTask(1000, wcet, i);
		
		return tasks;
	}
	

	// maxDegree is the maximum degree of parallelism
	// critical path is the longest path from a root node to an end node
	public Task[] harmonic5G(int maxCritPath, int maxDegree, int ntask)
	{
		Util.assert_(false, "Obsolete code");
		Task[] tasks = new Task[ntask];
		int i = 0;
		int id = 1;
				
		for (; i < ntask / 4; i++)
			tasks[i] = generalTask(125, maxCritPath, maxDegree, id++);
				
		for (; i < 2 * ntask / 4; i++)
			tasks[i] = generalTask(250, maxCritPath, maxDegree, id++);
		
		for (; i < 3 * ntask / 4; i++)
			tasks[i] = generalTask(500, maxCritPath, maxDegree, id++);
		
		for (; i < ntask; i++)
			tasks[i] = generalTask(1000, maxCritPath, maxDegree, id++);
		
		return tasks;
	}
	
	
	// A DAG with a specified number of maximum parallelism and critical ath length
	private Task generalTask(int period, int crit, int deg, int id)
	{
		int wcet = 20; 	// 20 us
		Task t   = new Task(period, id);
		Node root= CPUNode(wcet, 1, id);
		t.addNode(root);
		int nodeCnt = 2;
		
		Node[] prevLevel = new Node[1];
		prevLevel[0] = root;		
		
		int dept = random.nextInt(crit) + 1;
		for (int d = 2; d <= dept; d++)
		{
			int nodes = random.nextInt(deg) + 1;
			Node[] thisLevel = new Node[nodes];
			for (int n = 0; n < nodes; n++)
			{
				thisLevel[n] = CPUNode(wcet, nodeCnt++, id);	// new Node(cpu_wcet(wcet), task_counter + "_" + nodeCnt++);
				int prev = random.nextInt(prevLevel.length);
				thisLevel[n].addPred(prevLevel[prev]);
				t.addNode(thisLevel[n]);
			}
			prevLevel = thisLevel;
		}
		return t;
	}
}
