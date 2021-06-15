package application.models.dag.randomgen.randomgraphs;

import java.util.Random;

import application.common.enums.taskgen.EAppDomain;
import application.common.enums.taskgen.EGraphGenMethod;
import application.common.structs.TasksParam;
import application.models.dag.Node;
import application.models.dag.Task;

public abstract class AbsGraphCreator 
{
	static private final int SEED = 100;
	
	Random random;

	public abstract Task[] generate(TasksParam params, int ntasks);

	public Task generate(TasksParam params)
	{
		return generate(params, 1)[0];
	}
	
	public AbsGraphCreator(long seed) 
	{
		random = new Random(seed) ;
	}
	
	Node CPUNode(int wcet, int nodeNo, int task_id)
	{
		return Node.CPUNode(wcet, task_id + "_" + nodeNo);
	}

	Node MemoryNode(int wcet, int nodeNo, int task_id)
	{
		return Node.MemoryNode(wcet, task_id + "_" + nodeNo);
	}
	
	// A DAG with only one node
	Task singletonTask(int period, int wcet, int task_id)
	{
		Task t  = new Task(period, task_id);
		Node n  = CPUNode(wcet, 0, task_id); // new Node(cpu_wcet(wcet), task_counter + "");
		t.addNode(n);
		return t;
	}
	
	// A DAG with two nodes
	Task doubletonTask(int period, int wcet, int task_id)
	{
		Task t   = new Task(period, task_id);
		Node n1  = CPUNode(wcet, 1, task_id);
		Node n2  = CPUNode(wcet, 2, task_id);
		t.addNode(n1);
		t.addNode(n2);
		n2.addPred(n1);
		return t;
	}	

	public static AbsGraphCreator createInstance(EGraphGenMethod graph) 
	{
		if (graph == EGraphGenMethod.SeqParallel) // .LOG_UNIFORM || period_type == EAppDomain.AUTOSAR_SP)
			return new SPTasks(SEED);
		
		else if (graph == EGraphGenMethod.Layered) 
			return new LayeredDAGs(SEED);
		
		else
			System.out.println("Warning: unknown EAppDomain: " + graph);
		
		return null;
	}


	public Random rng() 
	{
		return random;
	}
}
