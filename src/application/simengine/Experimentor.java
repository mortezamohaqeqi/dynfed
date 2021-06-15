/**
 * Experimetnor is a singe-threaded class, which gets a taskset generator and an analyzer factory, 
 * Analyze random task sets until there is no task set. 
 */
package application.simengine;

import java.util.Iterator;
import java.util.LinkedList;

import application.analysis.dag.AbsTimeAnalyzer;
import application.analysis.dag.IAnalyzerFactory;
import application.analysis.dag.util.AUtil;
import application.common.AbsExperimentor;
import application.common.Debug;
import application.common.listeners.PlotListener;
import application.common.listeners.FinishListener;
import application.common.structs.SchedParam;
import application.common.structs.SchedResult;
import application.common.structs.ExpParam;
import application.common.structs.ExpResult;
import application.hwmodel.Platform;
import application.models.dag.DagUtil;
import application.models.dag.Node;
import application.models.dag.Task;
import application.models.dag.TaskIns;
import application.models.dag.randomgen.AbsTasksetGenerator;

public class Experimentor  implements AbsExperimentor 
{
	private ExpResult    result;
	private FinishListener  listener = null;
	private PlotListener plotListener = null;
	private boolean      canceled;
	private boolean      continu;
	private AbsTimeAnalyzer analyzer;
	private AbsTasksetGenerator       generator;	
		
	/**
	 * Experiment main loop
	 */
	public void run(ExpParam param, AbsTasksetGenerator gen, IAnalyzerFactory analFactory) 
	{	
		init(param);
		generator = gen;

		while (!canceled)
		{
			Task[] tasks = generator.next();
			if (tasks == null)
				break;
				
			Platform hw = new Platform(param.system, param.showAnySch());				
			analyzer    = analFactory.createAnalyzer();
			SchedResult res = analyzer.analyze(tasks, hw);				
			addResult(res, param, tasks);
		}
		finish();		
	}	

	public void setPlotListener(PlotListener plotter) 
	{
		plotListener = plotter;
	}

	public void printResults()
	{	
		if (result != null)
			result.print();
	}

	public void setListener(FinishListener listen) 
	{		
		listener = listen;		
	}
	
	public void cancel() 
	{
		canceled = true;
		if (analyzer != null)
			analyzer.cancel();
		generator.cancel();
	}
	
	public ExpResult result() 
	{
		return result;
	}
	
	public void continu() 
	{
		continu = true;		
	}
	
	//=========================================================================
	// Private functions
	//=========================================================================
	private void addResult(SchedResult res, ExpParam param, Task[] tasks)
	{
		result.add(res);
		
		if (Debug.PrintSchedule)
			AUtil.printSchedule(analyzer, res);

		if ((res.schedulable() == false && param.showAnySch()) || param.showAllSch())  // && res.util() <= 1 
		{
			plotSchedule(tasks);
			plotListener.plotTasks(tasks);
			waitUntilCommand();
			listener.toContinue();
		}	
	}
	
	private void plotSchedule(Task[] tasks)
	{
		continu = false;
		int[] periods = DagUtil.uniqe_periods(tasks);
		plotListener.plotSchedule(this, analyzer.traces(), periods);
	}
	
	private void waitUntilCommand()
	{
		while (continu == false && canceled == false)
			try {
				Thread.sleep(500);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
					}
	}
	
	private void init(ExpParam expparam)
	{
		result    = new ExpResult(expparam);
		canceled  = false;
	}

	private void finish() 
	{
		if (listener != null)
			listener.onFinish(canceled == false);
	}
}