package edu.pdx.vishakh.cubecalculus.tooltests;

import edu.pdx.vishakh.cubecalculus.core.BooleanCube;
import edu.pdx.vishakh.cubecalculus.core.TestUtils;
import edu.pdx.vishakh.cubecalculus.tools.KarnaughMapGenerator;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.Set;

public class KarnaughMapGeneratorTest {
    public static void main(String[] args) throws FileNotFoundException {
        PrintStream out = TestUtils.createPrintStream("temp");
        testThreeVariables(out);
        testFourVariables(out);
        testFiveVariables(out);
    }

    private static void testThreeVariables(PrintStream out) {
        Set<BooleanCube> covers = new HashSet<>();
        covers.add(BooleanCube.fromString("111"));
        covers.add(BooleanCube.fromString("00*"));
        covers.add(BooleanCube.fromString("*00"));
        KarnaughMapGenerator.createKarnaughMapGenerator(3, true).generate(out, covers);
    }

    private static void testFourVariables(PrintStream out) {
        Set<BooleanCube> covers = new HashSet<>();
        covers.add(BooleanCube.fromString("000*"));
        covers.add(BooleanCube.fromString("01*1"));
        covers.add(BooleanCube.fromString("111*"));
        covers.add(BooleanCube.fromString("1*10"));
        KarnaughMapGenerator.createKarnaughMapGenerator(4, true).generate(out, covers);
    }

    private static void testFiveVariables(PrintStream out) {
        Set<BooleanCube> covers = new HashSet<>();
        covers.add(BooleanCube.fromString("0*00*"));
        covers.add(BooleanCube.fromString("00**1"));
        covers.add(BooleanCube.fromString("***01"));
        covers.add(BooleanCube.fromString("01*10"));
        covers.add(BooleanCube.fromString("0*1*1"));
        KarnaughMapGenerator.createKarnaughMapGenerator(5, true).generate(out, covers);
    }

}
