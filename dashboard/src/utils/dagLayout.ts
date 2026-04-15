import dagre from '@dagrejs/dagre';
import type { DagGraph, DagNode } from '../types/api';

const NODE_WIDTH = 180;
const NODE_HEIGHT = 80;

export const applyDagLayout = (graph: DagGraph): DagNode[] => {
  const dagreGraph = new dagre.graphlib.Graph();
  dagreGraph.setDefaultEdgeLabel(() => ({}));
  
  // Set layout direction top-to-bottom
  dagreGraph.setGraph({ 
    rankdir: 'TB',
    nodesep: 50, // Spacing between nodes in same layer
    ranksep: 80, // Spacing between layers
  });

  // Add nodes
  graph.nodes.forEach((node) => {
    dagreGraph.setNode(node.jobId, { width: NODE_WIDTH, height: NODE_HEIGHT });
  });

  // Add edges
  graph.edges.forEach((edge) => {
    dagreGraph.setEdge(edge.sourceJobId, edge.targetJobId);
  });

  // Apply layout
  dagre.layout(dagreGraph);

  // Add positions back to nodes
  return graph.nodes.map((node) => {
    const nodeWithPosition = dagreGraph.node(node.jobId);
    return {
      ...node,
      position: {
        // Adjust for center positioning by dagre (shift to top-left)
        x: nodeWithPosition.x - NODE_WIDTH / 2,
        y: nodeWithPosition.y - NODE_HEIGHT / 2,
      },
    };
  });
};
