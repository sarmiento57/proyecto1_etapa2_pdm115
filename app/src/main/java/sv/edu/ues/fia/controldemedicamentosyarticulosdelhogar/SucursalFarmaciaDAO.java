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

public class SucursalFarmaciaDAO {

    private final Context context;
    private final WebServiceHelper ws;

    public SucursalFarmaciaDAO(Context context) {
        this.context = context;
        this.ws = new WebServiceHelper(context);
    }

    // Agregar Farmacia
    public void addSucursalFarmacia(SucursalFarmacia sucursal, Response.Listener<String> callback) {
        getSucursalFarmacia(sucursal.getIdFarmacia(), exists -> {
            if (exists != null && exists.getIdFarmacia() != 0) {
                Toast.makeText(context, R.string.farma_exists, Toast.LENGTH_SHORT).show();
                return;
            }

            Map<String, String> params = new HashMap<>();
            params.put("idfarmacia", String.valueOf(sucursal.getIdFarmacia()));
            params.put("iddireccion", String.valueOf(sucursal.getIdDireccion()));
            params.put("nombrefarmacia", sucursal.getNombreFarmacia());

            ws.post("sucursalfarmacia/insertar_sucursalfarmacia.php", params,
                    response -> {
                        Toast.makeText(context, R.string.save_message, Toast.LENGTH_SHORT).show();
                        callback.onResponse(response);
                    },
                    e -> Toast.makeText(context, R.string.connection_error, Toast.LENGTH_SHORT).show()
            );
        });
    }

    // Obtener Farmacia por ID
    public void getSucursalFarmacia(int id, Response.Listener<SucursalFarmacia> callback) {
        Map<String, String> params = new HashMap<>();
        params.put("idfarmacia", String.valueOf(id));

        ws.post("sucursalfarmacia/obtener_sucursalfarmacia.php", params,
                response -> {
                    try {
                        JSONObject obj = new JSONObject(response);
                        if (obj.has("idfarmacia")) {
                            SucursalFarmacia sucursal = new SucursalFarmacia(
                                    obj.getInt("idfarmacia"),
                                    obj.getInt("iddireccion"),
                                    obj.getString("nombrefarmacia"),
                                    context);
                            callback.onResponse(sucursal);
                        } else {
                            callback.onResponse(null);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        callback.onResponse(null);
                    }
                },
                e -> {
                    Toast.makeText(context, R.string.connection_error, Toast.LENGTH_SHORT).show();
                    callback.onResponse(null);
                });
    }

    // Listar todas las farmacias
    public void getAllSucursalFarmacia(Response.Listener<List<SucursalFarmacia>> callback) {
        ws.post("sucursalfarmacia/listar_sucursalfarmacia.php", new HashMap<>(),
                response -> {
                    try {
                        JSONArray array = new JSONArray(response);
                        List<SucursalFarmacia> list = new ArrayList<>();
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject obj = array.getJSONObject(i);
                            list.add(new SucursalFarmacia(
                                    obj.getInt("idfarmacia"),
                                    obj.getInt("iddireccion"),
                                    obj.getString("nombrefarmacia"),
                                    context));
                        }
                        callback.onResponse(list);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        callback.onResponse(Collections.emptyList());
                    }
                },
                e -> {
                    Toast.makeText(context, R.string.connection_error, Toast.LENGTH_SHORT).show();
                    callback.onResponse(Collections.emptyList());
                });
    }

    // Actualizar Farmacia
    public void updateSucursalFarmacia(SucursalFarmacia sucursal, Response.Listener<String> callback) {
        Map<String, String> params = new HashMap<>();
        params.put("idfarmacia", String.valueOf(sucursal.getIdFarmacia()));
        params.put("nombrefarmacia", sucursal.getNombreFarmacia());

        ws.post("sucursalfarmacia/actualizar_sucursalfarmacia.php", params,
                response -> {
                    Toast.makeText(context, R.string.update_message, Toast.LENGTH_SHORT).show();
                    callback.onResponse(response);
                },
                e -> Toast.makeText(context, R.string.connection_error, Toast.LENGTH_SHORT).show()
        );
    }

    // Eliminar Farmacia
    public void eliminarSucursal(int id, Response.Listener<String> callback) {
        Map<String, String> params = new HashMap<>();
        params.put("idfarmacia", String.valueOf(id));

        ws.post("sucursalfarmacia/eliminar_sucursalfarmacia.php", params,
                response -> {
                    Toast.makeText(context, R.string.delete_message, Toast.LENGTH_SHORT).show();
                    callback.onResponse(response);
                },
                e -> Toast.makeText(context, R.string.connection_error, Toast.LENGTH_SHORT).show()
        );
    }
}

