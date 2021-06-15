/*
 * This is an interface accessed by TaskInstances 
 * to tell the underlying system (supposedly, middleware, or OS) 
 * that a node is released (enabled to be executed) 
 */

package application.common;

import application.models.dag.Node;

public interface Middleware 
{
	public void nodeReleased(Node n);
}
