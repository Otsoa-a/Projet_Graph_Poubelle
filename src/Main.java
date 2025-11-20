import java.util.List;

public class Main {
    public static void main(String[] args) throws Exception {

        Graphe g = new Graphe();
        g.chargerDepuisFichier("Village.txt");

        System.out.println("Intersections chargées : " + g.intersections.size());

        for (Intersection i : g.intersections.values()) {
            System.out.println(i.id + " : " + i.sortants.size() + " arcs sortants");
        }

        System.out.println("=== Test Dijkstra ===");
        var chemin = Dijkstra.shortestPath(g, "I1", "I30");

        System.out.println("Chemin obtenu :");
        for (var inter : chemin) {
            System.out.println(" -> " + inter.id);
        }

        Intersection depart = g.getOrCreate("I1");
        int maxBatiments = 15;

        List<List<Arc>> tournees = g.tournéesEuleriennes(depart, maxBatiments);

        int numTournee = 1;
        for (List<Arc> t : tournees) {
            System.out.println("Tournée " + numTournee++);
            for (Arc a : t) {
                System.out.println(a.nom + " de " + a.depart.id + " à " + a.arrivee.id
                        + " (" + a.nbBatiments + " batiments)");
            }
            System.out.println();
        }
    }
}
