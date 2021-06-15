/*
 This contains defines a set of domain-independent utility functions.
*/

package application.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.util.SortedSet;

public class Util 
{
	public static double round(double value, int places) {
	    if (places < 0) throw new IllegalArgumentException();

	    long factor = (long) Math.pow(10, places);
	    value = value * factor;
	    long tmp = Math.round(value);
	    return (double) tmp / factor;
	}
	
	public static double round(double value) 
	{
	    return round(value, 3);
	}

	public static double avg(Collection<Integer> values) 
	{
		if (values.isEmpty())
			return 0;
		
		double avg = 0;
		for (int v : values)
			avg += v;
		
		return round(avg / values.size(), 3);
	}
	
	public static double avg(int[] values) 
	{
		if (values.length == 0)
			return 0;
		
		double avg = 0;
		for (int v : values)
			avg += v;
		
		return round(avg / values.length, 3);
	}

	public static int min(Collection<Integer> values) 
	{
		int min = 0;
		for (int v : values)
			min = Math.min(min, v);
		
		return min;
	}
	
	public static double min(Set<Double> values) 
	{
		double min = Double.MAX_VALUE;
		for (double v : values)
			min = Math.min(min, v);
		
		return min;	
	}
	
	public static double max(Set<Double> values) 
	{
		double max = Double.MIN_VALUE;
		for (double v : values)
			max = Math.max(max, v);
		
		return max;	
	}

	public static <T> HashMap<Double, T> removeKeys(
			HashMap<Double, T> hist, double min, double max) {
		Set<Double> utils = new HashSet<Double>(hist.keySet());
		for (double u : utils)
			if (u > max + 0.0000001 || u < min - 0.0000001)
				hist.remove(u);
		return hist;
	}

	public static <T> int nulls(T[] array) 
	{
		int nulls = 0;
		for (T t : array)
			if (t == null)
				nulls++;
		return nulls;
	}

	public static int max(int[] nums) 
	{
		int max = Integer.MIN_VALUE;
		for (int i : nums)
			max = Math.max(max, i);
		
		return max;
	}

	public static int max(SortedSet<Integer> nums) 
	{
		int max = Integer.MIN_VALUE;
		for (int i : nums)
			max = Math.max(max, i);
		
		return max;
	}

	public static int[] toArray(Collection<Integer> values) 
	{
		int[] res = new int[values.size()];
		Object[] arr = values.toArray();
		for (int i = 0; i < res.length; i++)
			res[i] = (int) arr[i];
	
		return res;
	}

	public static Collection<Integer> toList(int[] arr) 
	{
		LinkedList<Integer> list = new LinkedList<Integer>();
		for (int t : arr)
			list.add(t);
		return list;
	}

	public static double[] maxFirst(Collection<double[]> vals) 
	{
		double max = Integer.MIN_VALUE;		
		for (double[] val : vals)
			max = Math.max(val[0], max);

		return new double[]{max, 1, 1};
	}
	
	public static double[] minFirst(Collection<double[]> vals) 
	{
		double min = Integer.MAX_VALUE;		
		for (double[] val : vals)
			min = Math.min(val[0], min);

		return new double[]{min, 1, 1};
	}

	public static boolean intersects(int t1, int t2, int[] sorted_points) 
	{
		if (t1 > t2)
			return false;
		
		for (int t : sorted_points)
		{
			if (t1 <= t && t <= t2)
				return true;
			if (t2 < t)
				return false;
		}
		
		return false;
	}

	public static void print(double[] r) 
	{
		for (double d : r)
			System.out.print(d + " ");
		System.out.println();
	}
	
	public static int min(int[] nums) 
	{
		int min = Integer.MAX_VALUE;
		for (int i : nums)
			min = Math.min(min, i);
		
		return min;
	}

	public static double[] copy(double[] R) 
	{
		double []res = new double[R.length];
		for (int i = 0; i < R.length; i++)
			res[i] = R[i];
			
		return res;
	}

	public static double[] min(double[] r1, double[] r2) 
	{
		int len = Math.min(r1.length, r2.length);
		double[] res = new double[len];
		for (int i = 0; i < len; i++)
			res[i] = Math.min(r1[i],  r2[i]);
		return res;
	}

	public static int[] copy(int[] arr) 
	{
		int []res = new int[arr.length];
		for (int i = 0; i < arr.length; i++)
			res[i] = arr[i];
			
		return res;
	}

	public static void assert_(boolean cond, String msg) 
	{
		if (cond == false)
			throw new ArithmeticException("Student is not eligible for registration");  ; //throw new AssertionFailedError("DAG Sim Error: " + msg);
	}

	public static void assert_warning(boolean cond , String string) 
	{
		if (cond == false)
		{
			System.out.println("---------------------------------------------");
			System.out.println("Warning!!! " + string);
			System.out.println("---------------------------------------------");
		}
	}

	public static <T> void addUnique(LinkedList<T> list, LinkedList<T> items) 
	{
		for (T t : items)
			if (!list.contains(t))
				list.add(t);
	}

	public static int max(Collection<Integer> nums) 
	{
		int max = Integer.MIN_VALUE;
		for (int i : nums)
			max = Math.max(max, i);
		
		return max;
	}

	public static int toInt(String str) 
	{
		try {
			int v = Integer.parseInt(str);
			return v;
		} catch(Exception e)
		{
			System.out.println("Error: string is not integer: " + str);
		}
		return 0;
	}

	public static int[] intArr(int n, int defaultVal) 
	{
		int[]arr = new int[n];
		for (int i = 0; i < n; i++)
			arr[i] = -1;
		
		return arr;
	}

	public static int sysCores() 
	{
		 return Runtime.getRuntime().availableProcessors();	
	}

	public static boolean isInt(String str) 
	{
	    if (str == null) {
	        return false;
	    }
	    try {
	        Integer.parseInt(str);
	    } catch (NumberFormatException nfe) {
	        return false;
	    }
	    return true;
	}

	static public int closest(int[] values, double val) 
	{
		int res = values[0];
		double diff = abs(res - val);
		for (int v : values)
		{
			if (abs(v - val) < diff)
			{
				res = v;
				diff = abs(v - val);
			}
		}
		return res;	
	}
	
	static double abs(double d) 
	{
		if (d < 0)
			return -d;
		return d;
	}

	public static int indexOfMin(double[] vals) 
	{
		int index = -1;
		double min = Double.MAX_VALUE;
		for (int i = 0; i < vals.length; i++)
		{
			if (vals[i] < min)
			{
				min   = vals[i];
				index = i; 
			}
		}
		return index;
	}

	public static boolean isEven(int size) 
	{
		if (size % 2 == 0)
			return true;
		return false;
	}

	public static int sum(int[] vals) 
	{
		int sum = 0;
		for (int b : vals)
			sum += b;
		return sum;
	}
	
	public static double sum(double[] vals) 
	{
		double sum = 0;
		for (double v : vals)
			sum += v;
		return sum;
	}

	public static int sum(boolean[] bs) 
	{
		int sum = 0;
		for (boolean b : bs)
			if (b)
				sum++;
		return sum;
	}
	
	public static <T> LinkedList<T> reverse(LinkedList<T> list) 
	{
		LinkedList<T> reverse = new LinkedList<T>();
		Iterator<T> itr = list.descendingIterator();
	    while (itr.hasNext())
	    	reverse.addLast(itr.next());
	    
	    return reverse;
	}

	public static Integer[] rangeI(int len) 
	{
		final Integer[] II = new Integer[len];
		for (int i = 0; i < len; i++) 
			II[i] = i;
		return II;
	}
	
	public static int[] range(int len) 
	{
		final int[] II = new int[len];
		for (int i = 0; i < len; i++) 
			II[i] = i;
		return II;
	}
	
	// returns the index of values after sorting ("v" is kept unsorted)
	public static int[] indexSort(final double[] v) 
	{
		final Integer[] II = Util.rangeI(v.length);
		
		Arrays.sort(II, new Comparator<Integer>() {
			@Override
			public int compare(Integer o1, Integer o2) {
				return Double.compare(v[o1],v[o2]);
			}
		});
		int[] ii = new int[v.length];
		for (int i = 0; i < v.length; i++) 
			ii[i] = II[i];
		return ii;
	}

	public static int[] reverse(int[] arr) {
		int n = arr.length;
		int[] rev = new int[n];
		for (int i = 0; i < n; i++)
			rev[i] = arr[(n - i) - 1];
		return rev;
	}
}