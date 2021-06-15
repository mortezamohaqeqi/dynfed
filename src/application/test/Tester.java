package application.test;

import application.common.structs.ExpResult;
import application.simengine.ExperimentManagerNoGUI;
import application.test.examples.RTCSAExp1;
import application.test.examples.IExperiment;

public class Tester 
{	
	public static void main(String[] args)  
	{		
		IExperiment exp = new RTCSAExp1();
		
		ExperimentManagerNoGUI runner = new ExperimentManagerNoGUI();
		ExpResult res = runner.syncRun(exp.simParam(), exp.schParams());
		res.printPGF(.05);
	}	
}
