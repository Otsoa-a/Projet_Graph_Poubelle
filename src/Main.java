import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Main {
    public static void main(String[] args) throws Exception {

        Graphe g = new Graphe();
        g.chargerDepuisFichier("paris.txt");

        System.out.println("Intersections chargées : " + g.intersections.size());

        for (Intersection i : g.intersections.values()) {
            System.out.println(i.id + " : " + i.sortants.size() + " arcs sortants");
        }

        System.out.println("=== Test Dijkstra ===");
        var chemin = Dijkstra.shortestPath(g, "I1", "I11");

        System.out.println("Chemin obtenu :");
        for (var inter : chemin) {
            System.out.println(" -> " + inter.id);
        }

        Intersection depart = g.getOrCreate("I1");
        int maxBatiments = 25;


        List<List<Arc>> tournees = g.tournéesEuleriennes(depart,maxBatiments);
        int numTournee = 1;
        for (List<Arc> t : tournees) {
            System.out.println("Tournée " + numTournee++);
            for (Arc a : t) {
                System.out.println(a.nom + " de " + a.depart.id + " à " + a.arrivee.id
                        + " (" + a.nbBatiments + " batiments)");
            }
            System.out.println();
        }
        //Génération du graphe
        StringBuilder dot = new StringBuilder("digraph G {\n");
        dot.append("node [shape=circle, style=filled, color=lightblue];\n");

        Set<String> arcsAjoutes = new HashSet<>();

        for (Intersection i : g.intersections.values()) {
            for (Arc a : i.sortants) {
                String key = i.id + "->" + a.arrivee.id + ":" + a.nom;
                if (!arcsAjoutes.contains(key)) {
                    dot.append("  \"").append(i.id).append("\" -> \"")
                            .append(a.arrivee.id).append("\"")
                            .append(" [label=\"").append(a.nom).append(" (").append(a.nbBatiments).append(")\"];\n");
                    arcsAjoutes.add(key);
                }
            }
        }
        dot.append("}");
        Files.write(Paths.get("graphe.dot"), dot.toString().getBytes());
    }


}
