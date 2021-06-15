/*
 * Creates and runs a number of Experimentors in individual threads.
 */
package application.simengine;

import application.analysis.dag.AnalyzerFactory;
import application.analysis.dag.IAnalyzerFactory;
import application.common.listeners.FinishListener;
import application.common.listeners.PlotListener;
import application.common.listeners.ProgressListener;
import application.common.structs.ExpParam;
import application.common.structs.SchedParam;
import application.models.dag.randomgen.AbsTasksetGenerator;
import application.models.dag.randomgen.TasksetGenerator;

public class ExperimentorsRunner 
{
	Experimentor[]   experimentors;
	ProgressListener progressListener;
	
	public ExperimentorsRunner(int threads)
	{
		experimentors = new Experimentor[threads];	
		for (int i = 0; i < threads; i++)
			experimentors[i] = new Experimentor();		
	}
	
	public void setProgressListener(ProgressListener plistener)
	{
		progressListener = plistener;
	}
	
	public void setFinishListener(FinishListener flistener)
	{
		for (int i = 0; i < experimentors.length; i++)
			experimentors[i].setListener(flistener);
	}
	
	public void setPlotListener(PlotListener plotter)
	{
		for (int i = 0; i < experimentors.length; i++)
			experimentors[i].setPlotListener(plotter);
	}
	
	public Experimentor[] runExperimentors(ExpParam expParam, SchedParam schParams)
	{
		AbsTasksetGenerator tsGenerator  = new TasksetGenerator(expParam);
		tsGenerator.setProgressListener(progressListener);
		IAnalyzerFactory analyzerFactory = new AnalyzerFactory(expParam.runParam(), schParams);		
		
		for (int i = 0; i < experimentors.length; i++)
		{
			final int id = i;			
		   	new Thread()
			{
			    public void run() {
			    	experimentors[id].run(expParam.copy(), tsGenerator, analyzerFactory);
			    }
			}.start();
		}			

		return experimentors;
	}
}
