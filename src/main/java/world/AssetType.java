package world;


public enum AssetType {
    TREE_JOEL("vegetation/arbre-joel.png",100,140,"tree"),
    TREE_THOMAS("vegetation/arbre-thomas.png",100,140,"tree"),
    TRENCH("obstacles/tranchee-module.png",150,45,"ground"),
    BUSH("vegetation/buisson-aubepine.png",52,30,"prop"),
    PINE_D1("vegetation/sapin_taille1_sombre.png",42,60,"tree"),
    PINE_D2("vegetation/sapin_taille2_sombre.png",50,75,"tree"),
    PINE_D3("vegetation/sapin_taille3_sombre.png",60,90,"tree"),
    PINE_D4("vegetation/sapin_taille4_sombre.png",70,106,"tree"),
    PINE_L1("vegetation/sapin_taille1_clair.png",42,60,"tree"),
    PINE_L2("vegetation/sapin_taille2_clair.png",50,75,"tree"),
    PINE_L3("vegetation/sapin_taille3_clair.png",60,90,"tree"),
    PINE_L4("vegetation/sapin_taille4_clair.png",70,106,"tree"),
    LEAF1("vegetation/arbre_feuillu_1.png",42,48,"tree"),
    LEAF2("vegetation/arbre_feuillu_2.png",54,60,"tree"),
    LEAF3("vegetation/arbre_feuillu_3.png",64,72,"tree"),
    LEAF4("vegetation/arbre_feuillu_4.png",78,86,"tree"),
    FENCE_LONG("obstacles/palissade_bois_40.png",92,35,"fence"),
    FENCE_MEDIUM("obstacles/palissade_bois_32.png",74,35,"fence"),
    FENCE_WIRE_SHORT("obstacles/palissade_bois_24_barbele.png",58,39,"fence"),
    FENCE_WIRE_MEDIUM("obstacles/palissade_bois_32_barbele.png",74,39,"fence"),
    FENCE_WIRE_LONG("obstacles/palissade_bois_40_barbele.png",92,39,"fence"),
    WIRE("obstacles/barbele_44.png",72,24,"fence"),
    HEDGEHOG("obstacles/herisson_tcheque.png",34,32,"prop"),
    BAGS3("obstacles/sacs_de_sable_x3.png",36,20,"prop"),
    BAGS4("obstacles/sacs_de_sable_x4.png",44,22,"prop"),
    BAGS5("obstacles/sacs_de_sable_x5.png",52,23,"prop"),
    CRATER1("sol/cratere_1.png",40,27,"ground"),
    CRATER2("sol/cratere_2.png",59,39,"ground"),
    CRATER_WATER("sol/cratere_3_eau.png",56,37,"ground"),
    BARREL("objets/fut_carburant.png",21,31,"prop"),
    LOGS("objets/pile_de_rondins.png",62,29,"prop"),
    AMMO("objets/caisse_munitions.png",30,25,"prop"),
    WRECK("vehicules/char_detruit.png",160,90,"prop"),
    TANK("vehicules/char-intact-v2.png",160,95,"prop"),
    GUN("vehicules/canon_artillerie.png",68,46,"prop"),
    SHELTER("batiments/abri-metal-v2.png",140,90,"building"),
    BARRACK_LARGE("batiments/baraquement-grand-v2.png",200,150,"building"),
    BARRACK_SMALL("batiments/baraquement-petit-v2.png",160,120,"building"),
    PERIMETER_H("batiments/palissade_enceinte_horizontale_260.png",92,29,"fence"),
    PERIMETER_V("batiments/palissade_enceinte_verticale_145.png",18,106,"vertical"),
    BRIDGE("batiments/pont_bois.png",80,106,"ground"),
    RUIN("batiments/ferme-en-ruine-v2.png",210,150,"building"),
    CABIN("batiments/cabane-brulee-v2.png",140,120,"building"),
    CHEST("../coffres/caisse-HUGO-fermee-v2.png",56,40,"prop"),
    OPEN_CHEST("../coffres/caisse-HUGO-ouverte-v2.png",56,56,"prop");

    public final String path;
    public final int width,height;
    public final String footprint;
    AssetType(String path,int width,int height,String footprint){
        this.path=path.startsWith("../")?"/assets/"+path.substring(3):"/assets/decor/"+path;
        this.width=width*2;this.height=height*2;this.footprint=footprint;
    }
}
