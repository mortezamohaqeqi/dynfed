package application.common.structs;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import application.common.CommonUtil;
import application.common.enums.EMetric;
import application.common.enums.EPreemptMethod;
import application.util.Util;

public class ExpResult 
{
	private String                  title = "Empty Title!";
	private ExpParam                expparam;
	private LinkedList<SchedResult> results;
	private double                  elapsed;
	
	public ExpResult(ExpParam ep) 
	{
		expparam = ep;
		results  = new LinkedList<SchedResult>();
	}
	
	public void add(SchedResult res) 
	{
		results.add(res);
	}	
	
	public SchedResult add(int tasks, int jobs, double u, double sucRate, int[] lateness)
	{
		SchedResult res = new SchedResult(tasks, jobs, u, sucRate, lateness);
		results.add(res);
		// SimUtil.saveTS(tasks, "TS1");
		return res;
	}

	public void print()
	{	
		print(0.02);				
	}
	
	public void print(double step)
	{	
		for (SchedResult res : results)
			;// res.print(1);
		
		HashMap<Double, double[]> histo = hist(step, EMetric.Schedulability);
		System.out.println("------------- Aggregated ------------------");
		SortedSet<Double> keys = new TreeSet<>(histo.keySet());
		for (double key : keys)  
			System.out.println("u = " + Util.round(key, 3) + " schRate = " + Util.round((histo.get(key))[0], 3));		
	}
	
	public void printPGF(double step)
	{			
		HashMap<Double, double[]> histo = hist(step, EMetric.Schedulability);
		System.out.println("------------- Aggregated ------------------");
		SortedSet<Double> keys = new TreeSet<>(histo.keySet());
		for (double key : keys)  
			System.out.println("   (" + Util.round(key, 3) + ", " + Util.round((histo.get(key))[0], 3) + ")");		
	}
	
	public void printPGFvsTasks(int step)
	{			
		HashMap<Double, double[]> histo = ntasks_hist(step);
		System.out.println("------------- Aggregated ------------------");
		SortedSet<Double> keys = new TreeSet<>(histo.keySet());
		for (double key : keys)  
			System.out.println("   " + key + ", " + Util.round((histo.get(key))[0], 3) + "");		
	}
	
	public void printQSize(double step)
	{			
		HashMap<Double, double[]> histo = histQSize(step);
		System.out.println("------------- Queue Size ------------------");
		SortedSet<Double> keys = new TreeSet<>(histo.keySet());
		for (double key : keys)  
			System.out.println("(" + Util.round(key, 3) + ", " + (int)(histo.get(key))[0] + ")");		
	}
	
	
	// key is utilization and value is a triple: (1) the percent of schedulable tasksets, (2) pop, (3) avg task set size
	public HashMap<Double, double[]> hist(double step, EMetric metr)
	{
		if (metr == EMetric.Lateness_Overal)
			return lateness_hist(step);
				
		double start = Util.round(CommonUtil.minU(results), 1);
		double end   = Util.round(CommonUtil.maxU(results), 2);
		end          = Math.max(start, end);
		step         = get_step(step, start, end);
		
		// value is a triple: schedulable rate, pop, task set size
		HashMap<Double, double[]> points = emptyPoints(start, end, step);
		for (SchedResult res : results)
		{	
			double key   = closestKey(points, res.util());
			double[] val = points.get(key);
			
			if (metr == EMetric.Schedulability)
				val[0] += res.schedulable() ? 1 : 0;
			else if (metr == EMetric.JobSuccessRate)
				val[0] += res.succRate();
			else if (metr == EMetric.Lateness)
				val[0] += res.meanLateness();
			
			val[1] += 1;	// pop
			val[2] += res.tasks();
		}
		
		take_average(points);		
		points = Util.removeKeys(points, 0.2, 1);
		return points;
	}


	private HashMap<Double, double[]> ntasks_hist(double step) {
		
		int start = CommonUtil.minN(results);
		int end   = CommonUtil.maxN(results);
		end          = Math.max(start, end);
		step         = 1; // get_step(step, start, end);
		
		// value is a triple: schedulable rate, pop, task set size
		HashMap<Double, double[]> points = emptyPoints(start, end, step);
		for (SchedResult res : results)
		{	
			double key   = closestKey(points, res.tasks());
			double[] val = points.get(key);
			
			val[0] += res.schedulable() ? 1 : 0;
			
			val[1] += 1;	// pop
			val[2] += res.tasks();
		}
		
		take_average(points);		
		// points = Util.removeKeys(points, 0.2, 1);
		return points;
	}

	// key is utilization and value is a triple: (1) the percent of schedulable tasksets, (2) pop, (3) avg task set size
	public HashMap<Double, double[]> histQSize(double step)
	{
		// value is a triple: schedulable rate, pop, task set size
		HashMap<Double, double[]> points = emptyPoints(step);
		for (SchedResult res : results)
		{	
			double key   = closestKey(points, res.util());
			double[] val = points.get(key);
			
			val[0] += res.qSize();
			val[1] += 1;	// pop
			val[2] += res.tasks();
		}
		
		take_average(points);		
		points = Util.removeKeys(points, 0.2, 1);
		return points;
	}
	
	HashMap<Double, double[]> emptyPoints(double step)
	{
		double start = Util.round(CommonUtil.minU(results), 1);
		double end   = Util.round(CommonUtil.maxU(results), 2);
		end          = Math.max(start, end);
		step         = get_step(step, start, end);
		
		// value is a triple: schedulable rate, pop, task set size
		return emptyPoints(start, end, step);
	}
	
	// Output: key is lateness and value is a triple: (1) the percent of schedulable tasksets, (2) pop, (3) avg task set size
	private HashMap<Double, double[]> lateness_hist(double step)
	{
		int start = minLateness(); // Integer.MAX_VALUE; //  Util.round(SimUtil.minU(results), 1);
		int end   = maxLateness();	// Util.round(SimUtil.maxU(results), 2);
		step = get_step(step, start, end);
				
		// value is a triple: schedulable rate, pop, task set size
		HashMap<Double, double[]> points = emptyPoints(start, end, step);
		
		double pop = results.size();
		for (SchedResult res : results)
		{	
			for (int late : res.lateness())
			{
				double key   = closestKey(points, late);
				double[] val = points.get(key);
				val[0] += 1.0 / res.jobs();
				val[1] = pop; // population
			}			
		}
		take_average(points);
		return points;
	}

	private void take_average(HashMap<Double, double[]> points) 
	{
		for (double u : points.keySet())
		{
			double[] val = points.get(u);
			double   pop = val[1];
			if (pop == 0)
				continue;
			
			val[0] = val[0] / pop;	// sched. rate
			val[2] = val[2] / pop;	// TS size
		}		
	}
	
	private int minLateness() 
	{
		int min = Integer.MAX_VALUE; 
		for (SchedResult res : results)
			min = Math.min(res.minLateness(), min);
		
		return min;
	}

	private int maxLateness() 
	{
		int max = Integer.MIN_VALUE; 
		for (SchedResult res : results)
			max = Math.max(res.maxLateness(), max);
		
		return max;
	}
	
	private double get_step(double step, double start, double end) 
	{
		if (step <= 0)
			step = (end - start) / 20.0;
		if (step == 0)
		{
			step = 0.1; 
			Util.assert_warning(false, "Result data with step = 0.");
		}
		return step;
	}

	private HashMap<Double, double[]> emptyPoints(double start, double end,
			double step) {
		HashMap<Double, double[]>  points = new HashMap<>();
		for (double u = start; u <= end; u += step)
		{
			points.put(u, new double[] {0, 0, 0});
		}
		return points;
	}

	private static double closestKey(HashMap<Double, double[]> points, double v)
	{
		double res  = -1;
		double dist = -1;
		Set<Double>      keys = points.keySet();
		Iterator<Double> it   = keys.iterator();
		while (it.hasNext())
		{
			Double k = it.next();
			if (dist == -1) {
				res = k;
				dist = Math.abs(k - v);
				continue;
			}
			if (Math.abs(k - v) < dist) {
				dist = Math.abs(k - v);
				res = k;
			}
		}
		return res;
	}
	
	public double avgNode()
	{
		return expparam.avgNode();
	}

	public void setElaps(double elaps) 
	{
		elapsed = elaps;
	}
	
	public double elapsed() 
	{
		return elapsed;
	}

	public void integrate(ExpResult other) 
	{
		expparam.population += other.expparam.population;
		results.addAll(other.results);
	}

	public void printSize() 
	{
		System.out.println("pop = " + expparam.population + ", result size = " + results.size());
	}

	public void setTitle(SchedParam schedParams) 
	{
		EPreemptMethod method = schedParams.method();
		title = schedParams.policy().name() + "_" + expparam.system.cores;
    	
    	title += // policy() == EPolicy.Exhaustive            ? " (Ticked = " + tick() :
    			 method == EPreemptMethod.Non_Preemptive     ? " (Non_Preempt" :
				 method == EPreemptMethod.Ticked_Preemptive ? " (Ticked_Preemptive" :
    			 method == EPreemptMethod.Preemptive       ? " (Preemptive" :
				 method == EPreemptMethod.Ticked_Adaptive  ? " (Ticked_Adaptive" :
        	     method == EPreemptMethod.NW_Ticked_Preemptive  ? " (NW_Ticked_Preemptive = " + schedParams.tick() : "";
    	
    	title += ")";		
	}
	
	public String title()
    {
		return title;
    }	
}
