package sv.edu.ues.fia.controldemedicamentosyarticulosdelhogar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.widget.Toast;
import android.content.Context;
import com.android.volley.Response;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class FacturaCompraDAO {

    private final Context context;
    private final WebServiceHelper ws;

    public FacturaCompraDAO(Context context) {
        this.context = context;
        this.ws = new WebServiceHelper(context);
    }

    public void addFacturaCompra(FacturaCompra facturaCompra, Response.Listener<String> callback) {
        isDuplicate(facturaCompra.getIdCompra(), exists -> {
            if (exists) {
                Toast.makeText(context, R.string.duplicate_message, Toast.LENGTH_SHORT).show();
                return;
            }

            Map<String, String> params = new HashMap<>();
            params.put("idcompra", String.valueOf(facturaCompra.getIdCompra()));
            params.put("idfarmacia", String.valueOf(facturaCompra.getIdFarmacia()));
            params.put("idproveedor", String.valueOf(facturaCompra.getIdProveedor()));
            params.put("fechacompra", facturaCompra.getFechaCompra());
            params.put("totalcompra", String.valueOf(facturaCompra.getTotalCompra()));

            ws.post("facturacompra/insertar_facturacompra.php", params,
                    response -> {
                        Toast.makeText(context, R.string.save_message, Toast.LENGTH_SHORT).show();
                        callback.onResponse(response);
                    },
                    e -> Toast.makeText(context, R.string.connection_error, Toast.LENGTH_SHORT).show()
            );
        });
    }

    public void updateFacturaCompra(FacturaCompra facturaCompra, Response.Listener<String> callback) {
        Map<String, String> params = new HashMap<>();
        params.put("idcompra", String.valueOf(facturaCompra.getIdCompra()));
        params.put("idfarmacia", String.valueOf(facturaCompra.getIdFarmacia()));
        params.put("idproveedor", String.valueOf(facturaCompra.getIdProveedor()));
        params.put("fechacompra", facturaCompra.getFechaCompra());
        params.put("totalcompra", String.valueOf(facturaCompra.getTotalCompra()));

        ws.post("facturacompra/actualizar_facturacompra.php", params,
                response -> {
                    Toast.makeText(context, R.string.update_message, Toast.LENGTH_SHORT).show();
                    callback.onResponse(response);
                },
                e -> Toast.makeText(context, R.string.connection_error, Toast.LENGTH_SHORT).show()
        );
    }

    public void deleteFacturaCompra(int idCompra, Response.Listener<String> callback) {
        Map<String, String> params = new HashMap<>();
        params.put("idcompra", String.valueOf(idCompra));

        ws.post("facturacompra/eliminar_facturacompra.php", params,
                response -> {
                    Toast.makeText(context, R.string.delete_message, Toast.LENGTH_SHORT).show();
                    callback.onResponse(response);
                },
                e -> Toast.makeText(context, R.string.connection_error, Toast.LENGTH_SHORT).show()
        );
    }

    public void getAllFacturaCompra(Response.Listener<List<FacturaCompra>> callback) {
        ws.post("facturacompra/listar_facturacompra.php", new HashMap<>(),
                response -> {
                    try {
                        JSONArray array = new JSONArray(response);
                        List<FacturaCompra> list = new ArrayList<>();
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject obj = array.getJSONObject(i);
                            list.add(new FacturaCompra(
                                    obj.getInt("idcompra"),
                                    obj.getInt("idfarmacia"),
                                    obj.getInt("idproveedor"),
                                    obj.getString("fechacompra"),
                                    obj.getDouble("totalcompra"),
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

    public void getFacturaCompra(int id, Response.Listener<FacturaCompra> callback) {
        Map<String, String> params = new HashMap<>();
        params.put("idcompra", String.valueOf(id));

        ws.post("facturacompra/obtener_facturacompra.php", params,
                response -> {
                    try {
                        JSONObject obj = new JSONObject(response);
                        if (obj.has("idcompra")) {
                            FacturaCompra facturaCompra = new FacturaCompra(
                                    obj.getInt("idcompra"),
                                    obj.getInt("idfarmacia"),
                                    obj.getInt("idproveedor"),
                                    obj.getString("fechacompra"),
                                    obj.getDouble("totalcompra"),
                                    context
                            );
                            callback.onResponse(facturaCompra);
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

    // Obtener todas las sucursales de farmacias
    public void getAllFarmacias(Response.Listener<List<SucursalFarmacia>> callback) {
        ws.post("facturacompra/listar_sucursales.php", new HashMap<>(),
                response -> {
                    try {
                        JSONArray array = new JSONArray(response);
                        List<SucursalFarmacia> farmacias = new ArrayList<>();
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject obj = array.getJSONObject(i);
                            farmacias.add(new SucursalFarmacia(
                                    obj.getInt("idfarmacia"),
                                    obj.getString("nombrefarmacia")
                            ));
                        }
                        callback.onResponse(farmacias);
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

    // Obtener todos los proveedores
    public void getAllProveedores(Response.Listener<List<Proveedor>> callback) {
        ws.post("facturacompra/listar_proveedores.php", new HashMap<>(),
                response -> {
                    try {
                        JSONArray array = new JSONArray(response);
                        List<Proveedor> proveedores = new ArrayList<>();
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject obj = array.getJSONObject(i);
                            proveedores.add(new Proveedor(
                                    obj.getInt("idproveedor"),
                                    obj.getString("nombreproveedor"),
                                    context
                            ));
                        }
                        callback.onResponse(proveedores);
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

    private void isDuplicate(int id, Response.Listener<Boolean> callback) {
        Map<String, String> params = new HashMap<>();
        params.put("idcompra", String.valueOf(id));

        ws.post("facturacompra/verificar_facturacompra.php", params,
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

    public int obtenerIdFacturaCompra(Response.Listener<Integer> callback) {
        ws.post("facturacompra/obtener_max_id.php", new HashMap<>(),
                response -> {
                    try {
                        JSONObject obj = new JSONObject(response);
                        int ultimoId = obj.optInt("max_id", 0);
                        callback.onResponse(ultimoId + 1);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        callback.onResponse(1);
                    }
                },
                error -> {
                    Toast.makeText(context, R.string.connection_error, Toast.LENGTH_SHORT).show();
                    callback.onResponse(1);
                });
        return 1;
    }
}



