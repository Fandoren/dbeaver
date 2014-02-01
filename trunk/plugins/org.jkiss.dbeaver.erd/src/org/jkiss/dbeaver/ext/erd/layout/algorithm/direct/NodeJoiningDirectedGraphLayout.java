/*
 * Copyright (C) 2010-2014 Serge Rieder
 * serge@jkiss.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
/*
 * Created on Jul 16, 2004
 */
package org.jkiss.dbeaver.ext.erd.layout.algorithm.direct;

import org.eclipse.draw2d.graph.DirectedGraph;
import org.eclipse.draw2d.graph.DirectedGraphLayout;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;

/**
 * Extended version of DirectedGraphLayout which allows DirectedGraphLayout
 * functionality to be used even when graph nodes either have no edges, or when part
 * of clusters isolated from other clusters of Nodes
 * 
 * @author Serge Rieder
 */
public class NodeJoiningDirectedGraphLayout extends DirectedGraphLayout
{

    private AbstractGraphicalEditPart diagram;

    public NodeJoiningDirectedGraphLayout(AbstractGraphicalEditPart diagram)
    {
        this.diagram = diagram;
    }

    /**
	 * @param graph public method called to handle layout task
	 */
	@Override
    public void visit(DirectedGraph graph)
	{
		//add dummy edges so that graph does not fall over because some nodes
		// are not in relationships
		new StandaloneNodeConnector(diagram).visit(graph);
		
		// create edges to join any isolated clusters
        // TODO: investigate - cluster edges makes diagram ugly
        // TODO: what the reason to do it???
		//new ClusterEdgeCreator().visit(graph);

		super.visit(graph);
	}

}