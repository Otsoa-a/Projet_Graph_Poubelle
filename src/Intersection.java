import java.util.*;

public class Intersection {
    public final String id; // identifiant de l intersection
    public List<Arc> sortants = new ArrayList<>(); // liste de toutes les sections de rue  qui partent de cette intersection
    public List<Arc> entrants = new ArrayList<>(); //liste de toutes les sections de rue qui arrivent à cette intersection


    private String rue;
    private int numero;

    public Intersection(String id) {
        this.id = id;
    }

    public void setAdresse(String rue, int numero) {
        this.rue = rue;
        this.numero = numero;
    }

    public String getRue() { // Renvoie le nom de la rue
        return rue != null ? rue : id;
    }

    public int getNumero() { // renvoie le num de la rue
        return numero;
    }

    @Override
    public String toString() {
        return "Intersection " + id;
    } // affiche l intersection
}