package application.util.sortedset;

import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
 
public class MySortedSet implements ISortedSet
{
	private SortedSet<MyInt> set = new TreeSet<MyInt>();
	
	public void add(int i)
	{
		set.add(new MyInt(i));
	}
	
	public List<Integer> values()
	{
		LinkedList<Integer> list = new LinkedList<Integer>();
		for (MyInt i : set)
			list.addLast(i.val);
		
		return list;
	}
}

class MyInt implements Comparable<Object>
{
	int val;
	int id;
	static int id_counter = 0;
	
	public MyInt(int i) 
	{
		val = i;
		id = id_counter++;
	}

	@Override
	public int compareTo(Object o) 
	{
		MyInt other = (MyInt)o;
		if (other.val < this.val)
			return -1;
		if (other.val > this.val)
			return 1;
		
		if (other.id < this.id)
			return -1;
		return 1;		
	}	
}