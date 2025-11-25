import java.io.*;
import java.util.*;

public class Graphe {

    public Map<String, Intersection> intersections = new HashMap<>(); //structure pour stocker notre graphe

    public Intersection getOrCreate(String id) {
        return intersections.computeIfAbsent(id, Intersection::new); //on ajoute ou on crée une intersection
    }
    //méthode pour ajouter un arc à partir de deux intersections, de sa longueur, de son nom, du nb de batiments, et si elle est sens unique)
    public void ajouterArc(String idDepart, String idArrivee, String nom, double longueur, int batiments, boolean sensUnique) {
        Intersection d = getOrCreate(idDepart);
        Intersection a = getOrCreate(idArrivee);
        Arc arc = new Arc(nom, batiments, longueur, sensUnique, d, a);

        d.sortants.add(arc);
        a.entrants.add(arc);

       //on ajoute un arc de retour si rue sens
        if (!sensUnique) {
            Arc retour = new Arc(nom, batiments, longueur, false, a, d);
            a.sortants.add(retour);
            d.entrants.add(retour);
        }
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
        }
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
}
