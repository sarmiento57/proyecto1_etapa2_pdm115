package sv.edu.ues.fia.controldemedicamentosyarticulosdelhogar;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.content.Context;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class SubCategoriaDAO {
    private SQLiteDatabase dbConection;
    private Context context;

    public SubCategoriaDAO(SQLiteDatabase dbConection, Context context) {
        this.dbConection = dbConection;
        this.context = context;
    }

    public boolean insertarSubCategoria(SubCategoria subCategoria) {
        long insercion = 0;
        String[] idCategoria = {Integer.toString(subCategoria.getIdCategoria())};
        Cursor categoria = getDbConection().query("CATEGORIA", null, "IDCATEGORIA = ?", idCategoria, null, null, null);
        if (categoria.getCount() == 1) {
            ContentValues subCategory = new ContentValues();
            subCategory.put("IDSUBCATEGORIA", subCategoria.getIdSubCategoria());
            subCategory.put("IDCATEGORIA", subCategoria.getIdCategoria());
            subCategory.put("NOMBRESUBCATEGORIA", subCategoria.getNombreSubCategoria());
            insercion = dbConection.insert("SUBCATEGORIA", null, subCategory);
            if (insercion == -1) {
                Toast.makeText(this.context, context.getString(R.string.duplicate_message), Toast.LENGTH_SHORT).show();
                return false;
            }
            return true;
        } else if (categoria.getCount() > 1) {
            Toast.makeText(this.context, context.getString(R.string.error_multiple_categories), Toast.LENGTH_SHORT).show();
            return false;
        } else {
            Toast.makeText(this.context, context.getString(R.string.error_category_not_found), Toast.LENGTH_SHORT).show();
            return false;
        }

    }

    public ArrayList<SubCategoria> getAllRows() {
        ArrayList<SubCategoria> listado = new ArrayList<SubCategoria>();
        Cursor listadoDB = getDbConection().query("SUBCATEGORIA", null, null, null, null, null, null);
        if (listadoDB.moveToFirst()) {
            listadoDB.moveToFirst();
            for (int i = 0; i < listadoDB.getCount(); i++) {
                SubCategoria subCategoria = new SubCategoria();
                subCategoria.setIdSubCategoria(listadoDB.getInt(0));
                subCategoria.setIdCategoria(listadoDB.getInt(1));
                subCategoria.setNombreSubCategoria(listadoDB.getString(2));
                listado.add(subCategoria);
                listadoDB.moveToNext();
            }
        }

        return listado;
    }

    public ArrayList<SubCategoria> getRowsFiltredByCategory(int idCategoria){
        String [] id = {Integer.toString(idCategoria)};
        ArrayList<SubCategoria> listado = new ArrayList<SubCategoria>();
        Cursor listadoDB = getDbConection().query("SUBCATEGORIA", null, "IDCATEGORIA = ?", id, null, null, null);
        if (listadoDB.moveToFirst()) {
            listadoDB.moveToFirst();
            for (int i = 0; i < listadoDB.getCount(); i++) {
                SubCategoria subCategoria = new SubCategoria();
                subCategoria.setIdSubCategoria(listadoDB.getInt(0));
                subCategoria.setIdCategoria(listadoDB.getInt(1));
                subCategoria.setNombreSubCategoria(listadoDB.getString(2));
                listado.add(subCategoria);
                listadoDB.moveToNext();
            }
        }
        return listado;
    }

    public int updateSubCategoria(SubCategoria subCategoria) {
        String[] idCategoria = {Integer.toString(subCategoria.getIdCategoria())};
        String[] id = {Integer.toString(subCategoria.getIdSubCategoria())};
        ContentValues cambios = new ContentValues();

        Cursor categoria = getDbConection().query("CATEGORIA", null, "IDCATEGORIA = ?", idCategoria, null, null, null);
        if (categoria.getCount() == 1) {
            cambios.put("IDSUBCATEGORIA", subCategoria.getIdSubCategoria());
            cambios.put("IDCATEGORIA", subCategoria.getIdCategoria());
            cambios.put("NOMBRESUBCATEGORIA", subCategoria.getNombreSubCategoria());
            int control = dbConection.update("SUBCATEGORIA", cambios, "IDSUBCATEGORIA = ?", id);
            if (control == 1) {
                return 1;
            } else {
                return 0;
            }

        } else if (categoria.getCount() < 1) {
            Toast.makeText(this.context, context.getString(R.string.error_category_not_found), Toast.LENGTH_SHORT).show();
            return 2;
        } else {
            Toast.makeText(this.context, context.getString(R.string.error_multiple_categories), Toast.LENGTH_SHORT).show();
            return 2;
        }

        /*Guia para metodo:
        Retorna un entero entre 0 y 2.
        0 = No se actualizo la subcategoria
        1 = Se actualizo con exito
        2 = hay problema con la CATEGORIA que se quiere asociar*/
    }

    public int deleteSubCategoria(SubCategoria subCategoria){
        String [] id = {Integer.toString(subCategoria.getIdSubCategoria())};
        int registros = dbConection.delete("SUBCATEGORIA","IDSUBCATEGORIA = ?", id);
        return registros;
    }
    public SQLiteDatabase getDbConection() {
        return dbConection;
    }

    public void setDbConecction(SQLiteDatabase dbConecction) {
        this.dbConection = dbConecction;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public List<Categoria> getAllCategoria() {
        List<Categoria> lista = new ArrayList<>();
        String sql = "SELECT * FROM CATEGORIA";
        Cursor cursor = dbConection.rawQuery(sql, null);

        if (cursor.moveToFirst()) {
            do {
                int idcategoria = cursor.getInt(cursor.getColumnIndexOrThrow("IDCATEGORIA"));
                String nombrecategoria = cursor.getString(cursor.getColumnIndexOrThrow("NOMBRECATEGORIA"));

                lista.add(new Categoria(idcategoria, nombrecategoria, context));
            } while (cursor.moveToNext());
        }

        cursor.close();
        return lista;
    }

}
