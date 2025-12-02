import java.util.*;

class graphereduit {
    List<Pointcollecte> points;
    Map<String, Map<String, Double>> distances = new HashMap<>();

    public graphereduit(List<Pointcollecte> points, Graphe g) {
        this.points = points;
        // calculer toutes les distances minimales entre points de collecte
        for (Pointcollecte p1 : points) {
            distances.put(p1.id, new HashMap<>());
            for (Pointcollecte p2 : points) {
                if (!p1.id.equals(p2.id)) {
                    // Dijkstra sur le graphe complet
                    double dist = g.distanceEntre(p1.inter, p2.inter);
                    distances.get(p1.id).put(p2.id, dist);
                }
            }
        }
    }
}