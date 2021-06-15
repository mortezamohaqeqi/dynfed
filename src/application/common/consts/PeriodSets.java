package application.common.consts;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import application.util.Util;

public class PeriodSets 
{
	public static final int[] AUTOSAR_ext;
	public static final int[] AUTOSAR_PERIODS = new int[]{1000, 2000, 5000, 10000, 20000, 50000, 100000, 200000, 1000000}; // us
	public static final int[] AUTOSAR_HARMONIC = new int[]{1000, 2000, 10000, 20000, 100000, 200000, 1000000}; // us
	// public static final int[] AUTOSAR_HARMONIC = new int[]{1000, 5000, 10000, 50000, 100000, 200000, 1000000}; // us
	// public static final int[] AUTOSAR_HARMONIC = new int[]{1000, 2000, 4000, 8000, 16000, 32000, 64000, 128000, 256000, 512000, 1024000}; // us
	public static final Set<Integer> STANDARD = new HashSet<Integer>();
	
	
	static 
	{
		LinkedList<Integer> vals = new LinkedList<Integer>();
		vals = new LinkedList<Integer>();
		for (int x = 1; x <= 9; x++)
		{
			for (int y = 3; y <= 5; y++)
			{
				int p = (int)(x * Math.pow(10, y));
				if (p >= 500 && p <= 100000)
					vals.add(p);
			}
		}
		AUTOSAR_ext = Util.toArray(vals);
		
		STANDARD.addAll(Util.toList(AUTOSAR_ext));
		STANDARD.addAll(Util.toList(AUTOSAR_PERIODS));
		STANDARD.addAll(Util.toList(AUTOSAR_HARMONIC));
	}
	
	public static void test()
	{	
		System.out.println("--------------");
		System.out.println(Util.closest(AUTOSAR_ext, 10));
		System.out.println(Util.closest(AUTOSAR_ext, 100));
		System.out.println(Util.closest(AUTOSAR_ext, 1000));
		System.out.println(Util.closest(AUTOSAR_ext, 5500));
		System.out.println(Util.closest(AUTOSAR_ext, 55000));
		System.out.println(Util.closest(AUTOSAR_ext, 550000));
		System.out.println("--------------");		
	}
	
	public static boolean standardPeriod(int[]periods)
	{
		for (int p : periods)
			if (STANDARD.contains(p) == false)
				return false;
		return true;
	}
}
