import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class Graphe {
    // --- Centres fournis par toi ---
    private final List<String> centresQuartiers = List.of(
            "I2765","I955","I1023","I3724","I4758","I1443","I93","I5009",
            "I3158","I4211","I816","I1183","I1374","I4832","I8076","I2660"
    );

    // Résultats
    public Map<Intersection, Integer> quartierDe = new HashMap<>();
    public Map<Arc, Integer> quartierArc = new HashMap<>();

    public Map<String, Intersection> intersections = new HashMap<>(); //structure pour stocker notre graphe
    private Map<String, List<Arc>> arcsParRue = new HashMap<>();
    private List<Arc> tousLesArcs = new ArrayList<>();


    public Intersection getOrCreate(String id) {
        return intersections.computeIfAbsent(id, Intersection::new); //on ajoute ou on crée une intersection
    }

    //méthode pour ajouter un arc à partir de deux intersections, de sa longueur, de son nom, du nb de batiments, et si elle est sens unique)
    public void ajouterArc(String idDep, String idArr, String nomRue, double longueur, int nbBat, boolean sensUnique) {
        Intersection d = getOrCreate(idDep);
        Intersection a = getOrCreate(idArr);

        Arc arc = new Arc(nomRue, nbBat, longueur, sensUnique, d, a);

        d.sortants.add(arc);
        a.entrants.add(arc);

        tousLesArcs.add(arc);
        arcsParRue.computeIfAbsent(nomRue, k -> new ArrayList<>()).add(arc);
    }

    private void assignerNumeros() {
        for (List<Arc> liste : arcsParRue.values()) {
            int courant = 1;
            for (Arc a : liste) {
                a.numeroDebut = courant;
                a.numeroFin = courant + a.nbBatiments - 1;
                courant += a.nbBatiments;
            }
        }
    }

    public Intersection trouverIntersection(String nomRue, int numero) {

        List<Arc> liste = arcsParRue.get(nomRue);
        if (liste == null) return null;

        Arc cible = null;

        // Trouver l'arc contenant ce numéro
        for (Arc a : liste) {
            if (numero >= a.numeroDebut && numero <= a.numeroFin) {
                cible = a;
                break;
            }
        }

        if (cible == null) return null;

        // Ratio de position
        int index = numero - cible.numeroDebut + 1;
        double ratio = index / (double)cible.nbBatiments;

        return (ratio < 0.5) ? cible.depart : cible.arrivee;
    }


    //on récup le graphe depuis le fichier texte et on crée un arc pour chaque section et un sommet pour chaque intersection
    public void chargerDepuisFichier(String chemin) throws Exception {
        try (BufferedReader br = new BufferedReader(new FileReader(chemin))) {
            String ligne;
            while ((ligne = br.readLine()) != null) {
                ligne = ligne.trim();
                if (ligne.isEmpty() || ligne.startsWith("#")){
                    continue;
                }
                if (ligne.startsWith("INTERSECTION")) {
                    String[] t = ligne.split("\\s+");
                    String id = t[1];
                    getOrCreate(id);
                }
                else if (ligne.startsWith("SECTION")) {
                    String[] t = ligne.split("\\s+");
                    String sectionID = t[1];
                    String nomRue = t[2];
                    String idDepart = t[3];
                    String idArrivee = t[4];
                    double longueur = Double.parseDouble(t[5]);
                    boolean sensUnique = t[6].equals("D");
                    int nbBat = Integer.parseInt(t[7]);
                    ajouterArc(idDepart, idArrivee, nomRue, longueur, nbBat, sensUnique);
                }
            }
            assignerNumeros();
        }
    }

    public List<Intersection> DijkstraAdresse(
            String rueDepart, int numDepart,
            String rueArrivee, int numArrivee
    ) {
        Intersection iDep = trouverIntersection(rueDepart, numDepart);
        Intersection iArr = trouverIntersection(rueArrivee, numArrivee);

        if (iDep == null || iArr == null) {
            System.out.println("Adresse introuvable !");
            return null;
        }

        return Dijkstra(iDep.id, iArr.id);
    }

    //méthode dijkstra qui calcule le plus court chemin entre deux intersections
    public List<Intersection> Dijkstra (String depart, String arrivee) {
        Map<Intersection, Double> dist = new HashMap<>();
        Map<Intersection, Intersection> pred = new HashMap<>();
        PriorityQueue<Intersection> pq = new PriorityQueue<>(Comparator.comparingDouble(dist::get));

        Intersection start = this.intersections.get(depart);
        Intersection end = this.intersections.get(arrivee);

        for (Intersection i : this.intersections.values()){
            dist.put(i, Double.POSITIVE_INFINITY);
        }
        dist.put(start, 0.0);
        pq.add(start);
        while (!pq.isEmpty()) {
            Intersection u = pq.poll();

            if (u == end) break;

            for (Arc arc : u.sortants) {
                Intersection v = arc.arrivee;
                double alt = dist.get(u) + arc.longueur;

                if (alt < dist.get(v)) {
                    dist.put(v, alt);
                    pred.put(v, u);
                    pq.add(v);
                }
            }
        }
        LinkedList<Intersection> chemin = new LinkedList<>();
        Intersection curr = end;
        while (curr != null) {
            chemin.addFirst(curr);
            curr = pred.get(curr);
        }
        return chemin;
    }

    //Méthode simple pour les tournées des poubelles (récup les poubelles de chaque maison)
    //Ici on ne prend pas en compte le retour. On considère que le camion passe par les maisons,
    // quand il est rempli on commence une nouvelle tournée en passant à l'intersection la plus proche
    public List<List<Arc>> tournéesEuleriennes(Intersection depart, int maxBatiments) { // on renvoie une liste de tournée
        List<List<Arc>> tournees = new ArrayList<>(); //une tournée est une liste d'arc
        Stack<Intersection> pile = new Stack<>(); //pile des intersections à explorer
        pile.push(depart);
        List<Arc> currentTournee = new ArrayList<>(); //tournée actuelle
        int batimentsCourants = 0;
        while (!pile.isEmpty()) {
            Intersection current = pile.peek(); //on regarde le prochain sommet
            Arc nextArc = null;
            for (Arc a : current.sortants) { //chaque arc sortant du sommet est exploré  s'il n'est pas déjà marqué
                if (!a.utilise) {
                    nextArc = a;
                    break;
                }
            }
            if (nextArc != null) {
                nextArc.utilise = true;
                pile.push(nextArc.arrivee);
                //on ajoute à la tournée l'arc seulement si le camion ne dépasse pas sa limite de stockage
                if (batimentsCourants + nextArc.nbBatiments > maxBatiments) {
                    tournees.add(currentTournee);
                    currentTournee = new ArrayList<>();
                    batimentsCourants = 0;
                }
                currentTournee.add(nextArc);
                batimentsCourants += nextArc.nbBatiments;
            } else {
                pile.pop();
            }
        }
        if (!currentTournee.isEmpty()) {
            tournees.add(currentTournee); //on ajoute la dernière tournée
        }
        return tournees;
    }

    public List<List<Arc>> tourneesDepuisAdresse(String rue, int numero, int maxBatiments) {

        Intersection depart = trouverIntersection(rue, numero);
        if (depart == null) {
            System.out.println("Adresse introuvable !");
            return null;
        }

        return tournéesEuleriennes(depart, maxBatiments);
    }


    // Ajoute ceci dans ta classe Graphe
    public List<List<Pointcollecte>> decouperTournees(
            List<String> ordreVisite,
            Map<String, Pointcollecte> contenanceMap,
            int capaciteCamion) {

        List<List<Pointcollecte>> tournees = new ArrayList<>();
        List<Pointcollecte> currentTournee = new ArrayList<>();
        int charge = 0;

        if (ordreVisite == null || contenanceMap == null) return tournees;

        for (String id : ordreVisite) {
            Pointcollecte origine = contenanceMap.get(id);
            if (origine == null) continue;

            while (origine.contenance > 0) {
                int espace = capaciteCamion - charge;

                if (espace == 0) {
                    // Camion plein, on ferme la tournée
                    if (!currentTournee.isEmpty()) {
                        tournees.add(currentTournee);
                    }
                    currentTournee = new ArrayList<>();
                    charge = 0;
                    espace = capaciteCamion;
                }

                int prise = Math.min(espace, origine.contenance);

                // Créer un point fractionné
                Pointcollecte prisePt = origine.fractionner(prise);

                currentTournee.add(prisePt);
                charge += prise;

                // Décrémenter la contenance réelle
                origine.contenance -= prise;

                // Si camion plein après cette prise, fermer la tournée
                if (charge >= capaciteCamion) {
                    tournees.add(currentTournee);
                    currentTournee = new ArrayList<>();
                    charge = 0;
                }
            }
        }

        if (!currentTournee.isEmpty()) {
            tournees.add(currentTournee);
        }

        return tournees;
    }






    public double distanceEntre(Intersection depart, Intersection arrivee) {
        if (depart == null || arrivee == null) return Double.POSITIVE_INFINITY;

        Map<Intersection, Double> dist = new HashMap<>();
        PriorityQueue<Intersection> pq = new PriorityQueue<>(Comparator.comparingDouble(dist::get));

        for (Intersection i : intersections.values()) dist.put(i, Double.POSITIVE_INFINITY);
        dist.put(depart, 0.0);
        pq.add(depart);

        while (!pq.isEmpty()) {
            Intersection u = pq.poll();
            if (u == arrivee) break;

            for (Arc a : u.sortants) {
                Intersection v = a.arrivee;
                double alt = dist.get(u) + a.longueur;
                if (alt < dist.get(v)) {
                    dist.put(v, alt);
                    pq.add(v);
                }
            }
        }
        return dist.getOrDefault(arrivee, Double.POSITIVE_INFINITY);
    }

    // --- Partition du graphe en quartiers en fonction des centres ---
    public void partitionnerQuartiers() {

        Queue<Intersection> file = new ArrayDeque<>();
        Map<Intersection, Integer> dist = new HashMap<>();

        // Initialisation
        int q = 0;
        for (String id : centresQuartiers) {
            Intersection c = intersections.get(id);
            if (c != null) {
                quartierDe.put(c, q);
                dist.put(c, 0);
                file.add(c);
                q++;
            }
        }

        // BFS multi-source
        while (!file.isEmpty()) {
            Intersection u = file.poll();
            int qU = quartierDe.get(u);

            for (Arc a : u.sortants) {
                Intersection v = a.arrivee;
                if (!dist.containsKey(v)) {
                    dist.put(v, dist.get(u) + 1);
                    quartierDe.put(v, qU);
                    file.add(v);
                }
            }
            for (Arc a : u.entrants) {
                Intersection v = a.depart;
                if (!dist.containsKey(v)) {
                    dist.put(v, dist.get(u) + 1);
                    quartierDe.put(v, qU);
                    file.add(v);
                }
            }
        }

        // Attribuer un quartier aux arcs (quartier du départ)
        for (Arc a : tousLesArcs) {
            quartierArc.put(a, quartierDe.get(a.depart));
        }

        System.out.println("Partition terminée : " + q + " quartiers définis.");
    }
    // --- Coloration des quartiers ---
    public Map<Integer, Integer> colorierQuartiers() {

        // Construire adjacency quartier → régions voisines
        Map<Integer, Set<Integer>> adj = new HashMap<>();
        for (Arc a : tousLesArcs) {
            int q1 = quartierArc.get(a);
            int q2 = quartierDe.get(a.arrivee);
            if (q1 != q2) {
                adj.computeIfAbsent(q1, k -> new HashSet<>()).add(q2);
                adj.computeIfAbsent(q2, k -> new HashSet<>()).add(q1);
            }
        }

        // Coloration gloutonne
        Map<Integer, Integer> couleur = new HashMap<>();

        List<Integer> quartiers = new ArrayList<>(adj.keySet());
        quartiers.sort(Integer::compare);

        for (int q : quartiers) {
            Set<Integer> interdit = adj.get(q).stream()
                    .map(couleur::get)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            int c = 0;
            while (interdit.contains(c)) c++;

            couleur.put(q, c);
        }

        System.out.println("Coloration terminée.");
        return couleur;
    }
    // --- Génération des tournées optimisées quartier par quartier ---
    public Map<Integer, List<List<Arc>>> genererTourneesOptimisees(int capaciteCamion) {

        Map<Integer, List<List<Arc>>> resultat = new HashMap<>();

        // Grouper les arcs par quartier
        Map<Integer, List<Arc>> arcsParQuartier = new HashMap<>();
        for (Arc a : tousLesArcs) {
            int q = quartierArc.get(a);
            arcsParQuartier.computeIfAbsent(q, k -> new ArrayList<>()).add(a);
        }

        // Pour chaque quartier
        for (int q : arcsParQuartier.keySet()) {

            List<Arc> arcsQ = arcsParQuartier.get(q);
            Set<Arc> nonCollectes = new HashSet<>(arcsQ);
            List<List<Arc>> tournees = new ArrayList<>();

            // centre du quartier
            Intersection centre = null;
            for (String id : centresQuartiers) {
                Intersection c = intersections.get(id);
                if (c != null && quartierDe.get(c) == q) {
                    centre = c;
                    break;
                }
            }
            if (centre == null) continue;

            // Tournées successives
            while (!nonCollectes.isEmpty()) {

                List<Arc> tournee = new ArrayList<>();
                int charge = 0;
                Intersection pos = centre;

                while (true) {

                    // Trouver l’arc non collecté le plus proche depuis pos
                    Arc meilleur = null;
                    double meilleureDist = Double.POSITIVE_INFINITY;

                    for (Arc a : nonCollectes) {
                        double d = distanceEntre(pos, a.depart);
                        if (d < meilleureDist) {
                            meilleureDist = d;
                            meilleur = a;
                        }
                    }

                    if (meilleur == null) break;

                    // Vérifier la capacité camion
                    if (charge + meilleur.nbBatiments > capaciteCamion)
                        break;

                    // Collecte
                    tournee.add(meilleur);
                    charge += meilleur.nbBatiments;
                    nonCollectes.remove(meilleur);

                    pos = meilleur.arrivee;
                }

                if (!tournee.isEmpty()) tournees.add(tournee);
            }

            resultat.put(q, tournees);
        }

        System.out.println("Tournées optimisées générées.");
        return resultat;
    }
    public List<Arc> getTousLesArcs() {
        return tousLesArcs;
    }
    public List<Arc> getArcsDuQuartier(int qid) {
        List<Arc> arcsQuartier = new ArrayList<>();
        for (Map.Entry<Arc,Integer> e : quartierArc.entrySet()) {
            if (e.getValue() == qid) arcsQuartier.add(e.getKey());
        }
        return arcsQuartier;
    }


}
