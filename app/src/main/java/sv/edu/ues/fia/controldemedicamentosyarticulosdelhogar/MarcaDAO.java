package sv.edu.ues.fia.controldemedicamentosyarticulosdelhogar;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MarcaDAO {
    private SQLiteDatabase db;
    private Context context;

    public MarcaDAO(SQLiteDatabase db, Context context) {
        this.db = db;
        this.context = context;
    }

    public void addMarca(Marca marca) {
        if (isDuplicate(marca.getIdMarca())) {
            Toast.makeText(context, R.string.duplicate_message, Toast.LENGTH_SHORT).show();
            return;
        }

        ContentValues values = new ContentValues();
        values.put("IDMARCA", marca.getIdMarca());
        values.put("NOMBREMARCA", marca.getNombreMarca());

        db.insert("MARCA", null, values);
        Toast.makeText(context, R.string.save_message, Toast.LENGTH_SHORT).show();
    }

    public Marca getMarca(int id) {
        Cursor cursor = db.rawQuery("SELECT * FROM MARCA WHERE IDMARCA = ?", new String[]{String.valueOf(id)});
        if (cursor.moveToFirst()) {
            Marca marca = new Marca(
                    cursor.getInt(cursor.getColumnIndexOrThrow("IDMARCA")),
                    cursor.getString(cursor.getColumnIndexOrThrow("NOMBREMARCA")),
                    context
            );
            cursor.close();
            return marca;
        }
        cursor.close();
        Toast.makeText(context, R.string.not_found_message, Toast.LENGTH_SHORT).show();
        return null;
    }

    public List<Marca> getAllMarcas() {
        List<Marca> list = new ArrayList<>();
        Cursor cursor = db.rawQuery("SELECT * FROM MARCA", null);
        while (cursor.moveToNext()) {
            list.add(new Marca(
                    cursor.getInt(cursor.getColumnIndexOrThrow("IDMARCA")),
                    cursor.getString(cursor.getColumnIndexOrThrow("NOMBREMARCA")),
                    context
            ));
        }
        cursor.close();
        return list;
    }

    public void updateMarca(Marca marca) {
        ContentValues values = new ContentValues();
        values.put("NOMBREMARCA", marca.getNombreMarca());

        int rows = db.update("MARCA", values, "IDMARCA = ?", new String[]{String.valueOf(marca.getIdMarca())});
        Toast.makeText(context, rows > 0 ? R.string.update_message : R.string.not_found_message, Toast.LENGTH_SHORT).show();
    }

    public void deleteMarca(int id) {
        int rows = db.delete("MARCA", "IDMARCA = ?", new String[]{String.valueOf(id)});
        Toast.makeText(context, rows > 0 ? R.string.delete_message : R.string.not_found_message, Toast.LENGTH_SHORT).show();
    }

    private boolean isDuplicate(int id) {
        Cursor cursor = db.rawQuery("SELECT * FROM MARCA WHERE IDMARCA = ?", new String[]{String.valueOf(id)});
        boolean exists = cursor.moveToFirst();
        cursor.close();
        return exists;
    }
}
