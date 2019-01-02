package edu.pdx.vishakh.cubecalculus.algorithms;

import edu.pdx.vishakh.cubecalculus.core.CubeCalculusException;

import java.io.PrintStream;
import java.util.HashSet;
import java.util.Set;

/**
 * Algorithm to minimize
 */
public abstract class MinimizationAlgorithm<C> {

    /** Number of variables for all cubes and minterms ised in this algorithms. */
    protected int nVars;

    /** Set of ON cubes.  Cannot be {@Code null}.
     * The algorithm may change this set during execution.
     */
    protected Set<C> onCubes;

    /** Set of OFF cubes.  Can be {@Code null}.
     * The algorithm may change this set during execution.
     */
    protected Set<C> offCubes;

    /** Set of OFF cubes.  Can be {@Code null}.
     * The algorithm may change this set during execution.
     */
    protected Set<C> dcCubes;

    /** Print stream to print information during execution. Can be {@Code null}. */
    protected PrintStream detailsPrintStream = null;

    /** Print stream used only for debugging purposes. Can be {@Code null}. */
    protected PrintStream debugPrintStream = null;

    /** Object to collect statistics during execution. */
    protected StatsCollector statsCollector;

    public MinimizationAlgorithm(
            PrintStream detailsStream,
            PrintStream debugStream) {
        this.detailsPrintStream = detailsStream;
        this.debugPrintStream = debugStream;
        this.statsCollector = null;
    }

    /**
     *
     * @param nVars Number of variables.
     * @param onCubes The ON set of cubes.
     * @param offCubes The OFF sets of cubes.  Can be {@Code nhull}.
     * @param dcCubes The DONTCARE sets of cubes.  Can be {@Code nhull}.
     * @return Set of cubes in the minimized function.
     */
    public Set<C> minimize(
            int nVars,
            Set<C> onCubes,
            Set<C> offCubes,
            Set<C> dcCubes,
            StatsCollector statsCollector,
            boolean stopAtCyclic) {
        this.nVars = nVars;

        // We take copies of the sets, as we will be modifying them in the algorithm.
        if (onCubes == null) {
            throw new CubeCalculusException("ONcubes should be provided.");
        }
        this.onCubes = new HashSet<>(onCubes);

        this.offCubes = offCubes == null ? null : new HashSet<>(offCubes);
        this.dcCubes = dcCubes == null ? null : new HashSet<>(dcCubes);

        this.statsCollector = statsCollector;

        initialize();
        return doMinimize(stopAtCyclic);
    }

    protected abstract void initialize();

    protected abstract Set<C> doMinimize(boolean stopAtCyclic);

    /** Name of the algorithm, for printing. */
    public abstract String getName();

    /** Short name of the algorithm, for printing. */
    public abstract String getShortName();
}

