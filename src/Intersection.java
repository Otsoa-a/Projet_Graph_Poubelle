import java.util.*;

public class Intersection {
    public final String id;
    public List<Arc> sortants = new ArrayList<>();
    public List<Arc> entrants = new ArrayList<>();

    // Nouvelle partie
    private String rue;
    private int numero;

    public Intersection(String id) {
        this.id = id;
    }

    public void setAdresse(String rue, int numero) {
        this.rue = rue;
        this.numero = numero;
    }

    public String getRue() {
        return rue != null ? rue : id;
    }

    public int getNumero() {
        return numero;
    }

    @Override
    public String toString() {
        return "Intersection " + id;
    }
}