package application.analysis.dag;

import application.analysis.dag.simulation.FedNonPScheduler2;
import application.analysis.dag.simulation.SchedulerSimulator;
import application.common.enums.EPolicy;
import application.common.structs.RunParam;
import application.common.structs.SchedParam;
import application.util.Util;

public class AnalyzerFactory implements IAnalyzerFactory 
{
	private RunParam   runParam;
	private SchedParam schedParam;
	
	public AnalyzerFactory(RunParam runparam, SchedParam schparam)
	{
		runParam   = runparam;
		schedParam = schparam;
	}
	
	public AbsTimeAnalyzer createAnalyzer() //, boolean save)
	{
		AbsTimeAnalyzer scheduler = null;
		if (runParam.isAnalytical())
			scheduler = createAnalyticScheduler(schedParam);
		else
			scheduler = createSimBasedScheduler(schedParam);
		
		scheduler.setSave(runParam.showAnySch());
		return scheduler;
	}

	/* Private methods: */
	private static AbsTimeAnalyzer createSimBasedScheduler(SchedParam param) 
	{
		if (param.policy() == EPolicy.DynamicFederated ||
				param.policy() == EPolicy.DynamicFederated_BFS)
			return new FedNonPScheduler2(param);
		
		else
			error(param.policy());
		
		return null;
	}

	private static AbsTimeAnalyzer createAnalyticScheduler(SchedParam param) 
	{
		return null; // not implemented
	}

	private static void error(EPolicy policy) 
	{
		Util.assert_(false, "Unimplemented policy:" + policy);		
	}
	
}
