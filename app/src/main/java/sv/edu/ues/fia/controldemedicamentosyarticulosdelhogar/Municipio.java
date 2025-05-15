package sv.edu.ues.fia.controldemedicamentosyarticulosdelhogar;

public class Municipio {

    private int idMunicipio;

    private int idDepartamento;
    private String nombreMunicipio;


    public Municipio(int idMunicipio, int idDepartamento, String nombreMunicipio) {
        this.idMunicipio = idMunicipio;
        this.idDepartamento = idDepartamento;
        this.nombreMunicipio = nombreMunicipio;
    }

    public int getIdDepartamento() {
        return idDepartamento;
    }

    public void setIdDepartamento(int idDepartamento) {
        this.idDepartamento = idDepartamento;
    }

    public int getIdMunicipio() {
        return idMunicipio;
    }

    public void setIdMunicipio(int idMunicipio) {
        this.idMunicipio = idMunicipio;
    }

    public String getNombreMunicipio() {
        return nombreMunicipio;
    }

    public void setNombreMunicipio(String nombreMunicipio) {
        this.nombreMunicipio = nombreMunicipio;
    }

    @Override
    public String toString() {
        return nombreMunicipio;
    }
}
