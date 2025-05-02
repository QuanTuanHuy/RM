package hust.project.restaurant_management.algo;

import java.util.*;

public class TSPSolver {
    private final double[][] distances;
    private final int n;
    private final Map<String, Double> memo;
    private final Map<String, Integer> parent;

    public TSPSolver(double[][] distances) {
        this.distances = distances;
        this.n = distances.length;
        this.memo = new HashMap<>();
        this.parent = new HashMap<>();
    }

    public List<Integer> solve() {
        // Với bài toán có ít điểm, có thể sử dụng brute-force cho đơn giản
        if (n <= 4) {
            return solveBruteForce();
        } else {
            return solveHeldKarp();
        }
    }

    // Giải bằng brute-force cho trường hợp ít điểm
    private List<Integer> solveBruteForce() {
        List<Integer> vertices = new ArrayList<>();
        for (int i = 1; i < n; i++) { // bắt đầu từ 1, giữ 0 làm điểm xuất phát
            vertices.add(i);
        }

        List<Integer> bestPath = new ArrayList<>();
        bestPath.add(0); // Điểm xuất phát
        double minDistance = Double.MAX_VALUE;

        // Tạo và kiểm tra tất cả các hoán vị có thể
        List<List<Integer>> permutations = generatePermutations(vertices);
        for (List<Integer> path : permutations) {
            path.add(0, 0); // Thêm điểm xuất phát vào đầu

            double distance = calculatePathDistance(path);
            if (distance < minDistance) {
                minDistance = distance;
                bestPath = new ArrayList<>(path);
            }
        }

        return bestPath;
    }

    // Tạo tất cả các hoán vị có thể của danh sách vertices
    private List<List<Integer>> generatePermutations(List<Integer> vertices) {
        List<List<Integer>> result = new ArrayList<>();
        generatePermutationsHelper(vertices, 0, result);
        return result;
    }

    private void generatePermutationsHelper(List<Integer> vertices, int start, List<List<Integer>> result) {
        if (start == vertices.size()) {
            result.add(new ArrayList<>(vertices));
            return;
        }

        for (int i = start; i < vertices.size(); i++) {
            // Hoán đổi
            Collections.swap(vertices, start, i);
            // Đệ quy
            generatePermutationsHelper(vertices, start + 1, result);
            // Khôi phục
            Collections.swap(vertices, start, i);
        }
    }

    // Tính tổng khoảng cách của một path
    private double calculatePathDistance(List<Integer> path) {
        double distance = 0;
        for (int i = 0; i < path.size() - 1; i++) {
            distance += distances[path.get(i)][path.get(i + 1)];
        }
        // Thêm khoảng cách từ điểm cuối về điểm xuất phát để hoàn thành chu trình
        distance += distances[path.get(path.size() - 1)][path.get(0)];
        return distance;
    }

    // Giải bằng Held-Karp algorithm cho trường hợp nhiều điểm
    private List<Integer> solveHeldKarp() {
        // Gọi hàm quy hoạch động từ điểm xuất phát (0) với tất cả các điểm còn lại
        Set<Integer> set = new HashSet<>();
        for (int i = 1; i < n; i++) {
            set.add(i);
        }

        // Tìm chi phí tối thiểu
        tsp(0, set);

        // Khôi phục đường đi từ parent map
        List<Integer> path = new ArrayList<>();
        String subproblem = getSubproblem(0, set);

        while (parent.containsKey(subproblem)) {
            int nextCity = parent.get(subproblem);
            path.add(nextCity);

            // Cập nhật lại set và subproblem
            set.remove(nextCity);
            subproblem = getSubproblem(nextCity, set);
        }

        // Thêm điểm xuất phát vào đầu và cuối
        path.add(0, 0);

        return path;
    }

    // Hàm chính của thuật toán Held-Karp
    private double tsp(int city, Set<Integer> cities) {
        // Nếu đã đi qua tất cả các thành phố, quay về điểm xuất phát
        if (cities.isEmpty()) {
            return distances[city][0]; // Quay về thành phố xuất phát (0)
        }

        // Tạo key duy nhất cho subproblem này
        String subproblem = getSubproblem(city, cities);

        // Kiểm tra xem đã tính toán trước đó chưa
        if (memo.containsKey(subproblem)) {
            return memo.get(subproblem);
        }

        // Tính toán chi phí tối thiểu
        double minCost = Double.MAX_VALUE;
        int bestNextCity = -1;

        for (int nextCity : cities) {
            // Tạo tập hợp các thành phố còn lại
            Set<Integer> remainingCities = new HashSet<>(cities);
            remainingCities.remove(nextCity);

            // Tính chi phí khi đi từ city hiện tại đến nextCity và tiếp tục
            double cost = distances[city][nextCity] + tsp(nextCity, remainingCities);

            if (cost < minCost) {
                minCost = cost;
                bestNextCity = nextCity;
            }
        }

        // Lưu lại kết quả để tái sử dụng
        memo.put(subproblem, minCost);
        parent.put(subproblem, bestNextCity);

        return minCost;
    }

    // Tạo key cho subproblem
    private String getSubproblem(int city, Set<Integer> cities) {
        StringBuilder sb = new StringBuilder();
        sb.append(city).append("|");

        List<Integer> sortedCities = new ArrayList<>(cities);
        Collections.sort(sortedCities);

        for (int c : sortedCities) {
            sb.append(c).append(",");
        }

        return sb.toString();
    }
}