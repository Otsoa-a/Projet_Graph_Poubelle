import java.util.*;

class MST {
    public static List<String[]> prim(graphereduit gr, String depotId) {
        Set<String> visited = new HashSet<>();
        List<String[]> mstEdges = new ArrayList<>();
        PriorityQueue<String[]> pq = new PriorityQueue<>(Comparator.comparingDouble(a -> a[2].length() == 0 ? 0 : Double.parseDouble(a[2])));

        visited.add(depotId);

        // Ajouter les arcs initiaux
        for (Map.Entry<String, Double> e : gr.distances.get(depotId).entrySet()) {
            pq.add(new String[]{depotId, e.getKey(), String.valueOf(e.getValue())});
        }

        while (visited.size() < gr.points.size()) {
            String[] arc = pq.poll();
            if (arc == null) break;
            String from = arc[0], to = arc[1];
            double dist = Double.parseDouble(arc[2]);
            if (visited.contains(to)) continue;

            visited.add(to);
            mstEdges.add(arc);

            for (Map.Entry<String, Double> e : gr.distances.get(to).entrySet()) {
                if (!visited.contains(e.getKey())) {
                    pq.add(new String[]{to, e.getKey(), String.valueOf(e.getValue())});
                }
            }
        }
        return mstEdges;
    }
}