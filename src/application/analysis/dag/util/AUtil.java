package application.analysis.dag.util;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import application.analysis.dag.AbsTimeAnalyzer;
import application.common.structs.SchedResult;
import application.hwmodel.enums.EResourceType;
import application.util.Util;
import application.models.dag.DagUtil;

import application.models.dag.Node;
import application.models.dag.NodeIns;
import application.models.dag.Task;
import application.models.dag.TaskIns;

public class AUtil 
{	
	public static void free(boolean[] core, int start, int wcet) 
	{
		for (int t = start; t < start + wcet; t++)
			core[t] = false;		
	}
	
	public static LinkedList<NodeIns> allJobs(Task[] tasks, long HP) 
	{
		LinkedList<NodeIns> jobs = new LinkedList<NodeIns>(); 
		for (Task t : tasks)
		{
			LinkedList<Node> nodes = t.BFS();
			for (int r = 0; r < HP; r += t.period())
			{
				HashMap<Node, NodeIns> n2ni = new HashMap<Node, NodeIns>(); 
				for (Node n : nodes)
				{
					int     last_start = r + t.period() - t.criticalPath(n);
					NodeIns ni         = new NodeIns(r, n.wcet(EResourceType.CPU), last_start, n.name(), r + t.period());
					
					// add prevs:
					for (Node p : n.parents())
						ni.addPred(n2ni.get(p));					
					
					n2ni.put(n, ni);
					jobs.add(ni);
				}				
			}
		}
		return jobs;
	}
		
	static void print(LinkedList<NodeIns> jobs)
	{
		System.out.println("----------------------------------");
		for (NodeIns n : jobs)
			System.out.println("(" + n.name() + ": rel = " + n.release() + ", est = " + 1 /* n.earliest_start() */ + ", lst = " + n.last_start() + ") ");
	}

	public static void printN(LinkedList<Node> nodes) 
	{
		for (Node n : nodes)
		{
			System.out.println(n.name() + ": " + 
							  " dl = " + n.owner().absDeadline() + 
							  " sl = " + DagUtil.slack(n));
		}
		System.out.println("------------------------");
	}

	public static LinkedList<Node> subtract(List<Node> minuend, List<Node> subtrahend) 
	{
		LinkedList<Node> diff = new LinkedList<Node>(minuend);
		for (Node n : subtrahend)
			diff.remove(n);
		return diff;
	}

	public static LinkedList<Task> toList(Task[] tasks) 
	{
		LinkedList<Task> res = new LinkedList<Task>();		
		for (Task n : tasks)
			if (n != null)
				res.add(n);
		return res;
	}

	public static Task[] sortByRM_CP(Task[] tasks, int m) 
	{
		Map<Task, Integer> R = DagUtil.individualWCRTs(tasks, m);
		
		Task[] res = new Task[tasks.length];
		List<Task> list = toList(tasks);
		int ind = 0;
		while (list.size() > 0)
		{
			res[ind] = shortestPeriod(list, R);
			list.remove(res[ind]);
			ind++;
		}
		return res;
	}

	private static Task shortestPeriod(List<Task> list, Map<Task, Integer> R) 
	{
		Util.assert_(list.size() > 0, "shortest period called on empty list");
		Task res = null;
		for (Task t : list)
		{
			if (res == null)
			{
				res = t;
			}
			else if (t.period() < res.period())
			{
				res = t;
			}
			else if (t.period() == res.period())
			{
				// if t.criticalPath > res.criticalPath
				if (R.get(t) > R.get(res)) // select the one with bigger WCRT
					res = t;
			}
		}
		return res;
	}

	public static int min(int i1, int i2, int i3) 
	{
		return Math.min(Math.min(i1, i2), i3);
	}

	public static boolean isSorted(Task[] tasks) 
	{
		for (int i = 1; i < tasks.length; i++)
		{
			if (tasks[i].period() <= tasks[i-1].period())
				return false;
		}
		return true;
	}

	public static LinkedList<Node> head(LinkedList<Node> nodes, int size) 
	{
		if (nodes.size() <= size)
			return nodes;
		
		LinkedList<Node> res = new LinkedList<Node>();
		for (Node n : nodes)
		{
			if (res.size() >= size)
				break;
			res.add(n);
		}
			
		return res;
	}

	public static LinkedList<TaskIns> actives(LinkedList<TaskIns> tasks) 
	{
		LinkedList<TaskIns> actives = new LinkedList<TaskIns>();
		for (TaskIns task : tasks)
		{
			if (task.done())
				continue;
			actives.add(task);
		}
		return actives;
	}

	public static LinkedList<TaskIns> started(LinkedList<TaskIns> tasks) 
	{
		LinkedList<TaskIns> res = new LinkedList<TaskIns>();
		for (TaskIns task : tasks)
		{
			if (task.progressed())
				res.add(task);
		}
		return res;
	}

	public static TaskIns haighest_rate(LinkedList<TaskIns> tasks) 
	{
		TaskIns res = null;
		for (TaskIns t : tasks)
		{
			if (res == null)
				res = t;
			else if (t.period() < res.period())
				res = t;
		}
		// verify:
		int cnt = 0;
		for (TaskIns t : tasks)
			if (t.period() == res.period())
				cnt++;

		Util.assert_(cnt == 1, "Two tasks with the highest rate");
		return res;
	}
	
	public static Task haighest_rate(Task[] tasks) 
	{
		Task res = null;
		for (Task t : tasks)
		{
			if (res == null)
				res = t;
			else if (t.period() < res.period())
				res = t;
		}
		return res;
	}

	public static LinkedList<TaskIns> removeMissed(LinkedList<TaskIns> tasks, int time) 
	{
		LinkedList<TaskIns> res = new LinkedList<TaskIns>();
		for (TaskIns t : tasks)
			if (t.absDeadline() > time)
				res.add(t);
				
		return res;
	}

	public static TaskIns first_come(LinkedList<TaskIns> tasks) 
	{
		LinkedList<TaskIns> er = new LinkedList<TaskIns>(); // earliest releases
		
		for (TaskIns t : tasks)
		{
			if (er.size() == 0)
				er.add(t);
			else if (er.getFirst().releaseTime() == t.releaseTime())
				er.add(t);
			else if (er.getFirst().releaseTime() > t.releaseTime())
			{
				er.clear();
				er.add(t);
			}				
		}
		
		return haighest_rate(er);
	}

	public static boolean missed(LinkedList<TaskIns> tasks, int time) 
	{
		for (TaskIns t : tasks)
			if (t.done() == false && t.absDeadline() <= time)
				return true;
		
		return false;
	}

	public static void printSchedule(AbsTimeAnalyzer scheduler, SchedResult res) 
	{
		if (res.schedulable() == false)
		{
			System.out.println("System Unschedulable :(");
			return;
		}
		
		scheduler.printSchedule();		
		
		System.out.println("Utilization = " + Util.round(res.util(), 3) + ", tasks = " + res.tasks());
		System.out.println("===================================================");	
	}
	
	public static Node bfs(Task task, LinkedList<Node> eligs) 
	{
		for (Node n : eligs)
			if (n.owner() != task)
				System.out.println("Error: nodes from different tasks cannot be compared w.r.t. BFS");
		
		for (Node n : task.BFS())
			if (eligs.contains(n))
				return n;
		
		return null;
	}
	
	static public void ensure_progress(int time, int lastTime) 
	{
		int progres = time - lastTime;
		Util.assert_(progres > 0, "Error: no progress in system! (now = " + time + ", last = " + lastTime + ")");
	}	
}
