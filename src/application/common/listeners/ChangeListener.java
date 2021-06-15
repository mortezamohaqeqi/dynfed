package application.common.listeners;

import application.common.structs.RunParam;
import application.common.structs.SchedParam;
import application.hwmodel.HardwareParam;

public interface ChangeListener {

	public void onParamChanged(RunParam param);
	public void onParamChanged(HardwareParam param);
	public void onParamChanged(SchedParam param);
}
