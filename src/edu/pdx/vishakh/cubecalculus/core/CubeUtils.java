package edu.pdx.vishakh.cubecalculus.core;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class CubeUtils {
    /** Converts an array of integers representing the bit patterns of minterms into a set of cubes. */
    public static Set<BooleanCube> cubeSetFromNumberArray(int nVars, long[] minterms) {
        Set<BooleanCube> cubes = new HashSet<>();
        for (long minterm : minterms) {
            Minterm m = new Minterm(nVars, minterm);
            BooleanCube c = BooleanCube.fromMinterm(m);
            if (c == null) {
                return null;
            }
            cubes.add(c);
        }
        return cubes;
    }

    /** Converts an array of Strings representing the bit patterns of minterms into a set of cubes. */
    public static Set<BooleanCube> cubeSetFromStringArray(String[] cubesAsString) {
        Set<BooleanCube> cubes = new HashSet<>();
        for (String s : cubesAsString) {
            BooleanCube c = BooleanCube.fromString(s);
            if (c == null) {
                return null;
            }
            cubes.add(c);
        }
        return cubes;
    }

    /** Returns the cubes adjacent to {@Code cube} that belong to the set {@Code setToCheck}. */
    public static Set<BooleanCube> getAdjacentCubesIn(BooleanCube cube, Set<BooleanCube> setToCheck) {
        int nVars = cube.getNumberOfVariables();
        Set<BooleanCube> adjacentCubes = new HashSet<>();
        for (int i = 0; i < nVars; ++i) {
            if (!cube.isDontCareVar(i)) {
                BooleanCube adjCube = new BooleanCube(cube);
                adjCube.flipVar(i);
                if (setToCheck.contains(adjCube)) {
                    adjacentCubes.add(adjCube);
                }
            }
        }
        return adjacentCubes;

    }

    /** Returns the cubes adjacent to {@Code cube} that do not belong to the set {@Code setToCheck}. */
    public static Set<BooleanCube> getAdjacentCubesWithMintermsNotIn(BooleanCube cube, Set<BooleanCube> setToCheck) {
        int nVars = cube.getNumberOfVariables();
        Set<BooleanCube> adjacentCubes = new HashSet<>();
        for (int i = 0; i < nVars; ++i) {
            if (!cube.isDontCareVar(i)) {
                BooleanCube adjCube = new BooleanCube(cube);
                adjCube.flipVar(i);
                Set<BooleanCube> minterms = getMinterms(adjCube);
                boolean isValid = true;
                for (BooleanCube m : minterms) {
                    if (setToCheck.contains(m)) {
                        isValid = false;
                        break;
                    }
                }
                if (isValid) {
                    adjacentCubes.add(adjCube);
                }
            }
        }
        return adjacentCubes;

    }


    /** Returns all minterms (0-order cubes) contained {@Code cube}. */
    public static Set<BooleanCube> getMinterms(BooleanCube cube) {
        Set<BooleanCube> cubes = new HashSet<>();

        addMintermsToSet(cubes, cube);
        return cubes;
    }


    /** Recursively finds minterms and adds to the given set. */
    private static void addMintermsToSet(Set<BooleanCube> cubes, BooleanCube cube) {
        int nVars = cube.getNumberOfVariables();

        boolean isMinterm = true;
        for (int i = 0; i < nVars; ++i) {
            if (cube.isDontCareVar(i)) {
                isMinterm = false;

                BooleanCube c1 = new BooleanCube(cube);
                c1.makeOnVar(i);
                addMintermsToSet(cubes, c1);

                BooleanCube c2 = new BooleanCube(cube);
                c2.makeOffVar(i);
                addMintermsToSet(cubes, c2);
                break;
            }
        }

        if (isMinterm) {
            cubes.add(cube);
        }
    }

    public static boolean isCubeACover(BooleanCube cube, Set<BooleanCube> onCubes, Set<BooleanCube> offCubes, Set<BooleanCube> dcCubes) {
        if (cube.getOrder() == 0) {
            if (onCubes.contains(cube)) {
                return true;
            }
            if (offCubes != null && offCubes.contains(cube)) {
                return false;
            }
            if (dcCubes != null && dcCubes.contains(cube)) {
                return true;
            }

            return offCubes == null ? false : true;
        }
        Set<BooleanCube> nextCubes = getNextOrderCubes(cube);
        for (BooleanCube c : nextCubes) {
            if (!isCubeACover(c, onCubes, offCubes, dcCubes)) {
                return false;
            }
        }
        return true;
    }

    // Returns the two cubes obtained by replacing the first DONTCARE with OFF and ON.
    private static Set<BooleanCube> getNextOrderCubes(BooleanCube c) {
        int nVars = c.getNumberOfVariables();
        Set<BooleanCube> cubes = new HashSet<>();
        for (int i = 0; i < nVars; ++i) {
            if (c.isDontCareVar(i)) {
                BooleanCube c1 = new BooleanCube(c);
                c1.makeOnVar(i);
                cubes.add(c1);

                BooleanCube c2 = new BooleanCube(c);
                c2.makeOffVar(i);
                cubes.add(c2);
                break;
            }
        }
        return cubes;
    }

    public static String cubeSetToNumericString(Set<BooleanCube> cubes) {
        ArrayList<BooleanCube> cubeList = new ArrayList<>(cubes);
        cubeList.sort(Comparator.comparing(BooleanCube::toPlainNumericString));

        StringBuilder numeric = new StringBuilder();
        numeric.append("[ ");
        for (BooleanCube minterm : cubeList) {
            numeric.append(minterm.toNumericString());
            numeric.append(" ");
        }
        numeric.append("]");
        return numeric.toString();
    }


    public static String cubeSetToPlainNumericString(Set<BooleanCube> cubes) {
        List<BooleanCube> cubeList = new ArrayList<>(cubes);
        cubeList.sort(Comparator.comparing(BooleanCube::toPlainNumericString));
        StringBuilder numeric = new StringBuilder();
        numeric.append("[ ");
        for (BooleanCube minterm : cubeList) {
            numeric.append(minterm.toPlainNumericString());
            numeric.append(" ");
        }
        numeric.append("]");
        return numeric.toString();
    }

    public static String cubeSetSetToPlainNumericString(Set<Set<BooleanCube>> sets) {
        StringBuilder sb = new StringBuilder("[ ");
        boolean first = true;
        for (Set<BooleanCube> set : sets) {
            if (first) {
                first = false;
            } else {
                sb.append(", ");
            }
            sb.append(cubeSetToPlainNumericString(set));
        }
        sb.append(" ]");
        return sb.toString();
    }


    public static String cubeSetToAlgebraicString(Set<BooleanCube> cubes) {
        StringBuilder numeric = new StringBuilder();
        boolean first = true;
        for (BooleanCube minterm : cubes) {
            if (first) {
                first = false;
            } else {
                numeric.append(" + ");
            }
            numeric.append(minterm.toAlgebraicString());
        }
        return numeric.toString();
    }



    // TESTS.

    private static long[] BISWAS_MINTERMS = {0, 1, 3, 5, 7, 8, 9, 10, 13, 14, 15, 17, 21, 25, 29};

    public static void main(String[] args) {
        boolean latex = false;
        testAddToSetInMap(latex);
        testIsCubeACover(latex);
        testGetMinterms(latex);
    }

    private static void testGetMinterms(boolean latex) {

        testMintermsFor("10110", 1, latex);
        testMintermsFor("101*0", 2, latex);
        testMintermsFor("*1*01", 4, latex);
        testMintermsFor("01***", 8, latex);
        testMintermsFor("***0*", 16, latex);
    }

    private static void testMintermsFor(String stringRep, int nMinterms, boolean latex) {
        Set<BooleanCube> minterms = getMinterms(BooleanCube.fromString(stringRep));
        assert minterms.size() == nMinterms;
        if (latex) {
            TestUtils.printCubes(System.out, minterms, "Minterms of " + stringRep);
        }
    }

    private static void testIsCubeACover(boolean latex) {
        Set<BooleanCube> onCubes = cubeSetFromNumberArray(5, BISWAS_MINTERMS);

        TestUtils.assertTrue(isCubeACover(BooleanCube.fromString("00001"), onCubes, null, null), "Test 1");
        TestUtils.assertTrue(isCubeACover(BooleanCube.fromString("00001"), onCubes, null, null), "Test 2");
        TestUtils.assertTrue(isCubeACover(BooleanCube.fromString("01*10"), onCubes, null, null), "Test 3");
        TestUtils.assertTrue(isCubeACover(BooleanCube.fromString("0*00*"), onCubes, null, null), "Test 4");
        TestUtils.assertTrue(isCubeACover(BooleanCube.fromString("***01"), onCubes, null, null), "Test 5");

        TestUtils.assertNotTrue(isCubeACover(BooleanCube.fromString("11010"), onCubes, null, null), "Test 6");
        TestUtils.assertNotTrue(isCubeACover(BooleanCube.fromString("*1010"), onCubes, null, null), "Test 7");
        TestUtils.assertNotTrue(isCubeACover(BooleanCube.fromString("*11*1"), onCubes, null, null), "Test 8");
        TestUtils.assertNotTrue(isCubeACover(BooleanCube.fromString("***00"), onCubes, null, null), "Test 9");
        TestUtils.assertNotTrue(isCubeACover(BooleanCube.fromString("0****"), onCubes, null, null), "Test 10");


    }

    private static void testAddToSetInMap(boolean latex) {
        Map<Integer, Set<BooleanCube>> map1 = new HashMap<>();

        Set<BooleanCube> s5, s7;

        BooleanCube c1 = BooleanCube.fromString("0001");
        SetUtils.addToSetInMap(5, c1, map1);
        assert map1.size() == 1;
        s5 = map1.get(5);
        assert(s5.size() == 1);
        assert s5.contains(c1);

        BooleanCube c2 = BooleanCube.fromString("1001");
        SetUtils.addToSetInMap(5, c2, map1);
        assert map1.size() == 1;
        s5 = map1.get(5);
        assert s5.size() == 2;
        assert s5.contains(c1);
        assert s5.contains(c2);

        BooleanCube c3 = BooleanCube.fromString("1101");
        SetUtils.addToSetInMap(7, c3, map1);
        assert map1.size() == 2;
        s5 = map1.get(5);
        s7 = map1.get(7);
        assert s5.size() == 2;
        assert s5.contains(c1);
        assert s5.contains(c2);
        assert s7.size() == 1;
        assert s7.contains(c3);

    }

    public static String cubeSetToString(Set<BooleanCube> cubes) {
        List<String> array = cubes.stream()
                .map(c -> c.toNumericString())
                .collect(Collectors.toList());
        array.sort((Comparator) (o1, o2) -> {
            String s1 = (String) o1;
            String s2 = (String) o2;
            return s1.compareTo(s2);
        });
        StringBuilder sb = new StringBuilder();
        for (String minterm : array) {
            sb.append("\\bitp{");
            sb.append(minterm);
            sb.append("} ");
        }
        return sb.toString();
    }

    public static long totalCostOfCubes(Set<BooleanCube> cubes) {
        long cost = 0;
        for (BooleanCube c : cubes) {
            cost += c.cost();
        }
        return cost;
    }

    // Lists a boolean function in the sigma notation.
    public static String functionString(Set<BooleanCube> onSet, Set<BooleanCube> dcSet) {
        StringBuilder sb = new StringBuilder();
        sb.append("\\sum ");
        if (onSet != null) {
            sb.append("m(");
            List<String> onList = onSet.stream()
                    .sorted(Comparator.comparing(BooleanCube::numericValue))
                    .map(c -> String.valueOf(c.numericValue()))
                    .collect(Collectors.toList());

            sb.append(String.join(" ,", onList));
            sb.append(")");
        }
        if (dcSet != null) {
            sb.append(", d(");
            List<String> dcList = dcSet.stream()
                    .sorted(Comparator.comparing(BooleanCube::numericValue))
                    .map(c -> String.valueOf(c.numericValue()))
                    .collect(Collectors.toList());

            sb.append(String.join(" ,", dcList));
            sb.append(")");
        }
        return sb.toString();
    }
}
