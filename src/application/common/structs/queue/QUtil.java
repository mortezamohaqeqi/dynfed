/*
 * This contains general functions required by the queues  
 * 
 */
package application.common.structs.queue;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;

import application.hwmodel.enums.EResourceType;
import application.models.dag.DagUtil;
import application.models.dag.Node;

public class QUtil 
{

	static void removeExpired(LinkedList<Node> q, int now) 
	{
		Iterator<Node> itr = q.iterator();
		while (itr.hasNext()) 
		{
			Node n = (Node) itr.next();
			if (n.owner().absDeadline() <= now)
				itr.remove();
		}
	}
	
	
	// returns (all) node(s) with the least slack
	public static LinkedList<Node> lss(LinkedList<Node> nodes)
	{
		LinkedList<Node> LSs = new LinkedList<Node>(); 
		int ls = Integer.MAX_VALUE;
		for (Node n : nodes)
		{
			if (slack(n) < ls)
			{
				LSs.clear();
				LSs.add(n);
				ls = slack(n);
			}
			else if (slack(n) == ls)
				LSs.add(n);			
		}
		return LSs;
	}
	
	// the task deadline MINUS the remaining execution of the critical path that starts from n
	static int slack(Node n)
	{
		int WCETsum = n.owner().criticalPath(n);		
		return n.owner().period() - WCETsum;
	}

	// least slack
	public static Node ls(LinkedList<Node> nodes)
	{		
		// Among Earliest Deadlines, get the one with shortest slack time
		Node res = null;
		for (Node n : nodes)
		{
			if (res == null)
				res = n;
			else if (DagUtil.slack(n) < DagUtil.slack(res))
				res = n;
		}
		
		return res;
	}
	
	// most (largest) slack
	public static Node ms(LinkedList<Node> nodes)
	{		
		// Among Earliest Deadlines, get the one with shortest slack time
		Node res = null;
		for (Node n : nodes)
		{
			if (res == null)
				res = n;
			else if (DagUtil.slack(n) > DagUtil.slack(res))
				res = n;
		}
		
		return res;
	}
	
	
	static public LinkedList<Node> eds(LinkedList<Node> nodes)
	{
		LinkedList<Node> EDs = new LinkedList<Node>(); 
		int ed = Integer.MAX_VALUE;
		for (Node n : nodes)
		{
			if (deadline(n) < ed)
			{
				EDs.clear();
				EDs.add(n);
				ed = deadline(n);
			}
			else if (deadline(n) == ed)
				EDs.add(n);			
		}
		return EDs;
	}
	
	// returns a node with earliest deadline (of its task)
	static public Node ed(LinkedList<Node> nodes)
	{
		Node res = null;
		for (Node n : nodes)
		{
			if (res == null) {
				res = n; 
				continue;
			}
			if (deadline(n) < deadline(res))
				res = n;
		}
		return res;
	}	

	public static LinkedList<Node> filter(LinkedList<Node> jobs, EResourceType resrc, int size) 
	{
		LinkedList<Node> typed = new LinkedList<Node>(); 
		for (Node n : jobs)
		{
			if (n.conforms(resrc))
				typed.add(n);
			
			if (typed.size() >= size)
				break;
		}		
		return typed;
	}
	
	static LinkedList<Node> random(LinkedList<Node> nodes, int size, Random rng)
	{
		LinkedList<Node> copy = new LinkedList<Node>(nodes);
		LinkedList<Node> res  = new LinkedList<Node>();

		for (int i = 0; i < size && copy.size() > 0; i++)
		{
			Node n = copy.get(rng.nextInt(copy.size()));	// get(0); // 
			res.add(n);
			copy.remove(n);
		}
		return res;
	}

	static private int deadline(Node n)
	{
		return n.owner().absDeadline();	
	}
}
