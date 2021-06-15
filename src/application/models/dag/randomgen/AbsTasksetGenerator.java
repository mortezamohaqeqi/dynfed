package application.models.dag.randomgen;

import application.common.listeners.ProgressListener;
import application.models.dag.Task;

public abstract class AbsTasksetGenerator 
{
	abstract public Task[] nextTaskSet();
	abstract public void setProgressListener(ProgressListener lstr);
	abstract public void cancel();

	public final Task[] next()
	{
	    synchronized (this) 
	    {
	    	return nextTaskSet();
	    }
	}
}
