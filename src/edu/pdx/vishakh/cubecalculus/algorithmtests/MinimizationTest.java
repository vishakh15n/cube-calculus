package edu.pdx.vishakh.cubecalculus.algorithmtests;

import edu.pdx.vishakh.cubecalculus.algorithms.QuineMcCluskeyAlgorithm;
import edu.pdx.vishakh.cubecalculus.core.BooleanCube;
import edu.pdx.vishakh.cubecalculus.core.CubeUtils;
import edu.pdx.vishakh.cubecalculus.core.TestUtils;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Set;

public class MinimizationTest {
    String dir = System.getProperty("user.home") + "/projects/cube-calculus/tests/";

    private long[] biswasON = {0, 1, 3, 5, 7, 8, 9, 10, 13, 14, 15, 17, 21, 25, 29};
    private long[] wikipediaON = {4, 8, 10, 11, 12, 15};
    private long[] wikipediaDC = {9, 14};
    private long[] perkowskibook1 = {0, 1, 4, 7, 9, 11, 13, 16, 18, 20, 22, 23, 26, 27, 29, 31};
    private long[] basirON = {0, 3, 5, 6, 7, 10, 12, 13};
    private long[] basirDC = {2, 9, 15};
    private long[] test1 = {6, 7};
    private long [] test2 = {0, 1, 5, 7, 8, 10, 14, 15};
    private long[] test3 = {0, 2, 4, 5, 7};



    public void testQuineMcCluskey() throws FileNotFoundException {
        runQMTest("$f_1$", 5, biswasON, null, null, "qm-f1.tex");
        runQMTest("$f_2$", 5, perkowskibook1, null, null, "qm-f2.tex");
        runQMTest("$f_3$", 4, wikipediaON, null, wikipediaDC, "qm-f3.tex");
        runQMTest("$f_4$", 4, basirON, null, basirDC, "qm-f4.tex");
        runQMTest("$f_5$", 4, test1, null, null, "qm-f5.tex");
        runQMTest("$f_6$", 4, test2, null, null, "qm-f5.tex");
        runQMTest("$f_7$", 4, test3, null, null, "qm-f5.tex");
    }

    private void runQMTest(String testName, int nVars,
                           long[] onCubesAsNumber,
                           long[] offCubesAsNumber,
                           long[] dcCubesAsNumber,
                           String outputFileName) throws FileNotFoundException {

        Set<BooleanCube> onCubes = onCubesAsNumber == null ? null : CubeUtils.cubeSetFromNumberArray(nVars, onCubesAsNumber);
        Set<BooleanCube> offCubes = offCubesAsNumber == null ? null : CubeUtils.cubeSetFromNumberArray(nVars, offCubesAsNumber);
        Set<BooleanCube> dcCubes = dcCubesAsNumber == null ? null : CubeUtils.cubeSetFromNumberArray(nVars, dcCubesAsNumber);

        PrintStream ps = new PrintStream(new FileOutputStream(dir + outputFileName));
        QuineMcCluskeyAlgorithm qmAlg = new QuineMcCluskeyAlgorithm(ps, System.out);


        TestUtils.printCubes(ps, onCubes, testName + ": Quine-McCluskey Algorithm: Input Cubes");

        Set<BooleanCube> primeCubes = qmAlg.minimize(nVars, onCubes, offCubes, dcCubes, null, false);
        TestUtils.printCubes(ps, primeCubes, testName + ": Quine-McCluskey Algorithm: Minimized Prime Cubes");

        ps.close();
    }


    public static void main(String[] args) throws FileNotFoundException {
        MinimizationTest test = new MinimizationTest();
        test.testQuineMcCluskey();
    }
}
