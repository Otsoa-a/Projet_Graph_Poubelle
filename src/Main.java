import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.io.*;

public class Main {
    // fichiers utilisés
    private static final String FICHIER_PARTICULIERS = "demandes_particuliers.txt";
    private static final String FICHIER_CENTRES = "centres_quartiers.txt";

    public static void main(String[] args) throws Exception {

        Scanner sc = new Scanner(System.in);
        Graphe g = new Graphe();
        g.chargerDepuisFichier("vincennes_sections_realnum.txt");

        System.out.println("Graphe chargé : " + g.intersections.size() + " intersections.");

        // Boucle principale : on demande le rôle
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
                    // Menu particulier : une seule action possible (demande de ramassage)
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

                    // On enregistre l'adresse dans le fichier (format : rue|numero)
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

                    // Après enregistrement on retourne au choix de rôle
                    continue mainLoop;
                }

                case 'M' -> {
                    // Menu mairie (boucle interne)
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
                                // Retour au choix de rôle
                                break mairieLoop;
                            }

                            case '0' -> {
                                // Afficher intersections (comme ancien case 1)
                                System.out.println("\n--- Intersections ---");
                                for (Intersection i : g.intersections.values())
                                    System.out.println(i.id + " : " + i.sortants.size() + " arcs sortants");
                            }

                            case '1' -> {
                                // Calculer l'itinéraire depuis une adresse choisie par la mairie
                                // jusqu'à la première adresse dans le fichier particuliers
                                System.out.print("Nom rue départ : ");
                                String rueDep = sc.nextLine();
                                System.out.print("Numéro départ : ");
                                int numDep = Integer.parseInt(sc.nextLine());

                                // Lire la première adresse du fichier
                                List<String> demandes = lireDemandesParticuliers();
                                if (demandes.isEmpty()) {
                                    System.out.println("Aucune demande particulière dans " + FICHIER_PARTICULIERS);
                                    break;
                                }
                                String premiere = demandes.get(0); // format rue|num
                                String[] parts = premiere.split("\\|");
                                String rueArr = parts[0];
                                int numArr = Integer.parseInt(parts[1]);

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

                            case '2' -> {
                                // Faire le tour de toutes les adresses données par les particuliers
                                // Optimisation via MST comme dans ton ancien case 6 (on convertit demandes → Pointcollecte)
                                List<String> demandes = lireDemandesParticuliers();
                                if (demandes.isEmpty()) {
                                    System.out.println("Aucune adresse de particuliers trouvée.");
                                    break;
                                }

                                List<Pointcollecte> pointsCollecte = new ArrayList<>();
                                // on prend contenance = 1 pour chaque particulier (unités)
                                for (String d : demandes) {
                                    String[] p = d.split("\\|");
                                    if (p.length < 2) {
                                        System.out.println("⚠ Format d'adresse invalide (ignorée): " + d);
                                        continue;
                                    }
                                    String rue = p[0];
                                    int num;
                                    try {
                                        num = Integer.parseInt(p[1]);
                                    } catch (NumberFormatException nfe) {
                                        System.out.println("⚠ Numéro invalide (ignorée): " + d);
                                        continue;
                                    }
                                    Intersection inter = g.trouverIntersection(rue, num);
                                    if (inter != null) {
                                        pointsCollecte.add(new Pointcollecte(inter, 1, rue, num));
                                    } else {
                                        System.out.println("⚠ Adresse introuvable (ignorée): " + rue + " " + num);
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

                                // Ici on n'utilise pas la capacité : on affiche l'ordre optimisé
                                System.out.println("\nOrdre optimisé des adresses (MST préfixe) :");
                                int idx = 1;
                                for (String id : ordreVisite) {
                                    Pointcollecte pc = contenanceMap.get(id);
                                    if (pc != null)
                                        System.out.println(idx++ + " - " + id + " (Rue: " + pc.rue + " Num: " + pc.numero + ")");
                                    else
                                        System.out.println(idx++ + " - " + id + " (Point inconnu)");
                                }

                                // --- Affichage détaillé des chemins entre chaque adresse consécutive ---
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

                                    // on utilise Dijkstra existant (chemin en intersections)
                                    List<Intersection> chemin = g.Dijkstra(A.id, B.id);
                                    if (chemin == null || chemin.isEmpty()) {
                                        System.out.println("  ⚠ Aucun chemin trouvé !");
                                        continue;
                                    }

                                    // Affichage des intersections parcourues
                                    System.out.println("  Intersections parcourues :");
                                    for (Intersection inter : chemin) {
                                        System.out.println("    - " + inter.id);
                                    }

                                    // Affichage des rues empruntées et accumulation de la distance
                                    System.out.println("  Rues empruntées :");
                                    double distanceTotale = 0.0;
                                    for (int j = 0; j < chemin.size() - 1; j++) {
                                        Intersection X = chemin.get(j);
                                        Intersection Y = chemin.get(j + 1);

                                        // Chercher un arc X -> Y (respect du sens). Si introuvable, on cherche Y -> X au cas où graphe non orienté/logique)
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
                                // La mairie organise la recolte en partant d'une adresse donnée
                                System.out.print("Nom rue départ : ");
                                String rue = sc.nextLine();
                                System.out.print("Numéro départ : ");
                                int numero = Integer.parseInt(sc.nextLine());
                                System.out.print("Capacité camion : ");
                                int capacite = Integer.parseInt(sc.nextLine());

                                var tours = g.tourneesDepuisAdresse(rue, numero, capacite);
                                if (tours == null || tours.isEmpty()) System.out.println("⚠ Aucun tour trouvé !");
                                else {
                                    int numTour = 1;
                                    for (List<Arc> tour : tours) {
                                        System.out.println("\n=== Tournée " + numTour++ + " ===");
                                        for (Arc a : tour) System.out.println("Rue : " + a.nom + " (" + a.nbBatiments + " maisons)");
                                    }
                                }
                            }

                            case '4' -> {
                                // Ramassage des points de collecte : reprendre ton ancien case 6
                                List<Pointcollecte> pointsCollecte = new ArrayList<>();
                                // DEPOT (exemple) - garder ce que tu avais
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
                                // Organisation en fonction des jours : utilise case 7 que tu avais.
                                // Lecture des centres depuis FICHIER_CENTRES si disponible
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
                                        System.out.println("Aucun fichier " + FICHIER_CENTRES + " trouvé : utilisation des centres par défaut dans Graphe.");
                                    }
                                } catch (IOException ioe) {
                                    System.out.println("Erreur lecture centres : " + ioe.getMessage());
                                }

                                // Partitionner : si Graphe possède une méthode acceptant une liste de centres, on l'utilise (reflection),
                                // sinon on appelle partitionnerQuartiers() qui utilise ses centres internes.
                                boolean partitionnee = false;
                                try {
                                    Method m = g.getClass().getMethod("partitionnerParIntersections", List.class);
                                    m.invoke(g, centres);
                                    partitionnee = true;
                                } catch (NoSuchMethodException ns) {
                                    // méthode non présente : essayer d'affecter un champ 'centresQuartiers' si présent (peut être final)
                                    try {
                                        Field f = g.getClass().getDeclaredField("centresQuartiers");
                                        f.setAccessible(true);
                                        // attention : si champ final présent, set() peut lancer une exception
                                        f.set(g, centres);
                                        // appeler partitionnerQuartiers ensuite
                                        Method m2 = g.getClass().getMethod("partitionnerQuartiers");
                                        m2.invoke(g);
                                        partitionnee = true;
                                    } catch (NoSuchFieldException | IllegalAccessException | NoSuchMethodException ex) {
                                        // fallback
                                    }
                                } catch (Exception ex) {
                                    System.out.println("Erreur invocation partition par intersections : " + ex.getMessage());
                                }

                                if (!partitionnee) {
                                    // fallback simple
                                    g.partitionnerQuartiers();
                                }

                                int nbQuartiers = new HashSet<>(g.quartierArc.values()).size();
                                System.out.println("Partition en quartiers effectuée. Nombre de quartiers : " + nbQuartiers);

                                // Colorier
                                Map<Integer, Integer> couleurQuartier = g.colorierQuartiers();
                                System.out.println("\n=== Coloration des quartiers ===");
                                for (Map.Entry<Integer, Integer> e : couleurQuartier.entrySet()) {
                                    System.out.println("Quartier " + e.getKey() + " -> couleur " + e.getValue());
                                }

                                // Afficher les rues par quartier
                                System.out.println("\n=== Rues par quartier ===");
                                for (int qid : new HashSet<>(g.quartierArc.values())) {
                                    System.out.println("Quartier " + qid + " : ");
                                    for (Arc a : g.getArcsDuQuartier(qid)) {
                                        System.out.println("  - " + a.toString());
                                    }
                                    System.out.println();
                                }

                                // Réinitialiser les arcs si besoin
                                for (Arc a : g.getTousLesArcs()) a.utilise = false;

                                // Demander capacité camion
                                System.out.print("Capacité camion pour simulation des tournées : ");
                                int cap = Integer.parseInt(sc.nextLine());

                                // Préparer arcs par quartier
                                Map<Integer, List<Arc>> arcsParQuartier = new HashMap<>();
                                for (int qid : new HashSet<>(g.quartierArc.values())) {
                                    List<Arc> arcsQuartier = g.getArcsDuQuartier(qid);
                                    List<Arc> copies = new ArrayList<>();
                                    for (Arc a : arcsQuartier) copies.add(a.copierAvecNbBatiments(a.nbBatiments));
                                    arcsParQuartier.put(qid, copies);
                                }

                                // Couleurs ordonnées
                                Set<Integer> couleursSet = new HashSet<>(couleurQuartier.values());
                                List<Integer> couleursOrdonnees = new ArrayList<>(couleursSet);
                                Collections.sort(couleursOrdonnees);

                                int jour = 1;
                                boolean resteARamasser = true;
                                while (resteARamasser) {
                                    resteARamasser = false;

                                    int couleurDuJour = couleursOrdonnees.get((jour - 1) % couleursOrdonnees.size());
                                    System.out.println("\n=== Jour " + jour + " — couleur " + couleurDuJour + " ===");

                                    // Tous les quartiers de cette couleur
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

                                            if (prise == a.nbBatiments) it.remove();
                                            else {
                                                Arc reste = a.copierAvecNbBatiments(a.nbBatiments - prise);
                                                it.remove();
                                                arcsRestants.add(reste);
                                            }
                                        }

                                        System.out.println("\nQuartier " + qid + " (couleur " + couleurDuJour +
                                                ") -> Tournée (" + tourDuJour.size() + " arc) :");

                                        for (Arc a : tourDuJour) {
                                            System.out.println("  - " + a.nom + " (" + a.nbBatiments + " maisons)");
                                        }
                                    }

                                    jour++;
                                }

                                System.out.println("\nSimulation terminée (organisation par jours).");
                            }

                            case '6' -> {
                                // Recherche d'une intersection proche (comme ancien case 4)
                                System.out.print("Nom rue : ");
                                String rue = sc.nextLine();
                                System.out.print("Numéro : ");
                                int numero = Integer.parseInt(sc.nextLine());

                                Intersection proche = g.trouverIntersection(rue, numero);
                                if (proche != null)
                                    System.out.println("Intersection la plus proche : " + proche.id);
                                else
                                    System.out.println("⚠ Rue ou numéro non trouvé !");
                            }
                            case '7' -> {
                                StringBuilder dot = new StringBuilder("digraph G {\n");
                                dot.append("  rankdir=LR;\n");
                                dot.append("  graph [splines=true, overlap=false];\n");
                                dot.append("  node [shape=circle, style=filled, fillcolor=lightblue, fontname=\"Arial\", fontsize=14];\n");
                                dot.append("  edge [fontname=\"Arial\", fontsize=12, color=gray50];\n");

                                Set<String> arcsAjoutes = new HashSet<>();

                                for (Intersection i : g.intersections.values()) {

                                    // (C) Forme différente pour les intersections "majeures"
                                    if (i.sortants.size() >= 4) {
                                        dot.append("  \"").append(i.id).append("\"")
                                                .append(" [shape=doublecircle, fillcolor=lightgreen];\n");
                                    } else {
                                        dot.append("  \"").append(i.id).append("\";\n");
                                    }

                                    for (Arc a : i.sortants) {
                                        String key = i.id + "->" + a.arrivee.id + ":" + a.nom;

                                        if (!arcsAjoutes.contains(key)) {

                                            // (A) Couleur en fonction du nombre de bâtiments
                                            String color;
                                            if (a.nbBatiments > 10) color = "red";
                                            else if (a.nbBatiments > 5) color = "orange";
                                            else color = "green";

                                            // (B) Épaisseur de trait proportionnelle
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
                            default -> System.out.println("⚠ Choix invalide !");
                        } // end switch mairie
                    } // end while mairie
                } // end case M

                default -> {
                    System.out.println("Choix invalide, recommencez.");
                }
            } // end switch role
        } // end main loop
    } // end main

    // --------- Utilitaires pour lire le fichier des particuliers ----------
    private static List<String> lireDemandesParticuliers() {
        try {
            if (!Files.exists(Paths.get(FICHIER_PARTICULIERS))) return Collections.emptyList();
            List<String> lines = Files.readAllLines(Paths.get(FICHIER_PARTICULIERS), StandardCharsets.UTF_8);
            List<String> out = new ArrayList<>();
            for (String L : lines) {
                String s = L.trim();
                if (s.isEmpty()) continue;
                // si ligne contient des espaces on remplace par '|' si l'utilisateur a saisi autrement
                if (s.contains("|")) out.add(s);
                else {
                    // Supporte format "RueName 12" ou "RueName|12", on assume dernière token est le numero
                    String[] toks = s.split("\\s+");
                    String num = toks[toks.length - 1];
                    String rue = String.join("", Arrays.copyOf(toks, toks.length - 1));
                    // fallback si parsing raté : garder la ligne brute
                    try {
                        Integer.parseInt(num);
                        out.add(rue + "|" + num);
                    } catch (Exception ex) {
                        out.add(s); // on laisse tel quel (mais risque d'erreur plus tard)
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
