package sv.edu.ues.fia.controldemedicamentosyarticulosdelhogar;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class ProveedorDAO {
    private SQLiteDatabase conexionDB;
    private Context context;

    public ProveedorDAO(SQLiteDatabase conexionDB, Context context) {
        this.conexionDB = conexionDB;
        this.context = context;
    }

    public List<Proveedor> getAllProveedores() {
        List<Proveedor> proveedores = new ArrayList<>();
        String sql = "SELECT * FROM PROVEEDOR";
        Cursor cursor = conexionDB.rawQuery(sql, null);
        while (cursor.moveToNext()) {
            proveedores.add(new Proveedor(
                    cursor.getInt(cursor.getColumnIndexOrThrow("IDPROVEEDOR")),
                    cursor.getString(cursor.getColumnIndexOrThrow("NOMBREPROVEEDOR")),
                    cursor.getString(cursor.getColumnIndexOrThrow("TELEFONOPROVEEDOR")),
                    cursor.getString(cursor.getColumnIndexOrThrow("DIRECCIONPROVEEDOR")),
                    cursor.getString(cursor.getColumnIndexOrThrow("RUBROPROVEEDOR")),
                    cursor.getString(cursor.getColumnIndexOrThrow("NUMREGPROVEEDOR")),
                    cursor.getString(cursor.getColumnIndexOrThrow("NIT")),
                    cursor.getString(cursor.getColumnIndexOrThrow("GIROPROVEEDOR")),
                    context
            ));
        }
        cursor.close();
        return proveedores;
    }

    public Proveedor getProveedorById(int idProveedor) {
        Proveedor proveedor = null;
        String sql = "SELECT * FROM PROVEEDOR WHERE IDPROVEEDOR = ?";
        Cursor cursor = conexionDB.rawQuery(sql, new String[]{String.valueOf(idProveedor)});
        if (cursor.moveToFirst()) {
            proveedor = new Proveedor(
                    cursor.getInt(cursor.getColumnIndexOrThrow("IDPROVEEDOR")),
                    cursor.getString(cursor.getColumnIndexOrThrow("NOMBREPROVEEDOR")),
                    cursor.getString(cursor.getColumnIndexOrThrow("TELEFONOPROVEEDOR")),
                    cursor.getString(cursor.getColumnIndexOrThrow("DIRECCIONPROVEEDOR")),
                    cursor.getString(cursor.getColumnIndexOrThrow("RUBROPROVEEDOR")),
                    cursor.getString(cursor.getColumnIndexOrThrow("NUMREGPROVEEDOR")),
                    cursor.getString(cursor.getColumnIndexOrThrow("NIT")),
                    cursor.getString(cursor.getColumnIndexOrThrow("GIROPROVEEDOR")),
                    context
            );
        }
        cursor.close();
        return proveedor;
    }

    public void addProveedor(Proveedor proveedor) {
        if (isDuplicate(proveedor.getIdProveedor(), proveedor.getNitProveedor())) {
            Toast.makeText(context, context.getString(R.string.provider_exists), Toast.LENGTH_SHORT).show();
            return;
        }

        ContentValues values = new ContentValues();
        values.put("IDPROVEEDOR", proveedor.getIdProveedor());
        values.put("NOMBREPROVEEDOR", proveedor.getNombreProveedor());
        values.put("TELEFONOPROVEEDOR", proveedor.getTelefonoProveedor());
        values.put("DIRECCIONPROVEEDOR", proveedor.getDireccionProveedor());
        values.put("RUBROPROVEEDOR", proveedor.getRubroProveedor());
        values.put("NUMREGPROVEEDOR", proveedor.getNumRegProveedor());
        values.put("NIT", proveedor.getNitProveedor());
        values.put("GIROPROVEEDOR", proveedor.getGiroProveedor());
        conexionDB.insert("PROVEEDOR", null, values);
        Toast.makeText(context, R.string.save_message, Toast.LENGTH_SHORT).show();
    }

    public void updateProveedor(Proveedor proveedor) {
        if (isDuplicateNIT(proveedor.getNitProveedor(), proveedor.getIdProveedor())) {
            Toast.makeText(context, R.string.duplicate_nit_message, Toast.LENGTH_SHORT).show();
            return;
        }

        ContentValues values = new ContentValues();
        values.put("NOMBREPROVEEDOR", proveedor.getNombreProveedor());
        values.put("TELEFONOPROVEEDOR", proveedor.getTelefonoProveedor());
        values.put("DIRECCIONPROVEEDOR", proveedor.getDireccionProveedor());
        values.put("RUBROPROVEEDOR", proveedor.getRubroProveedor());
        values.put("NUMREGPROVEEDOR", proveedor.getNumRegProveedor());
        values.put("NIT", proveedor.getNitProveedor());
        values.put("GIROPROVEEDOR", proveedor.getGiroProveedor());

        int rows = conexionDB.update("PROVEEDOR", values, "IDPROVEEDOR = ?", new String[]{String.valueOf(proveedor.getIdProveedor())});
        if (rows == 0) {
            Toast.makeText(context, R.string.not_found_message, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, R.string.update_message, Toast.LENGTH_SHORT).show();
        }
    }


    public void deleteProveedor(int idProveedor) {
        int rows = conexionDB.delete("PROVEEDOR", "IDPROVEEDOR = ?", new String[]{String.valueOf(idProveedor)});
        if (rows == 0) {
            Toast.makeText(context, R.string.not_found_message, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, R.string.delete_message, Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isDuplicate(int idProveedor, String nit) {
        Cursor cursor = conexionDB.rawQuery(
                "SELECT COUNT(*) FROM PROVEEDOR WHERE IDPROVEEDOR = ? OR NIT = ?",
                new String[]{String.valueOf(idProveedor), nit}
        );
        boolean exists = false;
        if (cursor.moveToFirst()) {
            exists = cursor.getInt(0) > 0;
        }
        cursor.close();
        return exists;
    }

    private boolean isDuplicateNIT(String nit, int currentId) {
        Cursor cursor = conexionDB.rawQuery(
                "SELECT COUNT(*) FROM PROVEEDOR WHERE NIT = ? AND IDPROVEEDOR != ?",
                new String[]{nit, String.valueOf(currentId)}
        );
        boolean exists = false;
        if (cursor.moveToFirst()) {
            exists = cursor.getInt(0) > 0;
        }
        cursor.close();
        return exists;
    }


}

