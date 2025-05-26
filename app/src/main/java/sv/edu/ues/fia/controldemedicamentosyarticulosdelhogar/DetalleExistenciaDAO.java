package sv.edu.ues.fia.controldemedicamentosyarticulosdelhogar;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;

import com.android.volley.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DetalleExistenciaDAO {

    private final Context context;
    private final WebServiceHelper ws;

    public DetalleExistenciaDAO(Context context) {
        this.context = context;
        this.ws = new WebServiceHelper(context);
    }

    public void addExistencia(DetalleExistencia detalleExistencia, Response.Listener<String> callback) {
        isDuplicate(detalleExistencia.getIdDetalleExistencia(), exists -> {
            if (exists) {
                Toast.makeText(context, R.string.duplicate_message, Toast.LENGTH_SHORT).show();
                return;
            }
            isUsed(detalleExistencia, used -> {
                if (used) {
                    Toast.makeText(context, R.string.duplicate_message, Toast.LENGTH_SHORT).show();
                    return;
                }
                Map<String, String> params = new HashMap<>();
                params.put("idarticulo", String.valueOf(detalleExistencia.getIdArticulo()));
                params.put("iddetalleexistencia", String.valueOf(detalleExistencia.getIdDetalleExistencia()));
                params.put("idfarmacia", String.valueOf(detalleExistencia.getIdFarmacia()));
                params.put("cantidadexistencia", String.valueOf(detalleExistencia.getCantidadExistencia()));
                params.put("fechadevencimiento", detalleExistencia.getFechaDeVencimiento());

                ws.post("detalleexistencia/insertar_detalleexistencia.php", params,
                        response -> {
                            Toast.makeText(context, R.string.save_message, Toast.LENGTH_SHORT).show();
                            callback.onResponse(response);
                        },
                        e -> Toast.makeText(context, R.string.connection_error, Toast.LENGTH_SHORT).show()
                );
            });
        });
    }

    public void updateExistencia(DetalleExistencia detalleExistencia, Response.Listener<String> callback) {
        Map<String, String> params = new HashMap<>();
        params.put("iddetalleexistencia", String.valueOf(detalleExistencia.getIdDetalleExistencia()));
        params.put("cantidadexistencia", String.valueOf(detalleExistencia.getCantidadExistencia()));
        params.put("fechadevencimiento", detalleExistencia.getFechaDeVencimiento());

        ws.post("detalleexistencia/actualizar_detalleexistencia.php", params,
                response -> {
                    Toast.makeText(context, R.string.update_message, Toast.LENGTH_SHORT).show();
                    callback.onResponse(response);
                },
                e -> Toast.makeText(context, R.string.connection_error, Toast.LENGTH_SHORT).show()
        );
    }

    public void getAllDetallesExistencia(Response.Listener<List<DetalleExistencia>> callback) {
        ws.post("detalleexistencia/listar_detalleexistencia.php", new HashMap<>(),
                response -> {
                    try {
                        JSONArray array = new JSONArray(response);
                        List<DetalleExistencia> list = new ArrayList<>();
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject obj = array.getJSONObject(i);
                            list.add(new DetalleExistencia(
                                    obj.getInt("idarticulo"),
                                    obj.getInt("iddetalleexistencia"),
                                    obj.getInt("idfarmacia"),
                                    obj.getInt("cantidadexistencia"),
                                    obj.getString("fechadevencimiento"),
                                    context
                            ));
                        }
                        callback.onResponse(list);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        callback.onResponse(Collections.emptyList());
                    }
                },
                error -> {
                    Toast.makeText(context, R.string.connection_error, Toast.LENGTH_SHORT).show();
                    callback.onResponse(Collections.emptyList());
                });
    }

    public void getAllDetallesExistenciaByIdArticulo(int id, Response.Listener<List<DetalleExistencia>> callback) {
        Map<String, String> params = new HashMap<>();
        params.put("idarticulo", String.valueOf(id));
        ws.post("detalleexistencia/listar_por_articulo.php", params,
                response -> {
                    try {
                        JSONArray array = new JSONArray(response);
                        List<DetalleExistencia> list = new ArrayList<>();
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject obj = array.getJSONObject(i);
                            list.add(new DetalleExistencia(
                                    obj.getInt("idarticulo"),
                                    obj.getInt("iddetalleexistencia"),
                                    obj.getInt("idfarmacia"),
                                    obj.getInt("cantidadexistencia"),
                                    obj.getString("fechadevencimiento"),
                                    context
                            ));
                        }
                        callback.onResponse(list);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        callback.onResponse(Collections.emptyList());
                    }
                },
                error -> {
                    Toast.makeText(context, R.string.connection_error, Toast.LENGTH_SHORT).show();
                    callback.onResponse(Collections.emptyList());
                });
    }

    public void getAllDetallesExistenciaByIdFarm(int id, Response.Listener<List<DetalleExistencia>> callback) {
        Map<String, String> params = new HashMap<>();
        params.put("idfarmacia", String.valueOf(id));
        ws.post("detalleexistencia/listar_por_farmacia.php", params,
                response -> {
                    try {
                        JSONArray array = new JSONArray(response);
                        List<DetalleExistencia> list = new ArrayList<>();
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject obj = array.getJSONObject(i);
                            list.add(new DetalleExistencia(
                                    obj.getInt("idarticulo"),
                                    obj.getInt("iddetalleexistencia"),
                                    obj.getInt("idfarmacia"),
                                    obj.getInt("cantidadexistencia"),
                                    obj.getString("fechadevencimiento"),
                                    context
                            ));
                        }
                        callback.onResponse(list);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        callback.onResponse(Collections.emptyList());
                    }
                },
                error -> {
                    Toast.makeText(context, R.string.connection_error, Toast.LENGTH_SHORT).show();
                    callback.onResponse(Collections.emptyList());
                });
    }

    public void getAllArticulos(Response.Listener<List<Articulo>> callback) {
        ws.post("detalleexistencia/listar_articulos.php", new HashMap<>(),
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
                    } catch (JSONException e) {
                        e.printStackTrace();
                        callback.onResponse(Collections.emptyList());
                    }
                },
                error -> {
                    Toast.makeText(context, R.string.connection_error, Toast.LENGTH_SHORT).show();
                    callback.onResponse(Collections.emptyList());
                });
    }



    public void getArticulo(int id, Response.Listener<Articulo> callback) {
        Map<String, String> params = new HashMap<>();
        params.put("idarticulo", String.valueOf(id));
        ws.post("articulo/obtener_articulo.php", params,
                response -> {
                    try {
                        JSONObject obj = new JSONObject(response);
                        if (obj.has("idarticulo")) {
                            Articulo articulo = new Articulo(
                                    obj.getInt("idarticulo"),
                                    obj.getInt("idmarca"),
                                    obj.getInt("idviaadministracion"),
                                    obj.getInt("idsubcategoria"),
                                    obj.getInt("idformafarmaceutica"),
                                    obj.getString("nombrearticulo"),
                                    obj.getString("descripcionarticulo"),
                                    obj.getInt("restringidoarticulo") == 1,
                                    obj.getDouble("precioarticulo"),
                                    context
                            );
                            callback.onResponse(articulo);
                        } else {
                            callback.onResponse(null);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        callback.onResponse(null);
                    }
                },
                error -> {
                    Toast.makeText(context, R.string.connection_error, Toast.LENGTH_SHORT).show();
                    callback.onResponse(null);
                });
    }

    public void deleteExistencia(int id, Response.Listener<String> callback) {
        Map<String, String> params = new HashMap<>();
        params.put("iddetalleexistencia", String.valueOf(id));

        ws.post("detalleexistencia/eliminar_detalleexistencia.php", params,
                response -> {
                    Toast.makeText(context, R.string.delete_message, Toast.LENGTH_SHORT).show();
                    callback.onResponse(response);
                },
                e -> Toast.makeText(context, R.string.connection_error, Toast.LENGTH_SHORT).show()
        );
    }

    private void isDuplicate(int id, Response.Listener<Boolean> callback) {
        Map<String, String> params = new HashMap<>();
        params.put("iddetalleexistencia", String.valueOf(id));
        ws.post("detalleexistencia/verificar_detalleexistencia.php", params,
                response -> {
                    try {
                        JSONObject obj = new JSONObject(response);
                        callback.onResponse(obj.optBoolean("existe", false));
                    } catch (JSONException e) {
                        e.printStackTrace();
                        callback.onResponse(false);
                    }
                },
                error -> {
                    Toast.makeText(context, R.string.connection_error, Toast.LENGTH_SHORT).show();
                    callback.onResponse(false);
                });
    }

    private void isUsed(DetalleExistencia detalleExistencia, Response.Listener<Boolean> callback) {
        Map<String, String> params = new HashMap<>();
        params.put("idarticulo", String.valueOf(detalleExistencia.getIdArticulo()));
        params.put("idfarmacia", String.valueOf(detalleExistencia.getIdFarmacia()));
        ws.post("detalleexistencia/verificar_existencia.php", params,
                response -> {
                    try {
                        JSONObject obj = new JSONObject(response);
                        callback.onResponse(obj.optBoolean("usado", false));
                    } catch (JSONException e) {
                        e.printStackTrace();
                        callback.onResponse(false);
                    }
                },
                error -> {
                    Toast.makeText(context, R.string.connection_error, Toast.LENGTH_SHORT).show();
                    callback.onResponse(false);
                });
    }
}

