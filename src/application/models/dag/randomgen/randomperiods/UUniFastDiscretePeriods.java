/*
 * This assigns UUniFast periods, but the period values should be only in a given set.
 * So, once the UUniFast period is obtained, the closest value from the set is used instead.
 */
package application.models.dag.randomgen.randomperiods;

import java.util.Random;

import application.models.dag.Task;
import application.util.Util;

public class UUniFastDiscretePeriods extends UUniFastPeriods
{	
	public UUniFastDiscretePeriods(int[] periodValues) 
	{
		super(periodValues);
	}
	
	protected void setPeriod(Task task, double u, Random random) 
	{
		double rawPeriod = (double)task.volume() / u;
		task.setPeriod(Util.closest(periodSet, rawPeriod));
		double scale = u / util(task);
		task.scaleWCETs(scale);
	}
}
