package sv.edu.ues.fia.controldemedicamentosyarticulosdelhogar;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class ViaAdministracionDAO {
    private SQLiteDatabase db;
    private Context context;

    public ViaAdministracionDAO(SQLiteDatabase db, Context context) {
        this.db = db;
        this.context = context;
    }

    public void addViaAdministracion(ViaAdministracion viaAdministracion) {
        if (isDuplicate(viaAdministracion.getIdViaAdministracion())) {
            Toast.makeText(context, R.string.duplicate_message, Toast.LENGTH_SHORT).show();
            return;
        }
        ContentValues values = new ContentValues();
        values.put("IDVIAADMINISTRACION", viaAdministracion.getIdViaAdministracion());
        values.put("TIPOADMINISTRACION", viaAdministracion.getTipoAdministracion());
        db.insert("VIAADMINISTRACION", null, values);
        Toast.makeText(context, R.string.save_message, Toast.LENGTH_SHORT).show();
    }

    public ViaAdministracion getViaAdministracion(int idViaAdministracion) {
        String sql = "SELECT * FROM VIAADMINISTRACION WHERE IDVIAADMINISTRACION=?";
        Cursor cursor = db.rawQuery(sql, new String[]{String.valueOf(idViaAdministracion)});
        if (cursor.moveToFirst()) {
            ViaAdministracion viaAdministracion = new ViaAdministracion(
                    cursor.getInt(cursor.getColumnIndexOrThrow("IDVIAADMINISTRACION")),
                    cursor.getString(cursor.getColumnIndexOrThrow("TIPOADMINISTRACION")),
                    context
            );
            cursor.close();
            return viaAdministracion;
        }
        cursor.close();
        Toast.makeText(context, R.string.not_found_message, Toast.LENGTH_SHORT).show();
        return null;
    }

    public List<ViaAdministracion> getAllViaAdministraciones() {
        List<ViaAdministracion> lista = new ArrayList<>();
        Cursor cursor = db.rawQuery("SELECT * FROM VIAADMINISTRACION", null);
        while (cursor.moveToNext()) {
            ViaAdministracion viaAdministracion = new ViaAdministracion(
                    cursor.getInt(cursor.getColumnIndexOrThrow("IDVIAADMINISTRACION")),
                    cursor.getString(cursor.getColumnIndexOrThrow("TIPOADMINISTRACION")),
                    context
            );
            lista.add(viaAdministracion);
        }
        cursor.close();
        return lista;
    }

    public void updateViaAdministracion(ViaAdministracion viaAdministracion) {
        ContentValues values = new ContentValues();
        values.put("TIPOADMINISTRACION", viaAdministracion.getTipoAdministracion());
        int rowsAffected = db.update("VIAADMINISTRACION", values, "IDVIAADMINISTRACION=?", new String[]{String.valueOf(viaAdministracion.getIdViaAdministracion())});
        if (rowsAffected == 0) {
            Toast.makeText(context, context.getString(R.string.not_found_message) + context.getString(R.string.id_via_administracion), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, R.string.update_message, Toast.LENGTH_SHORT).show();
        }
    }

    public void deleteViaAdministracion(int idViaAdministracion) {
        int rowsAffected = db.delete("VIAADMINISTRACION", "IDVIAADMINISTRACION=?", new String[]{String.valueOf(idViaAdministracion)});
        if (rowsAffected == 0) {
            Toast.makeText(context, context.getString(R.string.not_found_message) + context.getString(R.string.id_via_administracion), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, R.string.delete_message, Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isDuplicate(int idViaAdministracion) {
        String sql = "SELECT 1 FROM VIAADMINISTRACION WHERE IDVIAADMINISTRACION=?";
        Cursor cursor = db.rawQuery(sql, new String[]{String.valueOf(idViaAdministracion)});
        boolean exists = cursor.moveToFirst();
        cursor.close();
        return exists;
    }
}