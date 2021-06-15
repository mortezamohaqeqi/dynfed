/* 
 * This is the parent of any timing (schedulability) analyzer class.
 */

package application.analysis.dag;


import application.common.structs.SchedResult;
import application.hwmodel.Platform;
import application.hwmodel.trace.Trace;
import application.models.dag.DagUtil;
import application.models.dag.Task;


public abstract class AbsTimeAnalyzer 
{
	protected boolean save;
	protected boolean canceled;
	
	/**
	 * Perfroms a schedulability analysis for tasks on pf hardware.
	 * @param tasks
	 * @param pf
	 * @return A real between 0 and 1, that is job success ratio (in one hyperperiod)
	 */
	public abstract SchedResult analyze(Task []tasks, Platform pf); 	
	public abstract void        printSchedule();
	public abstract Trace[]     traces();
	
	/**
	 * @return The lateness of job instances.
	 * @see TaskIns#lateness
	 */
	public abstract int[]   lateness();
	
	public AbsTimeAnalyzer(/* boolean save_ */) 
	{
		// save = save_;
	}

	
	public void init()
	{
		canceled = false;
	}
	
	static protected long hyperperiod(Task[] tasks)
	{
		return DagUtil.hyperperiod(tasks);
	}	
	
	public void cancel() 
	{
		canceled = true;		
	}

	public void time_log() 
	{
		
	}

	public void setSave(boolean saveTrace) 
	{
		save = saveTrace;		
	}
}
