package sv.edu.ues.fia.controldemedicamentosyarticulosdelhogar;

import android.content.Context;

public class Categoria {
    private int idCategoria;
    private String nombreCategoria;
    private Context context;

    public Categoria(int idCategoria, String nombreCategoria) {
        this.idCategoria = idCategoria;
        this.nombreCategoria = nombreCategoria;
    }

    public Categoria(int idCategoria, String nombreCategoria, Context context) {
        this.idCategoria = idCategoria;
        this.nombreCategoria = nombreCategoria;
        this.context = context;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public Categoria(){}

    public int getIdCategoria() {
        return idCategoria;
    }

    public void setIdCategoria(int idCategoria) {
        this.idCategoria = idCategoria;
    }

    public String getNombreCategoria() {
        return nombreCategoria;
    }

    public void setNombreCategoria(String nombreCategoria) {
        this.nombreCategoria = nombreCategoria;
    }

    @Override
    public String toString() {

        return "ID: "+ getIdCategoria() + "\n" +"Nombre:" + getNombreCategoria();
    }
}

