package sv.edu.ues.fia.controldemedicamentosyarticulosdelhogar;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;

public class DetalleCompraDAO {
    private SQLiteDatabase conexionDB;
    private Context context;

    public DetalleCompraDAO(SQLiteDatabase conexionDB, Context context) {
        this.conexionDB = conexionDB;
        this.context = context;
    }

    public void addDetalleCompra(DetalleCompra detalleCompra) {
        if (isDuplicateIdDetalle(detalleCompra.getIdDetalleCompra())) {
            Toast.makeText(context, R.string.duplicate_iddetalle_message, Toast.LENGTH_SHORT).show();
            return;
        }

        if (!existeDetalleExistencia(detalleCompra.getIdArticulo(), detalleCompra.getIdCompra())) {
            Toast.makeText(context, context.getString(R.string.error_detalle_existencia_no_encontrado), Toast.LENGTH_SHORT).show();
            return;
        }

        int idExistente = getIdDetalleCompraExistente(
                detalleCompra.getIdCompra(),
                detalleCompra.getIdArticulo(),
                detalleCompra.getPrecioUnitarioCompra()
        );

        if (idExistente != -1) {
            String sql = "UPDATE DETALLECOMPRA SET CANTIDADCOMPRA = CANTIDADCOMPRA + ?, " +
                    "TOTALDETALLECOMPRA = TOTALDETALLECOMPRA + ? WHERE IDDETALLECOMPRA = ?";
            conexionDB.execSQL(sql, new Object[]{
                    detalleCompra.getCantidadCompra(),
                    detalleCompra.getTotalDetalleCompra(),
                    idExistente
            });

            Toast.makeText(context,
                    context.getString(R.string.stock_updated_message) + idExistente,
                    Toast.LENGTH_SHORT).show();
            return;
        }

        ContentValues values = new ContentValues();
        values.put("IDCOMPRA", detalleCompra.getIdCompra());
        values.put("IDARTICULO", detalleCompra.getIdArticulo());
        values.put("IDDETALLECOMPRA", detalleCompra.getIdDetalleCompra());
        values.put("FECHADECOMPRA", detalleCompra.getFechaDeCompra());
        values.put("PRECIOUNITARIOCOMPRA", detalleCompra.getPrecioUnitarioCompra());
        values.put("CANTIDADCOMPRA", detalleCompra.getCantidadCompra());
        values.put("TOTALDETALLECOMPRA", detalleCompra.getTotalDetalleCompra());

        conexionDB.insert("DETALLECOMPRA", null, values);
        Toast.makeText(context, R.string.save_message, Toast.LENGTH_SHORT).show();
    }

    public void updateDetalleCompra(DetalleCompra detalleCompra) {

        ContentValues values = new ContentValues();
        values.put("IDARTICULO", detalleCompra.getIdArticulo());
        values.put("FECHADECOMPRA", detalleCompra.getFechaDeCompra());
        values.put("PRECIOUNITARIOCOMPRA", detalleCompra.getPrecioUnitarioCompra());
        values.put("CANTIDADCOMPRA", detalleCompra.getCantidadCompra());
        values.put("TOTALDETALLECOMPRA", detalleCompra.getTotalDetalleCompra());

        int rowsAffected = conexionDB.update(
                "DETALLECOMPRA",
                values,
                "IDCOMPRA = ? AND IDDETALLECOMPRA = ?",
                new String[]{
                        String.valueOf(detalleCompra.getIdCompra()),
                        String.valueOf(detalleCompra.getIdDetalleCompra())
                }
        );

        if (rowsAffected == 0) {
            Toast.makeText(context, R.string.not_found_message, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, R.string.update_message, Toast.LENGTH_SHORT).show();
        }
    }

    public void deleteDetalleCompra(int idDetalle) {
        int rowsAffected = conexionDB.delete("DETALLECOMPRA", "IDDETALLECOMPRA = ?", new String[]{String.valueOf(idDetalle)});

        if (rowsAffected == 0) {
            Toast.makeText(context, R.string.not_found_message, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, R.string.delete_message, Toast.LENGTH_SHORT).show();
        }
    }

    public List<DetalleCompra> getAllDetalleCompra() {
        List<DetalleCompra> detalleCompraList = new ArrayList<>();
        String sql = "SELECT * FROM DETALLECOMPRA";
        Cursor cursor = conexionDB.rawQuery(sql, null);
        while (cursor.moveToNext()) {
            DetalleCompra detalle = new DetalleCompra(
                    cursor.getInt(cursor.getColumnIndexOrThrow("IDCOMPRA")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("IDARTICULO")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("IDDETALLECOMPRA")),
                    cursor.getString(cursor.getColumnIndexOrThrow("FECHADECOMPRA")),
                    cursor.getDouble(cursor.getColumnIndexOrThrow("PRECIOUNITARIOCOMPRA")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("CANTIDADCOMPRA")),
                    cursor.getDouble(cursor.getColumnIndexOrThrow("TOTALDETALLECOMPRA")),
                    context
            );
            detalleCompraList.add(detalle);
        }
        cursor.close();
        return detalleCompraList;
    }

    public DetalleCompra getDetalleCompra(int idDetalleCompra) {
        String sql = "SELECT * FROM DETALLECOMPRA WHERE IDDETALLECOMPRA = ?";
        Cursor cursor = conexionDB.rawQuery(sql, new String[]{String.valueOf(idDetalleCompra)});

        if(cursor.moveToFirst()){
            DetalleCompra detalleCompra = new DetalleCompra(
                    cursor.getInt(cursor.getColumnIndexOrThrow("IDCOMPRA")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("IDARTICULO")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("IDDETALLECOMPRA")),
                    cursor.getString(cursor.getColumnIndexOrThrow("FECHADECOMPRA")),
                    cursor.getDouble(cursor.getColumnIndexOrThrow("PRECIOUNITARIOCOMPRA")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("CANTIDADCOMPRA")),
                    cursor.getDouble(cursor.getColumnIndexOrThrow("TOTALDETALLECOMPRA")),
                    context
            );
            cursor.close();
            return detalleCompra;
        }

        cursor.close();
        Toast.makeText(context, R.string.not_found_message, Toast.LENGTH_SHORT).show();
        return null;
    }

    public List<FacturaCompra> getAllFacturaCompra() {
        List<FacturaCompra> lista = new ArrayList<>();
        String sql = "SELECT * FROM FACTURACOMPRA";
        Cursor cursor = conexionDB.rawQuery(sql, null);

        if (cursor.moveToFirst()) {
            do {
                int idCompra = cursor.getInt(cursor.getColumnIndexOrThrow("IDCOMPRA"));
                int idProveedor = cursor.getInt(cursor.getColumnIndexOrThrow("IDPROVEEDOR"));
                String fechaCompra = cursor.getString(cursor.getColumnIndexOrThrow("FECHACOMPRA"));

                lista.add(new FacturaCompra(idCompra, idProveedor, fechaCompra, context));
            } while (cursor.moveToNext());
        }

        cursor.close();
        return lista;
    }

    public List<Articulo> getAllArticulo() {
        List<Articulo> lista = new ArrayList<>();
        String sql = "SELECT * FROM ARTICULO";
        Cursor cursor = conexionDB.rawQuery(sql, null);

        if (cursor.moveToFirst()) {
            do {
                int idCompra = cursor.getInt(cursor.getColumnIndexOrThrow("IDARTICULO"));
                String nombreArticulo = cursor.getString(cursor.getColumnIndexOrThrow("NOMBREARTICULO"));
                lista.add(new Articulo(idCompra, nombreArticulo,  context));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return lista;
    }

    public List<DetalleCompra> getDetallesCompra(int idCompra) {
        List<DetalleCompra> detalles = new ArrayList<>();
        String sql = "SELECT * FROM DETALLECOMPRA WHERE IDCOMPRA = ?";
        Cursor cursor = conexionDB.rawQuery(sql, new String[]{String.valueOf(idCompra)});

        while (cursor.moveToNext()) {
            detalles.add(new DetalleCompra(
                    cursor.getInt(cursor.getColumnIndexOrThrow("IDCOMPRA")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("IDARTICULO")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("IDDETALLECOMPRA")),
                    cursor.getString(cursor.getColumnIndexOrThrow("FECHADECOMPRA")),
                    cursor.getDouble(cursor.getColumnIndexOrThrow("PRECIOUNITARIOCOMPRA")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("CANTIDADCOMPRA")),
                    cursor.getDouble(cursor.getColumnIndexOrThrow("TOTALDETALLECOMPRA")),
                    context
            ));
        }

        cursor.close();
        return detalles;
    }

    private boolean isDuplicateIdDetalle(int idDetalleCompra) {
        String sql = "SELECT 1 FROM DETALLECOMPRA WHERE IDDETALLECOMPRA = ?";
        Cursor cursor = conexionDB.rawQuery(sql, new String[]{String.valueOf(idDetalleCompra)});
        boolean exists = cursor.moveToFirst();
        cursor.close();
        return exists;
    }

    private int getIdDetalleCompraExistente(int idCompra, int idArticulo, double precioUnitario) {
        String sql = "SELECT IDDETALLECOMPRA FROM DETALLECOMPRA WHERE IDCOMPRA = ? AND IDARTICULO = ? AND PRECIOUNITARIOCOMPRA = ?";
        Cursor cursor = conexionDB.rawQuery(sql, new String[]{
                String.valueOf(idCompra),
                String.valueOf(idArticulo),
                String.valueOf(precioUnitario)
        });

        int idDetalle = -1;
        if (cursor.moveToFirst()) {
            idDetalle = cursor.getInt(0);
        }
        cursor.close();
        return idDetalle;
    }

    private boolean existeDetalleExistencia(int idArticulo, int idCompra) {
        String query = "SELECT 1 " +
                "FROM DETALLEEXISTENCIA DE " +
                "INNER JOIN FACTURACOMPRA FC ON DE.IDFARMACIA = FC.IDFARMACIA " +
                "WHERE DE.IDARTICULO = ? AND FC.IDCOMPRA = ?";

        Cursor cursor = conexionDB.rawQuery(query, new String[]{
                String.valueOf(idArticulo),
                String.valueOf(idCompra)
        });

        boolean existe = cursor.moveToFirst();
        cursor.close();
        return existe;
    }

}

