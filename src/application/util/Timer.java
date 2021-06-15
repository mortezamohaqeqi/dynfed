/**
 * This is used for debugging: measuring time overheads.
 * It keeps a set of 20 timers, which can start and stpo by tic() and toc()
 */
package application.util;

public class Timer 
{
	private static long []elapsed = new long[20];
	private static long []start = new long[20];
	
	public static void tic(int index) 
	{
		start[index] = System.currentTimeMillis();
	}
	
	public static long toc(int index) 
	{
		elapsed[index] += System.currentTimeMillis() - start[index];
		return elapsed[index];
	}
	
	public static void reset(int index) 
	{
		elapsed[index] = 0;
	}
	
	public static void tocAndPrint(String text, int index) 
	{
		toc(index);
		print(text, index);
	}
	
	public static void print(String text, int index) 
	{
		System.out.println("Elapsed time for " + text + " = " + Util.round(elapsed[index]/1000.0, 3) + " sec.");
	}		
}
