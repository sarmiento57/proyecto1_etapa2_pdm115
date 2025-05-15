package sv.edu.ues.fia.controldemedicamentosyarticulosdelhogar;

import android.content.ContentValues;
import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.Cursor;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class CategoriaDAO {

    private SQLiteDatabase dbConection;
    private Context context;

    public CategoriaDAO(SQLiteDatabase dbConection, Context context) {
        this.dbConection = dbConection;
        this.context = context;
    }

    //Create

    public boolean insertarCategoria(Categoria categoria){
        long insercion = 0;
        ContentValues category = new ContentValues();
        category.put("IDCATEGORIA", categoria.getIdCategoria());
        category.put("NOMBRECATEGORIA", categoria.getNombreCategoria());
        try {
            insercion = dbConection.insert("CATEGORIA", null, category);
            if (insercion == -1){
                Toast.makeText(this.context, context.getString(R.string.duplicate_message), Toast.LENGTH_SHORT).show();
                return false;
            }
            return true;
        } catch (SQLException e){
            e.printStackTrace();
        }
        return false;
    }

    public ArrayList<Categoria> getAllRows(){
        ArrayList<Categoria> listado = new ArrayList<Categoria>();
        Cursor listadoDB = getDbConection().query("CATEGORIA",null,null,null,null,null,null);
        if (listadoDB.moveToFirst()) {
            listadoDB.moveToFirst();
            for (int i = 0; i < listadoDB.getCount(); i++) {
                Categoria categoria = new Categoria();
                categoria.setIdCategoria(listadoDB.getInt(0));
                categoria.setNombreCategoria(listadoDB.getString(1));
                listado.add(categoria);
                listadoDB.moveToNext();
            }
        }

        return listado;
    }

    public boolean updateCategoria(Categoria categoria){
        String [] id = {Integer.toString(categoria.getIdCategoria())};
        ContentValues cambios = new ContentValues();
        cambios.put("NOMBRECATEGORIA", categoria.getNombreCategoria());
        int control = dbConection.update("CATEGORIA",cambios,"IDCATEGORIA = ?",id);
        if(control == 1){
            return true;
        }else{
            return false;
        }

    }

    public int deleteCategoria(Categoria categoria){
        String [] id = {Integer.toString(categoria.getIdCategoria())};
        int registros = dbConection.delete("CATEGORIA","IDCATEGORIA = ?", id);
        return registros;

    }

    public SQLiteDatabase getDbConection() {
        return dbConection;
    }

    public void setDbConection(SQLiteDatabase dbConection) {
        this.dbConection = dbConection;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public Categoria getCategoria(int id) {
        Cursor cursor = getDbConection().rawQuery("SELECT * FROM CATEGORIA WHERE IDCATEGORIA = ?", new String[]{String.valueOf(id)});
        if (cursor.moveToFirst()) {
            Categoria categoria = new Categoria(
                    cursor.getInt(cursor.getColumnIndexOrThrow("IDCATEGORIA")),
                    cursor.getString(cursor.getColumnIndexOrThrow("NOMBRECATEGORIA"))
            );
            cursor.close();
            return categoria;
        }
        cursor.close();
        Toast.makeText(context, R.string.not_found_message, Toast.LENGTH_SHORT).show();
        return null;
    }
}