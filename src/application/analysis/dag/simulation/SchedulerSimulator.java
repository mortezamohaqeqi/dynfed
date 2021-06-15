/*
 * This is the ancestor opf any class that does a "simulation-based" timing analysis.
 */

package application.analysis.dag.simulation;

import application.hwmodel.Platform;
import application.hwmodel.enums.EResourceType;
import application.hwmodel.trace.Trace;

import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import application.analysis.dag.AbsTimeAnalyzer;
import application.analysis.dag.util.AUtil;
import application.util.Util;
import application.common.Middleware;
import application.common.enums.EPolicy;
import application.common.enums.EPreemptMethod;
import application.common.structs.SchedParam;
import application.common.structs.SchedResult;
import application.common.structs.queue.IReadyQueue;
import application.common.structs.queue.ListReadyQueue;
import application.models.dag.Node;
import application.models.dag.TaskIns;
import application.models.dag.Task;
import application.models.dag.DagUtil;

public class SchedulerSimulator extends AbsTimeAnalyzer implements Middleware 
{
	private      long HP; //   = 1000; // hyper-period
	protected    EPolicy       policy;
	private      EPreemptMethod  method;
	private      int           tick;
	protected    Platform      platform;
	private      List<Integer> ticks    = null;
	private      int[]         releases = null;
	protected    IReadyQueue   readyQ_CPU;	
	protected    IReadyQueue   readyQ_Mem;
	LinkedList<TaskIns>        activeTasks;	
	
	public SchedulerSimulator(SchedParam param) //, boolean save_)
	{
		// super(save_);
		policy = param.policy();
		method = param.method();
		tick   = param.tick;
		readyQ_CPU  = new ListReadyQueue(policy);
		readyQ_Mem  = new ListReadyQueue(policy);
		activeTasks = new LinkedList<TaskIns>();
	}
	
	// returns job success ratio (throughput)
	public SchedResult analyze(Task []tasks, Platform pf)
	{
		platform = pf;
		tasks = DagUtil.combine(tasks);
		init(cores(), tasks);
		validate();
		
		return simLoop(tasks);
	}

	private boolean EDF_family(EPolicy p) 
	{
		if (p == EPolicy.LLED || p == EPolicy.EDLL ||  p == EPolicy.EDF)  
			return true;
		return false;
	}

	protected void init(int cores, Task []tasks) 
	{
		super.init();
		HP    = hyperperiod(tasks);
		ticks = null;
		releases = DagUtil.releasePoints(tasks, HP);
	}
	
	public int[] lateness()
	{
		if (platform == null)
			return new int[0];
		return platform.latenessArr();
	}
	
	public void printSchedule()
	{
		if (platform != null)
			platform.printScheduleTable(tick);		
	}
	
	public Trace[] traces()
	{
		return platform.traces();
	}
	
	//===============================================
	//Private functions
	protected SchedResult simLoop(Task []tasks)
	{
		// release points
		int lastTime                    = -1;
		SortedSet<Integer> events       = staticEvents(tasks);
		int missedJob = 0;		
		
		while (events.size() > 0)
		{
			int time = pop(events);
			AUtil.ensure_progress(time, lastTime);
			
			// proceed Hardware
			proceed(time, lastTime);
			
			// Step 1. Handle deadline miss
			LinkedList<TaskIns> expired = expiredTasks(activeTasks, time);
			missedJob += unfinished(expired);				
			if (platform.firmRT() && expired.size() > 0)
			{
				activeTasks.removeAll(expired);
				updateRQ(time);
			}
			
			// Step 2. Release new jobs
			releaseTasks(tasks, lastTime, time, HP);
			
			// Step 3. Dispatch
			events.addAll(map2recourses(time));
			
			lastTime = time;
		}
		
		return result(tasks, missedJob);
	}
	
	private void updateRQ(int now) 
	{
		readyQ_CPU.removeExpired(now);
		readyQ_Mem.removeExpired(now);
	}

	private void proceed(int time, int lastTime) 
	{
		platform.proceed(time, lastTime);
	}

	protected SchedResult result(Task[] tasks, int missedJob)
	{
		int njob       = DagUtil.jobs(HP, tasks);		
		double sucRate = ((double)(njob - missedJob)) / njob;
		double u       = DagUtil.CPULoad(tasks) / cores();
		
		platform.verifyEoS(njob);
		
		Util.assert_(sucRate >= 0, "Success rate is negative!");
		
		SchedResult res = new SchedResult(tasks.length, njob, u, sucRate, lateness());
		res.setQSize(readyQ_CPU.avgSize());
				
		return res;		
	}
	
	private List<Integer> map2recourses(int time) 
	{
		EPreemptMethod approach = null;
		switch (method) {
		case Non_Preemptive:
			approach = EPreemptMethod.Non_Preemptive;
			break;
			
		case Preemptive:
			approach = EPreemptMethod.Preemptive;
			break;
			
		case Ticked_Preemptive:
			if (isTick(time))
				approach = EPreemptMethod.Preemptive;
			else
				approach = EPreemptMethod.Non_Preemptive;
			break;
		
		case NW_Ticked_Preemptive:
			if (isTick(time))
				approach = EPreemptMethod.Preemptive;
			break;
			
		case Ticked_Adaptive:
			if (platform.allFree() == true)
				;			// TODO: dequeue, and then, if until last event no relsease, non-preemptive
			break;
			
		default:
			System.out.println("Error: unimplemented preemption policy: " + method);
		}
			
		List<Integer> events = new LinkedList<>();
		
		if (approach == EPreemptMethod.Preemptive) {
			events.addAll(preemptiveScheduling(time, EResourceType.CPU));
			events.addAll(preemptiveScheduling(time, EResourceType.Memory));
		}
		else if (approach == EPreemptMethod.Non_Preemptive) {
			events.addAll(nonPreemptiveScheduling(time, EResourceType.CPU));
			events.addAll(nonPreemptiveScheduling(time, EResourceType.Memory));
		}
		
		if (method == EPreemptMethod.NW_Ticked_Preemptive)
		{
			if (events.size() > 0)
			{
				events.clear();
				if (time >= HP) // && eligs.size() > 0) // && platform.firmRT() == false)
				{
					events.add(time + tick);
					ticks.add(time + tick);
				}
			}
		}
					
		return events;
	}
	
	private List<Integer> preemptiveScheduling(int time, EResourceType type) 
	{
		int free = availableUnits(type, time);	// m?
		List<Node> running = platform.runnings(type);
		
		enqueue(running, type);
		LinkedList<Node> mostEligs = dequeue(free, type);
		
		platform.preempt(AUtil.subtract(running, mostEligs));
		
		List<Node> toRun = AUtil.subtract(mostEligs, running);
		
		return platform.map2resource(toRun, time, type);
	}

	private List<Integer> nonPreemptiveScheduling(int time, EResourceType type) 
	{
		int free = availableUnits(type, time);	
		LinkedList<Node> mostEligs = dequeue(free, type);				
		return platform.map2resource(mostEligs, time, type);
	}	
	
	private int availableUnits(EResourceType type, int now)
	{
		  return type == EResourceType.CPU ? availableCores(now) : 
				         				     availableMems(now);
	}
	
	private int availableCores(int now)
	{
		if (preemptPoint(now))
			return platform.cpus();
		else
			return platform.freeCores();
	}
	
	private int availableMems(int now)
	{
		if (preemptPoint(now))
			return platform.memories();
		else
			return platform.freeMems();
	}
	
	private boolean preemptPoint(int now) 
	{
		if (method == EPreemptMethod.Preemptive)
		{
			return true;
		}
		else if (method == EPreemptMethod.Ticked_Preemptive || method == EPreemptMethod.NW_Ticked_Preemptive)
		{
			if (isTick(now))
			{
				return true;
			}
		}
		return false;
	}
	
	protected int pop(SortedSet<Integer> set)
	{
		int res = set.first();
		set.remove(res);
		return res;
	}
	
	protected static LinkedList<TaskIns> expiredTasks(LinkedList<TaskIns> tasks, int current)
	{
		LinkedList<TaskIns> expired = new LinkedList<TaskIns>();
		for (TaskIns t : tasks)
		{
		    if (t.absDeadline() == current)
		    	expired.add(t);
		}
		return expired;
	}	
	
	protected int unfinished(LinkedList<TaskIns> tasks)
	{
		int unfinish = 0;
		for (TaskIns t : tasks) 
		{
		    if (t.done() == false)
		    {
		    	unfinish++;
		    	for (Node j : t.unfinishedNodes())
		    		platform.addDLMiss(j);
		    }
		}
		
		return unfinish;
	}
	
	protected void releaseTasks(Task []tasks, int previous, int current, long HP)
	{		
		LinkedList<TaskIns> news = DagUtil.releaseTasks(tasks, previous, current, HP, this);
		activeTasks.addAll(news);
	}
	
	protected SortedSet<Integer> staticEvents(Task[] tasks)
	{
		SortedSet<Integer> events = new TreeSet<>();
		events.addAll(Util.toList(releases));
		if (method == EPreemptMethod.Ticked_Preemptive || method == EPreemptMethod.NW_Ticked_Preemptive)
		{
			events.addAll(ticks()); // for LP
		}
		
		return events;
	}
	
	// Warning: This is very time-consuming!
	private boolean isTick(int time)
	{
		for (int t : ticks())
			if (time == t)
				return true;
		return false;
	}
	
	private List<Integer> ticks()
	{
		if (ticks != null)
			return ticks;
		
		ticks = new LinkedList<Integer>();		
		for (int t = 0; t < HP; t += tick /* minP */)
		{
				ticks.add(t); 
		}
		ticks.add((int)HP);
		return ticks;
	}
	
	protected int cores()
	{
		return platform.cpus();
	}	
	
	protected void enqueue(List<Node> running, EResourceType type) 
	{
		if (type == EResourceType.CPU)
			readyQ_CPU.enqueue(running);
		else if (type == EResourceType.Memory)
			readyQ_Mem.enqueue(running);
		else
			Util.assert_(false, "Unimplmeneted resource type: " + type);
		
	}
	
	protected LinkedList<Node> dequeue(int free, EResourceType type) 
	{
		if (type == EResourceType.CPU)
			return readyQ_CPU.dequeue(free);
		else if (type == EResourceType.Memory)
			return readyQ_Mem.dequeue(free);
		else
		{
			Util.assert_(false, "Unimplmeneted resource type: " + type);
			return null;
		}
	}
	
	@Override
	public void nodeReleased(Node n) 
	{
		if (n.conforms(EResourceType.CPU))
			readyQ_CPU.enqueue(n);
		else if (n.conforms(EResourceType.Memory))
			readyQ_Mem.enqueue(n);
		else
			Util.assert_(false, "Unimplmeneted resource type of node.");					
	}
	
	private void validate() 
	{
		boolean methodIsValid = method == EPreemptMethod.Ticked_Preemptive 		|| 
				 				method == EPreemptMethod.Non_Preemptive     	|| 
				 				method == EPreemptMethod.Preemptive		 		||
				 				method == EPreemptMethod.NW_Ticked_Preemptive  	||
				 				method == EPreemptMethod.Ticked_Adaptive;
		
		Util.assert_(methodIsValid, "Uneexpected method for simulation: " + method);
	}
}
