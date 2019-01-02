package edu.pdx.vishakh.cubecalculus.algorithms;

import edu.pdx.vishakh.cubecalculus.core.BooleanCube;
import edu.pdx.vishakh.cubecalculus.core.CubeCalculusException;
import edu.pdx.vishakh.cubecalculus.core.CubeUtils;
import edu.pdx.vishakh.cubecalculus.core.Minterm;
import edu.pdx.vishakh.cubecalculus.core.SetUtils;
import edu.pdx.vishakh.cubecalculus.core.TestUtils;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Minimizes a set of cubes using the Quince-McClunskey Algorithm.
 */
public class QuineMcCluskeyAlgorithm extends MinimizationAlgorithm<BooleanCube> {

    /** Map of a minterm to the cubes it belongs to. */
    private Map<BooleanCube, Set<BooleanCube>> mintermToPrimeMap;

    /** Map of a cube to the minterms it contains. */
    private Map<BooleanCube, Set<BooleanCube>> primeToMintermMap;

    /** Starts with the essential primes, and then finally includes the full minimized cover. */
    private Set<BooleanCube> minimizedPrimes;

    /** Primes identified but not yet part of the final cover. Each BooleanCube in this set will either be included
     * in the final cover (minimizedPrimes) or will be discarded as redundant primes.
     */
    private Set<BooleanCube> remainingPrimes;

    /** Minterms that are not part of any primes in minimizedPrimes.  In the end, this set should be empty. */
    private Set<BooleanCube> remainingMinterms;


    public QuineMcCluskeyAlgorithm(PrintStream detailsStream, PrintStream debugStream) {
        super(detailsStream, debugStream);
    }

    @Override
    protected void initialize() {
        if (onCubes == null) {
            throw new CubeCalculusException("ON cubes for Quine McCluskey algorithm cannot be null.");
        }

        if (dcCubes == null) {
            dcCubes = new HashSet<>();
            if (offCubes != null) {
                // An explicit off cubes is provided.  Deduce DC cubes from that.
                for (int i = 0; i < (1 << nVars); ++i) {
                    Minterm m = new Minterm(nVars, i);
                    BooleanCube c = BooleanCube.fromMinterm(m);
                    if (!(onCubes.contains(c) || offCubes.contains(c))) {
                        dcCubes.add(c);
                    }
                }
            }
        } else {

            // Both ON cubes and DC cubes provided.  Make sure they do not overlap.
            Set<BooleanCube> overlap1 = SetUtils.findSetIntersection(onCubes, dcCubes);
            int overlap = overlap1.size();
            if (overlap > 0) {
                throw new CubeCalculusException("ON set and DC set have " + overlap + " elements in common.");
            }
        }

        // We don't use OFF cubes.
        offCubes = null;


    }

    @Override
    public String getName() {
        return "Quine-McCluskey Algorithm";
    }

    @Override
    public String getShortName() {
        return "Q";
    }

    @Override
    public Set<BooleanCube> doMinimize(boolean stopAtCyclic) {

        // For finding all primes, we use ON and DC minterms.
        Set<BooleanCube> mintermsForFindingAllPrimes = new HashSet<>(onCubes);
        mintermsForFindingAllPrimes.addAll(dcCubes);

        Set<BooleanCube> allPrimes = findAllPrimes(nVars, mintermsForFindingAllPrimes);

        if (detailsPrintStream != null) {
            TestUtils.printCubes(detailsPrintStream, allPrimes, "All primes");
        }

        // Use only ON cubes for prime implicant chart.
        remainingMinterms = new HashSet(onCubes);

        // ... but use ON and DC for primes.
        remainingPrimes = allPrimes;

        // Prepare the maps equivalent to the SPC table.
        prepareMaps(remainingPrimes, remainingMinterms);

        minimizedPrimes = new HashSet<>();

        if (extractEssentialPrimes()) {
            printIntermediateResults("Essential primes");
        } else {
            printDetailedMessage("No essential primes");
        }

        // Now, we have the following:
        // minimizedPrimes:  The final set of covers.  Currently has only the essential primes.
        // remainingPrimes: Primes that are not in final cover.
        // remainingMinterms: Uncovered minterms.

        boolean done = false;
        while (!done) {
            done = true;

            // We apply dominance relation theorem and extraction of essential primes repeatedly.
            boolean rowDominance = false;
            while (!rowDominance) {
                rowDominance = doRowDominance();
                if (rowDominance) {
                    printDetailedMessage("No row dominance");
                } else {
                    printIntermediateResults("After another round of row dominance");
                    done = false;
                    if (extractEssentialPrimes()) {
                        printIntermediateResults("After extracting more covers");
                    }
                }
            }


            if (!remainingMinterms.isEmpty() && !remainingPrimes.isEmpty()) {
                // No more dominance/extraction, meaning the function is cyclic.
                if (statsCollector != null) {
                    statsCollector.setCyclic(true);
                }

                // Add one of the remaining primes into final cover.
                BooleanCube randomPrime = remainingPrimes.iterator().next();

                promoteToMinimized(randomPrime);
                printIntermediateResults( "After picking a random prime $" + randomPrime.toNumericString() + "$");

                if (extractEssentialPrimes()) {
                    printIntermediateResults("After picking more essential primes");
                }

                // Repeat the process to check whether we can apply dominance relation theorem further.
                done = false;
            }
        }
        return minimizedPrimes;
    }


    /**
     * Prepares the maps (one for rows and another for columns) for the SPC table.
     * These maps get modified as the algorithm proceeds.
     * @param primes Primes (rows in the table)
     * @param minterms Minterms (Columns in the table)
     */
    private void prepareMaps(Set<BooleanCube> primes, Set<BooleanCube> minterms) {
        mintermToPrimeMap = new HashMap<>();
        primeToMintermMap = new HashMap<>();
        for (BooleanCube minterm : minterms) {
            for (BooleanCube prime : primes) {
                if (prime.includes(minterm)) {
                    SetUtils.addToSetInMap(minterm, prime, mintermToPrimeMap);
                    SetUtils.addToSetInMap(prime, minterm, primeToMintermMap);
                }
            }
        }

    }

    /**
     * Finds all primes in a boolean function.
     * @param nVars Number of variables.
     * @param cubes Set of all ON and DC cubes.
     * @return Set of all primes.
     * TODO: This function is too big and complex.  Refactor to simplify.
     */
    public Set<BooleanCube> findAllPrimes(int nVars, Set<BooleanCube> cubes) {

        // Map stores the set of cubes that has specific number of ones in them.
        Map<Integer, Set<BooleanCube>> currentMap = new HashMap<>();

        // Initialize the maps with the input minterms/cubes.
        for (BooleanCube cube : cubes) {
            SetUtils.addToSetInMap(cube.width(), cube, currentMap);
        }

        // Holds the prime cubes.
        Set<BooleanCube> primeSet = new HashSet<>();

        // The last set of cubes in the upper level, to be handled in the end.
        Set<BooleanCube> lastUpper = null;

        // Set of cubes removed after merging with another cube.
        Set<BooleanCube> removedSet = new HashSet<>();


        boolean done = false;


        int order = 0;
        while (!done) {
            done = true;

            // The lower cubes in a pair of adjacent sets.
            Set<BooleanCube> lower;

            // The lower cubes in a pair of adjacent sets.
            Set<BooleanCube> upper;

            // Map for the next round.
            Map<Integer, Set<BooleanCube>> nextMap = new HashMap<>();


            int width = 0;
            while (width < nVars) {
                if (debugPrintStream != null) {
                    debugPrintStream.println("Width " + order);
                }
                lower = currentMap.get(width);
                upper = currentMap.get(width + 1);

                if (lower == null) {
                    ++width;
                    lastUpper = upper;
                    continue;
                }

                if (upper == null) {
                    for (BooleanCube c : lower) {
                        if (!removedSet.contains(c)) {
                            primeSet.add(c);
                            removedSet.add(c);
                        }
                    }
                    ++width;
                    lastUpper = upper;
                    continue;
                }

                // Neither lower nor upper is null.  Check whether any of them can be merged.
                for (BooleanCube lowerCube : lower) {
                    for (BooleanCube upperCube : upper) {

                        // Check whether lowerCube and upperCube are adjacent.
                        if (lowerCube.isAdjacentTo(upperCube)) {

                            // Merge if possible.
                            if (statsCollector != null) {
                                statsCollector.increamentMerges();
                            }
                            BooleanCube newCube = lowerCube.merge(upperCube);
                            if (debugPrintStream != null) {
                                debugPrintStream.printf("Merging %s and %s to get %s\n",
                                        lowerCube.toNumericString(), upperCube.toNumericString(),
                                        newCube.toNumericString());
                            }

                            // Promote the resulting cube to the next level.
                            SetUtils.addToSetInMap(newCube.width(), newCube, nextMap);

                            // Remove merged cubes from the set.
                            // These cubes may be part of other clusters that can be merged.
                            // So, they are not removed from the cube set.
                            removedSet.add(lowerCube);
                            removedSet.add(upperCube);

                            done = false;
                        }
                    }
                }

                // All the cubes in the lower set that are not merged are prime cubes.
                // Add them to the prime set.
                for (BooleanCube c : lower) {
                    if (!removedSet.contains(c)) {
                        primeSet.add(c);
                        removedSet.add(c);
                    }
                }

                lastUpper = upper;
                ++width;
            }

            // Find the primes from the last group also.
            if (lastUpper != null) {
                for (BooleanCube c : lastUpper) {
                    if (!removedSet.contains(c)) {
                        primeSet.add(c);
                        removedSet.add(c);
                    }
                }
            }
            printFindAllPrimesStepTableHeader(detailsPrintStream);
            printStep(detailsPrintStream, nVars, primeSet, currentMap);
            printFindAllPrimesStepTableFooter(detailsPrintStream, ++order);

            currentMap = nextMap;
        }


        return primeSet;
    }


    /**
     * Extracts essential primes from remaining primes and minterms, and promotes them.
     * @return
     */
    private boolean extractEssentialPrimes() {
        // Identify essential primes.
        Set<BooleanCube> essentialPrimes = new HashSet<>();
        boolean done = false;
        while (!done) {
            done = true;
            Set<BooleanCube> newEssentialPrimes = findEssentialPrimes(remainingMinterms);
            if (!newEssentialPrimes.isEmpty()) {
                done = false;
                essentialPrimes.addAll(newEssentialPrimes);
                for (BooleanCube essentialPrime : newEssentialPrimes) {
                    promoteToMinimized(essentialPrime);
                }
            }
        }


        return !essentialPrimes.isEmpty();
    }


    public Set<BooleanCube> findEssentialPrimes(Set<BooleanCube> minterms) {
        Set<BooleanCube> essentialPrimes = new HashSet<>();

        Set<BooleanCube> mintermsForIter = new HashSet<>(minterms);
        for (BooleanCube minterm : mintermsForIter) {
            if (mintermToPrimeMap.containsKey(minterm)) {
                Set<BooleanCube> primesForThisMinterm = mintermToPrimeMap.get(minterm);
                if (primesForThisMinterm.size() == 1) {
                    BooleanCube prime = primesForThisMinterm.iterator().next();
                    essentialPrimes.add(prime);
                    minterms.removeAll(CubeUtils.getMinterms(prime));
                }
            }
        }
        return essentialPrimes;
    }

    /** Make a prime part of the final cover. */
    private void promoteToMinimized(BooleanCube prime) {
        // Add to minimized set.
        minimizedPrimes.add(prime);

        // Remove from remaining set.
        remainingPrimes.remove(prime);

        Set<BooleanCube> minterms = new HashSet<>(CubeUtils.getMinterms(prime));

        // Remove all of its minterms.
        remainingMinterms.removeAll(minterms);

        // Detatch each minterm from its primes.
        for (BooleanCube m : minterms) {
            if (mintermToPrimeMap.containsKey(m)) {
                Set<BooleanCube> primes = new HashSet<>(mintermToPrimeMap.get(m));
                for (BooleanCube p : primes) {
                    SetUtils.removeFromSetInMap(p, m, primeToMintermMap);
                    SetUtils.removeFromSetInMap(m, p, mintermToPrimeMap);
                }
            }
        }
    }

    /** Remove all references of a redundant prime. */
    private void removeRedundantPrime(BooleanCube prime) {
        // Remove the prime from remaining primes.
        remainingPrimes.remove(prime);

        // Detatch the prime from each of its minterms.
        Set<BooleanCube> minterms = CubeUtils.getMinterms(prime);
        for (BooleanCube m : minterms) {
            SetUtils.removeFromSetInMap(m, prime, mintermToPrimeMap);
        }

        // Detach all minterms from this prime.
        primeToMintermMap.remove(prime);

    }

    /**
     * Applies Row dominance relation theorem on the existing data.
     * @return True if this didn't make any modifications and we are done.
     */
    private boolean doRowDominance() {
        boolean done = true;

        // We copy the set of cubes to a list of cubes for two purposes.
        // (1) We can iterate the list and delete from the set, without causing any
        //     concurrency issue.
        // (2) This will be more efficient, because the inner loop will loop only after the
        //     outer loop's index.  This is not possible with sets.

        // A boolean value just to distinguish the first line while printing.
        List<BooleanCube> primeList = new ArrayList<>(remainingPrimes);

        boolean firstRow = true;

        for (int i = 0; i < primeList.size(); ++i) {

            // Identify a prime and its minterms.
            BooleanCube prime1 = primeList.get(i);
            Set<BooleanCube> minterms1 = primeToMintermMap.get(prime1);

            // TODO: This may be a redundant check.  Remove.
            if (minterms1 == null) {
                continue;
            }

            // TODO: This may be a redundant check.  Remove.
            if (minterms1.isEmpty()) {
                removeRedundantPrime(prime1);
                continue;
            }


            for (int j = i+1; j < primeList.size(); ++j) {

                // Identify a second prime and its minterms.
                BooleanCube prime2 = primeList.get(j);

                Set<BooleanCube> minterms2 = primeToMintermMap.get(prime2);

                // TODO: This may be a redundant check.  Remove.
                if (minterms2 == null) {
                    continue;
                }

                // TODO: This may be a redundant check.  Remove.
                if (minterms2.isEmpty()) {
                    removeRedundantPrime(prime2);
                    continue;
                }

                int cost1 = prime1.cost();
                int cost2 = prime2.cost();

                BooleanCube dominatingPrime = null;
                Set<BooleanCube> dominatingMinterms = null;
                BooleanCube dominatedPrime = null;
                Set<BooleanCube> dominatedMinterms = null;
                if (cost1 > cost2) {
                    if (dominates(minterms2, minterms1)) {
                        dominatingPrime = prime2;
                        dominatingMinterms = minterms2;
                        dominatedPrime = prime1;
                        dominatedMinterms = minterms1;
                    }

                } else if (cost2 > cost1) {
                    if (dominates(minterms1, minterms2)) {
                        dominatingPrime = prime1;
                        dominatingMinterms = minterms1;
                        dominatedPrime = prime2;
                        dominatedMinterms = minterms2;
                    }

                } else { // Equal cost
                    if (dominatesForEqualCost(minterms2, minterms1)) {
                        dominatingPrime = prime2;
                        dominatingMinterms = minterms2;
                        dominatedPrime = prime1;
                        dominatedMinterms = minterms1;
                    } else if (dominatesForEqualCost(minterms1, minterms2)) {
                        dominatingPrime = prime1;
                        dominatingMinterms = minterms1;
                        dominatedPrime = prime2;
                        dominatedMinterms = minterms2;
                    }
                }

                // If one prime dominates another, remove the dominated one as redundant.
                if (dominatingPrime != null) {
                    // If needed, print this row.
                    if (detailsPrintStream != null) {
                        if (firstRow) {
                            firstRow = false;
                            printDominanceTableHeader(detailsPrintStream);
                        }

                        // Print the row.
                        printDominanceRow(detailsPrintStream, dominatingPrime, dominatingMinterms, dominatedPrime, dominatedMinterms);
                    }
                    removeRedundantPrime(dominatedPrime);
                    done = false;
                }

            }
        }
        if (!firstRow) {
            printDominanceTableFooter(detailsPrintStream, "Row dominance table");
        }

        if (remainingPrimes.isEmpty()) {
            done = true;
        }
        return done;
    }

    /**
     * Returns whether the prime with minterms minterms1 dominates the cube with minterms2.
     * Returns true even when the two minterms are of equal size.
     */
    private boolean dominates(Set<BooleanCube> minterms1, Set<BooleanCube> minterms2) {
        return (minterms1.size() >= minterms2.size()) && (minterms1.containsAll(minterms2));
    }

    /**
     * Returns whether the prime with minterms minterms1 dominates the cube with minterms2.
     * Returns true only if the first minterms is longer than the second.
     */
    private boolean dominatesForEqualCost(Set<BooleanCube> minterms1, Set<BooleanCube> minterms2) {
        return (minterms1.size() > minterms2.size()) && (minterms1.containsAll(minterms2));
    }

    //// VARIOUS PRINTING FUNCTIONS.


    private void printDominanceTableHeader(PrintStream out) {
        if (out != null) {
            out.println("\n\\begin{table}[H]");
            out.println("\\centering");
            out.println("\\begin{tabular}{|c|c|c|c|}");
            out.println("\\hline");
            out.println("\\multicolumn{2}{|c|}{\\textbf{Dominating}} & \\multicolumn{2}{|c|}{\\textbf{Dominated}} \\\\");
            out.println("\\hline");
            out.println("\\multicolumn{1}{|c|}{\\textbf{BooleanCube}} & \\textbf{Minterms} & \\multicolumn{1}{|c|}{\\textbf{BooleanCube}} & \\textbf{Minterms} \\\\");
            out.println("\\hline");
        }

    }

    private void printDominanceTableFooter(PrintStream out, String caption) {
        if (out != null) {
            out.println("\\hline");
            out.println("\\end{tabular}");
            out.println("\\caption{" + caption + "}");
            out.println("\\end{table}\n");
        }
    }

    private void printDominanceRow(PrintStream out,
                                   BooleanCube dominatingPrime, Set<BooleanCube> dominatingMinterms,
                                   BooleanCube dominatedPrime, Set<BooleanCube> dominatedMinterms) {
        if (out != null) {
            out.printf("\\bitp{%s} & %s & \\bitp{%s} & %s\\\\\n",
                    dominatingPrime.toNumericString(), CubeUtils.cubeSetToNumericString(dominatingMinterms),
                    dominatedPrime.toNumericString(), CubeUtils.cubeSetToNumericString(dominatedMinterms)
            );
        }
    }


    private void printDetailedMessage(String message) {
        if (detailsPrintStream != null) {
            detailsPrintStream.println("\n\\begin{center}" + message + "\\end{center}\n");
        }
    }

    private void printIntermediateResults(String caption) {
        TestUtils.printCustomCubes(detailsPrintStream, minimizedPrimes, caption, "Minimized prime");
        TestUtils.printCustomCubes(detailsPrintStream, remainingPrimes, "Remaining primes", "Prime");
        TestUtils.printCustomCubes(detailsPrintStream, remainingMinterms, "Remaining minterms", "Minterm");
    }


    private void printStep(PrintStream out, int nVars, Set<BooleanCube> primeSet, Map<Integer, Set<BooleanCube>> orderMap) {
        if (out != null) {
            for (int k = 0; k <= nVars; k++) {
                if (orderMap.containsKey(k)) {
                    Set<BooleanCube> cubeSet = orderMap.get(k);
                    for (BooleanCube c : cubeSet) {
                        out.printf("%d & \\multicolumn{1}{c}{$%s$} & (\\bitp{%s}) & %c \\\\\n",
                                k, c.toAlgebraicString(), c.toNumericString(), primeSet.contains(c) ? 'Y' : ' ');
                    }

                    out.println("\\hline");
                }
            }
        }
    }

    private void printFindAllPrimesStepTableHeader(PrintStream out) {
        if (out != null) {
            out.println("\n\\begin{table}[H]");
            out.println("\\centering");
            out.println("\\begin{tabular}{|r|c|c|c|}");
            out.println("\\hline");
            out.println("\\textbf{Width} & \\multicolumn{2}{c|}{\\textbf{BooleanCube}} & \\textbf{Prime?} \\\\");
            out.println("\\hline");
        }
    }

    private void printFindAllPrimesStepTableFooter(PrintStream out, int order) {
        if (out != null) {
            out.println("\\end{tabular}");
            out.println("\\caption{Quine-McCluskey Algorithm: Step " + order + "}");
            out.println("\\end{table}\n");
        }
    }
}
