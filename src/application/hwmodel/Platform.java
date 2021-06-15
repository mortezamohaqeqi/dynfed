package application.hwmodel;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import application.hwmodel.enums.EResourceType;
import application.hwmodel.enums.ETimeConstrant;
import application.hwmodel.trace.Trace;
import application.util.Util;

import application.models.dag.DagUtil;
import application.models.dag.Node;
import application.models.dag.TaskIns;

public class Platform 
{
	private EnumMap<EResourceType, Integer> resources; // = new
	private Trace  trace    = null;
	private Trace  memtrace = null;
	private Node[] cores;
	private Node[] memories;
	private int    preemptTime;
	private int    commTime;
	private ETimeConstrant constraint;
	private Hashtable<TaskIns, Integer> lateness;
	private Hashtable<Node, Integer>    preemptOverhead;
	private Hashtable<Node, Integer>    memoryOverhead;
		
	public Platform(HardwareParam sys, boolean save)
	{
		resources = new EnumMap<EResourceType, Integer>(EResourceType.class);
		resources.put(EResourceType.CPU,    sys.cores);
		resources.put(EResourceType.Memory, sys.memories);
		resources.put(EResourceType.GPU,    0);
		resources.put(EResourceType.FPGA,   0);
		resources.put(EResourceType.SIMD,   0);
		
		cores      = new Node[sys.cores];
		memories   = new Node[sys.memories];
		lateness   = new Hashtable<TaskIns, Integer>();
		constraint = sys.timeconstraint;
		preemptTime= sys.preemptOverhead;
		commTime   = sys.commOverhead;
		preemptOverhead = new Hashtable<Node, Integer>();
		memoryOverhead  = new Hashtable<Node, Integer>();
		if (save)
		{
			trace    = new Trace("Core", sys.cores);
			memtrace = new Trace("Memory", sys.memories);
		}
	}
	
	public Platform(HardwareParam sys, int gpu, int fpga, int simd, boolean save)
	{
		this(sys, save);
		resources.put(EResourceType.GPU,  gpu);
		resources.put(EResourceType.FPGA, fpga);
		resources.put(EResourceType.SIMD, simd);
	}
	
	public int cpus() 
	{
		return resources.get(EResourceType.CPU);		
	}

	public int memories() 
	{
		return resources.get(EResourceType.Memory);		
	}

	public int gpus() 
	{
		return resources.get(EResourceType.GPU);		
	}
	
	public int fpgas() 
	{
		return resources.get(EResourceType.FPGA);		
	}

	public int simds() 
	{
		return resources.get(EResourceType.SIMD);		
	}

	public void proceed(int now, int prevTime) 
	{
		proceed(now, prevTime, cores, trace);
		proceed(now, prevTime, memories, memtrace);
	}
	
	private void proceed(int now, int prevTime, Node[] resources, Trace trac) 
	{
		for (int c = 0; c < resources.length; c++)
		{
			Node n = resources[c];
			if (n == null)
				continue;
			
			verifySchedule(n, now);
			executeNode(n, c, now, prevTime, trac);
						
			if (halted(n, now))
				resources[c] = null;
			
			addLateness(n.owner(), now);
		}
	}
	
	private void executeNode(Node n, int core, int now, int prevTime, Trace trac)
	{
		int dt = now - prevTime;
		
		int executedOH = executeOH(n, dt, preemptOverhead, "Preemption");
		if (executedOH > 0) {
			if (trac != null)
				trac.addOH(core, prevTime, executedOH, n);
		}
		else {
			executedOH = executeOH(n, dt, memoryOverhead, "Memory");
			if (executedOH > 0)
			{
				if (trac != null)
					trac.addCommOH(core, prevTime, executedOH, n);
			}
		}
		
		int netExecuted = dt - executedOH;
		if (netExecuted > 0)
		{
			int rem = n.owner().remained(n); // keep the order!
			Util.assert_(rem > 0, "Node with zero remained exec. time has been scheduled!");
			n.owner().nodeScheduled(n, netExecuted);
			
			if (trac != null)
			{
				int executed = Math.min(rem, netExecuted);
				trac.add(core, prevTime + executedOH, executed, n);	
			}
		}
	}
	
	private int executeOH(Node n, int dt, Hashtable<Node, Integer> overheads, String title) 
	{
		if (overheads.containsKey(n) == false)
			return 0;
		else 
		{
			int ovhd = overheads.get(n);
			Util.assert_(ovhd > 0, title + " overhead is zero");
			if (ovhd <= dt)
			{
				overheads.remove(n);
				return ovhd;
			}
			else
			{
				overheads.put(n, ovhd - dt);
				return dt;
			}
		}
	}

	private void addLateness(TaskIns job, int now) 
	{
		if (job.done())
		{
			int late = now - job.absDeadline();
			lateness.put(job, late);
			job.setLateness(late);
		}
	}
	
	private boolean halted(Node n, int now) 
	{
		if (n.owner().done(n))
			return true;
		
		if (firmRT())
			if (n.owner().absDeadline() <= now)
				return true;
		
		return false;
	}

	public LinkedList<Node> runnings(EResourceType type) 
	{
		if (type == EResourceType.CPU)
			return DagUtil.toList(cores);
		
		else if (type == EResourceType.Memory)
			return DagUtil.toList(memories);
		
		return null;
	}
	
	public LinkedList<Node> runnings()
	{
		LinkedList<Node> res = runnings(EResourceType.CPU);
		res.addAll(            runnings(EResourceType.Memory));
		return res;
	}
	

	public int freeCores() 
	{
		return Util.nulls(cores);
	}

	public int freeMems() 
	{
		return Util.nulls(memories);
	}
	
	public boolean allFree() 
	{		
		for (Node n : cores)
			if (n != null)
				return false;
		
		for (Node n : memories)
			if (n != null)
				return false;
	
		return true;
	}

	public int lastFinish(LinkedList<Node> nodes) 
	{
		int last = 0;
		for (Node n : nodes)
			last = Math.max(last, n.remained());
		
		return last;
	}
	

	public List<Integer> map2resource(List<Node> toRun, int time, EResourceType type) 
	{
		// readyQ.removeAll(toRun);
		
		if (type == EResourceType.CPU)
			return map2CPU(toRun, time);
		else
			return map2Mem(toRun, time);
	}
	
	public List<Integer> map2CPU(List<Node> eligs, int time) 
	{
		LinkedList<Node> nodes = new LinkedList<Node>(eligs);
		List<Integer> events = new LinkedList<Integer>();
		for (int c = 0; c < cores.length && nodes.isEmpty() == false; c++)
		{
			if (cores[c] == null)
			{
				cores[c] = nodes.removeFirst();
				int OH = add_overheads(cores[c], c);
				events.add(time + OH + cores[c].remained());
				
				cores[c].setLastCore(c);
			}				
		}  // TODO: if eligs not empty: error!
		Util.assert_(nodes.isEmpty(), "Nodes to map more than free cores.");
		// test(cores);
		
		return events;
	}
	
	private int add_overheads(Node node, int core) 
	{
		if (preemptTime == 0 && commTime == 0)
			return 0;
		
		if (reloaded(node))
		{
			if (preemptTime > 0)
				preemptOverhead.put(node, preemptTime);
			return preemptTime;
		}
		else
		{
			int OH = comm_overhead(node, core);
			if (OH > 0)
				memoryOverhead.put(node, OH);
			return OH;	
		}
	}
	
	private int comm_overhead(Node node, int core)
	{
		if (commTime == 0)
			return 0;
					
		int OH = 0;
		for (Node p : node.parents())
		{
			if (p.memoryNode())
				continue;
			if (p.lastCore() != core)
				OH = commTime;
		}
		return OH;
	}
	
	private boolean reloaded(Node n) 
	{
		return n.owner().progressed(n);	// Partly has execution
	}

	public List<Integer> map2Mem(List<Node> eligs, int time) 
	{
		List<Integer> events = new LinkedList<Integer>();
		LinkedList<Node> nodes = new LinkedList<Node>(eligs);
		
		for (int c = 0; c < memories.length && nodes.isEmpty() == false; c++)
		{
			if (memories[c] == null)
			{
				memories[c] = nodes.removeFirst();
				events.add(time + memories[c].remained());
				// debug(c, time, cores);
			}				
		}  // TODO: if eligs not empty: error!
		// test(cores);
		return events;
	}

	public Trace[] traces() 
	{
		return new Trace[]{memtrace, trace};
	}

	public double totalResrouces() 
	{
		return cpus() + memories() + gpus() + fpgas() + simds();
	}

	public void addDLMiss(Node j) 
	{
		if (trace != null)
			trace.addDLMiss(j);		
	}

	// Valid only for ticked based scheduling.
	public void printScheduleTable(int tik) 
	{
		Util.assert_(false, "Obsolete code");
		Node[][] schedule = trace2schedule();  // schedule table	
		if (schedule == null)
			return;
		
		int cores = schedule.length;
		int ticks = schedule[0].length;
		
		// print time ticks
		int periodTicks = 125 / tik;
		System.out.printf("%-10s", "");
		for (int tick = 0; tick < ticks; tick++)
		{
			System.out.printf("%-5s", "" + ((tick/periodTicks)*125+tick*(tick%periodTicks)));
			if (tick%periodTicks == (periodTicks-1))
				System.out.printf("%-5s", " ");
		}
		
		// print --------------------
		System.out.println();
		System.out.printf("%-10s", "");
		for (int tick = 0; tick < ticks; tick++)
		{
			System.out.printf("%-5s", "-----");
			if (tick%6 == 5)
				System.out.printf("%-5s", "-----");
		}
		System.out.println();
		
		// print cores
		for (int core = 0; core < cores; core++)
		{
			System.out.printf("%-10s", "Core " + (core+1) + ": ");
			for (int tick = 0; tick < ticks; tick++)
			{
				if (schedule[core][tick] == null)
					System.out.printf("%-5s", "-");
				else
					System.out.printf("%-5s", schedule[core][tick].name());
				if (tick%periodTicks == periodTicks-1)
					System.out.printf("%-5s", " |");
			}
			System.out.println("");
		}			
	}	
	
	public boolean firmRT() 
	{
		return constraint == ETimeConstrant.Firm;
	}
	
	// for debug
	public void printCores() 
	{
		System.out.print("Cores = ");
		for (int c = 0; c < cores.length; c++)
		{
			if (cores[c] != null)
				System.out.print(c+1 + ":" + cores[c].name() + ", ");
		}
		System.out.println();
	}

	public void printLateness() 
	{
		System.out.println(lateness.size());
//		for (TaskIns t : lateness.keySet())
//		{
//			System.out.println(t.period() + ":" + lateness.get(t));
//		}
	}

	public int finishedJobs() 
	{
		return lateness.size();
	}

	public int[] latenessArr() 
	{
		return Util.toArray(lateness.values());		
	}

	public TaskIns[] maxLateJobs() 
	{
		int maxL = maxLateness();
		LinkedList<TaskIns> latests = new LinkedList<TaskIns>();
		for (TaskIns job : lateness.keySet())
			if (job.lateness() == maxL)
				latests.add(job);
		
		return DagUtil.toArray(latests);
	}
	
	public int maxLateness() 
	{
		return DagUtil.latestFinishs(lateness.keySet());
	}
		
	/* public Hashtable<TaskIns, Integer> lateness() 
	{
		return lateness;
	} */

	public void preempt(List<Node> toPause) 
	{
		int paused = 0;
		for (int c = 0; c < cores.length; c++)
			if (cores[c] != null)
				if (toPause.contains(cores[c]))
				{
					cores[c] = null;
					paused++;
				}
		
		for (int m = 0; m < memories.length; m++)
			if (memories[m] != null)
				if (toPause.contains(memories[m]))
				{
					memories[m] = null;
					paused++;
				}
		Util.assert_(paused == toPause.size(), "Number of paused != size(toPause)");
	}

	public Set<TaskIns> doneJobs() 
	{
		return lateness.keySet();
	}
	
	// End of Simulation
	public void verifyEoS(int n) 
	{
		Util.assert_(allFree(), "Simulation finished but unfinished job exist!");
		if (constraint == ETimeConstrant.Soft)
			Util.assert_(n == doneJobs().size(), "Non finished jobs for soft time constraint");
	}
	
	public void clear()
	{
		cores      = new Node[cpus()];
		memories   = new Node[memories()];
		lateness   = new Hashtable<TaskIns, Integer>();
		preemptOverhead = new Hashtable<Node, Integer>();
		memoryOverhead  = new Hashtable<Node, Integer>();
		
		if (trace != null)
		{
			trace    = new Trace("Core", cpus());
			memtrace = new Trace("Memory", memories());
		}
	}
	
	//=========================================================
	// Private methods
	
	// convert platform's cpu trace to a schedule table
	private Node[][] trace2schedule()
	{
		Node[][] schedule = null;
		
		// TODO:
		/* if (method == ESchedMethod.Strictly_Ticked)
		{	
			int majorTicks = (int)Math.floor(HP / minP);
			int minorTicks = (int)Math.floor(minP / tick); 
			schedule = new Node[cores][majorTicks * minorTicks];
		} */		
		
		return schedule;		
	}

	private void verifySchedule(Node n, int now) 
	{
		if (firmRT())
			Util.assert_(n.owner().absDeadline() >= now, "Deadline has missed!" + n.owner().absDeadline() + "  " + now);
	}

	@SuppressWarnings("unused")
	private void test(Node[] cores) 
	{
		HashSet<Node> running = new HashSet<Node>();
		for (int c = 0; c < cores.length; c++)
		{
			if (cores[c] != null)
			{
				if (running.contains(cores[c]))
					System.out.println("Error: one node scheduled twice");
				running.add(cores[c]);
			}
		}		
	}
	
	public Hashtable<TaskIns, Integer> lateness() 
	{
		return lateness;
	}
	
	@SuppressWarnings("unused")
	private void preempt() 
	{
		beNull(cores);	
		beNull(memories);
	}
	
	void beNull(Node[] cores) 
	{
		for (int i = 0; i < cores.length; i++)
			cores[i] = null;
	}
}
