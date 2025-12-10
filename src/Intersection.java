import java.util.*;
//class d'intersectin
public class Intersection {
    public final String id; //identifiant de l intersection
    public List<Arc> sortants = new ArrayList<>(); //liste de toutes les sections de rue  qui partent de cette intersection
    public List<Arc> entrants = new ArrayList<>(); //liste de toutes les sections de rue qui arrivent à cette intersection


    private String rue;
    private int numero;

    public Intersection(String id) {
        this.id = id;
    }

    public String getRue() { // Renvoie le nom de la rue
        return rue != null ? rue : id;
    }

    @Override
    public String toString() {
        return "Intersection " + id;
    } // affiche l intersection
}