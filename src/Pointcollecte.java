
//classe de point de collecte avec toute les infos nécessaires.
class Pointcollecte {
    Intersection inter;  //Intersection réelle sur le graphe
    int contenance;     //Contenance restante pour ce point
    String id;          //ID de l'intersection
    String rue;         //Rue d'origine
    int numero;         //Numéro d'origine

    public Pointcollecte(Intersection inter, int contenance, String rue, int numero) {
        this.inter = inter;
        this.contenance = contenance;
        this.id = inter.id;
        this.rue = rue;
        this.numero = numero;
    }

    //créer un point fractionné pour le camion
    public Pointcollecte fractionner(int qte) {
        return new Pointcollecte(this.inter, qte, this.rue, this.numero);
    }
}
