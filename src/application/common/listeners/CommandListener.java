package application.common.listeners;

import application.common.structs.SchedParam;
import application.common.structs.ExpParam;

public interface CommandListener 
{
	public void onRun(ExpParam param, SchedParam schParams);
	public void cancelPressed();
}
