package application.analysis.dag.simulation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;

import application.analysis.dag.util.AUtil;
import application.analysis.dag.util.Block;
import application.analysis.dag.util.SingleTaskScheduler;
import application.common.structs.SchedParam;
import application.common.structs.SchedResult;
import application.hwmodel.trace.Trace;
import application.models.dag.DagUtil;
import application.models.dag.Node;
import application.models.dag.Task;
import application.models.dag.TaskIns;
import application.models.dag.TaskInsComparator;

public class FedNonPScheduler2 extends SchedulerSimulator  
{
	private long   HP;
	private Trace  trace    = null;
	private Trace  memtrace = null;	
	
	public FedNonPScheduler2(SchedParam param) 
	{
		super(param);	
	}
	
	void init2(Task []tasks)
	{
		trace    = new Trace("Core", cores());
		memtrace = new Trace("Memory", 1);
		HP       = hyperperiod(tasks);
	}
	
	protected LinkedList<TaskIns> releaseTasks2(Task []tasks, int previous, int current, long HP)
	{
		LinkedList<TaskIns> newActiveTasks = DagUtil.releaseTasks(tasks, previous, current, HP, null);
		Collections.sort(newActiveTasks, new TaskInsComparator()); 		
		return newActiveTasks;				 
	}
	
	protected SchedResult simLoop(Task []tasks)
	{
		init2(tasks);
		int []schedule = new int [cores()];
		int lastTime                    = -1;
		SortedSet<Integer> events       = staticEvents(tasks);
		int missedJob = 0;		
		
		HashMap<Integer, Block> miarray = precomputation(tasks);				
		LinkedList<TaskIns> remainedTasks = new LinkedList<TaskIns>();
		while (events.size() > 0)
		{			
			int time = pop(events);

			AUtil.ensure_progress(time, lastTime);
			
			// proceed			
			LinkedList<TaskIns> expired = expiredTasks(activeTasks, time);
			int misss = unfinished(expired);
			missedJob += misss;
			
			if (platform.firmRT())
				activeTasks.removeAll(expired);
			
			LinkedList<TaskIns> relTasks = releaseTasks2(tasks, lastTime, time, HP);		
			remainedTasks.addAll(relTasks);
			activeTasks.addAll(relTasks);
			
			if (time == HP)
				break;			

			LinkedList<TaskIns> scheduledTasks = new LinkedList<TaskIns>();
			for (TaskIns task : (remainedTasks)) 	// DagUtil.edf
			{
				if (miarray.get(task.id()) == null)  // task not schedulable even when alone
					break;
					
				int[] coreLastUsage = miarray.get(task.id()).coreLastUsage();  //mm miarray[task.id()-1];
				int m_i = coreLastUsage.length; // maxCore(coreUsage);
				
				List<Integer> list = idleCores(schedule, time, m_i); 
				
				if (list.size() < m_i) 
					break;
				
				scheduledTasks.add(task);
				
				fillTable(schedule, coreLastUsage, time, list, task.absDeadline());
				finishNodes(task, time, miarray.get(task.id()), list);
								
				for (int k = 0; k < coreLastUsage.length; k++)
					addEvent(events, time + coreLastUsage[k]);
			}
			remainedTasks.removeAll(scheduledTasks);			
			lastTime = time;
		}

		return result(tasks, missedJob);
	}

	private HashMap<Integer, Block> precomputation(Task[] tasks) 
	{
		HashMap<Integer, Block> blocks = new HashMap<Integer, Block>();

		for (int i = 0; i < tasks.length; i++) 
		{
			Block sch = SingleTaskScheduler.minCoreSchedule(tasks[i], cores(), policy);			
			blocks.put(tasks[i].id(), sch);
			
			if (sch == null)
				; //System.out.println("Warning: task is not schedulable even when alone!" + DagUtil.utilization(tasks[i]));
		}		
		return blocks;
	}

	private void fillTable(int[] schedule, int[] coreLastUsage, int time, List<Integer> list, int deadline) 
	{
		int i = 0;
		for (int j : list) 
		{
			schedule[j] = time + coreLastUsage[i];
			i++;
		}
	}

	private void finishNodes(TaskIns task, int time, Block block, List<Integer> freeCores) 
	{
		for (Node n : task.nodes())
		{
			int start = time + block.startTime(n.index());
			int slack = Math.max(0, task.absDeadline() - start);
			int exec = Math.min(slack, n.wcet());
			if (exec > 0)
				task.nodeScheduled(n, exec);
			if (save)
			{
				int core = freeCores.get(block.core(n.index()));
				if (exec > 0)
					trace.add(core, time + block.startTime(n.index()), exec, n);
				if (exec < n.wcet())
					trace.addDLMiss(n);
			}			
		}
	}

	private List<Integer> idleCores(int[] schedule, int time, int needed) 
	{
		List<Integer> list = new ArrayList<Integer>();
		for (int j = 0; j < cores(); j++) 
		{
			if (schedule[j] <= time) {
				list.add(j);
				if (list.size() == needed) {
					break;
				}		
			}
		}
		return list;
	}

	private void addEvent(SortedSet<Integer> events, int time) 
	{
		if (time <= 0)
			time = Integer.MAX_VALUE;
		events.add(time);
	}

	public Trace[] traces()
	{
		return new Trace[]{memtrace, trace};
	}
}