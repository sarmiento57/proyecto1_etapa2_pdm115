package sv.edu.ues.fia.controldemedicamentosyarticulosdelhogar;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class DireccionDAO {

    private SQLiteDatabase conexionDB;
    private Context context;

    public DireccionDAO(SQLiteDatabase conexionDB, Context context) {
        this.conexionDB = conexionDB;
        this.context = context;
    }

    public void addDireccion(Direccion direccion) {
        if (isDuplicate(direccion.getIdDireccion())) {
            Toast.makeText(context, R.string.duplicate_message, Toast.LENGTH_SHORT).show();
            return;
        }

        ContentValues values = new ContentValues();
        values.put("IDDIRECCION", direccion.getIdDireccion());
        values.put("IDDISTRITO", direccion.getIdDistrito());
        values.put("DIRECCIONEXACTA", direccion.getDireccionExacta());
        conexionDB.insert("DIRECCION", null, values);
        Toast.makeText(context, R.string.save_message, Toast.LENGTH_SHORT).show();
    }

    public Direccion getDireccion(int id) {
        String sql = "SELECT * FROM DIRECCION WHERE IDDIRECCION = ?";
        Cursor cursor = conexionDB.rawQuery(sql, new String[]{String.valueOf(id)});
        if (cursor.moveToFirst()) {
            Direccion direccion = new Direccion(
                    cursor.getInt(cursor.getColumnIndexOrThrow("IDDIRECCION")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("IDDISTRITO")),
                    cursor.getString(cursor.getColumnIndexOrThrow("DIRECCIONEXACTA")),
                    context
            );
            cursor.close();
            return direccion;
        }
        cursor.close();
        Toast.makeText(context, R.string.not_found_message, Toast.LENGTH_SHORT).show();
        return null;
    }



    public List<Direccion> getAllDireccion() {
        List<Direccion> list = new ArrayList<>();
        String sql = "SELECT * FROM DIRECCION";
        Cursor cursor = conexionDB.rawQuery(sql, null);
        while (cursor.moveToNext()) {
            list.add(new Direccion(
                    cursor.getInt(cursor.getColumnIndexOrThrow("IDDIRECCION")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("IDDISTRITO")),
                    cursor.getString(cursor.getColumnIndexOrThrow("DIRECCIONEXACTA")),
                    context
            ));
        }
        cursor.close();
        return list;
    }



    public List<Departamento> getAllDepartamentos() {
        List<Departamento> departamentos = new ArrayList<>();
        String sql = "SELECT * FROM DEPARTAMENTO";
        Cursor cursor = conexionDB.rawQuery(sql, null);
        while (cursor.moveToNext()) {
            departamentos.add(new Departamento(
                    cursor.getInt(cursor.getColumnIndexOrThrow("IDDEPARTAMENTO")),
                    cursor.getString(cursor.getColumnIndexOrThrow("NOMBREDEPARTAMENTO"))
            ));
        }
        cursor.close();
        return departamentos;
    }


    public Departamento getDepartamento(int id) {
        String sql = "SELECT * FROM DEPARTAMENTO WHERE IDDEPARTAMENTO = ?";
        Cursor cursor = conexionDB.rawQuery(sql, new String[]{String.valueOf(id)});
        if (cursor.moveToFirst()) {
            Departamento departamento= new Departamento(
                    cursor.getInt(cursor.getColumnIndexOrThrow("IDDEPARTAMENTO")),
                    cursor.getString(cursor.getColumnIndexOrThrow("NOMBREDEPARTAMENTO"))
            );
            cursor.close();
            return departamento;
        }
        cursor.close();
        Toast.makeText(context, R.string.not_found_message, Toast.LENGTH_SHORT).show();
        return null;
    }


    public Municipio getMunicipio(int id) {
        String sql = "SELECT * FROM MUNICIPIO WHERE IDMUNICIPIO = ?";
        Cursor cursor = conexionDB.rawQuery(sql, new String[]{String.valueOf(id)});
        if (cursor.moveToFirst()) {
            Municipio municipio= new Municipio(
                    cursor.getInt(cursor.getColumnIndexOrThrow("IDMUNICIPIO")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("IDDEPARTAMENTO")),
                    cursor.getString(cursor.getColumnIndexOrThrow("NOMBREMUNICIPIO"))
            );
            cursor.close();
            return municipio;
        }
        cursor.close();
        Toast.makeText(context, R.string.not_found_message, Toast.LENGTH_SHORT).show();
        return null;
    }


    public Distrito getDistrito(int id) {
        String sql = "SELECT * FROM DISTRITO WHERE IDDISTRITO = ?";
        Cursor cursor = conexionDB.rawQuery(sql, new String[]{String.valueOf(id)});
        if (cursor.moveToFirst()) {
            Distrito distrito= new Distrito(
                    cursor.getInt(cursor.getColumnIndexOrThrow("IDDISTRITO")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("IDMUNICIPIO")),
                    cursor.getString(cursor.getColumnIndexOrThrow("NOMBREDISTRITO"))
            );
            cursor.close();
            return distrito;
        }
        cursor.close();
        Toast.makeText(context, R.string.not_found_message, Toast.LENGTH_SHORT).show();
        return null;
    }

    public List<Municipio> getAllMunicipios(int id) {

        List<Municipio> municipios = new ArrayList<>();
        String sql = "SELECT * FROM MUNICIPIO WHERE IDDEPARTAMENTO = ?";
        Cursor cursor = conexionDB.rawQuery(sql, new String[]{String.valueOf(id)});
        while (cursor.moveToNext()) {
            municipios.add(new Municipio(
                    cursor.getInt(cursor.getColumnIndexOrThrow("IDMUNICIPIO")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("IDDEPARTAMENTO")),
                    cursor.getString(cursor.getColumnIndexOrThrow("NOMBREMUNICIPIO"))
            ));
        }
        cursor.close();

        return municipios;
    }

    public List<Distrito> getAllDistritos(int id) {

        List<Distrito> distritos = new ArrayList<>();
        String sql = "SELECT * FROM DISTRITO WHERE IDMUNICIPIO = ?";
        Cursor cursor = conexionDB.rawQuery(sql, new String[]{String.valueOf(id)});
        while (cursor.moveToNext()) {
            distritos.add(new Distrito(
                    cursor.getInt(cursor.getColumnIndexOrThrow("IDDISTRITO")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("IDMUNICIPIO")),
                    cursor.getString(cursor.getColumnIndexOrThrow("NOMBREDISTRITO"))
            ));
        }
        cursor.close();

        return distritos;
    }





    public void updateDireccion(Direccion direccion) {
        ContentValues values = new ContentValues();
        values.put("IDDISTRITO", direccion.getIdDistrito());
        values.put("DIRECCIONEXACTA", direccion.getDireccionExacta());
        int rowsAffected = conexionDB.update("DIRECCION", values, "IDDIRECCION = ?",
                new String[]{String.valueOf(direccion.getIdDireccion())});
        if (rowsAffected == 0) {
            Toast.makeText(context, R.string.not_found_message, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, R.string.update_message, Toast.LENGTH_SHORT).show();
        }
    }






    // hacer comprobacion de si esta siendo usada en una farmacia
    public void deleteDireccion(int id) {
            int rowsAffected = conexionDB.delete("DIRECCION", "IDDIRECCION = ?", new String[]{String.valueOf(id)});
            if (rowsAffected == 0) {
                Toast.makeText(context, R.string.not_found_message, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, R.string.delete_message, Toast.LENGTH_SHORT).show();
            }
    }

    private boolean isDuplicate(int id) {
        String sql = "SELECT * FROM DIRECCION WHERE IDDIRECCION = ?";
        Cursor cursor = conexionDB.rawQuery(sql, new String[]{String.valueOf(id)});
        boolean exists = cursor.moveToFirst();
        cursor.close();
        return exists;
    }
}



