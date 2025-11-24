import java.io.*;
import java.util.*;

public class Graphe {

    public Map<String, Intersection> intersections = new HashMap<>();

    // Récupère ou crée une intersection
    public Intersection getOrCreate(String id) {
        return intersections.computeIfAbsent(id, Intersection::new);
    }

    // Ajoute un arc
    public void ajouterArc(String idDepart, String idArrivee,
                           String nom, double longueur, int batiments,
                           boolean sensUnique) {

        Intersection d = getOrCreate(idDepart);
        Intersection a = getOrCreate(idArrivee);

        Arc arc = new Arc(nom, batiments, longueur, sensUnique, d, a);

        d.sortants.add(arc);
        a.entrants.add(arc);

        // Si rue à double sens
        if (!sensUnique) {
            Arc retour = new Arc(nom, batiments, longueur, false, a, d);
            a.sortants.add(retour);
            d.entrants.add(retour);
        }
    }

    // ======== LECTURE DU FICHIER TEXTE ========
    public void chargerDepuisFichier(String chemin) throws Exception {

        try (BufferedReader br = new BufferedReader(new FileReader(chemin))) {

            String ligne;

            while ((ligne = br.readLine()) != null) {

                ligne = ligne.trim();
                if (ligne.isEmpty() || ligne.startsWith("#"))
                    continue;

                // ---- INTERSECTION ----
                if (ligne.startsWith("INTERSECTION")) {
                    // Exemple : INTERSECTION I1 0 0
                    String[] t = ligne.split("\\s+");
                    String id = t[1];
                    getOrCreate(id);
                }

                // ---- SECTION = ARC ----
                else if (ligne.startsWith("SECTION")) {
                    // Format :
                    // SECTION S1 RueCentre I1 I2 50 D 8
                    String[] t = ligne.split("\\s+");

                    String sectionID = t[1];   // S1 (non utilisé)
                    String nomRue = t[2];
                    String idDepart = t[3];
                    String idArrivee = t[4];
                    double longueur = Double.parseDouble(t[5]);
                    boolean sensUnique = t[6].equals("D");
                    int nbBat = Integer.parseInt(t[7]);

                    ajouterArc(idDepart, idArrivee, nomRue, longueur, nbBat, sensUnique);
                }
            }
        }
    }

    public List<List<Arc>> tournéesEuleriennes(Intersection depart, int maxBatiments) {
        List<List<Arc>> tournees = new ArrayList<>();
        Stack<Intersection> pile = new Stack<>();
        pile.push(depart);

        List<Arc> currentTournee = new ArrayList<>();
        int batimentsCourants = 0;

        while (!pile.isEmpty()) {
            Intersection current = pile.peek();
            Arc nextArc = null;

            // Chercher une arête non utilisée
            for (Arc a : current.sortants) {
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
            tournees.add(currentTournee); // ajouter la dernière tournée
        }

        return tournees;
    }
}
