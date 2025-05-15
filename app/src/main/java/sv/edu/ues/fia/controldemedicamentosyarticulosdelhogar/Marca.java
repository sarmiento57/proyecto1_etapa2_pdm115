package sv.edu.ues.fia.controldemedicamentosyarticulosdelhogar;

import android.content.Context;

public class Marca {
    private int idMarca;
    private String nombreMarca;
    private Context context;

    public Marca(int idMarca, String nombreMarca, Context context) {
        this.idMarca = idMarca;
        this.nombreMarca = nombreMarca;
        this.context = context;
    }



    public int getIdMarca() {
        return idMarca;
    }

    public void setIdMarca(int idMarca) {
        this.idMarca = idMarca;
    }

    public String getNombreMarca() {
        return nombreMarca;
    }

    public void setNombreMarca(String nombreMarca) {
        this.nombreMarca = nombreMarca;
    }

    @Override
    public String toString() {
        return context.getString(R.string.id_marca) + ": " + idMarca + "\n" +
                context.getString(R.string.nombre_marca) + ": " + nombreMarca;
    }
}
