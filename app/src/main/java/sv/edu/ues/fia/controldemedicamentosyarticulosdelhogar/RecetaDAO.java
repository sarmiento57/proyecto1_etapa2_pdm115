package sv.edu.ues.fia.controldemedicamentosyarticulosdelhogar;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class RecetaDAO {
    private SQLiteDatabase db;
    private Context context;

    public RecetaDAO(SQLiteDatabase db, Context context) {
        this.db = db;
        this.context = context;
    }

    public void addReceta(Receta receta) {
        if (!existeDoctor(receta.getIdDoctor())) {
            Toast.makeText(context, context.getString(R.string.not_found_message) + context.getString(R.string.id_doctor), Toast.LENGTH_SHORT).show();
            return;
        }

        if (!existeCliente(receta.getIdCliente())) {
            Toast.makeText(context, context.getString(R.string.not_found_message) + context.getString(R.string.id_doctor), Toast.LENGTH_SHORT).show();
            return;
        }

        if (isDuplicate(receta.getIdReceta())) {
            Toast.makeText(context, R.string.duplicate_message, Toast.LENGTH_SHORT).show();
            return;
        }

        ContentValues values = new ContentValues();
        values.put("IDDOCTOR", receta.getIdDoctor());
        values.put("IDCLIENTE", receta.getIdCliente());
        values.put("IDRECETA", receta.getIdReceta());
        values.put("FECHAEXPEDIDA", receta.getFechaExpedida());
        values.put("DESCRIPCION", receta.getDescripcion());

        db.insert("RECETA", null, values);
        Toast.makeText(context, R.string.save_message, Toast.LENGTH_SHORT).show();
    }


    public Receta getReceta(int idReceta) {
        String sql = "SELECT * FROM RECETA WHERE IDRECETA=?";
        Cursor cursor = db.rawQuery(sql, new String[]{
                String.valueOf(idReceta)
        });

        if (cursor.moveToFirst()) {
            Receta receta = new Receta(
                    cursor.getInt(cursor.getColumnIndexOrThrow("IDDOCTOR")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("IDCLIENTE")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("IDRECETA")),
                    cursor.getString(cursor.getColumnIndexOrThrow("FECHAEXPEDIDA")),
                    cursor.getString(cursor.getColumnIndexOrThrow("DESCRIPCION")),
                    context
            );
            cursor.close();
            return receta;
        }

        cursor.close();
        Toast.makeText(context, R.string.not_found_message, Toast.LENGTH_SHORT).show();
        return null;
    }

    public List<Receta> getAllRecetas() {
        List<Receta> lista = new ArrayList<>();
        Cursor cursor = db.rawQuery("SELECT * FROM RECETA", null);

        while (cursor.moveToNext()) {
            Receta receta = new Receta(
                    cursor.getInt(cursor.getColumnIndexOrThrow("IDDOCTOR")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("IDCLIENTE")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("IDRECETA")),
                    cursor.getString(cursor.getColumnIndexOrThrow("FECHAEXPEDIDA")),
                    cursor.getString(cursor.getColumnIndexOrThrow("DESCRIPCION")),
                    context
            );
            lista.add(receta);
        }
        cursor.close();
        return lista;
    }

    public void updateReceta(Receta receta) {
        ContentValues values = new ContentValues();
        values.put("FECHAEXPEDIDA", receta.getFechaExpedida());
        values.put("DESCRIPCION", receta.getDescripcion());

        int rowsAffected = db.update(
                "RECETA",
                values,
                "IDRECETA=?",
                new String[]{
                        String.valueOf(receta.getIdReceta())
                });

        if (rowsAffected == 0) {
            Toast.makeText(context, context.getString(R.string.not_found_message) + context.getString(R.string.id_receta), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, R.string.update_message, Toast.LENGTH_SHORT).show();
        }
    }

    public void deleteReceta(int idReceta) {
        int rowsAffected = db.delete(
                "RECETA",
                "IDRECETA=?",
                new String[]{
                        String.valueOf(idReceta)
                });

        if (rowsAffected == 0) {
            Toast.makeText(context, context.getString(R.string.not_found_message) + context.getString(R.string.id_receta), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, R.string.delete_message, Toast.LENGTH_SHORT).show();
        }
    }

    public List<Doctor> getAllDoctor() {
        List<Doctor> lista = new ArrayList<>();
        String sql = "SELECT * FROM DOCTOR";
        Cursor cursor = db.rawQuery(sql, null);

        if (cursor.moveToFirst()) {
            do {
                int idDoctor = cursor.getInt(cursor.getColumnIndexOrThrow("IDDOCTOR"));
                String nombreDoctor = cursor.getString(cursor.getColumnIndexOrThrow("NOMBREDOCTOR"));

                lista.add(new Doctor(idDoctor, nombreDoctor, context));
            } while (cursor.moveToNext());
        }

        cursor.close();
        return lista;
    }

    public List<Cliente> getAllCliente() {
        List<Cliente> lista = new ArrayList<>();
        String sql = "SELECT * FROM CLIENTE";
        Cursor cursor = db.rawQuery(sql, null);

        if (cursor.moveToFirst()) {
            do {
                int idCliente = cursor.getInt(cursor.getColumnIndexOrThrow("IDCLIENTE"));
                String nombreCliente = cursor.getString(cursor.getColumnIndexOrThrow("NOMBRECLIENTE"));

                lista.add(new Cliente(idCliente, nombreCliente, context));
            } while (cursor.moveToNext());
        }

        cursor.close();
        return lista;
    }

    private boolean isDuplicate(int idReceta) {
        String sql = "SELECT 1 FROM RECETA WHERE IDRECETA=?";
        Cursor cursor = db.rawQuery(sql, new String[]{
                String.valueOf(idReceta)
        });

        boolean exists = cursor.moveToFirst();
        cursor.close();
        return exists;
    }


    private boolean existeDoctor(int idDoctor) {
        Cursor cursor = db.rawQuery("SELECT 1 FROM DOCTOR WHERE IDDOCTOR = ?", new String[]{String.valueOf(idDoctor)});
        boolean existe = cursor.moveToFirst();
        cursor.close();
        return existe;
    }

    private boolean existeCliente(int idCliente) {
        Cursor cursor = db.rawQuery("SELECT 1 FROM CLIENTE WHERE IDCLIENTE = ?", new String[]{String.valueOf(idCliente)});
        boolean existe = cursor.moveToFirst();
        cursor.close();
        return existe;
    }
}