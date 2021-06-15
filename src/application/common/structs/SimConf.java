package application.common.structs;

public class SimConf 
{
	private final boolean  showAllSchedul;
	private final boolean  showFailedSchedul;
	public  final int      timeUnit;  // in nano second
	
	public SimConf(boolean showAll, boolean showUnsched, int unit)
	{
		showAllSchedul    = showAll;
		showFailedSchedul = showUnsched;
		timeUnit          = unit; 
	}

	public boolean showFailedSchedul() {
		return showFailedSchedul;
	}

	public boolean showAllSchedul() {
		return showAllSchedul;
	}
}
