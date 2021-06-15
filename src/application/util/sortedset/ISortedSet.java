package application.util.sortedset;

import java.util.List;

public interface ISortedSet 
{
	public void add(int i);
	public List<Integer> values(); // returns all elements in a sorted way (descending)
}
