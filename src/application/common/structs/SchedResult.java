package application.common.structs;

import application.util.Util;

public class SchedResult 
{
	private int     tasks;
	private int     jobs;
	private double  succRate;
	private double  util;	// utilization
	private int[]   lateness;
	private int     avgQSize;
	
	public SchedResult(int ntask, int njob, double util, double sucRate, int[] lates) 
	{
		this.tasks    = ntask;
		this.jobs     = njob;
		this.succRate = sucRate;
		this.util     = util;
		this.lateness = lates;
	}
	
	public boolean schedulable()
	{
		return succRate == 1;
	}
	
	public double succRate() 
	{
		return succRate;
	}
	
	public int tasks()
	{
		return tasks;
	}
	
	public int jobs()
	{
		return jobs;
	}
	
	public double util()
	{
		return util;
	}
	
	public int[] lateness()
	{
		return lateness;
	}
	
	// higher level means more detailed
	public void print(int level) 
	{
		System.out.println("u = " + Util.round(util, 3) + "  isSch = " + schedulable());
	}

	public int minLateness() 
	{
		return Util.min(lateness);
	}
	
	public int maxLateness() 
	{
		return Util.max(lateness);
	}

	public double meanLateness() 
	{
		return Util.avg(lateness);
	}

	public void setQSize(int avgSize) 
	{
		avgQSize = avgSize;
	}

	public double qSize() 
	{
		return avgQSize;
	}
}
