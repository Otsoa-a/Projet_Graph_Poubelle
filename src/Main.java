import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Main {
    public static void main(String[] args) throws Exception {

        Graphe g = new Graphe();
        g.chargerDepuisFichier("Paname.txt");

        System.out.println("Intersections chargées : " + g.intersections.size());

        for (Intersection i : g.intersections.values()) {
            System.out.println(i.id + " : " + i.sortants.size() + " arcs sortants");
        }

        System.out.println("\n=== Test Dijkstra depuis une adresse ===");

        // Adresse de départ et d'arrivée
        String rueDep = "AvenuedeVersailles";
        int numDep = 51;

        String rueArr = "RuedesÉcoles";
        int numArr = 11;

        var chemin = g.DijkstraAdresse(rueDep, numDep, rueArr, numArr);

        if (chemin == null || chemin.isEmpty()) {
            System.out.println("⚠ Aucun chemin trouvé !");
        } else {
            System.out.println("\nChemin obtenu depuis " + rueDep + " n°" + numDep +
                    " jusqu'à " + rueArr + " n°" + numArr + " :");

            for (var inter : chemin) {
                System.out.println(" -> " + inter.id);
            }

            // 🔥 Afficher maintenant les rues empruntées
            System.out.println("\n=== Rues empruntées ===");

            for (int i = 0; i < chemin.size() - 1; i++) {
                Intersection a = chemin.get(i);
                Intersection b = chemin.get(i + 1);

                // Trouver l'arc correspondant
                Arc arc = a.sortants.stream()
                        .filter(x -> x.arrivee == b)
                        .findFirst()
                        .orElse(null);

                if (arc != null) {
                    System.out.println("De " + a.id + " à " + b.id + " par la rue : " + arc.nom);
                } else {
                    System.out.println("⚠ Aucun arc entre " + a.id + " et " + b.id);
                }
            }
        }



        /*
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
        */

        System.out.println("=== Test recherche adresse ===");
        String rueTest = "RueduMidi";   // change par un vrai nom de rue qui existe dans ton fichier
        int numeroTest = 9;            // le numéro que tu veux tester

        Intersection proche = g.trouverIntersection(rueTest, numeroTest);

        if (proche != null) {
            System.out.println("Pour " + rueTest + " n°" + numeroTest + " : intersection la plus proche = " + proche.id);
        } else {
            System.out.println("Rue ou numéro non trouvé !");
        }

        //méthode qui s'occupe de la génération du graphe
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
