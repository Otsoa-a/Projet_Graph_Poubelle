import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class Main {
    public static void main(String[] args) throws Exception {

        Scanner sc = new Scanner(System.in);
        Graphe g = new Graphe();
        g.chargerDepuisFichier("Paname.txt");

        System.out.println("Graphe chargé : " + g.intersections.size() + " intersections.");

        while (true) {
            System.out.println("\n===== MENU =====");
            System.out.println("1 - Afficher les intersections");
            System.out.println("2 - Dijkstra depuis une adresse");
            System.out.println("3 - Tournées depuis une adresse");
            System.out.println("4 - Recherche d'intersection proche");
            System.out.println("5 - Générer fichier graphe.dot");
            System.out.println("6 - MST + tournées depuis points de collecte");
            System.out.println("0 - Quitter");
            System.out.print("Choix : ");
            int choix = sc.nextInt();
            sc.nextLine(); // consommer le retour chariot

            switch (choix) {

                case 1 -> {
                    System.out.println("\n--- Intersections ---");
                    for (Intersection i : g.intersections.values())
                        System.out.println(i.id + " : " + i.sortants.size() + " arcs sortants");
                }

                case 2 -> {
                    System.out.print("Nom rue départ : ");
                    String rueDep = sc.nextLine();
                    System.out.print("Numéro départ : ");
                    int numDep = sc.nextInt();
                    sc.nextLine();
                    System.out.print("Nom rue arrivée : ");
                    String rueArr = sc.nextLine();
                    System.out.print("Numéro arrivée : ");
                    int numArr = sc.nextInt();
                    sc.nextLine();

                    var chemin = g.DijkstraAdresse(rueDep, numDep, rueArr, numArr);
                    if (chemin == null || chemin.isEmpty()) {
                        System.out.println("⚠ Aucun chemin trouvé !");
                    } else {
                        System.out.println("\nChemin obtenu :");
                        for (var inter : chemin) System.out.println(" -> " + inter.id);

                        System.out.println("\nRues empruntées :");
                        for (int i = 0; i < chemin.size() - 1; i++) {
                            Intersection a = chemin.get(i);
                            Intersection b = chemin.get(i + 1);
                            Arc arc = a.sortants.stream().filter(x -> x.arrivee == b).findFirst().orElse(null);
                            System.out.println((arc != null) ? "De " + a.id + " à " + b.id + " par " + arc.nom
                                    : "⚠ Aucun arc entre " + a.id + " et " + b.id);
                        }
                    }
                }

                case 3 -> {
                    System.out.print("Nom rue départ : ");
                    String rue = sc.nextLine();
                    System.out.print("Numéro départ : ");
                    int numero = sc.nextInt();
                    sc.nextLine();
                    System.out.print("Capacité camion : ");
                    int capacite = sc.nextInt();
                    sc.nextLine();

                    var tours = g.tourneesDepuisAdresse(rue, numero, capacite);
                    if (tours == null || tours.isEmpty()) System.out.println("⚠ Aucun tour trouvé !");
                    else {
                        int numTour = 1;
                        for (List<Arc> tour : tours) {
                            System.out.println("\n=== Tournée " + numTour++ + " ===");
                            for (Arc a : tour) System.out.println("Rue : " + a.nom + "\n");
                        }
                    }
                }

                case 4 -> {
                    System.out.print("Nom rue : ");
                    String rue = sc.nextLine();
                    System.out.print("Numéro : ");
                    int numero = sc.nextInt();
                    sc.nextLine();

                    Intersection proche = g.trouverIntersection(rue, numero);
                    if (proche != null)
                        System.out.println("Intersection la plus proche : " + proche.id);
                    else
                        System.out.println("⚠ Rue ou numéro non trouvé !");
                }

                case 5 -> {
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
                    System.out.println("Fichier graphe.dot généré !");
                }

                case 6 -> {
                    List<Pointcollecte> pointsCollecte = new ArrayList<>();
                    //DEPOT
                    pointsCollecte.add(new Pointcollecte(g.trouverIntersection("RuedesEntrepreneurs", 18), 0, "RuedesEntrepreneurs", 18));
                    //POINT DE COLLECTE
                    pointsCollecte.add(new Pointcollecte(g.trouverIntersection("BoulevardduMontparnasse", 123), 105, "BoulevardduMontparnasse", 123));
                    pointsCollecte.add(new Pointcollecte(g.trouverIntersection("RueSaint-Honoré", 50), 705, "RueSaint-Honoré", 50));
                    pointsCollecte.add(new Pointcollecte(g.trouverIntersection("RuedeCharonne", 2), 100, "RuedeCharonne", 2));
                    pointsCollecte.add(new Pointcollecte(g.trouverIntersection("RueNicolasFortin", 6), 105, "RueNicolasFortin", 6));
                    Pointcollecte depot = pointsCollecte.get(0);
                    graphereduit gr = new graphereduit(pointsCollecte, g);
                    List<String[]> mstEdges = MST.prim(gr, depot.id);

                    Map<String, List<String>> arbre = new HashMap<>();
                    for (String[] edge : mstEdges) {
                        arbre.computeIfAbsent(edge[0], k -> new ArrayList<>()).add(edge[1]);
                        arbre.computeIfAbsent(edge[1], k -> new ArrayList<>()).add(edge[0]);
                    }

                    List<String> ordreVisite = new ArrayList<>();
                    MSTParcours.parcoursPrefixe(depot.id, arbre, new HashSet<>(), ordreVisite);

                    Map<String, Pointcollecte> contenanceMap = new HashMap<>();
                    for (Pointcollecte p : pointsCollecte) contenanceMap.put(p.id, p);

                    System.out.print("Capacité camion : ");
                    int capaciteCamion = sc.nextInt();
                    sc.nextLine();

                    List<List<Pointcollecte>> tourneesMST = g.decouperTournees(ordreVisite, contenanceMap, capaciteCamion);
                    int numTour = 1;
                    for (List<Pointcollecte> tournee : tourneesMST) {
                        System.out.println("\n=== Tournée de ramassage "+ numTour++ + " ===");

                        // Affichage du dépôt au début
                        System.out.println("Dépot : " + depot.id
                                + " - Rue : " + depot.rue
                                + " - Numéro : " + depot.numero
                                + " - Contenance : " + depot.contenance);

                        // Affichage des points de collecte de la tournée
                        for (Pointcollecte pc : tournee) {
                            if (pc != depot) { // on évite de répéter le dépôt
                                System.out.println("Point de collecte : " + pc.id
                                        + " - Rue : " + pc.rue
                                        + " - Numéro : " + pc.numero
                                        + " - Contenance : " + pc.contenance);
                            }
                        }
                    }
                }

                case 0 -> {
                    System.out.println("Au revoir !");
                    sc.close();
                    return;
                }

                default -> System.out.println("⚠ Choix invalide !");
            }
        }
    }
}
