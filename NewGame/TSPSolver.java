import java.util.ArrayList;
import java.util.List;

public class TSPSolver {
    private static final int MAX_POINTS_FOR_EXACT = 8;
    
    public static List<double[]> solveTSP(double startX, double startY, List<double[]> points) {
        if (points.isEmpty()) {
            List<double[]> result = new ArrayList<>();
            result.add(new double[]{startX, startY});
            return result;
        }
        
        if (points.size() > MAX_POINTS_FOR_EXACT) {
            return solveTSPHeuristic(startX, startY, points);
        }
        
        List<double[]> bestPath = new ArrayList<>();
        double[] bestDistance = {Double.MAX_VALUE};
        List<double[]> currentPath = new ArrayList<>();
        currentPath.add(new double[]{startX, startY});
        boolean[] visited = new boolean[points.size()];
        
        solveTSPRecursive(startX, startY, points, visited, currentPath, 0.0, bestPath, bestDistance);
        
        if (!bestPath.isEmpty()) {
            bestPath.add(new double[]{startX, startY});
        }
        return bestPath;
    }
    
    private static List<double[]> solveTSPHeuristic(double startX, double startY, List<double[]> points) {
        List<double[]> result = new ArrayList<>();
        result.add(new double[]{startX, startY});
        List<double[]> remaining = new ArrayList<>(points);
        double cx = startX, cy = startY;
        
        while (!remaining.isEmpty()) {
            int nearest = 0;
            double minDist = dist(cx, cy, remaining.get(0)[0], remaining.get(0)[1]);
            for (int i = 1; i < remaining.size(); i++) {
                double d = dist(cx, cy, remaining.get(i)[0], remaining.get(i)[1]);
                if (d < minDist) {
                    minDist = d;
                    nearest = i;
                }
            }
            double[] p = remaining.remove(nearest);
            result.add(p);
            cx = p[0];
            cy = p[1];
        }
        result.add(new double[]{startX, startY});
        return result;
    }
    
    private static void solveTSPRecursive(double startX, double startY, List<double[]> points,
            boolean[] visited, List<double[]> currentPath, double currentDist,
            List<double[]> bestPath, double[] bestDistance) {
        
        boolean allVisited = true;
        for (boolean v : visited) if (!v) { allVisited = false; break; }
        
        if (allVisited) {
            double[] last = currentPath.get(currentPath.size() - 1);
            double total = currentDist + dist(last[0], last[1], startX, startY);
            if (total < bestDistance[0]) {
                bestDistance[0] = total;
                bestPath.clear();
                bestPath.addAll(new ArrayList<>(currentPath));
            }
            return;
        }
        
        if (currentDist >= bestDistance[0]) return;
        
        for (int i = 0; i < points.size(); i++) {
            if (!visited[i]) {
                visited[i] = true;
                double[] p = points.get(i);
                double[] last = currentPath.get(currentPath.size() - 1);
                double d = dist(last[0], last[1], p[0], p[1]);
                currentPath.add(p);
                solveTSPRecursive(startX, startY, points, visited, currentPath, currentDist + d, bestPath, bestDistance);
                currentPath.remove(currentPath.size() - 1);
                visited[i] = false;
            }
        }
    }
    
    private static double dist(double x1, double y1, double x2, double y2) {
        double dx = x2 - x1, dy = y2 - y1;
        return Math.sqrt(dx * dx + dy * dy);
    }
}
