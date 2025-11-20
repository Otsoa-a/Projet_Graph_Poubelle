import java.util.*;

public class Dijkstra {

    public static List<Intersection> shortestPath(Graphe g, String depart, String arrivee) {
        Map<Intersection, Double> dist = new HashMap<>();
        Map<Intersection, Intersection> pred = new HashMap<>();
        PriorityQueue<Intersection> pq = new PriorityQueue<>(Comparator.comparingDouble(dist::get));

        Intersection start = g.intersections.get(depart);
        Intersection end = g.intersections.get(arrivee);

        for (Intersection i : g.intersections.values())
            dist.put(i, Double.POSITIVE_INFINITY);

        dist.put(start, 0.0);
        pq.add(start);

        while (!pq.isEmpty()) {
            Intersection u = pq.poll();

            if (u == end) break;

            for (Arc arc : u.sortants) {
                Intersection v = arc.arrivee;
                double alt = dist.get(u) + arc.longueur;

                if (alt < dist.get(v)) {
                    dist.put(v, alt);
                    pred.put(v, u);
                    pq.add(v);
                }
            }
        }

        // reconstruction du chemin
        LinkedList<Intersection> chemin = new LinkedList<>();
        Intersection curr = end;

        while (curr != null) {
            chemin.addFirst(curr);
            curr = pred.get(curr);
        }

        return chemin;
    }
}
