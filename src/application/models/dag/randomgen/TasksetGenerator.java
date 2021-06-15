package application.models.dag.randomgen;

import java.util.LinkedList;
import application.common.enums.taskgen.ENumOfTasksPolicy;
import application.common.listeners.ProgressListener;
import application.common.structs.ExpParam;
import application.models.dag.DagUtil;
import application.models.dag.Task;
import application.models.dag.randomgen.randomgraphs.AbsGraphCreator;
import application.models.dag.randomgen.randomperiods.AbsPeriodAssigner;

public class TasksetGenerator extends AbsTasksetGenerator
{
	final boolean VARY_UTIL = true;  // utilization is varied
	
	
	private AbsGraphCreator creator;
	private AbsPeriodAssigner perioder;
	private ExpParam param;
	private int      count;
	private double   util;
	private boolean  canceled;
	private int      ntasks;
	
	private ProgressListener progressListener = null;
	int nodes = 0;
		
	private static final double MINU  = 0.02;
	private static final double MAXU  = .98 + 0.02;
	private static final double STEPU = 0.02;
	
	private static final int  MINN = 5;
	private static final int  MAXN = 50;
	private static final int  STEPN = 1;
	
	
	public TasksetGenerator(ExpParam simParam) 
	{
		this.param    = simParam;
		count         = 0;
		canceled      = false;
		
		ntasks        = MINN; // aram.tasks.ntasks 
		util          = 0.25; // MINU;
		
		// Variable util:
		if (VARY_UTIL) {
			ntasks        = simParam.tasks.ntasks; 
			util          = MINU;
		}
		
		this.creator  = AbsGraphCreator.createInstance  (simParam.tasks.graphType);
		this.perioder = AbsPeriodAssigner.createInstance(simParam.tasks);
	}
	
	public Task[] nextTaskSet()
	{
		if (count >= pop())
			return null;
		
		Task[] tasks = feasibleRandomTaskSet(util);	
		
		if (param.saveTasks())
			saveTS(tasks);
		
		if (VARY_UTIL)
			progress();
		else
			progressN();
		
		return tasks;
	}
	
	public void setProgressListener(ProgressListener lstr) 
	{		
		progressListener = lstr;		
	}

	public void cancel() 
	{
		canceled = true;
	}

	/*
	 * Private methods:
	 */
	private Task[] feasibleRandomTaskSet(double u) 
	{
		Task[] tasks = randomTaskset(u);
		int cnt = 1;
		while (infeasible(tasks) && canceled == false)
		{
			tasks = randomTaskset(u);
			cnt++;
			if (cnt % 1000 == 0)
				System.out.println("Warning: more than " + cnt + "  taskset infeasible, u = " + util);
		}
		
		if (canceled)
			return null;
		
		return tasks;
	}
	
	private Task[] randomTaskset(double u) 
	{
		Task[] taskArr = null;
		if (param.tasks.numOfTasks == ENumOfTasksPolicy.FIXED)
		{
			taskArr = creator.generate(param.tasks, ntasks /* aram.tasks.ntasks */ );
			perioder.assignPeriods(taskArr, u * param.cores(), creator.rng());
		}
		else // utilization based
		{
			LinkedList<Task> tasks = new LinkedList<Task>();
			while (utilization(tasks) < u)
			{
				Task t = creator.generate(param.tasks);
				tasks.add(t);
				perioder.assignPeriod(t, creator.rng());				
			}
			taskArr = DagUtil.toArrayT(tasks);
		}
		
		addMemoryJobs(taskArr, param.memTime());
		
		return taskArr;
	}

	private void addMemoryJobs(Task[] tasks, int memTime) 
	{
		if (memTime > 0)
		{
			for (Task t : tasks)
				t.addMemoryNode(memTime);
		}
	}
	
	private void progress() 
	{
		count++;
		util += STEPU;
		if (util > MAXU)
			util = MINU;
		
		if (progressListener != null)
		{
			int percent = (int) (100.0 * ((double)count / pop()));
			progressListener.onProgress(percent);
		}
	}
	
	private void progressN() 
	{
		count++;
		ntasks += STEPN;
		if (ntasks > MAXN)
			ntasks = MINN;
		
		if (progressListener != null)
		{
			int percent = (int) (100.0 * ((double)count / pop()));
			progressListener.onProgress(percent);
		}
	}
	
	private boolean infeasible(Task[] tasks)
	{
		//double uc = utilization(tasks);
		//double um = DagUtil.memLoad(tasks) / param.system.memories;
		//if (uc > 1 || um > 1)
		//	return true;		
		
		for (Task t : tasks)
		{
			if (t.criticalPath() > t.period())
				return true;			
		}
			
		return false;
	}
	
	private double utilization(Task[] tasks) 
	{
		 return DagUtil.CPULoad(tasks) / param.cores();
	}
	
	private double utilization(LinkedList<Task> tasks) 
	{
		return utilization(DagUtil.toArrayT(tasks));
	}

	private void saveTS(Task[] tasks)
	{
		DagUtil.saveTS(tasks, "TS");	// TODO: find a non-existing name for file
	}
	
	private int pop() 
	{
		return param.population;
	}
}
