/*
 * Parameters for evaluating systems with 10 tasks
 */
package application.test.examples;

import application.common.enums.EPolicy;
import application.common.enums.taskgen.EAppDomain;
import application.common.enums.taskgen.EGraphGenMethod;
import application.common.enums.taskgen.ENumOfTasksPolicy;
import application.common.structs.ExpConf;
import application.common.structs.SchedParam;
import application.common.structs.SimConf;
import application.common.structs.ExpParam;
import application.common.structs.TasksParam;
import application.hwmodel.HardwareParam;

public class DACExp1 implements IExperiment 
{
	public ExpParam simParam()
	{
		int           cores     = 8;
		HardwareParam hw        = HardwareParam.defaultHardware(cores);	
		TasksParam    taskParam = taskParam(); 
		SimConf       simConf   = simConf();
		ExpConf       expConf   = expConf();
		return new ExpParam(hw, taskParam, simConf, expConf);
	}
	
	public SchedParam schParams() 
	{
		SchedParam schParams = SchedParam.defaultParams();
		schParams.setPolicy(EPolicy.DynamicFederated);
		return schParams;
	}

	static private ExpConf expConf() 
	{
		boolean analytic = false; // true;
		boolean save     = false;
		int extensive    = 1;
		String threds    = "3"; // StaticSettings.DEFAULT_THREADS;
		return new ExpConf(analytic, save, extensive, threds);
	}

	static private SimConf simConf() 
	{
		boolean showAll     = false;
		boolean showUnsched = false;
		int unit            = 1000; //  nano second
		return new SimConf(showAll, showUnsched, unit);
	}

	static private TasksParam taskParam()
	{
		int minNode = 1;
		int maxNode = 12;
		int minW    = 15;
		int maxW    = 20;
		int layer   = 4;
		double p    = 0.3;
		EAppDomain domain = EAppDomain.AUTOSAR_EXT;
		ENumOfTasksPolicy numTasks = ENumOfTasksPolicy.FIXED;
		int ntask = 10;
		EGraphGenMethod dagType = EGraphGenMethod.SeqParallel;
		return new TasksParam(minNode, maxNode, minW, maxW, layer, p, domain, numTasks, ntask, dagType);
	}
}
