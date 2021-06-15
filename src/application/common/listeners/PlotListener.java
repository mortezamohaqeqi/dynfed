/**
 * Interface for plotting schedule table, and also Tasks.
 * This serves as an interface, to make the SimEngine independent of GUI classes. 
 */

package application.common.listeners;

import application.common.AbsExperimentor;
import application.hwmodel.trace.Trace;
import application.models.dag.Task;

public interface PlotListener 
{
	public void plotSchedule(AbsExperimentor   sim, 
									Trace[] traces, 
									int[]   periods);
	
	public void plotTasks(Task[] tasks);
}
