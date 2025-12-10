import java.util.*;
//classe pour la MST afin d'avoir un plus petit graphe
class graphereduit {
    List<Pointcollecte> points;// sommet
    Map<String, Map<String, Double>> distances = new HashMap<>();//arc : plus court chemin entre chaque sommet

    public graphereduit(List<Pointcollecte> points, Graphe g) {
        this.points = points;
        //calculer toutes les distances minimales entre points de collecte
        for (Pointcollecte p1 : points) {
            distances.put(p1.id, new HashMap<>());
            for (Pointcollecte p2 : points) {
                if (!p1.id.equals(p2.id)) {
                    //on fait dijkstra sur le graphe complet
                    double dist = g.distanceEntre(p1.inter, p2.inter);
                    distances.get(p1.id).put(p2.id, dist);
                }
            }
        }
    }
}