package edu.pdx.vishakh.cubecalculus.algorithmtests;

import edu.pdx.vishakh.cubecalculus.algorithms.MinimizationAlgorithm;
import edu.pdx.vishakh.cubecalculus.algorithms.QuineMcCluskeyAlgorithm;
import edu.pdx.vishakh.cubecalculus.core.BooleanCube;
import edu.pdx.vishakh.cubecalculus.core.CubeUtils;

import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Set;

public class AllMinimizationTests {
    public static void main(String[] args) {
        PrintStream out = System.out;
        String[] tests = {
                "brayton1",
                "wikipedia",
                "biswasExample1",
                "perkowskiStaircase",
                "perkowskiCyclic",
                "perkowskiBookExample1",
                "perkowskiBookExample2",
                "biswasCyclic",
                "perkowskiBookExample3",
                "rothExample1",
                "cyclic1",

                "allMinterms",
                "randomTest1",

                "roth2",
                "roth3",
                "roth4",
                "test1",
                "test2",

                "brown1",
                "brown2",
                "brown3",
                "brown4",
                "brown5",
                "diff1",

                "cycle1",
                "cycle2",
                "cycle3",
                "cycle4",
                "cycle5",
                "chumma1",
                "chummaCyclic",
                "chummaSecond",
                "try1"

        };

        MinimizationAlgorithm[] algorithms = {
                new QuineMcCluskeyAlgorithm(null, null)
        };

        Class c = MinimizationTestData.class;

        int testNo = 0;
        for (String test : tests) {
            try {
                String solution = test + "_solution";
                Field testField = c.getDeclaredField(test);
                Field solutionField = c.getDeclaredField(solution);
                MinimizationTestData.MinimizationData input = (MinimizationTestData.MinimizationData) testField.get(null);
                MinimizationTestData.ExpectedMinimizedValue expectedOutput = (MinimizationTestData.ExpectedMinimizedValue) solutionField.get(null);
                out.printf("\nTest %d : %s (%s)\n", ++testNo, test, input.shortDescription);
                out.printf("    Input (%d variables) : ON = %s", input.nVars, Arrays.toString(input.onMinterms));
                if (input.offMinterms != null) {
                    out.printf(", OFF = %s", Arrays.toString(input.onMinterms));
                }
                if (input.dcMinterms != null) {
                    out.printf(", DC = %s", Arrays.toString(input.dcMinterms));
                }
                out.println();
                Set<Set<BooleanCube>> values = expectedOutput.getValues();
                int vsize = values.size();
                if (vsize == 0) {
                    out.println("    Expected cost = " + expectedOutput.getCost());
                } else if (vsize == 1) {
                    Set<BooleanCube> value = expectedOutput.getUniqueValue();
                    out.println("    Expected value = " + CubeUtils.cubeSetToPlainNumericString(value) + " (Cost = " + CubeUtils.totalCostOfCubes(value) + ")");
                } else {
                    Set<BooleanCube> value = values.iterator().next();
                    out.println("    Expected = " + CubeUtils.cubeSetSetToPlainNumericString(values) + " (Cost = " + CubeUtils.totalCostOfCubes(value) + ")");
                }
                for (MinimizationAlgorithm alg : algorithms) {
                    out.println("    " + alg.getShortName() + " : " + MinimizationTestData.validate(alg, input, expectedOutput));
                }
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

    }
}
