import java.util.*;

public class Graphe {
    public Map<String, Intersection> intersections = new HashMap<>();

    public Intersection getOrCreate(String id) {
        return intersections.computeIfAbsent(id, Intersection::new);
    }

    public void ajouterArc(String idDepart, String idArrivee,
                           String nom, double longueur, int batiments,
                           boolean sensUnique) {

        Intersection d = getOrCreate(idDepart);
        Intersection a = getOrCreate(idArrivee);

        Arc arc = new Arc(nom, batiments, longueur, sensUnique, d, a);

        d.sortants.add(arc);
        a.entrants.add(arc);

        if (!sensUnique) {
            Arc retour = new Arc(nom, batiments, longueur, false, a, d);
            a.sortants.add(retour);
            d.entrants.add(retour);
        }
    }
}
