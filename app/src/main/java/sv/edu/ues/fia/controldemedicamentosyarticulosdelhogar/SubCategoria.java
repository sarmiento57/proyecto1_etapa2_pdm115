package sv.edu.ues.fia.controldemedicamentosyarticulosdelhogar;

import android.content.Context;

public class SubCategoria {
    private int idSubCategoria;
    private int idCategoria;
    private String nombreSubCategoria;

    private Context context;

    public SubCategoria(int idSubCategoria, int idCategoria, String nombreSubCategoria) {
        this.idSubCategoria = idSubCategoria;
        this.idCategoria = idCategoria;
        this.nombreSubCategoria = nombreSubCategoria;
    }

    public SubCategoria(int idSubCategoria, String nombreSubCategoria, Context context) {
        this.idSubCategoria = idSubCategoria;
        this.nombreSubCategoria = nombreSubCategoria;
        this.context = context;
    }

    public SubCategoria() {
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public int getIdSubCategoria() {
        return idSubCategoria;
    }
    public void setIdSubCategoria(int idSubCategoria) {
        this.idSubCategoria = idSubCategoria;
    }

    public int getIdCategoria() {
        return idCategoria;
    }

    public void setIdCategoria(int idCategoria) {
        this.idCategoria = idCategoria;
    }

    public String getNombreSubCategoria() {
        return nombreSubCategoria;
    }

    public void setNombreSubCategoria(String nombreSubCategoria) {
        this.nombreSubCategoria = nombreSubCategoria;
    }



    @Override
    public String toString() {

        return "ID: "+ getIdSubCategoria() + "\n" +"Nombre: " + getNombreSubCategoria();
    }
}


