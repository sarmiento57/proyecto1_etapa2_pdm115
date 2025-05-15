package sv.edu.ues.fia.controldemedicamentosyarticulosdelhogar;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class FacturaVentaDAO {
    private SQLiteDatabase conexionDB;
    private Context context;

    public FacturaVentaDAO(SQLiteDatabase conexionDB, Context context) {
        this.context = context;
        this.conexionDB = conexionDB;
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

    // obtener todos los Clientes
    public List<Cliente> getAllClientes() {
        List<Cliente> clientes  = new ArrayList<>();
        String sql = "SELECT * FROM CLIENTE";
        Cursor cursor = conexionDB.rawQuery(sql, null);
        while (cursor.moveToNext()) {
            clientes.add(new Cliente( // Added context
                    cursor.getInt(cursor.getColumnIndexOrThrow("IDCLIENTE")),
                    cursor.getString(cursor.getColumnIndexOrThrow("NOMBRECLIENTE")),
                    context // Added context
            ));
        }
        cursor.close();
        return clientes;
    }
    // --- Operaciones CRUD ---

    public void addFacturaVenta(FacturaVenta factura) {
        if(isDuplicate(factura.getIdVenta())){ //
            Toast.makeText(context, R.string.duplicate_message, Toast.LENGTH_SHORT).show();
            return;
        }
        ContentValues values = new ContentValues();
        values.put("IDVENTA", factura.getIdVenta());
        values.put("IDCLIENTE", factura.getIdCliente());
        values.put("IDFARMACIA", factura.getIdFarmacia());
        values.put("FECHAVENTA", factura.getFechaVenta());
        values.put("TOTALVENTA", factura.getTotalVenta());

        long result = conexionDB.insert("FACTURAVENTA", null, values);
        if (result == -1) {
            Toast.makeText(context, R.string.error_saving_sale_invoice, Toast.LENGTH_SHORT).show();
            Log.e("FacturaVentaDAO", "Error inserting sale invoice: ID " + factura.getIdVenta());
        } else {
            Toast.makeText(context, R.string.save_message, Toast.LENGTH_SHORT).show();
        }
    }


    public FacturaVenta getFacturaVentaById(int idVenta) {
        FacturaVenta factura = null;
        Cursor cursor = conexionDB.query("FACTURAVENTA", null, "IDVENTA = ?",
                new String[]{String.valueOf(idVenta)}, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            factura = new FacturaVenta( // Added context
                    cursor.getInt(cursor.getColumnIndexOrThrow("IDVENTA")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("IDCLIENTE")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("IDFARMACIA")),
                    cursor.getString(cursor.getColumnIndexOrThrow("FECHAVENTA")),
                    cursor.getDouble(cursor.getColumnIndexOrThrow("TOTALVENTA")),
                    context // Added context
            );
        }
        if (cursor != null) {
            cursor.close();
        }

        return factura;
    }

    public List<FacturaVenta> getAllFacturasVenta() {
        List<FacturaVenta> listaFacturas = new ArrayList<>();
        Cursor cursor = conexionDB.query("FACTURAVENTA", null, null, null, null, null, "FECHAVENTA DESC, IDVENTA DESC");

        if (cursor != null && cursor.moveToFirst()) {
            do {
                FacturaVenta factura = new FacturaVenta(
                        cursor.getInt(cursor.getColumnIndexOrThrow("IDVENTA")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("IDCLIENTE")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("IDFARMACIA")),
                        cursor.getString(cursor.getColumnIndexOrThrow("FECHAVENTA")),
                        cursor.getDouble(cursor.getColumnIndexOrThrow("TOTALVENTA")),
                        context
                );
                listaFacturas.add(factura);
            } while (cursor.moveToNext());
        }
        if (cursor != null) {
            cursor.close();
        }
        return listaFacturas;
    }

    public void updateFacturaVenta(FacturaVenta factura) {
        ContentValues values = new ContentValues();
        values.put("IDCLIENTE", factura.getIdCliente());
        values.put("IDFARMACIA", factura.getIdFarmacia());
        values.put("FECHAVENTA", factura.getFechaVenta());
        values.put("TOTALVENTA", factura.getTotalVenta());

        int rowsAffected = conexionDB.update("FACTURAVENTA", values, "IDVENTA = ?",
                new String[]{String.valueOf(factura.getIdVenta())});

        if (rowsAffected > 0) {
            Toast.makeText(context, R.string.update_message, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, R.string.not_found_message, Toast.LENGTH_SHORT).show();
             Log.w("FacturaVentaDAO", "No rows updated for sale invoice: ID " + factura.getIdVenta());
        }
    }

    public void deleteFacturaVenta(int idVenta) {
        int rowsAffected = conexionDB.delete("FACTURAVENTA", "IDVENTA = ?",
                new String[]{String.valueOf(idVenta)});

        if (rowsAffected > 0) {
            Toast.makeText(context, R.string.delete_message, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, R.string.not_found_message, Toast.LENGTH_SHORT).show();
             Log.w("FacturaVentaDAO", "No rows deleted for sale invoice: ID " + idVenta);
        }
    }

    // Get global next IDVENTA
     public int obtenerIdFacturaVenta() {
        Cursor cursor = conexionDB.rawQuery("SELECT MAX(IDVENTA) FROM FACTURAVENTA", null);
        int id = 1;
        if (cursor != null && cursor.moveToFirst()) {
            id = cursor.getInt(0) + 1;
            if (id == 1 && cursor.getInt(0) == 0) {
            } else if (cursor.isNull(0)) {
                id = 1;
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        if (id == 0) id = 1;
        return id;
    }

    // --- Métodos de Validación ---
    private boolean isDuplicate(int idVenta) {
        Cursor cursor = conexionDB.query("FACTURAVENTA", new String[]{"IDVENTA"},
                "IDVENTA = ?",
                new String[]{String.valueOf(idVenta)},
                null, null, null);
        boolean exists = (cursor != null && cursor.getCount() > 0);
        if (cursor != null) {
            cursor.close();
        }
        return exists;
    }
    public SucursalFarmacia getFarmaciaById(int idFarmacia) {
        SucursalFarmacia farmacia = null;
        String sql = "SELECT * FROM SUCURSALFARMACIA WHERE IDFARMACIA = ?";
        Cursor cursor = conexionDB.rawQuery(sql, new String[]{String.valueOf(idFarmacia)});
        if (cursor.moveToFirst()) {
            farmacia = new SucursalFarmacia(
                    cursor.getInt(cursor.getColumnIndexOrThrow("IDFARMACIA")),
                    cursor.getString(cursor.getColumnIndexOrThrow("NOMBREFARMACIA"))
            );
        }
        cursor.close();
        return farmacia;
    }

    // obtener Cliente por su id
    public Cliente getClienteById(int idCliente) {
        Cliente cliente = null;
        String sql = "SELECT * FROM CLIENTE WHERE IDCLIENTE = ?";
        Cursor cursor = conexionDB.rawQuery(sql, new String[]{String.valueOf(idCliente)});
        if (cursor.moveToFirst()) {
            cliente = new Cliente(
                    cursor.getInt(cursor.getColumnIndexOrThrow("IDCLIENTE")),
                    cursor.getString(cursor.getColumnIndexOrThrow("NOMBRECLIENTE")),
                    context
            );
        }
        cursor.close();
        return cliente;
    }

}
