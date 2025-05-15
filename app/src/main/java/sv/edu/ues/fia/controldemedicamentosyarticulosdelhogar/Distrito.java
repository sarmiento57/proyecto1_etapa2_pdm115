package sv.edu.ues.fia.controldemedicamentosyarticulosdelhogar;

public class Distrito {
    private int idDistrito;
    private int idMunicipio;
    private String nombreDistrito;

    public Distrito(int idDistrito, int idMunicipio, String nombreDistrito) {
        this.idDistrito = idDistrito;
        this.idMunicipio = idMunicipio;
        this.nombreDistrito = nombreDistrito;
    }

    public int getIdMunicipio() {
        return idMunicipio;
    }

    public void setIdMunicipio(int idMunicipio) {
        this.idMunicipio = idMunicipio;
    }

    public int getIdDistrito() {
        return idDistrito;
    }

    public void setIdDistrito(int idDistrito) {
        this.idDistrito = idDistrito;
    }

    public String getNombreDistrito() {
        return nombreDistrito;
    }

    public void setNombreDistrito(String nombreDistrito) {
        this.nombreDistrito = nombreDistrito;
    }

    @Override
    public String toString() {
        return nombreDistrito;
    }
}
