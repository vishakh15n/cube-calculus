package edu.pdx.vishakh.cubecalculus.tools;

import java.io.PrintStream;

import edu.pdx.vishakh.cubecalculus.core.BooleanCube;
import edu.pdx.vishakh.cubecalculus.core.CubeCalculusException;
import edu.pdx.vishakh.cubecalculus.core.CubeUtils;
import edu.pdx.vishakh.cubecalculus.core.Minterm;

import java.util.*;

public class KarnaughMapGenerator {
    private final int nVars;
    private final boolean showNumber;

    /** Bit patterns (in Gray code order) of length size. */
    private List<String> bitPatternList;

    /** Bit patterns that go from left to right. */
    private List<String> topList;

    /** Bit patterns that appear from top to bottom. */
    private List<String> leftList;

    /** Bit pattern to number map. */
    private Map<String, Integer> bitPatternMap = new HashMap<>();

    /** Bit pattern to column position */
    private Map<String, Integer> topMap  = new HashMap<>();

    /** Bit pattern to row position */
    private Map<String, Integer> leftMap  = new HashMap<>();

    /** Colors used to indicate different covers. */
    private String[] colors = {
            "ColorM",
            "ColorU",
            "ColorF",
            "ColorC",
            "ColorO",
            "ColorJ",
            // "ColorZA",
            "ColorZI",
            // "ColorZF",
            "ColorB",
    };

    private int numberOfHorizontalVariables = 2;
    private int numberOfVerticalVariables;
    private int numberOfRows;
    private int numberOfColumns;

    private KarnaughMapGenerator(int nVars, boolean showNumber) {
        this.nVars = nVars;
        this.showNumber = showNumber;

        bitPatternList = GrayCodeGenerator.generate(nVars);
        topList = GrayCodeGenerator.generate(2);

        // Copying the list since it has to be reversed.
        leftList = new ArrayList<>(GrayCodeGenerator.generate(nVars - 2));

        for (int i = 0; i < bitPatternList.size(); ++i) {
            bitPatternMap.put(bitPatternList.get(i), i);
        }

        for (int i = 0; i < topList.size(); ++i) {
            topMap.put(topList.get(i), i);
        }

        for (int i = 0; i < leftList.size(); ++i) {
            leftMap.put(leftList.get(i), i);
        }

        Collections.reverse(leftList);

    }

    public static KarnaughMapGenerator createKarnaughMapGenerator(int nVars, boolean showNumber) {
        if (nVars < 3 || nVars > 10) {
            return null;
        } else {
            return new KarnaughMapGenerator(nVars, showNumber);
        }
    }

    public void printInput(PrintStream out, Set<BooleanCube> onSet, Set<BooleanCube> offSet, Set<BooleanCube> dcSet) {
        printKmapHeader(out);
        printKmapSkeleton(out, onSet, offSet, dcSet);
        printKmapFooter(out, "Input");

    }

    public void generateWithMinterms(PrintStream out, Set<BooleanCube> covers,
                                     Set<BooleanCube> onSet, Set<BooleanCube> offSet, Set<BooleanCube> dcSet) {
        printKmapHeader(out);
        printKmapSkeleton(out, onSet, offSet, dcSet);
        String formula = printCovers(out, covers);
        printKmapFooter(out, "k-map showing the covers "+ formula);

    }

    public void generate(PrintStream out, Set<BooleanCube> covers) {
        printKmapHeader(out);
        printKmapSkeleton(out, null, null, null);
        String formula = printCovers(out, covers);
        printKmapFooter(out, "k-map showing the covers "+ formula);
    }

    private void printKmapSkeleton(PrintStream out, Set<BooleanCube> onSet, Set<BooleanCube> offSet, Set<BooleanCube> dcSet) {
        numberOfVerticalVariables = nVars - numberOfHorizontalVariables;
        numberOfRows = 1 << numberOfVerticalVariables;
        numberOfColumns = 1 << numberOfHorizontalVariables;

        double xMin = 0.0;
        double xMax = numberOfColumns;
        double yMax = numberOfRows;
        double yMin = 0.0;

        StringBuilder sb = new StringBuilder();
        sb.append("$");
        int i;
        for (i = 0; i < numberOfVerticalVariables; ++i) {
            sb.append(String.format("x_{%d}", i+1));
        }
        sb.append("$");
        String leftVars = sb.toString();

        sb = new StringBuilder();
        sb.append("$");
        for (; i < nVars; ++i) {
            sb.append(String.format("x_{%d}", i+1));
        }
        sb.append("$");
        String topVars = sb.toString();

        // Draw horizontal lines.
        for (double y = yMin; y <= yMax; y += 1.0) {
            out.printf("  \\draw [very thick] (%.1f, %.1f) -- (%.1f, %.1f);\n", xMin, y, xMax, y);
        }

        // Draw vertical lines.
        for (double x = xMin; x <= xMax; x += 1.0) {
            out.printf("  \\draw [very thick] (%.1f, %.1f) -- (%.1f, %.1f);\n", x, yMin, x, yMax);
        }

        // Draw the slanted line.
        out.printf("  \\draw [very thick] (%.1f, %.1f) -- (%.1f, %.1f);\n", 0.0, yMax, -0.5, yMax + 0.5);

        // Write the variables.
        out.printf("  \\node at (%.2f, %.2f) [anchor=east] {%s};\n", xMin - 0.2, yMax + 0.15, leftVars);
        out.printf("  \\node at (%.2f, %.2f) [anchor=west] {%s};\n", xMin - 0.2, yMax + 0.35, topVars);


        // Write the bitpatterns on left.
        for (int var = 0; var < numberOfRows; ++var) {
            out.printf("\\node at (%.2f, %.2f) {\\bitp{%s}};\n", xMin - 0.2, var + 0.5, leftList.get(var));
        }

        // Write the bitpatterns on top.
        for (int var = 0; var < numberOfColumns  ; ++var) {
            out.printf("\\node at (%.2f, %.2f) {\\bitp{%s}};\n", var + 0.5, (yMax + 0.2), topList.get(var));
        }

        // Show number in each cell.
        for (int index = 0; index < (1 << nVars); ++index) {
            Integer[] coOrdinates = getCoordinatesForANumber(index);
            if (showNumber) {
                out.printf("\\node at (%.2f, %.2f) [anchor=south west] {{\\scriptsize %d}};\n",
                        coOrdinates[0] + 0.0, coOrdinates[1] + 0.0, coOrdinates[2]);
            }
            Minterm m = new Minterm(nVars, coOrdinates[2]);
            BooleanCube c = BooleanCube.fromMinterm(m);
            if (onSet != null && onSet.contains(c)) {
                out.printf("\\node at (%.2f, %.2f) {%s};\n",
                        coOrdinates[0] + 0.5, coOrdinates[1] + 0.5, "1");
            }
            else if (offSet != null && offSet.contains(c)) {
                out.printf("\\node at (%.2f, %.2f) {%s};\n",
                        coOrdinates[0] + 0.5, coOrdinates[1] + 0.5, "0");
            }
            else if (dcSet != null && dcSet.contains(c)) {
                out.printf("\\node at (%.2f, %.2f) {%s};\n",
                        coOrdinates[0] + 0.5, coOrdinates[1] + 0.5, "*");
            }
        }

    }

    private void printKmapFooter(PrintStream out, String caption) {
        out.println("\\end{tikzpicture}");
        if (caption != null) {
            out.printf("\\caption{%s}\n", caption);
        }
        out.println("\\end{table}");
    }

    private void printKmapHeader(PrintStream out) {
        out.println("\\begin{table}[H]");
        out.println("\\centering");
        out.println("\\begin{tikzpicture}[xscale=3.0,yscale=2.0]");
    }

    private String printCovers(PrintStream out, Set<BooleanCube> covers) {

        ArrayList<ArrayList<Integer>> coverArray = new ArrayList<>();
        for (int i = 0; i < (1 << nVars); ++i) {
            coverArray.add(null);
        }

        StringBuilder coverFormula = new StringBuilder("$");
        boolean first = true;

        double interval = 0.05;

        int id = 0;
        int parity = -1;
        for (BooleanCube cover : covers) {
            ++id;
            parity *= -1;
            if (first) {
                first = false;
            } else {
                coverFormula.append(" + ");
            }
            coverFormula.append(String.format("{\\color{%s} %s}", colors[id % colors.length], cover.toAlgebraicString()));
            Set<BooleanCube> minterms = CubeUtils.getMinterms(cover);

            // From the set of cubes, create a list of cubes in the order, for drawing.
            List<BooleanCube> mintermList = new CubeListNormalizer(nVars, minterms).normalize();

            double firstX = 0.0;
            double firstY = 0.0;
            String firstXy = null;

            double lastX = 0.0;
            double lastY = 0.0;
            String lastXy = null;
            int iid = (id % 10) * parity;
            String color = colors[id % colors.length];
            for (BooleanCube m : mintermList) {
                String s = m.toPlainNumericString();
                Integer[] xAndY = getCoordinatesFromBitPattern(s);
                Integer x = xAndY[0];
                Integer y = xAndY[1];
                if (y == null || x == null) {
                    throw new CubeCalculusException("Invalid cover.");
                }
                int index = 4 * y + x;

                double thisX = x + 0.5 + interval * iid;
                double thisY = y + 0.5 - interval * iid;
                String thisXy = String.format("%.2f, %.2f", thisX, thisY);

                out.printf("  \\node at (%s, %.2f) {{\\color{%s}\\circled{\\small\\texttt{%d}}}};\n", thisX, thisY, color, id);

                if (lastXy == null) {
                    firstX = thisX;
                    firstY = thisY;
                    firstXy = String.format("%.2f, %.2f", firstX, firstY);
                } else {
                    printLine(out, color, lastX, lastY, lastXy, thisX, thisY, thisXy);
                }

                lastX = thisX;
                lastY = thisY;
                lastXy = thisXy;

                ArrayList<Integer> coverValues = coverArray.get(index);
                if (coverValues == null) {
                    coverValues = new ArrayList<>();
                    coverArray.set(index, coverValues);
                }
                coverValues.add(id);
            }
            printLine(out, color, lastX, lastY, lastXy, firstX, firstY, firstXy);
        }
        coverFormula.append("$");

        return coverFormula.toString();
    }

    private void printLine(PrintStream out, String color, double lastX, double lastY, String lastXy, double thisX, double thisY, String thisXy) {
        if (Math.abs(thisX - lastX) > 1.5 || Math.abs(thisY - lastY) > 1.5) {
            out.printf("\\draw [%s,thin,dashed] (%s) -- (%s);\n", color, lastXy, thisXy);
        } else {
            out.printf("\\draw [%s,thin] (%s) -- (%s);\n", color, lastXy, thisXy);
        }
    }

    /** Gets the co-ordinates of the lower left corner of the cell showing the minterm of given index. */
    private Integer[] getCoordinatesForANumber(int number) {
        return getCoordinatesFromBitPattern(bitPatternList.get(number));
    }
    private Integer[] getCoordinatesFromBitPattern(String bitPattern) {
        String firstPart = bitPattern.substring(0, nVars - 2);
        String secondPart = bitPattern.substring(nVars - 2, nVars);
        Integer y = leftMap.get(firstPart);
        Integer x = topMap.get(secondPart);
        if (y == null || x == null) {
            throw new CubeCalculusException("Invalid cover.");
        }
        return new Integer[] {x, numberOfRows - 1 - y, Integer.parseInt(bitPattern, 2)};
    }

}
