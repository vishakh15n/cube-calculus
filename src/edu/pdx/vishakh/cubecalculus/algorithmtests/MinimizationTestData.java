package edu.pdx.vishakh.cubecalculus.algorithmtests;

import edu.pdx.vishakh.cubecalculus.algorithms.MinimizationAlgorithm;
import edu.pdx.vishakh.cubecalculus.algorithms.StatsCollector;
import edu.pdx.vishakh.cubecalculus.core.BooleanCube;
import edu.pdx.vishakh.cubecalculus.core.CubeCalculusException;
import edu.pdx.vishakh.cubecalculus.core.CubeUtils;

import java.util.HashSet;
import java.util.Set;

public class MinimizationTestData {
    /**
     * Represent one piece of data used as input in minimization problems.
     */
    public static class MinimizationData {
        public String shortDescription;
        public String longDescription;
        public int nVars;
        public long[] onMinterms;  // ON minterms.  Non-null.
        public long[] offMinterms; // OFF minterms.  Can be null.
        public long[] dcMinterms;  // DC minterms.  Can be null.

        public MinimizationData(
                String shortDescription,
                String longDescription,
                int nVars,
                long[] onMinterms,
                long[] offMinterms,
                long[] dcMinterms
        ) {
            this.shortDescription = shortDescription;
            this.longDescription = longDescription;
            this.nVars = nVars;
            this.onMinterms = onMinterms;
            this.offMinterms = offMinterms;
            this.dcMinterms = dcMinterms;
        }
    }

    /**
     * Represent the data used to validate the result of a minimization problem.  It can have two forms.
     * (1) If {@Code minimizedValues} is empty, we check against the {@Code minimizedCost} only.  This is when we have
     *     too many valid results and cannot list all of them.
     * (2) If {@Code minimizedValues} is not empty, it consists of one or more cube sets, each of which will be a valid
     *     solution, and each cube set will have a total cost equal to {@Code minimizedCost}.
     *
     *  For convenience of declaration, the data can be constructed in various ways.
     */
    public static class ExpectedMinimizedValue {
        private Set<Set<BooleanCube>> minimizedValues;
        private int minimizedCost;

        /** Creates empty object.  Solutions are later added by {@Code #addSolution}. */
        public ExpectedMinimizedValue() {
            minimizedValues = new HashSet<>();
            minimizedCost = -1;
        }

        /** Creates an object with only cost. */
        public ExpectedMinimizedValue(int cost) {
            minimizedValues = new HashSet<>();
            minimizedCost = cost;
        }

        /** Creates with a bunch of solutions. Each element of {@Code solutions} indicates a valid solution. */
        public ExpectedMinimizedValue(String[][] solutions) {
            minimizedValues = new HashSet<>();
            for (String[] solution : solutions) {
                addSolution(solution);
            }
        }

        /** Creates with a single solution.  */
        public ExpectedMinimizedValue(String[] solution) {
            minimizedValues = new HashSet<>();
            addSolution(solution);
        }

        /** Adds a solution to an existing set of solutions.  The new solution should have the same cost as the existing
         * ones.
         */
        public void addSolution(String[] solutions) {

            int cost = 0;
            Set<BooleanCube> solutionCubes = new HashSet<>();
            for (String solution : solutions) {
                BooleanCube solutionCube = BooleanCube.fromString(solution);
                solutionCubes.add(solutionCube);
                cost += solutionCube.cost();
            }
            if (!minimizedValues.isEmpty()) {
                if (cost != minimizedCost) {
                    throw new CubeCalculusException("Solutions should have same cost");
                }
            }
            minimizedValues.add(solutionCubes);
            minimizedCost = cost;
        }

        public int getCost() { return minimizedCost; }
        public Set<Set<BooleanCube>> getValues() { return minimizedValues; }
        public Set<BooleanCube> getUniqueValue() {
            if (minimizedValues.size() != 1) {
                throw new CubeCalculusException("Doesn't have single solution");
            }
            return minimizedValues.iterator().next();
        }
    }

    public static String validate(MinimizationAlgorithm alg, MinimizationData input,
                                  ExpectedMinimizedValue expectedOutput) {
        int nVars = input.nVars;
        Set<BooleanCube> onCubes = CubeUtils.cubeSetFromNumberArray(nVars, input.onMinterms);
        Set<BooleanCube> offCubes = input.offMinterms == null ? null : CubeUtils.cubeSetFromNumberArray(nVars, input.offMinterms);
        Set<BooleanCube> dcCubes = input.dcMinterms == null ? null : CubeUtils.cubeSetFromNumberArray(nVars, input.dcMinterms);
        StatsCollector sc = new StatsCollector();
        Set<BooleanCube> actual = alg.minimize(input.nVars, onCubes, offCubes, dcCubes, sc, false);

        if (actual == null) {
            return "CYCLIC";
        }
        String cyclic = sc.isCyclic() ? "*" : " ";

        long actualCost = CubeUtils.totalCostOfCubes(actual);
        int numberOfExpectedValues = expectedOutput.minimizedValues.size();
        String result = String.format(": %-4s : Result = %s (Cost = %d)", sc.getType(), CubeUtils.cubeSetToPlainNumericString(actual), actualCost);
        if (numberOfExpectedValues == 0) {
            int expCost = expectedOutput.minimizedCost;
            if (expCost == actualCost) {
                return  cyclic + "PASS: " + result + ", Expected cost is same as actual cost: " + expCost;
            } else {
                return cyclic + "FAIL: " + result + ", Expected Cost = " + expCost + ", Actual Cost = " + actualCost;
            }
        } else if (numberOfExpectedValues == 1) {
            if (expectedOutput.minimizedValues.contains(actual)) {
                return cyclic + "PASS: " + result + " = expected value";
            } else {
                return cyclic + "FAIL: " + result  + " != expected value.";
            }
        } else {
            if (expectedOutput.minimizedValues.contains(actual)) {
                return cyclic + "PASS: " + result + " is in the expected values.";
            } else {
                return cyclic + "FAIL: " + result  + " is not in the expected values.";
            }
        }
    }

    public static final MinimizationData biswasExample1 = new MinimizationData(
            "Biswas Example",
            "Example f1 from \\cite{BISW1993}",
            5,
            new long[]{0, 1, 3, 5, 7, 8, 9, 10, 13, 14, 15, 17, 21, 25, 29},
            null,
            null
    );

    public static final ExpectedMinimizedValue biswasExample1_solution = new ExpectedMinimizedValue(
            new String[] {"0*00*", "00**1", "***01", "01*10", "0*1*1"}
    );

    public static final MinimizationData perkowskiStaircase = new MinimizationData(
            "Perkowski Staircase Example",
            "Example given by Dr. Perkowski: Simple staircase",
            4,
            new long[]{0, 1, 5, 7, 10, 14, 15},
            null,
            null
    );

    public static final ExpectedMinimizedValue perkowskiStaircase_solution = new ExpectedMinimizedValue(
            new String[][] {
                    {"000*", "01*1", "111*", "1*10"},
                    {"000*", "0*01", "*111", "1*10"},
                    {"000*", "01*1", "*111", "1*10"},
            }
    );


    public static final MinimizationData perkowskiCyclic = new MinimizationData(
            "Perkowski cyclic example",
            "Example given by Dr. Perkowski: Cyclic",
            4,
            new long[]{0, 1, 5, 7, 8, 10, 14, 15},
            null,
            null
    );

    public static final ExpectedMinimizedValue perkowskiCyclic_solution = new ExpectedMinimizedValue(12);

    public static final MinimizationData perkowskiBookExample1 = new MinimizationData(
            "Perkowski book example 1, chapter 2",
            "Example given by Dr. Perkowski in the book \\cite{PERK2018}",
            5,
            new long[]{0, 1, 7, 5, 9, 11, 13, 16, 18, 20,  22, 23, 26, 27, 29, 31},
            null,
            null
    );

    public static final ExpectedMinimizedValue perkowskiBookExample1_solution = new ExpectedMinimizedValue(26);

    public static final MinimizationData perkowskiBookExample2 = new MinimizationData(
            "Perkowski book example 2, chapter 2",
            "Example given by Dr. Perkowski in the book \\cite{PERK2018}",
            6,
            new long[]{9, 24, 25, 27, 17, 29, 61, 47, 45, 44, 37},
            null,
            null
    );

    public static final ExpectedMinimizedValue perkowskiBookExample2_solution = new ExpectedMinimizedValue(40);

    public static final MinimizationData perkowskiBookExample3 = new MinimizationData(
            "Perkowski book example 3, chapter 2",
            "Example given by Dr. Perkowski in the book \\cite{PERK2018}",
            6,
            new long[]{9, 24, 25, 27, 17, 29, 61, 47, 45, 44, 37, 57},
            null,
            null
    );

    public static final ExpectedMinimizedValue perkowskiBookExample3_solution = new ExpectedMinimizedValue(39);

    public static final MinimizationData brayton1 = new MinimizationData(
            "Brayton example",
            "Example from \\cite{BRAYTON84}",
            3,
            new long[]{0, 1, 4, 5, 6},
            null,
            null
    );

    public static final ExpectedMinimizedValue brayton1_solution = new ExpectedMinimizedValue(
            new String[] {"*0*", "1*0"}
    );

    public static final MinimizationData biswasCyclic = new MinimizationData(
            "Biswas cyclic example",
            "Example from \\cite{BISW1993}: Cyclic function",
            5,
            new long[]{2, 6, 13, 14, 15, 18, 26, 30},
            null,
            null
    );

    public static final ExpectedMinimizedValue biswasCyclic_solution = new ExpectedMinimizedValue(
            new String[][] {
                    { "011*1",  "00*10", "1*010", "*1110"  },
                    { "011*1",  "*0010", "0*110", "11*10"  }
            }
    );

    public static final MinimizationData allMinterms = new MinimizationData(
            "Test Case 1",
            "Test Case 1",
            3,
            new long[]{0, 1, 2, 3, 4, 5, 6, 7},
            null,
            null
    );

    public static final ExpectedMinimizedValue allMinterms_solution = new ExpectedMinimizedValue(
            new String[] {"***"}
    );

    public static final MinimizationData randomTest1 = new MinimizationData(
            "Random data 1",
            "Random data 1",
            4,
            new long[]{2, 6, 8, 9, 11, 12, 14},
            null,
            null
    );

    public static final ExpectedMinimizedValue randomTest1_solution = new ExpectedMinimizedValue(12);


    public static final MinimizationData rothExample1 = new MinimizationData(
            "Example from Roth",
            "Example from \\cite{ROTH2010}",
            4,
            new long[]{0, 1, 2, 5, 6, 7, 8, 9, 10, 14},
            null,
            null
    );

    public static final ExpectedMinimizedValue rothExample1_solution = new ExpectedMinimizedValue(
            new String[] {"01*1", "*00*", "**10"}
    );



    public static final MinimizationData cyclic1 = new MinimizationData(
            "Test Case 1",
            "Test Case 1",
            3,
            new long[] {1, 2, 3, 4, 5, 6},
            null,
            null
    );

    public static final ExpectedMinimizedValue cyclic1_solution = new ExpectedMinimizedValue(6);


    // Incompletely specified functions

    public static final MinimizationData wikipedia = new MinimizationData(
            "Wikipedia example: with DCs",
            "Example from Wikipedia article on Quine McCluskey Algorithm \\cite{WIKI-QM}",
            4,
            new long[]{4, 8, 10, 11, 12, 15},
            null,
            new long[] {9, 14}
    );

    public static final ExpectedMinimizedValue wikipedia_solution = new ExpectedMinimizedValue(
            new String[][]{
                    {"1*1*", "*100", "10**"},
                    {"1*1*", "*100", "1**0"}
            }
    );


    public static final MinimizationData roth2 = new MinimizationData(
            "Worked out example (Roth)",
            "Worked out example from \\cite{ROTH2010}",
            4,
            new long[]{2, 3, 7, 9, 11, 13},
            null,
            new long[] {1, 10, 15}
    );

    public static final ExpectedMinimizedValue roth2_solution = new ExpectedMinimizedValue(
            new String[] {"*01*", "**11", "1**1"}
    );


    public static final MinimizationData roth3 = new MinimizationData(
            "Textbook exercise, 6.4 from Roth",
            "Exercise 6.4, \\cite{ROTH2010}",
            4,
            new long[]{1,3, 4, 5, 6, 7, 10, 12, 13},
            null,
            new long[] {2, 9, 15}
    );

    // 6.5
    public static final ExpectedMinimizedValue roth3_solution = new ExpectedMinimizedValue(9);


    public static final MinimizationData roth4 = new MinimizationData(
            "Textbook exercise, 6.5 from Roth",
            "Exercise 6.4, \\cite{ROTH2010}",
            4,
            new long[]{9, 12, 13, 15},
            null,
            new long[] {1, 4, 5, 7, 8, 11, 14}
    );

    // 6.5
    public static final ExpectedMinimizedValue roth4_solution = new ExpectedMinimizedValue(
            new String[][]{
                    {"11**", "**01"},
                    {"11**", "1*0*"},
                    {"11**", "1**1"},
                    {"1*0*", "1**1"},
                    {"1*0*", "*1*1"},
                    {"1**1", "*10*"}
            }
    );

    // A test where Perkowski algorithm was wrongly detecting cycles.
    public static final MinimizationData test1 = new MinimizationData(
            "A case where Per algorithm detected cycles wrongly",
            "A case where Per algorithm detected cycles wrongly",
            3,
            new long[]{3, 6, 7},
            null,
            new long[] {4}
    );


    public static final ExpectedMinimizedValue test1_solution = new ExpectedMinimizedValue(
            new String[] {"1*0", "*11"});

    public static final MinimizationData test2 = new MinimizationData(
            "A case where Per algorithm detected cycles wrongly",
            "A case where Per algorithm detected cycles wrongly",
            3,
            new long[]{7},
            null,
            new long[] {4}
    );


    public static final ExpectedMinimizedValue test2_solution = new ExpectedMinimizedValue(
            new String[] {"111"});

    public static final MinimizationData kohavi1 = new MinimizationData(
            "Kohavi Ex. 4.2",
            "Example 4.2 from \\cite{KOHA2010}",
            4,
            new long[]{0, 4, 5, 7, 8, 9, 13, 15},
            null,
            null
    );

    public static final ExpectedMinimizedValue kohavi1_solution = new ExpectedMinimizedValue(
            new String[] {"0*00", "100*", "1**1"});

    public static final MinimizationData kohavi2 = new MinimizationData(
            "Kohavi Ex. 4.3",
            "Example 4.3 from \\cite{KOHA2010}",
            4,
            new long[]{1, 5, 6, 7, 11, 12, 13, 15},
            null,
            null
    );

    public static final ExpectedMinimizedValue kohavi2_solution = new ExpectedMinimizedValue(
            new String[] {"110*", "1*11", "011*", "0*01"});

    public static final MinimizationData kohavi3 = new MinimizationData(
            "Kohavi Ex. 4.9",
            "Example 4.9 from \\cite{KOHA2010}",
            5,
            new long[]{1, 2, 6, 7, 9, 13, 14, 15, 17, 22, 23, 25, 29, 30, 31},
            null,
            null
    );

    public static final ExpectedMinimizedValue kohavi3_solution = new ExpectedMinimizedValue(
            new String[] {"**001", "*11*1", "**11*", "00*10"});

    public static final MinimizationData kohavi4 = new MinimizationData(
            "Kohavi Ex. 4.10",
            "Example 4.10 (Cyclic) from \\cite{KOHA2010}",
            4,
            new long[]{0, 2, 3, 4, 5, 7},
            null,
            null
    );

    public static final ExpectedMinimizedValue kohavi4_solution = new ExpectedMinimizedValue(12);

    public static final MinimizationData kohavi5 = new MinimizationData(
            "Kohavi Ex. 4.15",
            "Example 4.15 from \\cite{KOHA2010}",
            5,
            new long[]{13, 15, 17, 18, 19, 20, 21, 23, 25, 27, 29, 31},
            null,
            new long[]{1, 2, 12, 24}
    );

    public static final ExpectedMinimizedValue kohavi5_solution = new ExpectedMinimizedValue(
            new String[][] {
                    {"1***1", "*11*1", "1010*", "1001*"},
                    {"1***1", "*11*1", "1010*", "*0010"}
            });

    public static final MinimizationData brown1 = new MinimizationData(
            "Brown and Vranesic, Fig. 4.11",
            "Figure 4.11 from \\cite{BROWN2012}",
            4,
            new long[]{0, 4, 8, 10, 11, 12, 13, 15},
            null,
            null
    );

    public static final ExpectedMinimizedValue brown1_solution = new ExpectedMinimizedValue(
            new String[] {"101*", "11*1", "**00"});


    public static final MinimizationData brown2 = new MinimizationData(
            "Brown and Vranesic, Ex. 4.14",
            "Example 4.14 from \\cite{BROWN2012}",
            4,
            new long[]{0, 2, 5, 6, 7, 8, 9, 13},
            null,
            new long[]{1, 12, 15}
    );

    public static final ExpectedMinimizedValue brown2_solution = new ExpectedMinimizedValue(
            new String[] {"0*10", "*00*", "*1*1"});



    public static final MinimizationData brown3 = new MinimizationData(
            "Brown and Vranesic, Ex. 4.14",
            "Example 4.14 from \\cite{BROWN2012}",
            4,
            new long[]{0, 3, 10, 15},
            null,
            new long[]{1, 2, 7, 8, 11, 14}
    );

    public static final ExpectedMinimizedValue brown3_solution = new ExpectedMinimizedValue(
            new String[] {"00**", "1*1*"});

    public static final MinimizationData brown4 = new MinimizationData(
            "Brown and Vranesic, Ex. 4.20",
            "Example 4.20 from \\cite{BROWN2012}",
            5,
            new long[]{0, 1, 4, 8, 13, 15, 20, 21, 23, 26, 31},
            null,
            new long[]{5, 10, 24, 28}
    );

    public static final ExpectedMinimizedValue brown4_solution = new ExpectedMinimizedValue(
            new String[] {"00*0*", "*10*0", "*010*", "011*1", "1*111"});


    public static final MinimizationData brown5 = new MinimizationData(
            "Brown and Vranesic, Ex. 4.20",
            "Example 4.20 from \\cite{BROWN2012}",
            4,
            new long[]{4, 6, 8, 10, 11, 12, 15},
            null,
            new long[]{3, 5, 7, 9}
    );

    public static final ExpectedMinimizedValue brown5_solution = new ExpectedMinimizedValue(
            new String[][]{
                    {"01**", "10**", "**11", "1*00"},
                    {"01**", "10**", "**11", "*100"},

            });

    public static final MinimizationData diff1 = new MinimizationData(
            "Brown and Vranesic, Ex. 4.20",
            "Example 4.20 from \\cite{BROWN2012}",
            4,
            new long[]{10, 12, 14},
            null,
            new long[]{11, 13}
    );

    public static final ExpectedMinimizedValue diff1_solution = new ExpectedMinimizedValue(
            new String[]{"1*10", "11*0"});


    public static final MinimizationData sample1 = new MinimizationData(
            "Brown and Vranesic, Ex. 4.20",
            "Example 4.20 from \\cite{BROWN2012}",
            3,
            new long[]{5},
            null,
            new long[]{1, 4}
    );

    public static final MinimizationData cycle0 = new MinimizationData(
            "Cycle",
            "Cycle",
            3,
            new long[]{1, 2, 4},
            null,
            null
    );

    public static final ExpectedMinimizedValue cycle0_solution = new ExpectedMinimizedValue(
            new String[]{"1*10", "11*0"});

    public static final MinimizationData cycle1 = new MinimizationData(
            "Cycle",
            "Cycle",
            3,
            new long[]{1, 2, 4},
            null,
            new long[]{0, 3, 5, 6}
    );

    public static final ExpectedMinimizedValue cycle1_solution = new ExpectedMinimizedValue(2);

    public static final MinimizationData cycle2 = new MinimizationData(
            "Cycle",
            "Cycle",
            3,
            new long[]{1, 2, 4},
            null,
            new long[]{3, 5, 6}
    );

    public static final ExpectedMinimizedValue cycle2_solution = new ExpectedMinimizedValue(
            new String[]{"01*", "1*0", "*01"});

    public static final MinimizationData cycle3 = new MinimizationData(
            "Cycle",
            "Cycle",
            3,
            new long[]{1, 2, 4},
            null,
            new long[]{0, 5, 6}
    );

    public static final ExpectedMinimizedValue cycle3_solution = new ExpectedMinimizedValue(
            new String[]{"**0", "*0*"});

    public static final MinimizationData cycle4 = new MinimizationData(
            "Cycle",
            "Cycle",
            3,
            new long[]{1, 2, 4},
            null,
            new long[]{0, 3, 6}
    );

    public static final ExpectedMinimizedValue cycle4_solution = new ExpectedMinimizedValue(
            new String[]{"**0", "0**"});

    public static final MinimizationData cycle5 = new MinimizationData(
            "Cycle",
            "Cycle",
            3,
            new long[]{1, 2, 4},
            null,
            new long[]{0, 3, 5}
    );

    public static final ExpectedMinimizedValue cycle5_solution = new ExpectedMinimizedValue(
            new String[]{"0**", "*0*"});

    public static final MinimizationData chumma1 = new MinimizationData(
            "Chumma 1",
            "Chumma 1",
            3,
            new long[]{7},
            null,
            null
    );

    public static final ExpectedMinimizedValue chumma1_solution = new ExpectedMinimizedValue(
            new String[]{"111"});


    public static final MinimizationData chummaCyclic = new MinimizationData(
            "Chumma 2",
            "Chumma 2",
            4,
            new long[]{7, 11, 14, 10, 13, 9, 12},
            null,
            new long[]{6}
    );

    public static final ExpectedMinimizedValue chummaCyclic_solution = new ExpectedMinimizedValue(12);

    public static final MinimizationData chummaSecond = new MinimizationData(
            "Chumma 2",
            "Chumma 2",
            4,
            new long[]{7, 11, 14, 13, 15, 10, 9, 12},
            null,
            null
    );

    public static final ExpectedMinimizedValue chummaSecond_solution = new ExpectedMinimizedValue(
            new String[]{"1*1*", "*111",  "1**1", "11**"});


    public static final MinimizationData try1 = new MinimizationData(
            "try 1",
            "try 1",
            3,
            new long[]{1, 4, 2, 5, 0, 3, 6},
            null,
            null
    );

    public static final ExpectedMinimizedValue try1_solution = new ExpectedMinimizedValue(
            new String[]{"**0", "0**", "*0*"});

    public static final MinimizationData error1 = new MinimizationData(
            "Error",
            "Infinite loop",
            4,
            new long[]{1, 2, 3, 4, 5, 6, 8, 9, 10, 11, 12, 13, 14, 15},
            null,
            null
    );

    public static final ExpectedMinimizedValue error1_solution = new ExpectedMinimizedValue(
            new String[]{"**0", "0**", "*0*"});

}
