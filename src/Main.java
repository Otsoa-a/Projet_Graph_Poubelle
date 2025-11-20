public class Main {
    public static void main(String[] args) {

        Graphe g = new Graphe();

        // ---- TON PETIT GRAPHE DE TEST ----
        g.ajouterArc("I1", "I2", "RueA1", 100, 3, false);
        g.ajouterArc("I2", "I3", "RueA2", 150, 5, false);
        g.ajouterArc("I1", "I4", "RueB1", 80, 2, true);
        g.ajouterArc("I4", "I3", "RueB2", 70, 1, false);

        System.out.println("=== Test Dijkstra ===");
        var chemin = Dijkstra.shortestPath(g, "I1", "I3");

        System.out.println("Chemin obtenu :");
        for (var inter : chemin) {
            System.out.println(" -> " + inter.id);
        }
    }
}
// Ceci est un test réalisé par l'electr0central1
