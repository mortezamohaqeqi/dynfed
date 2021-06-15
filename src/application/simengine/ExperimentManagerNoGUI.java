package application.simengine;

import application.common.listeners.RunListener2;
import application.common.structs.SchedParam;
import application.common.structs.ExpParam;
import application.common.structs.ExpResult;
import application.util.Timer;

public class ExperimentManagerNoGUI  implements RunListener2
{
	private Experimentor[] experimentors;
	private int            activeThreads;
	private SchedParam     schedParams;
	private int            lastProg = 0;
	private ExpResult      result = null;
	private boolean        finished;

	// the caller is blocked until experiment is done
	// TODO: implement using Thread.wait() and notify()
	public ExpResult syncRun(ExpParam expParam, SchedParam schParams)
	{
		run(expParam, schParams);
		waitUntilFinish();
		return result();		
	}

	public void run(ExpParam expParam, SchedParam schParams) 
	{
		finished = false;
		lastProg = 0;
		schedParams = schParams;
		activeThreads = ExpUtil.optimalThreads(expParam, schParams); // 4;	 1; // 
		preStart();
		
		ExperimentorsRunner expRunner = new ExperimentorsRunner(activeThreads);
		expRunner.setProgressListener(this);
		expRunner.setFinishListener(this);
		experimentors = expRunner.runExperimentors(expParam, schParams);
	}

	@Override
	public void onProgress(int percent) 
	{
		if (percent > lastProg)
		{
			lastProg = percent;
			System.out.print(percent + " ");
		}				
	}

	@Override
	public void onFinish(boolean succ) 
	{
		activeThreads--;
		if (activeThreads > 0)
			return;

		if (succ) 
		{			
			//TODO Table.createAndShowGUI(simulator.result());			
			result = ExpUtil.aggregateResults(experimentors);
			result.setTitle(schedParams);
			result.setElaps(Timer.toc(0));
			doFinish();			
		}	
	}
	
	private void doFinish() 
	{
		finished = true;
		synchronized (this) {
			notify(); 
		}
	}

	private void waitUntilFinish() 
	{
		synchronized (this) {
			try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public void toContinue() 
	{
		// TODO Auto-generated method stub
	}

	public ExpResult result() 
	{
		return result;
	}

	public boolean finished() 
	{
		return finished;
	}
	
	private void preStart() 
	{
		Timer.tic(0);
		System.out.println("Running in " + activeThreads + " threads ...");
	}
}
