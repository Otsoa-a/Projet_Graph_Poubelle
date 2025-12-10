import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.io.*;

public class Main {
    private static final String FICHIER_PARTICULIERS = "demandes_particuliers.txt";
    private static final String FICHIER_CENTRES = "centres_quartiers.txt";

    public static void main(String[] args) throws Exception {

        Scanner sc = new Scanner(System.in);
        Graphe g = new Graphe();
        g.chargerDepuisFichier("Paname.txt");

        System.out.println("Graphe chargé : " + g.intersections.size() + " intersections.");

        // Demande rôle
        mainLoop:
        while (true) {
            System.out.println("\n--- Vous êtes : ---");
            System.out.println("P - Particulier");
            System.out.println("M - Mairie");
            System.out.println("Q - Quitter");
            System.out.print("Choix (P/M/Q) : ");
            String role = sc.nextLine().trim().toUpperCase();
            if (role.isEmpty()) continue;

            switch (role.charAt(0)) {
                case 'Q' -> {
                    System.out.println("Au revoir !");
                    sc.close();
                    return;
                }

                case 'P' -> {
                    // Menu particulier
                    System.out.println("\n--- Menu Particulier ---");
                    System.out.print("Souhaitez-vous demander un ramassage chez vous ? (O/N) : ");
                    String rep = sc.nextLine().trim().toUpperCase();
                    if (!rep.equals("O")) {
                        System.out.println("Retour...");
                        continue mainLoop;
                    }

                    System.out.print("Nom de la rue (sans espaces superflus, ex: RueDuPont) : ");
                    String rue = sc.nextLine().trim();
                    System.out.print("Numéro de la maison : ");
                    int numero;
                    try {
                        numero = Integer.parseInt(sc.nextLine().trim());
                    } catch (Exception ex) {
                        System.out.println("Numéro invalide. Retour au menu principal.");
                        continue mainLoop;
                    }

                    // Enregistrement adresse
                    String ligne = rue + "|" + numero;
                    try {
                        Files.write(Paths.get(FICHIER_PARTICULIERS),
                                (ligne + System.lineSeparator()).getBytes(StandardCharsets.UTF_8),
                                java.nio.file.StandardOpenOption.CREATE,
                                java.nio.file.StandardOpenOption.APPEND);
                        System.out.println("Votre demande a été enregistrée. Merci !");
                    } catch (IOException ioe) {
                        System.out.println("Erreur lors de l'enregistrement : " + ioe.getMessage());
                    }
                    continue mainLoop;
                }

                case 'M' -> {
                    // Menu mairie
                    mairieLoop:
                    while (true) {
                        System.out.println("\n===== MENU MAIRIE =====");
                        System.out.println("0 - Afficher les intersections");
                        System.out.println("1 - Calculer l'itinéraire jusqu'à la première adresse particulière");
                        System.out.println("2 - Faire le tour de toutes les adresses des particuliers (optimisé, sans capacité)");
                        System.out.println("3 - Organiser la collecte en partant d'une adresse (tournées avec capacité)");
                        System.out.println("4 - Ramassage des points de collecte (MST + tournées)");
                        System.out.println("5 - Organisation par jours (partition/colouring/planification) — centres depuis fichier");
                        System.out.println("6 - Recherche d'une intersection proche");
                        System.out.println("7 - IHM du graphe");
                        System.out.println("R - Retour (changer de rôle)");
                        System.out.println("Q - Quitter");
                        System.out.print("Choix : ");
                        String choix = sc.nextLine().trim().toUpperCase();
                        if (choix.isEmpty()) continue;

                        switch (choix.charAt(0)) {

                            case 'Q' -> {
                                System.out.println("Au revoir !");
                                sc.close();
                                return;
                            }

                            case 'R' -> {
                                // Retour choix de rôle
                                break mairieLoop;
                            }

                            case '0' -> {
                                // Afficher intersections
                                System.out.println("\n--- Intersections ---");
                                for (Intersection i : g.intersections.values())
                                    System.out.println(i.id + " : " + i.sortants.size() + " arcs sortants");
                            }

                            case '1' -> {
                                // Calculer l'itinéraire jusqu'à la première adresse demandes.txt
                                System.out.print("Nom rue départ : ");
                                String rueDep = sc.nextLine();
                                System.out.print("Numéro départ : ");
                                int numDep = Integer.parseInt(sc.nextLine());

                                // Lecture adresse
                                List<String> demandes = lireDemandesParticuliers();
                                if (demandes.isEmpty()) {
                                    System.out.println("Aucune demande particulière dans " + FICHIER_PARTICULIERS);
                                    break;
                                }
                                String premiere = demandes.get(0);
                                String[] parts = premiere.split("\\|");
                                String rueArr = parts[0];
                                int numArr = Integer.parseInt(parts[1]);

                                var chemin = g.DijkstraAdresse(rueDep, numDep, rueArr, numArr);
                                if (chemin == null || chemin.isEmpty()) {
                                    System.out.println("Aucun chemin trouvé !");
                                } else {
                                    System.out.println("\nChemin obtenu :");
                                    for (var inter : chemin) System.out.println(" -> " + inter.id);

                                    System.out.println("\nRues empruntées :");
                                    for (int i = 0; i < chemin.size() - 1; i++) {
                                        Intersection a = chemin.get(i);
                                        Intersection b = chemin.get(i + 1);
                                        Arc arc = a.sortants.stream().filter(x -> x.arrivee == b).findFirst().orElse(null);
                                        System.out.println((arc != null) ? "De " + a.id + " à " + b.id + " par " + arc.nom
                                                : "Aucun arc entre " + a.id + " et " + b.id);
                                    }
                                }
                            }

                            case '2' -> {
                                // Tour de toutes les adresses données par les particuliers
                                List<String> demandes = lireDemandesParticuliers();
                                if (demandes.isEmpty()) {
                                    System.out.println("Aucune adresse de particuliers trouvée.");
                                    break;
                                }

                                List<Pointcollecte> pointsCollecte = new ArrayList<>();
                                for (String d : demandes) {
                                    String[] p = d.split("\\|");
                                    if (p.length < 2) {
                                        System.out.println(" Format d'adresse invalide (ignorée): " + d);
                                        continue;
                                    }
                                    String rue = p[0];
                                    int num;
                                    try {
                                        num = Integer.parseInt(p[1]);
                                    } catch (NumberFormatException nfe) {
                                        System.out.println(" Numéro invalide (ignorée): " + d);
                                        continue;
                                    }
                                    Intersection inter = g.trouverIntersection(rue, num);
                                    if (inter != null) {
                                        pointsCollecte.add(new Pointcollecte(inter, 1, rue, num));
                                    } else {
                                        System.out.println(" Adresse introuvable (ignorée): " + rue + " " + num);
                                    }
                                }

                                if (pointsCollecte.isEmpty()) {
                                    System.out.println("Aucune adresse valide à traiter.");
                                    break;
                                }

                                // On choisit le premier comme depot
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

                                // Ordre optimisé
                                System.out.println("\nOrdre optimisé des adresses (MST préfixe) :");
                                int idx = 1;
                                for (String id : ordreVisite) {
                                    Pointcollecte pc = contenanceMap.get(id);
                                    if (pc != null)
                                        System.out.println(idx++ + " - " + id + " (Rue: " + pc.rue + " Num: " + pc.numero + ")");
                                    else
                                        System.out.println(idx++ + " - " + id + " (Point inconnu)");
                                }

                                // Affichage des chemins entre chaque adresse
                                System.out.println("\n=== Chemins détaillés entre les arrêts ===");

                                for (int i = 0; i < ordreVisite.size() - 1; i++) {
                                    String idA = ordreVisite.get(i);
                                    String idB = ordreVisite.get(i + 1);

                                    Intersection A = g.intersections.get(idA);
                                    Intersection B = g.intersections.get(idB);

                                    if (A == null || B == null) {
                                        System.out.println("\nDe " + idA + " vers " + idB + " : intersection introuvable.");
                                        continue;
                                    }

                                    System.out.println("\nDe " + A.id + " (" + contenanceMap.get(idA).rue + " " + contenanceMap.get(idA).numero + ")"
                                            + " → " + B.id + " (" + contenanceMap.get(idB).rue + " " + contenanceMap.get(idB).numero + ") :");

                                    // On utilise Djikstra
                                    List<Intersection> chemin = g.Dijkstra(A.id, B.id);
                                    if (chemin == null || chemin.isEmpty()) {
                                        System.out.println(" Aucun chemin trouvé !");
                                        continue;
                                    }

                                    // Affichage des intersections parcourues
                                    System.out.println("  Intersections parcourues :");
                                    for (Intersection inter : chemin) {
                                        System.out.println("    - " + inter.id);
                                    }

                                    // Affichage des rues empruntées distance
                                    System.out.println("  Rues empruntées :");
                                    double distanceTotale = 0.0;
                                    for (int j = 0; j < chemin.size() - 1; j++) {
                                        Intersection X = chemin.get(j);
                                        Intersection Y = chemin.get(j + 1);

                                        // Chercher un arc X -> Y sinon Y -> X
                                        Arc arc = X.sortants.stream().filter(a -> a.arrivee == Y).findFirst().orElse(null);
                                        if (arc == null) {
                                            arc = Y.sortants.stream().filter(a -> a.arrivee == X).findFirst().orElse(null);
                                        }

                                        if (arc != null) {
                                            System.out.printf("    • %s (%.2f m)%n", arc.nom, arc.longueur);
                                            distanceTotale += arc.longueur;
                                        } else {
                                            System.out.println("    • (aucune section directe trouvée entre " + X.id + " et " + Y.id + ")");
                                        }
                                    }

                                    System.out.printf("  ➜ Distance totale entre ces arrêts : %.2f m%n", distanceTotale);
                                }
                            }
                            case '3' -> {
                                // Récolte en partant d'une adresse donnée
                                System.out.print("Nom rue départ : ");
                                String rue = sc.nextLine();
                                System.out.print("Numéro départ : ");
                                int numero = Integer.parseInt(sc.nextLine());
                                System.out.print("Capacité camion : ");
                                int capacite = Integer.parseInt(sc.nextLine());

                                var tours = g.tourneesDepuisAdresse(rue, numero, capacite);
                                if (tours == null || tours.isEmpty()) System.out.println("Aucun tour trouvé !");
                                else {
                                    int numTour = 1;
                                    for (List<Arc> tour : tours) {
                                        System.out.println("\n=== Tournée " + numTour++ + " ===");
                                        for (Arc a : tour) System.out.println("Rue : " + a.nom + " (" + a.nbBatiments + " maisons)");
                                    }
                                }
                            }
                            case '4' -> {
                                // Ramassage des points de collecte
                                List<Pointcollecte> pointsCollecte = new ArrayList<>();
                                // Choisi arbitrairement :
                                pointsCollecte.add(new Pointcollecte(g.trouverIntersection("RuedesEntrepreneurs", 18), 0, "RuedesEntrepreneurs", 18));
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
                                int capaciteCamion = Integer.parseInt(sc.nextLine());

                                List<List<Pointcollecte>> tourneesMST = g.decouperTournees(ordreVisite, contenanceMap, capaciteCamion);
                                int numTour = 1;
                                for (List<Pointcollecte> tournee : tourneesMST) {
                                    System.out.println("\n=== Tournée de ramassage " + numTour++ + " ===");

                                    System.out.println("Dépot : " + depot.id
                                            + " - Rue : " + depot.rue
                                            + " - Numéro : " + depot.numero
                                            + " - Contenance : " + depot.contenance);

                                    for (Pointcollecte pc : tournee) {
                                        if (pc != depot) {
                                            System.out.println("Point de collecte : " + pc.id
                                                    + " - Rue : " + pc.rue
                                                    + " - Numéro : " + pc.numero
                                                    + " - Contenance : " + pc.contenance);
                                        }
                                    }
                                }
                            }
                            case '5' -> {
                                // Chargement  centres depuis le fichier
                                List<String> centres = new ArrayList<>();
                                try {
                                    if (Files.exists(Paths.get(FICHIER_CENTRES))) {
                                        List<String> lines = Files.readAllLines(Paths.get(FICHIER_CENTRES), StandardCharsets.UTF_8);
                                        for (String L : lines) {
                                            String s = L.trim();
                                            if (!s.isEmpty()) centres.add(s);
                                        }
                                        System.out.println("Centres chargés depuis " + FICHIER_CENTRES + " (" + centres.size() + " ids).");
                                    } else {
                                        System.out.println("Aucun fichier " + FICHIER_CENTRES + " trouvé. Impossible de partitionner.");
                                    }
                                } catch (IOException ioe) {
                                    System.out.println("Erreur lecture centres : " + ioe.getMessage());
                                }

                                if (centres.isEmpty()) {
                                    System.out.println("Aucun centre chargé : partition impossible.");
                                    break;
                                }

                                g.partitionnerParIntersections(centres);

                                int nbQuartiers = new HashSet<>(g.quartierArc.values()).size();
                                System.out.println("Partition en quartiers effectuée. Nombre de quartiers : " + nbQuartiers);

                                //  Coloration Welsh–Powell
                                Map<Integer, Integer> couleurQuartier = g.colorierQuartiers();

                                System.out.println("\n=== Coloration des quartiers ===");
                                for (Map.Entry<Integer, Integer> e : couleurQuartier.entrySet()) {
                                    System.out.println("Quartier " + e.getKey() + " -> couleur " + e.getValue());
                                }

                                // Affichage des rues par quartier
                                System.out.println("\n=== Rues par quartier ===");
                                for (int qid : new HashSet<>(g.quartierArc.values())) {
                                    System.out.println("Quartier " + qid + " : ");
                                    for (Arc a : g.getArcsDuQuartier(qid)) {
                                        System.out.println("  - " + a.nom + " : " + a.nbBatiments + " maisons");
                                    }
                                    System.out.println();
                                }

                                for (Arc a : g.getTousLesArcs()) a.utilise = false;

                                // Capacité
                                System.out.print("Capacité camion pour simulation des tournées : ");
                                int cap = Integer.parseInt(sc.nextLine());

                                // Préparation des arcs par quartier
                                Map<Integer, List<Arc>> arcsParQuartier = new HashMap<>();
                                for (int qid : new HashSet<>(g.quartierArc.values())) {
                                    List<Arc> arcsQuartier = g.getArcsDuQuartier(qid);
                                    List<Arc> copies = new ArrayList<>();
                                    for (Arc a : arcsQuartier) copies.add(a.copierAvecNbBatiments(a.nbBatiments));
                                    arcsParQuartier.put(qid, copies);
                                }

                                // Liste des couleurs
                                List<Integer> couleursOrdonnees = new ArrayList<>(new HashSet<>(couleurQuartier.values()));
                                Collections.sort(couleursOrdonnees);

                                // Simulation jour après jour
                                int jour = 1;
                                boolean resteARamasser = true;

                                while (resteARamasser) {
                                    resteARamasser = false;

                                    int couleurDuJour = couleursOrdonnees.get((jour - 1) % couleursOrdonnees.size());
                                    System.out.println("\n=== Jour " + jour + " — couleur " + couleurDuJour + " ===");

                                    for (int qid : arcsParQuartier.keySet()) {
                                        if (couleurQuartier.get(qid) != couleurDuJour) continue;

                                        List<Arc> arcsRestants = arcsParQuartier.get(qid);
                                        if (arcsRestants.isEmpty()) continue;

                                        resteARamasser = true;

                                        int capaciteRestante = cap;
                                        List<Arc> tourDuJour = new ArrayList<>();

                                        Iterator<Arc> it = arcsRestants.iterator();
                                        while (it.hasNext() && capaciteRestante > 0) {
                                            Arc a = it.next();
                                            int prise = Math.min(capaciteRestante, a.nbBatiments);
                                            tourDuJour.add(a.copierAvecNbBatiments(prise));
                                            capaciteRestante -= prise;
                                            if (prise == a.nbBatiments) {
                                                it.remove();
                                            } else {
                                                Arc reste = a.copierAvecNbBatiments(a.nbBatiments - prise);
                                                it.remove();
                                                arcsRestants.add(reste);
                                            }
                                        }

                                        System.out.println("\nQuartier " + qid + " (couleur " + couleurDuJour +
                                                ") -> Tournée (" + tourDuJour.size() + " arcs) :");
                                        for (Arc a : tourDuJour) {
                                            System.out.println("  - " + a.nom + " (" + a.nbBatiments + " maisons)");
                                        }
                                    }
                                    jour++;
                                }
                                System.out.println("\nSimulation terminée (organisation par jours).");
                            }

                            case '6' -> {
                                // Recherche d'une intersection proche
                                System.out.print("Nom rue : ");
                                String rue = sc.nextLine();
                                System.out.print("Numéro : ");
                                int numero = Integer.parseInt(sc.nextLine());

                                Intersection proche = g.trouverIntersection(rue, numero);
                                if (proche != null)
                                    System.out.println("Intersection la plus proche : " + proche.id);
                                else
                                    System.out.println("Rue ou numéro non trouvé !");
                            }
                            case '7' -> {
                                StringBuilder dot = new StringBuilder("digraph G {\n");
                                dot.append("  rankdir=LR;\n");
                                dot.append("  graph [splines=true, overlap=false];\n");
                                dot.append("  node [shape=circle, style=filled, fillcolor=lightblue, fontname=\"Arial\", fontsize=14];\n");
                                dot.append("  edge [fontname=\"Arial\", fontsize=12, color=gray50];\n");

                                Set<String> arcsAjoutes = new HashSet<>();

                                for (Intersection i : g.intersections.values()) {

                                    if (i.sortants.size() >= 4) {
                                        dot.append("  \"").append(i.id).append("\"")
                                                .append(" [shape=doublecircle, fillcolor=lightgreen];\n");
                                    } else {
                                        dot.append("  \"").append(i.id).append("\";\n");
                                    }

                                    for (Arc a : i.sortants) {
                                        String key = i.id + "->" + a.arrivee.id + ":" + a.nom;

                                        if (!arcsAjoutes.contains(key)) {

                                            // Couleur en fonction du nombre de bâtiments
                                            String color;
                                            if (a.nbBatiments > 10) color = "red";
                                            else if (a.nbBatiments > 5) color = "orange";
                                            else color = "green";

                                            // Épaisseur de trait proportionnelle
                                            int width = Math.min(5, 1 + a.nbBatiments / 3);

                                            dot.append("  \"").append(i.id).append("\" -> \"")
                                                    .append(a.arrivee.id).append("\"")
                                                    .append(" [label=\"").append(a.nom)
                                                    .append(" (").append(a.nbBatiments).append(")\"")
                                                    .append(", color=\"").append(color).append("\"")
                                                    .append(", penwidth=").append(width)
                                                    .append("];\n");

                                            arcsAjoutes.add(key);
                                        }
                                    }
                                }

                                dot.append("}");
                                Files.write(Paths.get("graphe.dot"), dot.toString().getBytes());
                                System.out.println("Fichier graphe.dot généré !");
                            }
                            default -> System.out.println("Choix invalide !");
                        }
                    }
                }

                default -> {
                    System.out.println("Choix invalide, recommencez.");
                }
            }
        }
    }

    //  Pour lire le fichier des particuliers
    private static List<String> lireDemandesParticuliers() {
        try {
            if (!Files.exists(Paths.get(FICHIER_PARTICULIERS))) return Collections.emptyList();
            List<String> lines = Files.readAllLines(Paths.get(FICHIER_PARTICULIERS), StandardCharsets.UTF_8);
            List<String> out = new ArrayList<>();
            for (String L : lines) {
                String s = L.trim();
                if (s.isEmpty()) continue;
                // si ligne contient des espaces on remplace par '|'
                if (s.contains("|")) out.add(s);
                else {
                    String[] toks = s.split("\\s+");
                    String num = toks[toks.length - 1];
                    String rue = String.join("", Arrays.copyOf(toks, toks.length - 1));
                    try {
                        Integer.parseInt(num);
                        out.add(rue + "|" + num);
                    } catch (Exception ex) {
                        out.add(s);
                    }
                }
            }
            return out;
        } catch (IOException ioe) {
            System.out.println("Impossible de lire " + FICHIER_PARTICULIERS + " : " + ioe.getMessage());
            return Collections.emptyList();
        }
    }
}
