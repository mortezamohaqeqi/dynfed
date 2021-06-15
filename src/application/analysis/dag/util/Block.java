/*
 * Used by SingleTaskScheduler to store the schedule info.  
 */

package application.analysis.dag.util;

public class Block
{
	int []coreLastUsage;   // coreLastUsage[c] is the latest time core 'c' has been busy
	int []nodesStartTime;
	int []nodesCore;
	
	public Block(int[] coresEndTime, int[] starts, int[] n2c) 
	{
		coreLastUsage  = coresEndTime;
		nodesStartTime = starts;
		nodesCore      = n2c; 
	}
	
	public int startTime(int nodeIndex)
	{
		return nodesStartTime[nodeIndex];
	}

	public int core(int nodeIndex) 
	{
		return nodesCore[nodeIndex];
	}
	
	public int cores() 
	{
		return coreLastUsage.length;
	}

	public int[] coreLastUsage() 
	{
		return coreLastUsage;
	}
}