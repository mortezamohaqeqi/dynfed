package application.common.structs;

import application.hwmodel.HardwareParam;

public class ExpParam 
{
	final public static int DEFAULT_MAX_NODES = 12; 
	// final public static int DEFAULT_MAX_POPUL = 1500; 
	
	public  int         population;
	// private int         minPeriod;
	private final int   timeUnit;  // in nano second
	
	private RunParam	runParam;
	private boolean     saveTasks;
	public  TasksParam  tasks;
	public  HardwareParam system;
	public  final	String threads;
	
	public ExpParam(HardwareParam hw, TasksParam taskParam, SimConf conf, ExpConf exp) 
	{
		tasks      = taskParam;
		timeUnit   = conf.timeUnit; 
		runParam   = new RunParam(exp, conf); 
		saveTasks  = exp.saveTasks;
		population = exp.extensivity * 1000;
		threads    = exp.threads;
		// minPeriod  = taskParam.minPeriod(conf.timeUnit);
		system     = hw;		
	}
	
	public int minPeriod() 
	{
		return tasks.minPeriod(timeUnit);
	}

	public double cores() 
	{
		return system.cores;
	}

	public int memTime() 
	{
		return system.memTime;
	}

	public int preemptTime() 
	{
		return system.preemptOverhead;
	}
	
	public int commTime() 
	{
		return system.commOverhead;
	}
	
	public double avgNode() 
	{
		return ((double)tasks.minNodes + tasks.maxNodes) / 2.0;
	}
//	
//	public int maxTasks() 
//	{
//		return maxTasks;
//	}
//	
//	public int minTasks() 
//	{
//		return minTasks;
//	}

	public boolean saveTasks() 
	{
		return saveTasks;
	}

	public boolean showFailSch() 
	{
		return runParam.isShowFailSch();
	}

	public boolean showAllSch() 
	{
		return runParam.isShowAllSch();
	}
	
	public boolean showAnySch() 
	{
		return runParam.showAnySch();
	}

	public RunParam runParam() 
	{
		return runParam;
	}

	public boolean isAnalytical() 
	{
		return runParam.isAnalytical();
	}
	
	public int timeUnit()
	{
		return timeUnit;
	}
	
	private ExpParam(HardwareParam hw, TasksParam taskParam, int unit, String threds) 
	{
		tasks     = taskParam;
		system    = hw;
		timeUnit  = unit;
		threads   = threds;
	}

	public ExpParam copy() 
	{
		ExpParam copy = new ExpParam(system, tasks, timeUnit, threads);
		copy.population = population;
		copy.runParam   = runParam;
		copy.saveTasks  = saveTasks;
		return copy;
	}
}
