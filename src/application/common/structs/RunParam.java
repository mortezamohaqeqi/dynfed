package application.common.structs;

public class RunParam 
{
	private  boolean analytical;
	private  boolean showAllSch;
	private  boolean showFailSch;	
		
	public RunParam(boolean analytical, boolean showAllSch, boolean showFailSch) {
		super();
		this.analytical = analytical;
		this.showAllSch = showAllSch;
		this.showFailSch = showFailSch;
	}
	
	public RunParam(ExpConf exp, SimConf conf) {
		showFailSch= exp.analytical ? false : conf.showFailedSchedul();
		showAllSch = exp.analytical ? false : conf.showAllSchedul();
		analytical = exp.analytical;    	
	}

	public boolean isAnalytical() {
		return analytical;
	}
	public void setAnalytical(boolean analytical) {
		this.analytical = analytical;
	}
	public boolean isShowAllSch() {
		return showAllSch;
	}
	public void setShowAllSch(boolean showAllSch) {
		this.showAllSch = showAllSch;
	}
	public boolean isShowFailSch() {
		return showFailSch;
	}
	public void setShowFailSch(boolean showFailSch) {
		this.showFailSch = showFailSch;
	}	
	
	public static RunParam defaultParams() {
		return new RunParam(false, false, false);
	}

	public boolean showAnySch() {
		return  isShowAllSch() || isShowFailSch();
	}
}
