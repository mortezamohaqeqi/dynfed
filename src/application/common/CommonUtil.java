package application.common;

import java.util.LinkedList;

import application.common.structs.SchedResult;

public class CommonUtil 
{
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

	public static int minN(LinkedList<SchedResult> results) {
		if (results.isEmpty())
			return 0;
		
		int min = results.getFirst().tasks();
		for (SchedResult res : results)
			if (res.tasks() < min)
				min = res.tasks();
		return min;
	}
	
	public static int maxN(LinkedList<SchedResult> results) {
		if (results.isEmpty())
			return 0;
		
		int max = results.getFirst().tasks();
		for (SchedResult res : results)
			if (res.tasks() > max)
				max = res.tasks();
		return max;
	}
}
