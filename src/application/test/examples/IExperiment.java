package application.test.examples;

import application.common.structs.SchedParam;
import application.common.structs.ExpParam;

public interface IExperiment
{
	ExpParam   simParam();
	SchedParam schParams();
}
