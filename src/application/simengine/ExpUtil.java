package application.simengine;

import java.util.LinkedList;

import application.util.Util;
import application.common.structs.SchedResult;
import application.common.enums.EPreemptMethod;
import application.common.enums.taskgen.EAppDomain;
import application.common.enums.taskgen.ENumOfTasksPolicy;
import application.common.structs.ExpParam;
import application.common.structs.ExpResult;
import application.common.structs.SchedParam;

public class ExpUtil 
{		
	public static ExpResult aggregateResults(Experimentor[] simulators) 
	{
		ExpResult result = simulators[0].result();
		for (int i = 1; i < simulators.length; i++)
			result.integrate(simulators[i].result());
		
		return result;
	}
	
	public static int optimalThreads(ExpParam simParam, SchedParam schParams) 
	{
		int threads = 1;
		if (simParam.saveTasks() || simParam.showAnySch())
		{
			threads = 1;
		}		
		else if (Util.isInt(simParam.threads))
		{
			threads = Math.max(1, Util.toInt(simParam.threads));
		}
		else // preferred
		{		
			switch(schParams.policy()) 
			{
			  case PReserved:
				  threads = 1;
				  break;
			  default:
				  threads = preferredCores();
				  break;
			}
		}
		
		System.out.println("Running in " + threads + " threads ...");
		
		return threads;
	}
	
	// The minimum utilization
	public static double minU(LinkedList<SchedResult> results) 
	{
		if (results.isEmpty())
			return 0;
		
		double min = results.getFirst().util();
		for (SchedResult res : results)
			if (res.util() < min)
				min = res.util();
		return min;
	}

	// 	The maximum utilization
	public static double maxU(LinkedList<SchedResult> results) 
	{
		if (results.isEmpty())
			return 0;
		
		double max = results.getFirst().util();
		for (SchedResult res : results)
			if (res.util() > max)
				max = res.util();
		return max;
	}

	/**************************/
	/******** Private: ********/	
	private static int preferredCores() 
	{
		int cores = Util.sysCores();
		cores = Math.max(1, cores - 1); 
		return cores;
	}
		
	private static boolean nonZeroOerhead(ExpParam expParams) 
	{
		if (expParams.memTime() + expParams.commTime() + expParams.preemptTime() > 0)
			return true;
		return false;
	}
}
