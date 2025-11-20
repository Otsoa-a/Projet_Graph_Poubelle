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
}
