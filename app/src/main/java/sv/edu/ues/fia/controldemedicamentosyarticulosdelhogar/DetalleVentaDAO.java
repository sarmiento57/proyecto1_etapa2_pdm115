package sv.edu.ues.fia.controldemedicamentosyarticulosdelhogar;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class DetalleVentaDAO {
    private SQLiteDatabase conexionDB;
    private Context context;

    public DetalleVentaDAO(SQLiteDatabase conexionDB, Context context) {
        this.conexionDB = conexionDB;
        this.context = context;
    }

    public void addDetalleVenta(DetalleVenta detalleVenta) {

        if (isDuplicate(detalleVenta.getIdCliente(), detalleVenta.getIdVenta(),
                detalleVenta.getIdArticulo(), detalleVenta.getIdVentaDetalle())) {
            Toast.makeText(context, R.string.duplicate_message, Toast.LENGTH_SHORT).show();
            return;
        }

        if (!existeDetalleExistenciaVenta(detalleVenta.getIdArticulo(), detalleVenta.getIdVenta(), detalleVenta.getIdCliente())) {
            Toast.makeText(context, R.string.error_detalle_existencia_no_encontrado, Toast.LENGTH_SHORT).show();
            return;
        }

        int stockDisponible = getStockDisponible(detalleVenta.getIdArticulo(), detalleVenta.getIdVenta(), detalleVenta.getIdCliente());
        if (detalleVenta.getCantidadVenta() > stockDisponible) {
            Toast.makeText(context,
                    context.getString(R.string.stock_insuficiente_message, stockDisponible),
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        ContentValues values = new ContentValues();
        values.put("IDCLIENTE", detalleVenta.getIdCliente());
        values.put("IDVENTA", detalleVenta.getIdVenta());
        values.put("IDARTICULO", detalleVenta.getIdArticulo());
        values.put("IDVENTADETALLE", detalleVenta.getIdVentaDetalle());
        values.put("CANTIDADVENTA", detalleVenta.getCantidadVenta());
        values.put("PRECIOUNITARIOVENTA", detalleVenta.getPrecioUnitarioVenta());
        values.put("FECHADEVENTA", detalleVenta.getFechaDeVenta());
        values.put("TOTALDETALLEVENTA", detalleVenta.getTotalDetalleVenta());

        conexionDB.insert("DETALLEVENTA", null, values);
        Toast.makeText(context, R.string.save_message, Toast.LENGTH_SHORT).show();
    }


    public void updateDetalleVenta(DetalleVenta detalleVenta) {
        ContentValues values = new ContentValues();

        values.put("CANTIDADVENTA", detalleVenta.getCantidadVenta());
        values.put("PRECIOUNITARIOVENTA", detalleVenta.getPrecioUnitarioVenta());
        values.put("FECHADEVENTA", detalleVenta.getFechaDeVenta());
        values.put("TOTALDETALLEVENTA", detalleVenta.getTotalDetalleVenta());


        int rowsAffected = conexionDB.update(
                "DETALLEVENTA",
                values,
                "IDCLIENTE = ? AND IDVENTA = ? AND IDARTICULO = ? AND IDVENTADETALLE = ?",
                new String[]{
                        String.valueOf(detalleVenta.getIdCliente()),
                        String.valueOf(detalleVenta.getIdVenta()),
                        String.valueOf(detalleVenta.getIdArticulo()),
                        String.valueOf(detalleVenta.getIdVentaDetalle())
                }
        );

        if (rowsAffected == 0) {
            Toast.makeText(context, R.string.not_found_message, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, R.string.update_message, Toast.LENGTH_SHORT).show();
        }
    }

    public void deleteDetalleVenta(int idCliente, int idVenta, int idArticulo, int idVentaDetalle) {
        int rowsAffected = conexionDB.delete("DETALLEVENTA",
                "IDCLIENTE = ? AND IDVENTA = ? AND IDARTICULO = ? AND IDVENTADETALLE = ?",
                new String[]{
                        String.valueOf(idCliente),
                        String.valueOf(idVenta),
                        String.valueOf(idArticulo),
                        String.valueOf(idVentaDetalle)
                });

        if (rowsAffected == 0) {
            Toast.makeText(context, R.string.not_found_message, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, R.string.delete_message, Toast.LENGTH_SHORT).show();
        }
    }

    public List<DetalleVenta> getAllDetalleVenta() {
        List<DetalleVenta> detalleVentaList = new ArrayList<>();
        String sql = "SELECT * FROM DETALLEVENTA";
        Cursor cursor = conexionDB.rawQuery(sql, null);
        while (cursor.moveToNext()) {
            DetalleVenta detalle = new DetalleVenta(
                    cursor.getInt(cursor.getColumnIndexOrThrow("IDCLIENTE")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("IDVENTA")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("IDARTICULO")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("IDVENTADETALLE")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("CANTIDADVENTA")),
                    cursor.getDouble(cursor.getColumnIndexOrThrow("PRECIOUNITARIOVENTA")),
                    cursor.getString(cursor.getColumnIndexOrThrow("FECHADEVENTA")),
                    cursor.getDouble(cursor.getColumnIndexOrThrow("TOTALDETALLEVENTA")),
                    context
            );
            detalleVentaList.add(detalle);
        }
        cursor.close();
        return detalleVentaList;
    }

    public DetalleVenta getDetalleVenta(int idCliente, int idVenta, int idArticulo, int idVentaDetalle) {
        String sql = "SELECT * FROM DETALLEVENTA WHERE IDCLIENTE = ? AND IDVENTA = ? AND IDARTICULO = ? AND IDVENTADETALLE = ?";
        Cursor cursor = conexionDB.rawQuery(sql, new String[]{
                String.valueOf(idCliente),
                String.valueOf(idVenta),
                String.valueOf(idArticulo),
                String.valueOf(idVentaDetalle)
        });

        if(cursor.moveToFirst()){
            DetalleVenta detalleVenta = new DetalleVenta(
                    cursor.getInt(cursor.getColumnIndexOrThrow("IDCLIENTE")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("IDVENTA")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("IDARTICULO")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("IDVENTADETALLE")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("CANTIDADVENTA")),
                    cursor.getDouble(cursor.getColumnIndexOrThrow("PRECIOUNITARIOVENTA")),
                    cursor.getString(cursor.getColumnIndexOrThrow("FECHADEVENTA")),
                    cursor.getDouble(cursor.getColumnIndexOrThrow("TOTALDETALLEVENTA")),
                    context
            );
            cursor.close();
            return detalleVenta;
        }

        cursor.close();
        Toast.makeText(context, R.string.not_found_message, Toast.LENGTH_SHORT).show();
        return null;
    }


    public List<FacturaVenta> getAllFacturaVenta() {
        List<FacturaVenta> lista = new ArrayList<>();
        String sql = "SELECT * FROM FACTURAVENTA";
        Cursor cursor = conexionDB.rawQuery(sql, null);

        if (cursor.moveToFirst()) {
            do {

                int idCliente = cursor.getInt(cursor.getColumnIndexOrThrow("IDCLIENTE"));
                int idVenta = cursor.getInt(cursor.getColumnIndexOrThrow("IDVENTA"));

                FacturaVenta factura = new FacturaVenta();
                factura.setIdCliente(idCliente);
                factura.setIdVenta(idVenta);
                factura.setFechaVenta(cursor.getString(cursor.getColumnIndexOrThrow("FECHAVENTA")));
                factura.setTotalVenta(cursor.getDouble(cursor.getColumnIndexOrThrow("TOTALVENTA")));
                factura.setIdFarmacia(cursor.getInt(cursor.getColumnIndexOrThrow("IDFARMACIA")));
                lista.add(factura);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return lista;
    }


    public List<Articulo> getAllArticulo() {
        List<Articulo> lista = new ArrayList<>();
        String sql = "SELECT IDARTICULO, NOMBREARTICULO, PRECIOARTICULO FROM ARTICULO"; // Added PRECIOARTICULO
        Cursor cursor = conexionDB.rawQuery(sql, null);

        if (cursor.moveToFirst()) {
            do {
                int idArticulo = cursor.getInt(cursor.getColumnIndexOrThrow("IDARTICULO"));
                String nombreArticulo = cursor.getString(cursor.getColumnIndexOrThrow("NOMBREARTICULO"));
                double precioArticulo = cursor.getDouble(cursor.getColumnIndexOrThrow("PRECIOARTICULO")); // Get price
                Articulo articulo = new Articulo(idArticulo, nombreArticulo, context);
                articulo.setPrecioArticulo(precioArticulo); // Set price
                lista.add(articulo);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return lista;
    }

    public List<DetalleVenta> getDetallesVenta(int idCliente, int idVenta) {
        List<DetalleVenta> detalles = new ArrayList<>();
        String sql = "SELECT * FROM DETALLEVENTA WHERE IDCLIENTE = ? AND IDVENTA = ?";
        Cursor cursor = conexionDB.rawQuery(sql, new String[]{String.valueOf(idCliente), String.valueOf(idVenta)});

        while (cursor.moveToNext()) {
            detalles.add(new DetalleVenta(
                    cursor.getInt(cursor.getColumnIndexOrThrow("IDCLIENTE")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("IDVENTA")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("IDARTICULO")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("IDVENTADETALLE")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("CANTIDADVENTA")),
                    cursor.getDouble(cursor.getColumnIndexOrThrow("PRECIOUNITARIOVENTA")),
                    cursor.getString(cursor.getColumnIndexOrThrow("FECHADEVENTA")),
                    cursor.getDouble(cursor.getColumnIndexOrThrow("TOTALDETALLEVENTA")),
                    context
            ));
        }
        cursor.close();
        return detalles;
    }

    private boolean isDuplicate(int idCliente, int idVenta, int idArticulo, int idVentaDetalle) {
        String sql = "SELECT * FROM DETALLEVENTA WHERE IDCLIENTE = ? AND IDVENTA = ? AND IDARTICULO = ? AND IDVENTADETALLE = ?";
        Cursor cursor = conexionDB.rawQuery(sql, new String[]{
                String.valueOf(idCliente),
                String.valueOf(idVenta),
                String.valueOf(idArticulo),
                String.valueOf(idVentaDetalle)
        });
        boolean exists = cursor.moveToFirst();
        cursor.close();
        return exists;
    }

    private boolean existeDetalleExistenciaVenta(int idArticulo, int idVenta, int idCliente) {
        String query = "SELECT 1 FROM DETALLEEXISTENCIA DE " +
                "INNER JOIN FACTURAVENTA FV ON DE.IDFARMACIA = FV.IDFARMACIA " +
                "WHERE DE.IDARTICULO = ? AND FV.IDVENTA = ? AND FV.IDCLIENTE = ?";

        Cursor cursor = conexionDB.rawQuery(query, new String[]{
                String.valueOf(idArticulo),
                String.valueOf(idVenta),
                String.valueOf(idCliente)
        });

        boolean existe = cursor.moveToFirst();
        cursor.close();
        return existe;
    }

    private int getStockDisponible(int idArticulo, int idVenta, int idCliente) {
        String query = "SELECT DE.CANTIDADEXISTENCIA FROM DETALLEEXISTENCIA DE " +
                "INNER JOIN FACTURAVENTA FV ON DE.IDFARMACIA = FV.IDFARMACIA " +
                "WHERE DE.IDARTICULO = ? AND FV.IDVENTA = ? AND FV.IDCLIENTE = ?";

        Cursor cursor = conexionDB.rawQuery(query, new String[]{
                String.valueOf(idArticulo),
                String.valueOf(idVenta),
                String.valueOf(idCliente)
        });

        int stock = 0;
        if (cursor.moveToFirst()) {
            stock = cursor.getInt(0);
        }
        cursor.close();
        return stock;
    }

}
