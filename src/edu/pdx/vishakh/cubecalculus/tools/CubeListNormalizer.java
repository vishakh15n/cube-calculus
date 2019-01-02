package edu.pdx.vishakh.cubecalculus.tools;

import edu.pdx.vishakh.cubecalculus.core.BooleanCube;
import edu.pdx.vishakh.cubecalculus.core.CubeCalculusException;
import edu.pdx.vishakh.cubecalculus.core.CubeUtils;

import java.util.*;

/**
 * Creates a List<BooleanCube> from a Set<BooleanCube> so that adjacent cubes are in adjacent positions.
 */
public class CubeListNormalizer {
    private final Set<BooleanCube> cubes;
    private final int nVars;
    private final int cubeSize;
    private final int order;
    private final HashSet<Integer> allGraycodesForOrder;
    Map<Integer, BooleanCube> grayCodeToCubeMap = new HashMap<>();
    Map<BooleanCube, Integer> cubeToGrayCodeMap = new HashMap<>();
    Set<BooleanCube> considered = new HashSet<>();
    Queue<BooleanCube> cubeQueue;

    public CubeListNormalizer(int nVar, Set<BooleanCube> cubes) {
        this.nVars = nVar;
        this.cubes = cubes;
        cubeSize = cubes.size();
        order = getOrder(cubeSize);
        allGraycodesForOrder = new HashSet<>();
        for (int i = 0; i < cubeSize; ++i) {
            allGraycodesForOrder.add(i);
        }

        debug("Number of cubes = " + cubeSize);
        debug("Order = " + order);
    }

    // Given a power of two, finds the power.
    private int getOrder(int powerOfTwo) {
        if ((powerOfTwo & (powerOfTwo - 1)) != 0) {
            throw new CubeCalculusException("Number of cubes is  not a power of two.");
        }
        int order = 0;
        while (powerOfTwo > 1) {
            ++order;
            powerOfTwo >>= 1;
        }
        return order;
    }

    public List<BooleanCube> normalize() {
        List<BooleanCube> result = new ArrayList<>();

        // Handle single-element case.
        if (cubeSize == 1) {
            result.add(cubes.iterator().next());
            return result;
        }

        cubeQueue = new LinkedList<>();
        if (!cubes.isEmpty()) {
            BooleanCube c = cubes.iterator().next();
            // grayCodeMap.put(c, 0, c);
            addCubeToQueue(c, 0);
        }

        while (!cubeQueue.isEmpty()) {

            // Process next vertex in the group that was assigned a graycode.
            BooleanCube c = cubeQueue.poll();
            Integer graycode = cubeToGrayCodeMap.get(c);
            debug("Processing cube " + c.toNumericString() + " with graycode " + graycode);

            // Create a set of possible graycodes for its neighbors.
            Set<Integer> candidateGraycodes = getNextGrayCodes(graycode);

            debug("Possible next graycodes = " + candidateGraycodes);

            // Find its neighbors that are not already considered.
            Set<BooleanCube> neighbors = findNeighbors(c);
            debug("Neighbors = " + CubeUtils.cubeSetToNumericString(neighbors));
            neighbors.removeAll(considered);
            debug("Neighbors not considered = " + CubeUtils.cubeSetToNumericString(neighbors));

            // Assign a graycode to each of the unconsdered neighbors.
            for (BooleanCube neighbor : neighbors) {
                debug("Checking the neighbor " + neighbor.toNumericString());

                // Get the neighbor's neighbors who are already considered.
                Set<BooleanCube> secondneighbors = findNeighbors(neighbor);
                secondneighbors.retainAll(considered);

                // No need to check the current node again.
                secondneighbors.remove(c);

                // Start with all graycodes next to the current cube.
                Set<Integer> possibleGraycodes = new HashSet<>(candidateGraycodes);

                // Restrict possible graycodes for each of these second neighbors.
                for (BooleanCube secondNeighbor : secondneighbors) {
                    Integer g = cubeToGrayCodeMap.get(secondNeighbor);
                    Set<Integer> nextGraycodes = getNextGrayCodes(g);
                    possibleGraycodes.retainAll(nextGraycodes);
                }

                // We should have at least one possible graycode, otherwise there is no cycle.
                if (possibleGraycodes.isEmpty()) {
                    // Report error if this is not the last cube.
                    if (considered.size() < cubeSize - 1) {
                        throw new CubeCalculusException("Cannot find cycle in graycodes");
                    }
                }

                // Take the first graycode from the feasible sets.
                Integer neighborsGraycode = possibleGraycodes.iterator().next();

                // This graycode should no longer a candidate.
                candidateGraycodes.remove(neighborsGraycode);

                // Add the cube to the queue, for further processing, with the graycode.
                addCubeToQueue(neighbor, neighborsGraycode);
            }
        }

        // Traverse the cubes in the graycode order.
        List<String> graycodes = GrayCodeGenerator.generate(order);

        for (String graycode : graycodes) {
            debug(graycode);
            int gc = Integer.parseInt(graycode, 2);
            result.add(grayCodeToCubeMap.get(gc));
        }

        return result;
    }

    private void addCubeToQueue(BooleanCube cube, Integer graycode) {
        assignGrayCode(cube, graycode);
        cubeQueue.add(cube);
        considered.add(cube);
        debug("Adding cube " + cube.toNumericString() + " with graycode " + graycode);
        debug("Queue = " + cubeQueue);
    }

    private void assignGrayCode(BooleanCube c, int g) {
        cubeToGrayCodeMap.put(c, g);
        grayCodeToCubeMap.put(g, c);
    }

    private static void debug(String s) {
        // System.out.println(s);
    }

    private Set<Integer> getNextGrayCodes(Integer grayCode) {
        Set<Integer> grayCodes = new HashSet<>();
        int mask = 1;
        for (int i = 0; i < order; ++i) {
            if ((grayCode & mask) == 0) {
                Integer g = grayCode | mask;
                grayCodes.add(g);
            }
            mask <<= 1;
        }
        return grayCodes;
    }

    private Set<BooleanCube> findNeighbors(BooleanCube c) {
        Set<BooleanCube> neighbors = new HashSet<>();
        for (BooleanCube neighbor : cubes) {
            if (c.isAdjacentTo(neighbor)) {
                neighbors.add(neighbor);
            }
        }
        return neighbors;
    }

    public static void main(String[] args) {

        // System.out.println(new CubeListNormalizer(5, null).getNextGrayCodes(10));
        {
            Set<BooleanCube> cubeSet = new HashSet<>();
            cubeSet.add(BooleanCube.fromString("00101"));
            cubeSet.add(BooleanCube.fromString("00111"));
            cubeSet.add(BooleanCube.fromString("01101"));
            cubeSet.add(BooleanCube.fromString("01111"));

            List<BooleanCube> cubeList = new CubeListNormalizer(5, cubeSet).normalize();
            System.out.println(cubeList);
        }

        {
            Set<BooleanCube> cubeSet = new HashSet<>();
            cubeSet.add(BooleanCube.fromString("00001"));
            cubeSet.add(BooleanCube.fromString("00101"));
            cubeSet.add(BooleanCube.fromString("01101"));
            cubeSet.add(BooleanCube.fromString("01001"));
            cubeSet.add(BooleanCube.fromString("11001"));
            cubeSet.add(BooleanCube.fromString("11101"));
            cubeSet.add(BooleanCube.fromString("10101"));
            cubeSet.add(BooleanCube.fromString("10001"));

            List<BooleanCube> cubeList = new CubeListNormalizer(5, cubeSet).normalize();
            System.out.println(cubeList);
        }

    }
}
