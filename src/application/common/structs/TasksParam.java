package application.common.structs;

import application.common.consts.PeriodSets;
import application.common.enums.taskgen.EAppDomain;
import application.common.enums.taskgen.EGraphGenMethod;
import application.common.enums.taskgen.ENumOfTasksPolicy;
import application.util.Util;

public class TasksParam 
{
	public final int        minNodes;
	public final int        maxNodes;
	public final int        minWCET  ;
	public final int        maxWCET ;
	public final int        layers  ;
	public final double     prob  ;
	// public final int[]      periods;	
	public final EAppDomain period_src;
	public final ENumOfTasksPolicy numOfTasks;
	public final int        ntasks;
	public final EGraphGenMethod graphType;
	
	public TasksParam(int minNode, int maxNode, int minW, int maxW, int layer, double p, 
			EAppDomain domain, ENumOfTasksPolicy numTasks, int ntask, EGraphGenMethod dagType) 
	{
		minNodes = minNode;
		maxNodes = maxNode;
		minWCET  = minW;
		maxWCET  = maxW;
		layers   = layer;
		prob     = p;
		period_src = domain;
		numOfTasks = numTasks;
		ntasks     = ntask;
		graphType  = dagType; 
	}

	public double avgLoad(int timeUnit) 
	{
		double avgLoad = (((double)(minNodes + maxNodes)) / 2) * ((double)(maxWCET + minWCET)) / 2;
		double avgRate = 0;
		for (double period : periods())
			avgRate += 1.0 / (period * 1000.0 / timeUnit);
		
		avgLoad = avgLoad * (avgRate) / periods().length; // divided by harmonic periods
		return avgLoad ;
	}

	public int minPeriod(int timeUnit) 
	{
		int min = Util.min(periods());
		return (int)(min * (1000.0 / timeUnit));
	}
	
	public int[] periods() 
	{
		switch (period_src)
		{
			case _5G:
				return PeriodSets.FIVE_G_PERIODS;
			
			case AUTOSAR:
				return PeriodSets.AUTOSAR_PERIODS;
			
			case AUTOSAR_HARMONIC:
				return PeriodSets.AUTOSAR_HARMONIC;
			
			case AUTOSAR_EXT:
				return PeriodSets.AUTOSAR_ext;
				
		default:
			break;
		}
		return null;	
	}
}
