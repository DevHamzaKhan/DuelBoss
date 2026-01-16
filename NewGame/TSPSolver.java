import java.util.ArrayList;
import java.util.List;

/**
 * Traveling Salesman Problem solver using recursive DFS (Depth-First Search).
 * Finds the shortest path to visit all nodes and return to the start.
 */
public class TSPSolver {
    
    private static final int MAX_POINTS_FOR_EXACT = 8; // Use exact TSP for <= 8 points, heuristic for more
    
    /**
     * Solves TSP recursively using DFS to find the shortest path.
     * For many points, uses a nearest-neighbor heuristic to avoid performance issues.
     * 
     * @param startX Starting X coordinate
     * @param startY Starting Y coordinate
     * @param points List of points to visit (each point is [x, y])
     * @return List of points in order (including start at beginning and end)
     */
    public static List<double[]> solveTSP(double startX, double startY, List<double[]> points) {
        if (points.isEmpty()) {
            List<double[]> result = new ArrayList<>();
            result.add(new double[]{startX, startY});
            return result;
        }
        
        // For many points, use nearest-neighbor heuristic to avoid factorial explosion
        if (points.size() > MAX_POINTS_FOR_EXACT) {
            return solveTSPHeuristic(startX, startY, points);
        }
        
        // Initialize best path tracking
        List<double[]> bestPath = new ArrayList<>();
        double[] bestDistance = {Double.MAX_VALUE};
        
        // Start recursive DFS
        List<double[]> currentPath = new ArrayList<>();
        currentPath.add(new double[]{startX, startY});
        boolean[] visited = new boolean[points.size()];
        
        solveTSPRecursive(startX, startY, points, visited, currentPath, 0.0, bestPath, bestDistance);
        
        // Add return to start
        if (!bestPath.isEmpty()) {
            bestPath.add(new double[]{startX, startY});
        }
        
        return bestPath;
    }
    
    /**
     * Nearest-neighbor heuristic for TSP when there are too many points.
     * Much faster but not guaranteed optimal.
     */
    private static List<double[]> solveTSPHeuristic(double startX, double startY, List<double[]> points) {
        List<double[]> result = new ArrayList<>();
        result.add(new double[]{startX, startY});
        
        List<double[]> remaining = new ArrayList<>(points);
        double currentX = startX;
        double currentY = startY;
        
        while (!remaining.isEmpty()) {
            // Find nearest unvisited point
            int nearestIndex = 0;
            double nearestDist = distance(currentX, currentY, remaining.get(0)[0], remaining.get(0)[1]);
            
            for (int i = 1; i < remaining.size(); i++) {
                double[] p = remaining.get(i);
                double dist = distance(currentX, currentY, p[0], p[1]);
                if (dist < nearestDist) {
                    nearestDist = dist;
                    nearestIndex = i;
                }
            }
            
            // Visit nearest point
            double[] nearest = remaining.remove(nearestIndex);
            result.add(nearest);
            currentX = nearest[0];
            currentY = nearest[1];
        }
        
        // Return to start
        result.add(new double[]{startX, startY});
        return result;
    }
    
    /**
     * Recursive DFS function to explore all possible paths.
     * 
     * @param startX Original starting X
     * @param startY Original starting Y
     * @param points All points to visit
     * @param visited Boolean array tracking which points have been visited
     * @param currentPath Current path being explored
     * @param currentDistance Current total distance traveled
     * @param bestPath Reference to store the best path found
     * @param bestDistance Reference to store the best distance found
     */
    private static void solveTSPRecursive(
            double startX, double startY,
            List<double[]> points,
            boolean[] visited,
            List<double[]> currentPath,
            double currentDistance,
            List<double[]> bestPath,
            double[] bestDistance) {
        
        // Base case: all points visited
        boolean allVisited = true;
        for (boolean v : visited) {
            if (!v) {
                allVisited = false;
                break;
            }
        }
        
        if (allVisited) {
            // Calculate distance back to start
            double[] lastPoint = currentPath.get(currentPath.size() - 1);
            double returnDistance = distance(lastPoint[0], lastPoint[1], startX, startY);
            double totalDistance = currentDistance + returnDistance;
            
            // Update best path if this is better
            if (totalDistance < bestDistance[0]) {
                bestDistance[0] = totalDistance;
                bestPath.clear();
                bestPath.addAll(new ArrayList<>(currentPath));
            }
            return;
        }
        
        // Pruning: if current distance already exceeds best, stop exploring
        if (currentDistance >= bestDistance[0]) {
            return;
        }
        
        // Recursive case: try visiting each unvisited point
        for (int i = 0; i < points.size(); i++) {
            if (!visited[i]) {
                // Mark as visited
                visited[i] = true;
                double[] point = points.get(i);
                
                // Calculate distance from last point to this point
                double[] lastPoint = currentPath.get(currentPath.size() - 1);
                double dist = distance(lastPoint[0], lastPoint[1], point[0], point[1]);
                
                // Add to current path
                currentPath.add(point);
                
                // Recursively explore
                solveTSPRecursive(startX, startY, points, visited, currentPath, 
                                 currentDistance + dist, bestPath, bestDistance);
                
                // Backtrack: remove from path and mark as unvisited
                currentPath.remove(currentPath.size() - 1);
                visited[i] = false;
            }
        }
    }
    
    /**
     * Calculate Euclidean distance between two points.
     */
    private static double distance(double x1, double y1, double x2, double y2) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        return Math.sqrt(dx * dx + dy * dy);
    }
}

