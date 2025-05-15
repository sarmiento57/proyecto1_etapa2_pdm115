package sv.edu.ues.fia.controldemedicamentosyarticulosdelhogar;

import android.content.Context;

public class Direccion {
    private int idDireccion;
    private int idDistrito;
    private String direccionExacta;

    private Context context;

    public Direccion(int idDireccion, int idDistrito, String direccionExacta , Context context) {
        this.idDireccion = idDireccion;
        this.idDistrito = idDistrito;
        this.direccionExacta = direccionExacta;
        this.context = context;
    }

    public int getIdDireccion() {
        return idDireccion;
    }

    public void setIdDireccion(int idDireccion) {
        this.idDireccion = idDireccion;
    }

    public int getIdDistrito() {
        return idDistrito;
    }

    public void setIdDistrito(int idDistrito) {
        this.idDistrito = idDistrito;
    }

    public String getDireccionExacta() {
        return direccionExacta;
    }

    public void setDireccionExacta(String direccionExacta) {
        this.direccionExacta = direccionExacta;
    }

    @Override
    public String toString() {
        return direccionExacta;
    }

}
