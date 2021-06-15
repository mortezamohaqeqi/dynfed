/* 
 * This class schedules a single DAG.
 */ 

package application.analysis.dag.util;

import java.util.LinkedList;

import application.common.enums.EPolicy;
import application.common.structs.queue.QUtil;
import application.models.dag.DagUtil;
import application.models.dag.Node;
import application.models.dag.Task;
import application.models.dag.TaskIns;
import application.util.Util;

public class SingleTaskScheduler 
{	
	public static Block minCoreSchedule(Task t, int maxCore, EPolicy policy)
	{
		TaskIns job = new TaskIns(t, 0, null);
		int cores = (int) Math.ceil((double) job.volume() / (double) job.period()); 
		
		Block schedule = null;
		while (schedule == null && cores <= maxCore)
		{
			schedule = schedule(job, cores, policy);
			cores++;
		}
		
		return schedule; 
	}
	

	private static void test(Block schedule, Task t) 
	{
		if (schedule == null)
			return;
		
		int sum = 0;
		for (int time : schedule.coreLastUsage)
			sum += time;

		if (t.volume() > sum)
			System.out.println("Error");
	}


	private static Block schedule(Task task, int m, EPolicy policy)
	{
		int RT[]        = Util.intArr(task.nnodes(), -1);
		int nodeCores[] = Util.intArr(task.nnodes(), -1);
		
		LinkedList<Interval>[]table = new LinkedList[m];
		for (int i = 0; i < m; i++)
		{
			table[i] = new LinkedList<Interval>();
			table[i].add(new Interval(0, task.period()));
		}
		
		while (done(RT) == false)
		{
			LinkedList<Node> eligibles = eligibles(task.nodes(), RT);
			//eligibles = est(eligibles, RT);
			
			Node hp = null;
			if (policy == EPolicy.DynamicFederated_BFS)
				hp = AUtil.bfs(task, eligibles);
			else if (policy == EPolicy.DynamicFederated)
				hp = QUtil.ls(eligibles); // least slack
			else
				System.out.println("Unexpected policy: " + policy);
			
			int startTime = est(hp, RT);
			int rt = allocateNode(hp, table, startTime, nodeCores); 
			if (rt < 0)
				return null;
			
			RT[hp.index()] = rt;
		}
		
		//int rt = Util.max(RT);
		// int []coresInTime = new int[rt];
		int []coresEndTime = new int[m];
		for (int i = 0; i < m; i++)
		{
			int end = task.period();
			for (Interval intv : table[i])
				if (intv.end == task.period())
					end = intv.start;
			
			coresEndTime[i] = end;
			
		}
		
		int []starts = DagUtil.startTimes(task, RT);
		return new Block(coresEndTime, starts, nodeCores);
	}
	
	private static LinkedList<Node> est(LinkedList<Node> eligibles, int[] RT) 
	{
		LinkedList<Node> est = new LinkedList<Node>();
		int start = Integer.MAX_VALUE;
		for (Node n : eligibles)
		{
			int st = est(n, RT);
			if (start > st)
			{
				est.clear();
				est.add(n);
				start = st;
			}
			else if (start == st)
				est.add(n);
		}
			
		return est;
	}

	// earliest start time
	private static int est(Node node, int[] RT) 
	{
		int res = 0;
		for (Node pred : node.parents())
			res = Math.max(res, RT[pred.index()]);
		
		return res;
	}
	
	// returns the finish time
	private static int allocateNode(Node n, LinkedList<Interval>[] table /* idle intervals */, int startTime, int[] nodeCores) 
	{
		if (n.wcet() == 0)
			return startTime;
				
		if (table.length == 0)
			return -1;
		
		int core = -1;
		int best_start = Integer.MAX_VALUE;
		Interval inl = null;
		for (int c = 0; c < table.length; c++)
		{
			for (Interval intv : table[c])
			{
				int nstart = Math.max(startTime, intv.start);
				if (intv.end - nstart >= n.wcet())
				{
					if (core == -1)
					{
						best_start = nstart;
						core = c;
						inl = intv;						
					}
					else
					{
						if (nstart < best_start)
						{
							best_start = nstart;
							core = c;
							inl = intv;			
						}
					}
				}
			}
		}
		
		if (core != -1)
		{
			Interval befor = new Interval(inl.start, best_start);
			Interval after = new Interval(best_start+n.wcet(), inl.end);
			table[core].remove(inl);
			if (befor.size() > 0)
				table[core].add(befor);
			if (after.size() > 0)
				table[core].add(after);
			
			nodeCores[n.index()] = core;
			return best_start + n.wcet();
		}
		return -1;
	}
	
	private static LinkedList<Node> eligibles(LinkedList<Node> nodes, int[] RT) 
	{
		LinkedList<Node> res = new LinkedList<Node>();
		for (Node n : nodes)
		{
			if (RT[n.index()] >= 0)	// already done
				continue;
			
			boolean parentsDone = true;
			for (Node p : n.parents())
				if (RT[p.index()] < 0)
				{
					parentsDone = false;
					break;
				}
			
			if (parentsDone)
				res.add(n);
		}
		return res;
	}

	private static boolean done(int[] RT) 
	{
		for (int r : RT)
			if (r < 0)
				return false;
		return true;
	}

	private static void test(boolean[][] table, int volume) {
		int sum = 0;
		for (int i = 0; i < table.length; i++)
			for (int k = 0; k < table[i].length; k++)
				if (table[i][k])
					sum++;
		
		if(sum != volume)
			System.out.println("Implementation Error: " + sum + " != " + volume);
	}	
}

class Interval
{
	public Interval(int s, int e) {
		start = s;
		end   = e;
	}
	
	public int size() {
		return end - start;
	}
	int start;
	int end;
}
