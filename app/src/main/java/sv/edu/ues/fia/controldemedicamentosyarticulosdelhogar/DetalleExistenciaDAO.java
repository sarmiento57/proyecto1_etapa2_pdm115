package sv.edu.ues.fia.controldemedicamentosyarticulosdelhogar;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class DetalleExistenciaDAO {

    private SQLiteDatabase conexionDB;
    private Context context;



    public DetalleExistenciaDAO(SQLiteDatabase conexionDB, Context context) {
        this.conexionDB = conexionDB;
        this.context = context;
    }


    public void addExistencia(DetalleExistencia detalleExistencia)
    {
        if (isDuplicate(detalleExistencia.getIdDetalleExistencia())) {
            Toast.makeText(context, R.string.duplicate_message, Toast.LENGTH_SHORT).show();
            return;
        }else {
            if (isUsed(detalleExistencia)) {
                Toast.makeText(context, R.string.duplicate_message , Toast.LENGTH_SHORT).show();
                return;
            }
            else {


                ContentValues values = new ContentValues();
                values.put("IDARTICULO", detalleExistencia.getIdArticulo());
                values.put("IDDETALLEEXISTENCIA", detalleExistencia.getIdDetalleExistencia());
                values.put("IDFARMACIA", detalleExistencia.getIdFarmacia());
                values.put("CANTIDADEXISTENCIA", detalleExistencia.getCantidadExistencia());
                values.put("FECHADEVENCIMIENTO", detalleExistencia.getFechaDeVencimiento());
                conexionDB.insert("DETALLEEXISTENCIA", null, values);
                Toast.makeText(context, R.string.save_message, Toast.LENGTH_SHORT).show();




            }
        }

    }


    public void updateExistencia(DetalleExistencia detalleExistencia)
    {

        ContentValues values = new ContentValues();
        values.put("CANTIDADEXISTENCIA", detalleExistencia.getCantidadExistencia());
        values.put("FECHADEVENCIMIENTO", detalleExistencia.getFechaDeVencimiento());
        int rowsAffected = conexionDB.update("DETALLEEXISTENCIA", values, "IDDETALLEEXISTENCIA = ?",
                new String[]{String.valueOf(detalleExistencia.getIdDetalleExistencia())});
        if (rowsAffected == 0) {
            Toast.makeText(context, R.string.not_found_message, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, R.string.update_message, Toast.LENGTH_SHORT).show();
        }
    }


    public List<DetalleExistencia> getAllDetallesExistencia(){
        List<DetalleExistencia> list = new ArrayList<>();
        String sql = "SELECT * FROM DETALLEEXISTENCIA";
        Cursor cursor = conexionDB.rawQuery(sql, null);
        while (cursor.moveToNext()) {
            list.add(new DetalleExistencia(
                    cursor.getInt(cursor.getColumnIndexOrThrow("IDARTICULO")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("IDDETALLEEXISTENCIA")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("IDFARMACIA")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("CANTIDADEXISTENCIA")),
                    cursor.getString(cursor.getColumnIndexOrThrow("FECHADEVENCIMIENTO")),
                    context


            ));
        }
        cursor.close();
        return list;
    }


    public List<Articulo> getAllArticulos(){
        List<Articulo> list = new ArrayList<>();
        String sql = "SELECT * FROM ARTICULO";
        Cursor cursor = conexionDB.rawQuery(sql, null);
        while (cursor.moveToNext()) {
            list.add(new Articulo(
                    cursor.getInt(cursor.getColumnIndexOrThrow("IDARTICULO")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("IDMARCA")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("IDVIAADMINISTRACION")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("IDSUBCATEGORIA")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("IDFORMAFARMACEUTICA")),
                    cursor.getString(cursor.getColumnIndexOrThrow("NOMBREARTICULO")),
                    cursor.getString(cursor.getColumnIndexOrThrow("DESCRIPCIONARTICULO")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("RESTRINGIDOARTICULO")) == 1,
                    cursor.getDouble(cursor.getColumnIndexOrThrow("PRECIOARTICULO"))
            ));
        }
        cursor.close();
        return list;
    }


    public Articulo getArticulo(int id)
    {
        String sql = "SELECT * FROM ARTICULO WHERE IDARTICULO = ?";
        Cursor cursor = conexionDB.rawQuery(sql, new String[]{String.valueOf(id)});
        if (cursor.moveToFirst()) {
            Articulo articulo = new Articulo(
                    cursor.getInt(cursor.getColumnIndexOrThrow("IDARTICULO")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("IDMARCA")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("IDVIAADMINISTRACION")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("IDSUBCATEGORIA")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("IDFORMAFARMACEUTICA")),
                    cursor.getString(cursor.getColumnIndexOrThrow("NOMBREARTICULO")),
                    cursor.getString(cursor.getColumnIndexOrThrow("DESCRIPCIONARTICULO")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("RESTRINGIDOARTICULO")) == 1,
                    cursor.getDouble(cursor.getColumnIndexOrThrow("PRECIOARTICULO"))
            );
            cursor.close();
            return articulo;
        }
            cursor.close();
            Toast.makeText(context, R.string.not_found_message, Toast.LENGTH_SHORT).show();
            return null;

    }


    private boolean isDuplicate(int id) {
        String sql = "SELECT 1 FROM DETALLEEXISTENCIA WHERE IDDETALLEEXISTENCIA = ?";
        Cursor cursor = conexionDB.rawQuery(sql, new String[]{String.valueOf(id)});
        boolean exists = cursor.moveToFirst();
        cursor.close();
        return exists;
    }

    private boolean isUsed(DetalleExistencia detalleExistencia) {
        String sql = "SELECT 1 FROM DETALLEEXISTENCIA WHERE IDARTICULO = ? AND IDFARMACIA = ?";
        Cursor cursor = conexionDB.rawQuery(sql, new String[]{
                String.valueOf(detalleExistencia.getIdArticulo()),
                String.valueOf(detalleExistencia.getIdFarmacia())
        });
        boolean exists = cursor.moveToFirst();
        cursor.close();
        return exists;
    }



    public List<DetalleExistencia> getAllDetallesExistenciaByIdArticulo(int id){
        List<DetalleExistencia> list = new ArrayList<>();
        String sql = "SELECT * FROM DETALLEEXISTENCIA WHERE IDARTICULO = ?";
        Cursor cursor = conexionDB.rawQuery(sql, new String[]{String.valueOf(id)});
        while (cursor.moveToNext()) {
            list.add(new DetalleExistencia(
                    cursor.getInt(cursor.getColumnIndexOrThrow("IDARTICULO")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("IDDETALLEEXISTENCIA")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("IDFARMACIA")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("CANTIDADEXISTENCIA")),
                    cursor.getString(cursor.getColumnIndexOrThrow("FECHADEVENCIMIENTO")),
                    context

            ));
        }
        cursor.close();
        return list;
    }

    public List<DetalleExistencia> getAllDetallesExistenciaByIdFarm(int id){
        List<DetalleExistencia> list = new ArrayList<>();
        String sql = "SELECT * FROM DETALLEEXISTENCIA WHERE IDFARMACIA = ?";
        Cursor cursor = conexionDB.rawQuery(sql, new String[]{String.valueOf(id)});
        while (cursor.moveToNext()) {
            list.add(new DetalleExistencia(
                    cursor.getInt(cursor.getColumnIndexOrThrow("IDARTICULO")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("IDDETALLEEXISTENCIA")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("IDFARMACIA")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("CANTIDADEXISTENCIA")),
                    cursor.getString(cursor.getColumnIndexOrThrow("FECHADEVENCIMIENTO")),
                    context


            ));
        }
        cursor.close();
        return list;
    }


    public void deleteExistencia(int id)
    {

        int rowsAffected = conexionDB.delete("DETALLEEXISTENCIA", "IDDETALLEEXISTENCIA = ?", new String[]{String.valueOf(id)});
        if (rowsAffected == 0) {
            Toast.makeText(context, R.string.not_found_message, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, R.string.delete_message, Toast.LENGTH_SHORT).show();
        }

    }


}
