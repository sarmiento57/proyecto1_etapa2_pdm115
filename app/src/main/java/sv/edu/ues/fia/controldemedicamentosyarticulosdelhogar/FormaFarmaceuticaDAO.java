package sv.edu.ues.fia.controldemedicamentosyarticulosdelhogar;

import java.util.ArrayList;
import java.util.List;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.content.ContentValues;
import android.widget.Toast;
import android.content.Context;

public class FormaFarmaceuticaDAO {
    private SQLiteDatabase conexionDB;
    private Context context;

    public FormaFarmaceuticaDAO(SQLiteDatabase conexionDB, Context context) {
        this.conexionDB = conexionDB;
        this.context = context;
    }

    public void addFormaFarmaceutica(FormaFarmaceutica formaFarmaceutica) {
        if (isDuplicate(formaFarmaceutica.getIdFormaFarmaceutica())) {
            Toast.makeText(context, R.string.duplicate_message, Toast.LENGTH_SHORT).show();
            return;
        }

        ContentValues values = new ContentValues();
        values.put("IDFORMAFARMACEUTICA", formaFarmaceutica.getIdFormaFarmaceutica());
        values.put("TIPOFORMAFARMACEUTICA", formaFarmaceutica.getTipoFormaFarmaceutica());
        conexionDB.insert("FORMAFARMACEUTICA", null, values);
        Toast.makeText(context, R.string.save_message, Toast.LENGTH_SHORT).show();
    }

    public FormaFarmaceutica getFormaFarmaceutica(int id) {
        String sql = "SELECT * FROM FORMAFARMACEUTICA WHERE IDFORMAFARMACEUTICA = ?";
        Cursor cursor = conexionDB.rawQuery(sql, new String[]{String.valueOf(id)});
        if (cursor.moveToFirst()) {
            FormaFarmaceutica formaFarmaceutica = new FormaFarmaceutica(
                    cursor.getInt(cursor.getColumnIndexOrThrow("IDFORMAFARMACEUTICA")),
                    cursor.getString(cursor.getColumnIndexOrThrow("TIPOFORMAFARMACEUTICA")),
                    context
            );
            cursor.close();
            return formaFarmaceutica;
        }
        cursor.close();
        Toast.makeText(context, R.string.not_found_message, Toast.LENGTH_SHORT).show();
        return null;
    }

    public List<FormaFarmaceutica> getAllFormaFarmaceutica() {
        List<FormaFarmaceutica> list = new ArrayList<>();
        String sql = "SELECT * FROM FORMAFARMACEUTICA";
        Cursor cursor = conexionDB.rawQuery(sql, null);
        while (cursor.moveToNext()) {
            list.add(new FormaFarmaceutica(
                    cursor.getInt(cursor.getColumnIndexOrThrow("IDFORMAFARMACEUTICA")),
                    cursor.getString(cursor.getColumnIndexOrThrow("TIPOFORMAFARMACEUTICA")),
                    context
            ));
        }
        cursor.close();
        return list;
    }

    public void updateFormaFarmaceutica(FormaFarmaceutica formaFarmaceutica) {
        ContentValues values = new ContentValues();
        values.put("TIPOFORMAFARMACEUTICA", formaFarmaceutica.getTipoFormaFarmaceutica());
        int rowsAffected = conexionDB.update("FORMAFARMACEUTICA", values, "IDFORMAFARMACEUTICA = ?", new String[]{String.valueOf(formaFarmaceutica.getIdFormaFarmaceutica())});
        if (rowsAffected == 0) {
            Toast.makeText(context, R.string.not_found_message, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, R.string.update_message, Toast.LENGTH_SHORT).show();
        }
    }

    public void deleteFormaFarmaceutica(int id) {
        int rowsAffected = conexionDB.delete("FORMAFARMACEUTICA", "IDFORMAFARMACEUTICA = ?", new String[]{String.valueOf(id)});
        if (rowsAffected == 0) {
            Toast.makeText(context, R.string.not_found_message, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, R.string.delete_message, Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isDuplicate(int id) {
        String sql = "SELECT * FROM FORMAFARMACEUTICA WHERE IDFORMAFARMACEUTICA = ?";
        Cursor cursor = conexionDB.rawQuery(sql, new String[]{String.valueOf(id)});
        boolean exists = cursor.moveToFirst();
        cursor.close();
        return exists;
    }
}
