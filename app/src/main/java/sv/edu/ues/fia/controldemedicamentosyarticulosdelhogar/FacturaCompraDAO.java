package sv.edu.ues.fia.controldemedicamentosyarticulosdelhogar;

import java.util.ArrayList;
import java.util.List;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.content.ContentValues;
import android.widget.Toast;
import android.content.Context;

public class FacturaCompraDAO {
    private SQLiteDatabase conexionDB;
    private Context context;

    public FacturaCompraDAO(SQLiteDatabase conexionDB, Context context) {
        this.conexionDB = conexionDB;
        this.context = context;
    }

    // obtener todas las sucursales de farmacias
    public List<SucursalFarmacia> getAllFarmacias() {
        List<SucursalFarmacia> farmacias = new ArrayList<>();
        String sql = "SELECT * FROM SUCURSALFARMACIA";
        Cursor cursor = conexionDB.rawQuery(sql, null);
        while (cursor.moveToNext()) {
            farmacias.add(new SucursalFarmacia(
                    cursor.getInt(cursor.getColumnIndexOrThrow("IDFARMACIA")),
                    cursor.getString(cursor.getColumnIndexOrThrow("NOMBREFARMACIA"))
            ));
        }
        cursor.close();
        return farmacias;
    }

    // obtener todos los proveedores
    public List<Proveedor> getAllProveedores() {
        List<Proveedor> proveedores = new ArrayList<>();
        String sql = "SELECT * FROM PROVEEDOR";
        Cursor cursor = conexionDB.rawQuery(sql, null);
        while (cursor.moveToNext()) {
            proveedores.add(new Proveedor(
                    cursor.getInt(cursor.getColumnIndexOrThrow("IDPROVEEDOR")),
                    cursor.getString(cursor.getColumnIndexOrThrow("NOMBREPROVEEDOR")),
                    context
            ));
        }
        cursor.close();
        return proveedores;
    }
    public void addFacturaCompra(FacturaCompra facturaCompra) {
        if(isDuplicate(facturaCompra.getIdCompra())){
            Toast.makeText(context, R.string.duplicate_message, Toast.LENGTH_SHORT).show();
            return;
        }
        ContentValues contentValues = new ContentValues();
        contentValues.put("IDCOMPRA", facturaCompra.getIdCompra());
        contentValues.put("FECHACOMPRA", facturaCompra.getFechaCompra());
        contentValues.put("TOTALCOMPRA", facturaCompra.getTotalCompra());
        contentValues.put("IDFARMACIA", facturaCompra.getIdFarmacia());
        contentValues.put("IDPROVEEDOR", facturaCompra.getIdProveedor());
        conexionDB.insert("FACTURACOMPRA", null, contentValues);
        Toast.makeText(context, R.string.save_message, Toast.LENGTH_SHORT).show();
    }

    public void updateFacturaCompra(FacturaCompra facturaCompra) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("FECHACOMPRA", facturaCompra.getFechaCompra());
        contentValues.put("TOTALCOMPRA", facturaCompra.getTotalCompra());
        contentValues.put("IDFARMACIA", facturaCompra.getIdFarmacia());
        contentValues.put("IDPROVEEDOR", facturaCompra.getIdProveedor());
        int rowsAffected = conexionDB.update("FACTURACOMPRA", contentValues, "IDCOMPRA = ?", new String[]{String.valueOf(facturaCompra.getIdCompra())});

        if (rowsAffected == 0) {
            Toast.makeText(context, R.string.not_found_message, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, R.string.update_message, Toast.LENGTH_SHORT).show();
        }
    }

    public void deleteFacturaCompra(int idCompra) {
        int rowsAffected = conexionDB.delete("FACTURACOMPRA", "IDCOMPRA = ?", new String[]{String.valueOf(idCompra)});

        if (rowsAffected == 0) {
            Toast.makeText(context, R.string.not_found_message, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, R.string.delete_message, Toast.LENGTH_SHORT).show();
        }
    }

    // obtener todas las facturas de compra
    public List<FacturaCompra> getAllFacturaCompra() {
        List<FacturaCompra> facturasCompra = new ArrayList<>();
        String sql = "SELECT * FROM FACTURACOMPRA";
        Cursor cursor = conexionDB.rawQuery(sql, null);
        while (cursor.moveToNext()) {
            facturasCompra.add(new FacturaCompra(
                    cursor.getInt(cursor.getColumnIndexOrThrow("IDCOMPRA")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("IDFARMACIA")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("IDPROVEEDOR")),
                    cursor.getString(cursor.getColumnIndexOrThrow("FECHACOMPRA")),
                    cursor.getDouble(cursor.getColumnIndexOrThrow("TOTALCOMPRA")),
                    context
            ));
        }
        cursor.close();
        return facturasCompra;
    }

    // obetener las factturas por su id
    public FacturaCompra getFacturaCompra(int id) {
        String sql = "SELECT * FROM FACTURACOMPRA WHERE IDCOMPRA = ?";
        Cursor cursor = conexionDB.rawQuery(sql, new String[]{String.valueOf(id)});

        if (cursor.moveToFirst()) {
            FacturaCompra facturaCompra = new FacturaCompra(
                    cursor.getInt(cursor.getColumnIndexOrThrow("IDCOMPRA")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("IDFARMACIA")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("IDPROVEEDOR")),
                    cursor.getString(cursor.getColumnIndexOrThrow("FECHACOMPRA")),
                    cursor.getDouble(cursor.getColumnIndexOrThrow("TOTALCOMPRA")),

                    context
            );
            cursor.close();
            return facturaCompra;
        }
        cursor.close();
        Toast.makeText(context, R.string.not_found_message, Toast.LENGTH_SHORT).show();
        return null;
    }

    private boolean isDuplicate(int id) {
        String sql = "SELECT * FROM FACTURACOMPRA WHERE IDCOMPRA = ?";
        Cursor cursor = conexionDB.rawQuery(sql, new String[]{String.valueOf(id)});
        boolean exists = cursor.moveToFirst();
        cursor.close();
        return exists;
    }

    public int obtenerIdFacturaCompra() {
        Cursor cursor = conexionDB.rawQuery("SELECT MAX(IDCOMPRA) FROM FACTURACOMPRA", null);
        if (cursor.moveToFirst()) {
            int ultimoId = cursor.getInt(0);
            cursor.close();
            return ultimoId + 1;
        } else {
            cursor.close();
            return 1; // si no hay uno creado le mete por defecto 1
        }
    }

}
