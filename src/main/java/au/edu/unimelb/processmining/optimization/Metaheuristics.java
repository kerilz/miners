package au.edu.unimelb.processmining.optimization;

import au.edu.qut.processmining.log.SimpleLog;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagram;

public interface Metaheuristics {
    BPMNDiagram searchOptimalSolution(SimpleLog slog, int order, int maxit, int neighbourhood, int timeout, String name);
}
