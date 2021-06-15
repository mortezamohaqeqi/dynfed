/*
 * An interface for ready queue used by schedulers
 * to keep elements ready to execute (but not currently running).
 */

package application.common.structs.queue;

import java.util.LinkedList;
import java.util.List;

import application.models.dag.Node;

public interface IReadyQueue 
{
	void enqueue(Node node);
	void enqueue(List<Node> nodes);
	void removeExpired(int now);
	LinkedList<Node> dequeue(int n);
	// LinkedList<Node> dequeueAll();	// used in FP-FIFO
	boolean isEmpty();
	
	/* methods for statistics */
	int size();
	void print();
	int avgSize();
}
