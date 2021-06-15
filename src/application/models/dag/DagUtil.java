/*!
 * Provides utility functions for the DAG task model. 
 */
 
 package application.models.dag;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import application.util.File;
import application.util.Util;
import application.common.Middleware;
import application.common.consts.PeriodSets;
import application.hwmodel.enums.EResourceType;

public class DagUtil 
{
	public static double CPULoad(Task[] tasks)
	{
		return workload(tasks, EResourceType.CPU);
	}

	public static double memLoad(Task[] tasks) 
	{
		return workload(tasks, EResourceType.Memory);
	}	

	private static double workload(Task[] tasks, EResourceType resource)
	{
		double v = 0;
		if (tasks != null)		
			for (int i = 0; i < tasks.length; i++)
				v += ((double)tasks[i].volume(resource)) / tasks[i].period();
		return v;
	}

	public static double utilization(Task task)
	{
		return ((double)task.volume(EResourceType.CPU)) / task.period(); 
	}

	public static double utilization(Task[] tasks)
	{
		double sum = 0;
		for (Task t : tasks)
			sum += utilization(t);

		return sum; 
	}


	public static int[] uniqe_periods(Task[]tasks)
	{
		HashSet<Integer> set = new HashSet<Integer>();
		for (Task t : tasks)
			set.add(t.period);

		return Util.toArray(set);
	}

	// assuming harmonic periods
	public static long hyperperiod(Task[] tasks) 
	{
		if (tasks.length == 0)
			return 0;

		int[] periods = uniqe_periods(tasks);
		if (PeriodSets.standardPeriod(periods) == false)
			return Long.MAX_VALUE;		

		long max = Util.max(periods);
		long HP  = 0;
		boolean done = false;
		while (!done)
		{
			HP += max;
			done = true;
			for (int p : periods)
			{
				if (HP % p != 0)
				{
					done = false;
					break;
				}
			}
		}
		return HP;
	}

	public static int minperiod(Task[] tasks) 
	{
		int min = Integer.MAX_VALUE;
		for (Task t : tasks)
			min = Math.min(min, t.period());
		return min;
	}

	public static int jobs(long HP, Task[] tasks) 
	{
		if (HP == Long.MAX_VALUE)
			return Integer.MAX_VALUE;

		// long HP = hyperperiod(tasks);
		int n = 0;
		for (Task t : tasks)
		{
			Util.assert_(HP % t.period() == 0, "HP is not hyperperiod!");
			n += HP / t.period();
		}
		return n;	
	}

	public static int jobs(Task[] tasks) 
	{
		long HP = hyperperiod(tasks);
		return jobs(HP, tasks);	
	}

	public static int[] releasePoints(Task[] tasks, long HP) 
	{
		int[] periods = uniqe_periods(tasks);
		SortedSet<Integer> releases = new TreeSet<Integer>();
		releases.add(0);
		for (int period : periods)
		{
			int r = period;
			while (r <= HP)
			{
				releases.add(r);
				r += period;
			}
		}
		return Util.toArray(releases);
	}

	public static LinkedList<TaskIns> releaseTasks(Task []tasks, int previous, int current, long HP, Middleware mw)
	{		
		LinkedList<TaskIns> newTasks = new LinkedList<TaskIns>();
		if (current >= HP)
			return newTasks;

		for (Task task : tasks)
		{
			int firstRelease = task.period() * (int)Math.ceil((double)(previous + 1) / task.period());
			for (int t = firstRelease; t <= current; t += task.period())
				// if (t % task.period() == 0)
				newTasks.add(new TaskIns(task, t, mw));
		}
		return newTasks;
	}

	public static TaskIns[] toArray(LinkedList<TaskIns> jobs) 
	{
		TaskIns[] res = new TaskIns[jobs.size()];
		int ind = 0;
		for (TaskIns job : jobs)
		{
			res[ind] = job;
			ind++;
		}
		return res;
	}

	public static Task[] toArrayT(LinkedList<Task> tasks) 
	{
		Task[] res = new Task[tasks.size()];
		int ind = 0;
		for (Task task : tasks)
		{
			res[ind] = task;
			ind++;
		}
		return res;
	}


	public static int latestFinishs(Set<TaskIns> jobs) 
	{
		int maxL = Integer.MIN_VALUE; 
		for (TaskIns job : jobs)
			if (job.lateness() > maxL)
				maxL = job.lateness();
		return maxL;
	}

	// Combined tasks with the same period.
	public static Task[] combine(Task[] tasks) 
	{
		LinkedList<Task> res = new LinkedList<Task>();
		int[] pr = uniqe_periods(tasks);
		if (pr.length == tasks.length)
			return tasks;

		for (int period : pr)
		{
			Task combined = null;
			for (Task t : tasks)
				if (t.period() == period)
				{
					if (combined == null) {
						combined = t.clone();
						continue;
					}

					for (Node n : t.nodes())
						combined.addNode(n);						
				}
			res.add(combined);
		}
		return toArrayT(res);
	}

	public static Map<Task, Integer> individualWCRTs(Task[] tasks, int cores) 
	{
		Map<Task, Integer> R = new HashMap<Task, Integer>();
		for (Task task : tasks)
			R.put(task, WCRT(task, cores));
		return R;
	}

	public static double[] individualWCRT(Task[] tasks, int cores) 
	{
		double []R = new double[tasks.length];	// WCRT of tasks		
		for (int i = 0; i < tasks.length; i++)
			R[i] = WCRT(tasks[i], cores);
		return R;
	}	

	public static int WCRT(Task task, int cores) 
	{
		Util.assert_warning(cores > 0, "Number of cores = 0 (in DagUtil.WCRT())");
		int len  = task.criticalPath();
		double R = len + ((double)task.volume() - len) / cores;
		return (int)Math.ceil(R);			
	}

	public static boolean binaryHarmonic(int[] periods) 
	{
		int []copy = Util.copy(periods);
		Arrays.sort(copy);

		for (int i = 1; i < copy.length; i++)
			if (copy[i] != 2*copy[i-1])
				return false;

		return true;
	}

	public static void printBFS(Task task) 
	{
		LinkedList<Node> bfs = task.BFS();
		for (Node n : bfs)
			System.out.print(n.absname() + ", ");
		System.out.println();
	}

	public static void print(Task[] tasks) 
	{
		for (Task t : tasks)
			System.out.println("p = " + t.period() + ", id = " + t.id() + ", vol =  " + t.volume() + ", len = " + t.criticalPath());		
	}

	public static void printBFS(Task[] tasks) 
	{
		for (Task t : tasks)
			printBFS(t);
	}

	public static void prints(LinkedList<TaskIns> jobs) {
		for (TaskIns j : jobs)
		{
			System.out.println("Name = " + j.name + ", period = " + j.period + ", vol = " + j.volume());
		}
	}

	public static void printPeriods(Task[] tasks) {
		for (Task t : tasks)
			System.out.println(t.period);
		System.out.println("---------------------");
	}

	public static void printWCETS(Task task) 
	{
		for (Node n : task.nodes)
			System.out.print(n.wcet() + ", ");
		System.out.println("---------------------");		
	}

	// extract start times from finish times
	public static int[] startTimes(Task task, int[] finishTimes) 
	{
		int[] starts = new int[finishTimes.length];
		for (Node n : task.nodes)
			starts[n.index()] = finishTimes[n.index()] - n.wcet();
		return starts;
	}

	public static void print(Task t) 
	{
		System.out.println(t.name() + ": p = " + t.period() + ", cp = " + t.criticalPath() + ", n = " + t.nnodes() + ", v = " + t.volume() + ", u = " + utilization(t));	
	}

	public static int nnodes(Task[] tasks) 
	{
		int sum = 0;
		for (Task t : tasks)
			sum += t.nnodes();
		return sum;
	}

	public static LinkedList<Node> rms(LinkedList<Node> nodes) 
	{
		Node rm = rm(nodes);
		LinkedList<Node> RMs = new LinkedList<Node>();
		for (Node n : nodes)
			if (n.period() == rm.period())
				RMs.add(n);
		return RMs;
	}

	// returns a node with shortest period (of its task)
	private static Node rm(LinkedList<Node> nodes) {
		Node res = null;
		for (Node n : nodes)
		{
			if (res == null) {
				res = n; 
				continue;
			}
			if (n.period() < res.period())
				res = n;
		}
		return res;
	}

	public static int slack(Node n)
	{
		int WCETsum = n.owner().criticalPath(n);		
		return n.owner().period() - WCETsum;
	}

	public static LinkedList<Node> toList(Node[] nodes) 
	{
		LinkedList<Node> res = new LinkedList<Node>();		
		for (Node n : nodes)
			if (n != null)
				res.add(n);
		return res;
	}

	// returns true if the two lists intersect
	public static boolean intersect(Collection<Node> L1, LinkedList<Node> L2) 
	{
		if (L1 == null || L2 == null)
			return false;

		for (Node n : L2)
			if (L1.contains(n))
				return true;

		return false;
	}

	public static void saveTS(Task[]tasks, String name)
	{
		String xml = Task.toXML(tasks, name);
		String dir = "tasksets";
		File.mkdir(dir);
		dir += "\\";
		File.save(dir + uniqueName(dir, name, ".xml"), xml);
	}

	public static String uniqueName(String path, String name, String ext)
	{
		int i = 0;
		String file = name + i;
		while (File.exists(path + file + ext))
			file = name + (++i);
		return file + ext;
	}

	// Returns a map, where each node is mapped to the list of its immediate successors.
	public static Map<Node, List<Node>> isucc(Task task) 
	{
		HashMap<Node, List<Node>> children = new HashMap<Node, List<Node>>();
		for (Node n : task.nodes())
			children.put(n, new LinkedList<Node>());


		for (Node n : task.nodes())
			for (Node pred : n.parents())
				children.get(pred).add(n);

		return children;
	}

	// Returns a map which maps each node to the list of its immediate predecessors.
	public static Map<Node, List<Node>> ipred(Task task) 
	{
		HashMap<Node, List<Node>> ancestors = new HashMap<Node, List<Node>>();
		for (Node n : task.nodes())
			ancestors.put(n, n.parents());

		return ancestors;
	}

	public static Node[] toArray(Set<Node> nodes) 
	{
		Node[] res = new Node[nodes.size()];
		int index = 0;
		for (Node n : nodes)
			res[index++] = n;

		return res;
	}

	public static void printDAG(Task[] tasks) 
	{
		for (Task t : tasks)
		{
			System.out.println("---------- " + t.name + " ----------");
			printDAG(t);				
		}
	}
	
	public static void printDAG(Task t) 
	{
		for (Node n : t.nodes)
		{
			LinkedList<Node> parents = n.parents();
			System.out.println(n.name() + "(wcet = " + n.wcet() + ")" + ":");
			for (Node p : parents)
				System.out.println("	" + p.name());
		}
	}

	// The output maps each node to its (direct and indirect) successors. 
	public static Map<Node, Set<Node>> succs(Task task) 
	{
		HashMap<Node, Set<Node>> succs = new HashMap<Node, Set<Node>>();
		Map<Node, List<Node>> isucc = isucc(task); 
		for (Node n : task.nodes)
		{
			HashSet<Node> nodeSuccs = new HashSet<Node>();
			LinkedList<Node> cache = new LinkedList<Node>(isucc.get(n));
			while (cache.size() > 0)
			{
				Node next = cache.remove();
				nodeSuccs.add(next);
				cache.addAll(isucc.get(next));
			}
			succs.put(n, nodeSuccs);
		}
		return succs;
	}

	// The maximum number of additional core requests that 
	// the task may cause after starting its execution. (Algorithm 1, [Serrano 2017])
	// Warning: this seems wrong. For a correct version, see sw_new().
	static public int sw(Task task)
	{
		Map<Node, List<Node>> isucc = isucc(task); // immediate successors (in [Serrano-17] it is not mentioned whether it is immediate succ. or all succ.)
		
		int sw = 0;
		HashSet<Node> N = new HashSet<Node>(); 
		for (Node n : task.nodes)
		{
			List<Node> succ = isucc.get(n);
			int cores = succ.size() - 1;
			for (Node v_j : succ)
			{
				if (N.contains(v_j))
					cores = cores - 1;
				else 
				{
					if (intersect(succ /* succs.get(n) */, v_j.predecessors))
						cores = cores - 1;
					// The following line seems to be wrong (it should be in an "else"). Here, we call  "removeUselessEdges" beforehand, which avoids this problem.
					N.add(v_j);
				}				
			}
			sw += Math.max(0, cores);
		}
		return sw;
	}

	// The maximum number of additional core requests that 
	// the task may cause after starting its execution. (Algorithm 1, [Serano 2017])
	static public int sw_new(Task task)
	{
		Map<Node, List<Node>> isucc = isucc(task); // immediate successors
		
		int sw = 0;
		HashSet<Node> N = new HashSet<Node>(); 
		for (Node n : task.nodes)
		{
			List<Node> succ = isucc.get(n);
			int cores = succ.size();
			for (Node s : succ)
				if (s.parents().size() == 1)
				{
					cores = succ.size() - 1;
					break;
				}

			for (Node v_j : succ)
			{
				if (N.contains(v_j))
					cores = cores - 1;
				else 
				{
					if (intersect(succ /* succs.get(n) */, v_j.predecessors))
						cores = cores - 1;
					// The following line seems to be wrong (it should be in "else"). Here, we call  "removeUselessEdges" beforehand, which avoids this problem.
					N.add(v_j);
				}				
			}
			sw += Math.max(0, cores);
		}
		return sw;
	}

	public static int sw_hp(Task task) 
	{
		Map<Node, List<Node>> isucc = isucc(task); // immediate successors

		int sw = 0;
		HashSet<Node> N = new HashSet<Node>(); 
		for (Node n : task.nodes)
		{
			List<Node> succ = isucc.get(n);
			int cores = succ.size() - 1;
			for (Node v_j : succ)
			{
				if (N.contains(v_j))
					cores = cores - 1;
				else 
					N.add(v_j);
			}
			sw += Math.max(0, cores);
		}
		return sw;
	}

	// Instead of counting the extra cores, it counts the "number of places" an extra core is needed.
	public static int sw_points(Task task) 
	{
		Map<Node, List<Node>> isucc = isucc(task); // immediate successors
		// Map<Node, Set<Node>> succs  = DagUtil.succs(task);	; // sets of successors (direct and indirect) of each node 

		int sw = 0;
		// HashSet<Node> N = new HashSet<Node>(); 
		for (Node n : task.nodes)
		{
			List<Node> succ = isucc.get(n);
			if (succ.size() > 1)
				sw++;
			//			int cores = succ.size();
			//					
			//			for (Node v_j : succ)
			//			{
			//				if (N.contains(v_j))
			//					cores = cores - 1;
			//				else 
			//					N.add(v_j);
			//			}
			//			int singulars = singleParents(succ);
			//			if (singulars == 1)
			//				cores = cores - 1;
			//			else if (singulars > 1)
			//				cores = cores - (singulars - 1); 
			//			sw += Math.max(0, cores - singulars);
		}
		return sw;
	}


	private static int singleParents(List<Node> succ) 
	{
		int sum = 0;
		for (Node n : succ)
			if (n.parents().size() == 1)
				sum++;
		return sum;
	}

	public static void removeUselessEdges(Task[] tasks) 
	{
		for (Task t : tasks)
			removeUselessEdges(t);
	}

	private static void removeUselessEdges(Task task) 
	{
		Map<Node, Set<Node>> succs  = DagUtil.succs(task); // sets of successors (direct and indirect) of each node 
		for (Node n : task.nodes)
		{
			List<Node> parents = new LinkedList<Node>(n.parents());
			for (Node p : parents)
				if (intersect(succs.get(p), n.parents())) // if there is another path between these two nodes
				{
					task.removeEdge(p, n); 
				}
		}	
	}


	public static void addZeroSource(Task[] tasks) 
	{
		for (Task task : tasks) 
			addZeroSource(task);
	}

	private static void addZeroSource(Task task) 
	{
		List<Node> roots = task.roots();
		if (roots.size() > 1)
		{
			Node newRoot = Node.CPUNode(0, "N0");
			task.addNode(newRoot);
			for (Node root : roots)
				task.addEdge(newRoot, root);		
		}
	}

	public static void removeZeroSource(Task[] tasks) 
	{
		for (Task task : tasks) 
			removeZeroSource(task);		
	}
	
	public static void removeZeroSource(Task task) 
	{
		LinkedList<Node> roots = task.roots();
		if (roots.size() == 1)
		{
			Node root = roots.getFirst();
			if (root.wcet() == 0)
				task.removeNode(root.id());			
		}			
	}

	public static int freeAfterRoot(Task task) 
	{
		LinkedList<Node> roots = task.roots();
		if (roots.size() > 1)
			System.out.println("Error: task is supposed to have only one root");
		Node root = roots.getFirst();
		List<Node> afterRoot = isucc(task).get(root); // TODO: improve efficiency
		int sum = 0;
		for (Node n : afterRoot)
			if (n.parents().size() == 1)
				sum++;
		return sum;
	}

	// Num. of nodes with exactly one outgoing edge.
	public static int singularOutputNodes(Task task) 
	{
		int res = 0;
		Map<Node, List<Node>> isucc = isucc(task); // immediate successors
		for (Node n : task.nodes)
			if (isucc.get(n).size() == 1)
				res++;
		return res;		
	}

	// Computes the number of nodes in the path who has the maximum number of nodes.
	public static int depth(Task task) 
	{		
		Set<Node> layer = task.leaves();
		int depth = 0;
		while (layer.size() > 0)
		{
			depth++;
			Set<Node> prevLayer = new HashSet<Node>();
			for (Node n : layer)
			{
				for (Node p : n.predecessors)
				{
					prevLayer.add(p);
				}					
			}			
			layer = prevLayer;
		}
		return depth;
	}

	public static double[] rates(Task[] tasks) 
	{
		double[] rates = new double[tasks.length];
		for (int i = 0; i < tasks.length; i++)
			rates[i] = 1.0 / (double)tasks[i].period;
		return rates;
	}

	public static boolean allMeet(Task[] tasks, double[] R) 
	{
		for (int i = 0; i < tasks.length; i++)
			if (R[i] > tasks[i].period())
				return false;

		return true;
	}

	public static List<Integer> WCETs(Task task) 
	{
		List<Integer> wcets = new LinkedList<Integer>();
		for (Node n : task.nodes)
			wcets.add(n.wcet());
		return wcets;
	}

	public static List<Integer> WCETs(List<Node> nodes) 
	{
		List<Integer> wcets = new LinkedList<Integer>();
		for (Node n : nodes)
			wcets.add(n.wcet());
		return wcets;
	}
	
	public static List<Integer> WCETs(Set<Node> nodes) 
	{
		List<Integer> wcets = new LinkedList<Integer>();
		for (Node n : nodes)
			wcets.add(n.wcet());
		return wcets;
	}

	public static int[] sinks(Task[] tasks) {
		int[] sinks = new int[tasks.length];
		for (int i = 0; i < tasks.length; i++)
			sinks[i] = sink(tasks[i]); 
		return sinks;
	}

	public static int sink(Task task) 
	{
		Set<Node> leaves = task.leaves();
		if (leaves.size() == 0)
		{
			System.out.println("Warning: task with no sink.");
			return 0;
		}

		if (leaves.size() != 1)
			System.out.println("Warning: task with more than ONE sink.");

		return leaves.iterator().next().wcet();
	}

	public static void assignNodeIndex(Task task) 
	{
		int ind = 0;
		for (Node node : task.nodes)
			node.setIndex(ind++);		
	}

	public static int volume(List<Node> nodes) 
	{
		int sum = 0;
		for (Node node : nodes)
			sum += node.wcet(); 
		return sum;
	}

	public static HashMap<Node, Node> copy(List<Node> nodes) {
		HashMap<Node, Node> res = new HashMap<Node, Node>();
		for (Node n : nodes) {
			res.put(n, n.cloneMe());
		}
		return res;
	}

	public static int[] WCETsArr(Task task) {
		int[] res = new int[task.nnodes()];
		int i = 0;
		for (Node n : task.nodes)
			res[i++] = n.wcet();
		return res;
	}
}

enum EGenMethod 
{
	StrictLayered, Layered
}