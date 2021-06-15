package application.common.structs.queue;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import application.common.enums.EPolicy;
import application.models.dag.DagUtil;
import application.models.dag.Node;

public class ListReadyQueue implements IReadyQueue 
{
	EPolicy policy;
	
	private  long n = 0;
	private  long sumSize = 0;
	
	private LinkedList<Node> q = new LinkedList<Node>();
	private Random  rng;
	
	public ListReadyQueue(EPolicy p)
	{
		policy = p;
		rng    = new Random(100);	// used for tie-breaking in EDF
	}
	
	@Override
	public void enqueue(Node node) 
	{
		q.add(node);		
	}

	@Override
	public void enqueue(List<Node> nodes) 
	{
		q.addAll(nodes);
	}

	@Override
	public void removeExpired(int now) 
	{
		QUtil.removeExpired(q, now);			
	}

	@Override
	public LinkedList<Node> dequeue(int n) 
	{
		if (n == 0)
			return new LinkedList<Node>();
					
		statistics();
		
		if (q.size() <= n)
			return dequeueAll();
		
		LinkedList<Node> res = new LinkedList<Node>();
		
		if (policy == EPolicy.Random)
		{
			res = QUtil.random(q, n, rng);
			q.removeAll(res);
			return res;
		}
		
		while (res.size() < n)
		{
			int needed = n - res.size();
			LinkedList<Node> toAdd = new LinkedList<Node>();
			if (policy == EPolicy.EDF)
			{
				toAdd = QUtil.eds(q);
				if (toAdd.size() > needed)
					toAdd = highestPrios(toAdd, needed);
			}
			else if (policy == EPolicy.LLED) // || policy == EPolicy.FP_FIFO)
			{   
				LinkedList<Node> eds = QUtil.eds(q);
				if (eds.size() > needed)					
					while (toAdd.size() < needed)
					{
						Node ls = QUtil.ls(eds);
						toAdd.add(ls);
						eds.remove(ls);
					}
				else
					toAdd = eds;
			}
			else if (policy == EPolicy.EDLL)
			{
				LinkedList<Node> lss = QUtil.lss(q);
				if (lss.size() > needed)					
					while (toAdd.size() < needed)
					{
						Node ed = QUtil.ed(lss);
						toAdd.add(ed);
						lss.remove(ed);
					}
				else
					toAdd = lss;		
			}
			else if (policy == EPolicy.RM)
			{
				LinkedList<Node> rms = DagUtil.rms(q); 
				// toAdd = random(eds, size - res.size());
				toAdd = highestPrios(rms, needed);
			}
			else if (policy == EPolicy.FP_FIFO)
			{
				toAdd.add(q.getFirst());
			}
			else
			{
				System.out.println("Unhandled policy : " + policy);
				toAdd = new LinkedList<Node>();
			}
				
			res.addAll(toAdd);
			q.removeAll(toAdd);
		}
		// q.removeAll(res);
		return res;
	}
	
	private LinkedList<Node> highestPrios(LinkedList<Node> nodes2, int size)
	{
		LinkedList<Node> copy = new LinkedList<Node>(nodes2);
		if (copy.size() <= size)
			return copy;
	
		LinkedList<Node> res = new LinkedList<Node>();

		for (int i = 0; i < size; i++)
		{
			Node n = highestPrio(copy); 
			res.add(n);
			copy.remove(n);
		}
		return res;
	}
	
	private LinkedList<Node> highestPriosBestLen(LinkedList<Node> nodes, int size)
	{
		LinkedList<Node> copy = new LinkedList<Node>(nodes);
		if (copy.size() <= size)
			return copy;
	
		LinkedList<Node> res = new LinkedList<Node>();

		for (int i = 0; i < size; i++)
		{
			Node n = bestLen(copy); 
			res.add(n);
			copy.remove(n);
		}
		return res;
	}
	
	private Node bestLen(LinkedList<Node> nodes) 
	{
		// TODO Auto-generated method stub
		return null;
	}

	private Node highestPrio(LinkedList<Node> nodes) 
	{		
		Node highest = nodes.getFirst();
		for (Node n : nodes)
		{
			if (n.owner().id() < highest.owner().id())
				highest = n;
			else 
				if (n.name().compareTo(highest.name()) > 0)
				highest = n;
		}

		return highest;
	}
	
	// @Override
	private LinkedList<Node> dequeueAll() 
	{
		LinkedList<Node> res = new LinkedList<Node>(q);
		q.clear();
		return res;
	}

	@Override
	public boolean isEmpty() 
	{
		return q.isEmpty();
	}

	@Override
	public int size() 
	{
		return q.size();
	}

	@Override
	public void print() 
	{
		for (Node n : q)
			System.out.print(n.name() + " (p =  " + n.owner().period() + "), ");
		System.out.println();
	}
	
	@Override
	public int avgSize()
	{
		if (n == 0)
			return 0;
		return (int)(sumSize / n);
	}

	private void statistics() 
	{
		if (q.isEmpty())
			return;
		
		n++;
		sumSize += q.size();
//		n = 1;
//		sumSize = Math.max(sumSize, q.size());
	}
}
