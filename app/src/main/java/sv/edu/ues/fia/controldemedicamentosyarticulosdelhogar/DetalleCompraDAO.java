package sv.edu.ues.fia.controldemedicamentosyarticulosdelhogar;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.widget.Toast;
import com.android.volley.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DetalleCompraDAO {

    private final Context context;
    private final WebServiceHelper ws;

    public DetalleCompraDAO(Context context) {
        this.context = context;
        this.ws = new WebServiceHelper(context);
    }

    public void addDetalleCompra(DetalleCompra detalleCompra, Response.Listener<String> callback) {
        // Verifica si el ID ya existe
        isDuplicateIdDetalle(detalleCompra.getIdDetalleCompra(), existsId -> {
            if (existsId) {
                Toast.makeText(context, R.string.duplicate_iddetalle_message, Toast.LENGTH_SHORT).show();
                return;
            }

            // Verifica si existe un detalle de existencia relacionado
            existeDetalleExistencia(detalleCompra.getIdArticulo(), detalleCompra.getIdCompra(), existsExistencia -> {
                if (!existsExistencia) {
                    Toast.makeText(context, context.getString(R.string.error_detalle_existencia_no_encontrado), Toast.LENGTH_SHORT).show();
                    return;
                }

                // Prepara los parámetros para la solicitud
                Map<String, String> params = new HashMap<>();
                params.put("idcompra", String.valueOf(detalleCompra.getIdCompra()));
                params.put("idarticulo", String.valueOf(detalleCompra.getIdArticulo()));
                params.put("iddetallecompra", String.valueOf(detalleCompra.getIdDetalleCompra()));
                params.put("fechadecompra", detalleCompra.getFechaDeCompra());
                params.put("preciounitariocompra", String.valueOf(detalleCompra.getPrecioUnitarioCompra()));
                params.put("cantidadcompra", String.valueOf(detalleCompra.getCantidadCompra()));
                params.put("totaldetallecompra", String.valueOf(detalleCompra.getTotalDetalleCompra()));

                // Realiza la solicitud POST al servidor
                ws.post("detallecompra/insertar_detallecompra.php", params,
                        response -> {
                            Toast.makeText(context, R.string.save_message, Toast.LENGTH_SHORT).show();
                            callback.onResponse(response);
                        },
                        error -> Toast.makeText(context, R.string.connection_error, Toast.LENGTH_SHORT).show()
                );
            });
        });
    }


    public void updateDetalleCompra(DetalleCompra detalleCompra, Response.Listener<String> callback) {
        Map<String, String> params = new HashMap<>();
        params.put("idcompra", String.valueOf(detalleCompra.getIdCompra()));
        params.put("iddetallecompra", String.valueOf(detalleCompra.getIdDetalleCompra()));
        params.put("idarticulo", String.valueOf(detalleCompra.getIdArticulo()));
        params.put("fechadecompra", detalleCompra.getFechaDeCompra());
        params.put("preciounitariocompra", String.valueOf(detalleCompra.getPrecioUnitarioCompra()));
        params.put("cantidadcompra", String.valueOf(detalleCompra.getCantidadCompra()));
        params.put("totaldetallecompra", String.valueOf(detalleCompra.getTotalDetalleCompra()));

        ws.post("detallecompra/actualizar_detallecompra.php", params,
                response -> {
                    Toast.makeText(context, R.string.update_message, Toast.LENGTH_SHORT).show();
                    callback.onResponse(response);
                },
                error -> Toast.makeText(context, R.string.connection_error, Toast.LENGTH_SHORT).show()
        );
    }

    public void deleteDetalleCompra(int idDetalleCompra, Response.Listener<String> callback) {
        Map<String, String> params = new HashMap<>();
        params.put("iddetallecompra", String.valueOf(idDetalleCompra));

        ws.post("detallecompra/eliminar_detallecompra.php", params,
                response -> {
                    Toast.makeText(context, R.string.delete_message, Toast.LENGTH_SHORT).show();
                    callback.onResponse(response);
                },
                error -> Toast.makeText(context, R.string.connection_error, Toast.LENGTH_SHORT).show()
        );
    }

    public void getAllDetalleCompra(Response.Listener<List<DetalleCompra>> callback) {
        ws.post("detallecompra/listar_detallecompra.php", new HashMap<>(),
                response -> {
                    try {
                        JSONArray array = new JSONArray(response);
                        List<DetalleCompra> list = new ArrayList<>();
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject obj = array.getJSONObject(i);
                            list.add(new DetalleCompra(
                                    obj.getInt("idcompra"),
                                    obj.getInt("idarticulo"),
                                    obj.getInt("iddetallecompra"),
                                    obj.getString("fechadecompra"),
                                    obj.getDouble("preciounitariocompra"),
                                    obj.getInt("cantidadcompra"),
                                    obj.getDouble("totaldetallecompra"),
                                    context
                            ));
                        }
                        callback.onResponse(list);
                    } catch (Exception e) {
                        e.printStackTrace();
                        callback.onResponse(new ArrayList<>());
                    }
                },
                error -> {
                    Toast.makeText(context, R.string.connection_error, Toast.LENGTH_SHORT).show();
                    callback.onResponse(new ArrayList<>());
                });
    }

    public void getDetalleCompra(int idDetalleCompra, Response.Listener<DetalleCompra> callback) {
        Map<String, String> params = new HashMap<>();
        params.put("iddetallecompra", String.valueOf(idDetalleCompra));

        ws.post("detallecompra/get_detallecompra.php", params,
                response -> {
                    try {
                        JSONObject obj = new JSONObject(response);
                        DetalleCompra detalleCompra = new DetalleCompra(
                                obj.getInt("idcompra"),
                                obj.getInt("idarticulo"),
                                obj.getInt("iddetallecompra"),
                                obj.getString("fechadecompra"),
                                obj.getDouble("preciounitariocompra"),
                                obj.getInt("cantidadcompra"),
                                obj.getDouble("totaldetallecompra"),
                                context
                        );
                        callback.onResponse(detalleCompra);
                    } catch (Exception e) {
                        e.printStackTrace();
                        callback.onResponse(null);
                    }
                },
                error -> {
                    Toast.makeText(context, R.string.connection_error, Toast.LENGTH_SHORT).show();
                    callback.onResponse(null);
                });
    }

    private void isDuplicateIdDetalle(int idDetalleCompra, Response.Listener<Boolean> callback) {
        Map<String, String> params = new HashMap<>();
        params.put("iddetallecompra", String.valueOf(idDetalleCompra));
        ws.post("detallecompra/verificar_detallecompra.php", params,
                response -> {
                    try {
                        JSONObject obj = new JSONObject(response);
                        callback.onResponse(obj.optBoolean("existe", false));
                    } catch (Exception e) {
                        e.printStackTrace();
                        callback.onResponse(false);
                    }
                },
                error -> {
                    Toast.makeText(context, R.string.connection_error, Toast.LENGTH_SHORT).show();
                    callback.onResponse(false);
                });
    }

    private void existeDetalleExistencia(int idArticulo, int idCompra, Response.Listener<Boolean> callback) {
        // Obtener la Farmacia de la Factura Compra
        getFacturaFarmacia(idCompra, farmaciaId -> {
            if (farmaciaId != -1) {
                // Verificar si existe un Detalle Existencia con la misma Farmacia y Artículo
                Map<String, String> params = new HashMap<>();
                params.put("idarticulo", String.valueOf(idArticulo));
                params.put("idfarmacia", String.valueOf(farmaciaId));

                ws.post("detallecompra/verificar_existencia.php", params,
                        response -> {
                            try {
                                JSONObject obj = new JSONObject(response);
                                boolean exists = obj.optBoolean("existe", false);
                                callback.onResponse(exists);  // Devolver true o false
                            } catch (Exception e) {
                                e.printStackTrace();
                                callback.onResponse(false);
                            }
                        },
                        error -> {
                            Toast.makeText(context, R.string.connection_error, Toast.LENGTH_SHORT).show();
                            callback.onResponse(false);
                        });
            } else {
                // En caso de error al obtener la farmacia de la factura
                callback.onResponse(false);
            }
        });
    }

    private void getFacturaFarmacia(int idCompra, Response.Listener<Integer> callback) {
        // Obtener la Farmacia asociada con la Factura Compra
        Map<String, String> params = new HashMap<>();
        params.put("idcompra", String.valueOf(idCompra));

        ws.post("detallecompra/get_farmacia.php", params,
                response -> {
                    try {
                        JSONObject obj = new JSONObject(response);
                        int farmaciaId = obj.optInt("idfarmacia", -1);  // Si no encuentra la farmacia, devuelve -1
                        callback.onResponse(farmaciaId);
                    } catch (Exception e) {
                        e.printStackTrace();
                        callback.onResponse(-1);  // Error en la consulta, devuelve -1
                    }
                },
                error -> {
                    Toast.makeText(context, R.string.connection_error, Toast.LENGTH_SHORT).show();
                    callback.onResponse(-1);  // Error en la consulta, devuelve -1
                });
    }

    public void getAllFacturaCompra(Response.Listener<List<FacturaCompra>> callback) {
        ws.post("detallecompra/listar_facturacompra.php", new HashMap<>(),
                response -> {
                    try {
                        JSONArray array = new JSONArray(response);
                        List<FacturaCompra> list = new ArrayList<>();
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject obj = array.getJSONObject(i);
                            list.add(new FacturaCompra(
                                    obj.getInt("idcompra"),
                                    obj.getInt("idproveedor"),
                                    obj.getString("fechacompra"),
                                    context
                            ));
                        }
                        callback.onResponse(list);
                    } catch (Exception e) {
                        e.printStackTrace();
                        callback.onResponse(new ArrayList<>());
                    }
                },
                error -> {
                    Toast.makeText(context, R.string.connection_error, Toast.LENGTH_SHORT).show();
                    callback.onResponse(new ArrayList<>());
                });
    }

    public void getAllArticulo(Response.Listener<List<Articulo>> callback) {
        ws.post("detallecompra/listar_articulo.php", new HashMap<>(),
                response -> {
                    try {
                        JSONArray array = new JSONArray(response);
                        List<Articulo> list = new ArrayList<>();
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject obj = array.getJSONObject(i);
                            list.add(new Articulo(
                                    obj.getInt("idarticulo"),
                                    obj.getString("nombrearticulo"),
                                    context
                            ));
                        }
                        callback.onResponse(list);
                    } catch (Exception e) {
                        e.printStackTrace();
                        callback.onResponse(new ArrayList<>());
                    }
                },
                error -> {
                    Toast.makeText(context, R.string.connection_error, Toast.LENGTH_SHORT).show();
                    callback.onResponse(new ArrayList<>());
                });
    }

    public void getDetallesCompra(int idCompra, Response.Listener<List<DetalleCompra>> callback) {
        Map<String, String> params = new HashMap<>();
        params.put("idcompra", String.valueOf(idCompra));

        ws.post("detallecompra/listar_detalles_compra.php", params,
                response -> {
                    try {
                        JSONArray array = new JSONArray(response);
                        List<DetalleCompra> list = new ArrayList<>();
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject obj = array.getJSONObject(i);
                            list.add(new DetalleCompra(
                                    obj.getInt("idcompra"),
                                    obj.getInt("idarticulo"),
                                    obj.getInt("iddetallecompra"),
                                    obj.getString("fechadecompra"),
                                    obj.getDouble("preciounitariocompra"),
                                    obj.getInt("cantidadcompra"),
                                    obj.getDouble("totaldetallecompra"),
                                    context
                            ));
                        }
                        callback.onResponse(list);
                    } catch (Exception e) {
                        e.printStackTrace();
                        callback.onResponse(new ArrayList<>());
                    }
                },
                error -> {
                    Toast.makeText(context, R.string.connection_error, Toast.LENGTH_SHORT).show();
                    callback.onResponse(new ArrayList<>());
                });
    }
}


