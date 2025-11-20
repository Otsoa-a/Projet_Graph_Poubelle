public class Main {
    public static void main(String[] args) throws Exception {

        Graphe g = new Graphe();
        g.chargerDepuisFichier("Village.txt");

        System.out.println("Intersections chargées : " + g.intersections.size());

        for (Intersection i : g.intersections.values()) {
            System.out.println(i.id + " : " + i.sortants.size() + " arcs sortants");
        }

        System.out.println("=== Test Dijkstra ===");
        var chemin = Dijkstra.shortestPath(g, "I1", "I26");

        System.out.println("Chemin obtenu :");
        for (var inter : chemin) {
            System.out.println(" -> " + inter.id);
        }
    }
}
