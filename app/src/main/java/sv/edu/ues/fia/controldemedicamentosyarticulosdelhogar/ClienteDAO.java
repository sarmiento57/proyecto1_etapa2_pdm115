package sv.edu.ues.fia.controldemedicamentosyarticulosdelhogar;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log; // Import Log for debugging
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class ClienteDAO {
    private SQLiteDatabase conexionDB;
    private Context context;

    public ClienteDAO(SQLiteDatabase conexionDB, Context context) {
        this.conexionDB = conexionDB;
        this.context = context;
    }

    public void addCliente(Cliente cliente) {
        if (conexionDB == null || !conexionDB.isOpen()) {
            Log.e("ClienteDAO", "Database is not open for writing.");
            Toast.makeText(context, R.string.db_error, Toast.LENGTH_SHORT).show();
            return;
        }
        if (isDuplicate(cliente.getIdCliente(), cliente.getCorreoCliente())) {
            Toast.makeText(context, R.string.duplicate_client_message, Toast.LENGTH_SHORT).show();
            return;
        }

        ContentValues values = new ContentValues();
        values.put("IDCLIENTE", cliente.getIdCliente());
        values.put("NOMBRECLIENTE", cliente.getNombreCliente());
        values.put("TELEFONOCLIENTE", cliente.getTelefonoCliente());
        values.put("CORREOCLIENTE", cliente.getCorreoCliente());

        long result = conexionDB.insert("CLIENTE", null, values);
        if (result == -1) {
            Toast.makeText(context, R.string.error_saving, Toast.LENGTH_SHORT).show();
            Log.e("ClienteDAO", "Error inserting client with ID: " + cliente.getIdCliente());
        } else {
            Toast.makeText(context, R.string.save_message, Toast.LENGTH_SHORT).show();
        }
    }

    public Cliente getClienteById(int idCliente) {
        if (conexionDB == null || !conexionDB.isOpen()) {
            Log.e("ClienteDAO", "Database is not open for reading.");
            return null;
        }
        Cliente cliente = null;
        Cursor cursor = conexionDB.query("CLIENTE", null, "IDCLIENTE = ?",
                new String[]{String.valueOf(idCliente)}, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            cliente = new Cliente(
                    cursor.getInt(cursor.getColumnIndexOrThrow("IDCLIENTE")),
                    cursor.getString(cursor.getColumnIndexOrThrow("NOMBRECLIENTE")),
                    cursor.getString(cursor.getColumnIndexOrThrow("TELEFONOCLIENTE")),
                    cursor.getString(cursor.getColumnIndexOrThrow("CORREOCLIENTE")),
                    context
            );
        }
        if (cursor != null) {
            cursor.close();
        }

        return cliente;
    }

    public List<Cliente> getAllClientes() {
        if (conexionDB == null || !conexionDB.isOpen()) {
            Log.e("ClienteDAO", "Database is not open for reading.");
            return new ArrayList<>();
        }
        List<Cliente> listaClientes = new ArrayList<>();
        Cursor cursor = conexionDB.query("CLIENTE", null, null, null, null, null, "NOMBRECLIENTE ASC");

        if (cursor != null && cursor.moveToFirst()) {
            do {
                Cliente cliente = new Cliente(
                        cursor.getInt(cursor.getColumnIndexOrThrow("IDCLIENTE")),
                        cursor.getString(cursor.getColumnIndexOrThrow("NOMBRECLIENTE")),
                        cursor.getString(cursor.getColumnIndexOrThrow("TELEFONOCLIENTE")),
                        cursor.getString(cursor.getColumnIndexOrThrow("CORREOCLIENTE")),
                        context
                );
                listaClientes.add(cliente);
            } while (cursor.moveToNext());
        }
        if (cursor != null) {
            cursor.close();
        }
        return listaClientes;
    }

    public void updateCliente(Cliente cliente) {
        if (conexionDB == null || !conexionDB.isOpen()) {
            Log.e("ClienteDAO", "Database is not open for writing.");
            Toast.makeText(context, R.string.db_error, Toast.LENGTH_SHORT).show();
            return;
        }
        // Verificar si el nuevo correo ya existe en otro cliente
        if (isDuplicateEmailOnUpdate(cliente.getIdCliente(), cliente.getCorreoCliente())) {
            Toast.makeText(context, R.string.duplicate_email_message, Toast.LENGTH_SHORT).show();
            return;
        }

        ContentValues values = new ContentValues();
        values.put("NOMBRECLIENTE", cliente.getNombreCliente());
        values.put("TELEFONOCLIENTE", cliente.getTelefonoCliente());
        values.put("CORREOCLIENTE", cliente.getCorreoCliente());

        int rowsAffected = conexionDB.update("CLIENTE", values, "IDCLIENTE = ?",
                new String[]{String.valueOf(cliente.getIdCliente())});

        if (rowsAffected > 0) {
            Toast.makeText(context, R.string.update_message, Toast.LENGTH_SHORT).show();
        } else {

            Toast.makeText(context, R.string.not_found_message, Toast.LENGTH_SHORT).show();
            Log.e("ClienteDAO", "Error updating client with ID: " + cliente.getIdCliente() + " or no changes made.");
        }
    }

    public void deleteCliente(int idCliente) {
        if (conexionDB == null || !conexionDB.isOpen()) {
            Log.e("ClienteDAO", "Database is not open for writing.");
            Toast.makeText(context, R.string.db_error, Toast.LENGTH_SHORT).show();
            return;
        }
        int rowsAffected = conexionDB.delete("CLIENTE", "IDCLIENTE = ?",
                new String[]{String.valueOf(idCliente)});

        if (rowsAffected > 0) {
            Toast.makeText(context, R.string.delete_message, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, R.string.not_found_message, Toast.LENGTH_SHORT).show();
            Log.e("ClienteDAO", "Error deleting client with ID: " + idCliente);
        }
    }

    public int obtenerSiguienteIdCliente() {
        if (conexionDB == null || !conexionDB.isOpen()) {
            Log.e("ClienteDAO", "Database is not open for reading.");
            return 1;
        }
        Cursor cursor = conexionDB.rawQuery("SELECT MAX(IDCLIENTE) FROM CLIENTE", null);
        int id = 1;
        if (cursor != null && cursor.moveToFirst()) {
            id = cursor.getInt(0) + 1;
        }
        if (cursor != null) {
            cursor.close();
        }
        return id;
    }

    private boolean isDuplicate(int idCliente, String correo) {
        if (conexionDB == null || !conexionDB.isOpen()) {
            Log.e("ClienteDAO", "Database is not open for reading in isDuplicate.");
            return true;
        }
        Cursor cursor = conexionDB.query("CLIENTE", new String[]{"IDCLIENTE"},
                "IDCLIENTE = ? OR CORREOCLIENTE = ?",
                new String[]{String.valueOf(idCliente), correo},
                null, null, null);
        boolean exists = (cursor != null && cursor.getCount() > 0);
        if (cursor != null) {
            cursor.close();
        }
        return exists;
    }

    private boolean isDuplicateEmailOnUpdate(int idClienteActual, String correo) {
        if (conexionDB == null || !conexionDB.isOpen()) {
            Log.e("ClienteDAO", "Database is not open for reading in isDuplicateEmailOnUpdate.");
            return true;
        }
        Cursor cursor = conexionDB.query("CLIENTE", new String[]{"IDCLIENTE"},
                "CORREOCLIENTE = ? AND IDCLIENTE != ?",
                new String[]{correo, String.valueOf(idClienteActual)},
                null, null, null);
        boolean exists = (cursor != null && cursor.getCount() > 0);
        if (cursor != null) {
            cursor.close();
        }
        return exists;
    }
}
