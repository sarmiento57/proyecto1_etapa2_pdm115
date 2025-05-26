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

public class ProveedorDAO {

    private final Context context;
    private final WebServiceHelper ws;

    public ProveedorDAO(Context context) {
        this.context = context;
        this.ws = new WebServiceHelper(context);
    }

    // Obtener todos los proveedores
    public void getAllProveedores(Response.Listener<List<Proveedor>> callback) {
        ws.post("proveedor/listar_proveedores.php", new HashMap<>(),
                response -> {
                    try {
                        JSONArray array = new JSONArray(response);
                        List<Proveedor> proveedores = new ArrayList<>();
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject obj = array.getJSONObject(i);
                            proveedores.add(new Proveedor(
                                    obj.getInt("idproveedor"),
                                    obj.getString("nombreproveedor"),
                                    obj.getString("telefonoproveedor"),
                                    obj.getString("direccionproveedor"),
                                    obj.getString("rubroproveedor"),
                                    obj.getString("numregproveedor"),
                                    obj.getString("nit"),
                                    obj.getString("giroproveedor"),
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

    // Obtener proveedor por ID
    public void getProveedorById(int idProveedor, Response.Listener<Proveedor> callback) {
        Map<String, String> params = new HashMap<>();
        params.put("idproveedor", String.valueOf(idProveedor));
        ws.post("proveedor/obtener_proveedor.php", params,
                response -> {
                    try {
                        JSONObject obj = new JSONObject(response);
                        Proveedor proveedor = new Proveedor(
                                obj.getInt("idproveedor"),
                                obj.getString("nombreproveedor"),
                                obj.getString("telefonoproveedor"),
                                obj.getString("direccionproveedor"),
                                obj.getString("rubroproveedor"),
                                obj.getString("numregproveedor"),
                                obj.getString("nit"),
                                obj.getString("giroproveedor"),
                                context
                        );
                        callback.onResponse(proveedor);
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

    // Agregar nuevo proveedor
    public void addProveedor(Proveedor proveedor, Response.Listener<String> callback) {
        isDuplicate(proveedor.getIdProveedor(), proveedor.getNitProveedor(), exists -> {
            if (exists) {
                Toast.makeText(context, R.string.provider_exists, Toast.LENGTH_SHORT).show();
                return;
            }

            Map<String, String> params = new HashMap<>();
            params.put("idproveedor", String.valueOf(proveedor.getIdProveedor()));
            params.put("nombreproveedor", proveedor.getNombreProveedor());
            params.put("telefonoproveedor", proveedor.getTelefonoProveedor());
            params.put("direccionproveedor", proveedor.getDireccionProveedor());
            params.put("rubroproveedor", proveedor.getRubroProveedor());
            params.put("numregproveedor", proveedor.getNumRegProveedor());
            params.put("nit", proveedor.getNitProveedor());
            params.put("giroproveedor", proveedor.getGiroProveedor());

            ws.post("proveedor/insertar_proveedor.php", params,
                    response -> {
                        Toast.makeText(context, R.string.save_message, Toast.LENGTH_SHORT).show();
                        callback.onResponse(response);
                    },
                    error -> Toast.makeText(context, R.string.connection_error, Toast.LENGTH_SHORT).show()
            );
        });
    }


    // Actualizar proveedor
    public void updateProveedor(Proveedor proveedor, Response.Listener<String> callback) {
        isDuplicateNIT(proveedor.getNitProveedor(), proveedor.getIdProveedor(), isDuplicated -> {
            if (isDuplicated) {
                Toast.makeText(context, R.string.duplicate_nit_message, Toast.LENGTH_SHORT).show();
                return;
            }

            Map<String, String> params = new HashMap<>();
            params.put("idproveedor", String.valueOf(proveedor.getIdProveedor()));
            params.put("nombreproveedor", proveedor.getNombreProveedor());
            params.put("telefonoproveedor", proveedor.getTelefonoProveedor());
            params.put("direccionproveedor", proveedor.getDireccionProveedor());
            params.put("rubroproveedor", proveedor.getRubroProveedor());
            params.put("numregproveedor", proveedor.getNumRegProveedor());
            params.put("nit", proveedor.getNitProveedor());
            params.put("giroproveedor", proveedor.getGiroProveedor());

            ws.post("proveedor/actualizar_proveedor.php", params,
                    response -> {
                        Toast.makeText(context, R.string.update_message, Toast.LENGTH_SHORT).show();
                        callback.onResponse(response);
                    },
                    error -> Toast.makeText(context, R.string.connection_error, Toast.LENGTH_SHORT).show()
            );
        });
    }


    // Eliminar proveedor
    public void deleteProveedor(int idProveedor, Response.Listener<String> callback) {
        Map<String, String> params = new HashMap<>();
        params.put("idproveedor", String.valueOf(idProveedor));

        ws.post("proveedor/eliminar_proveedor.php", params,
                response -> {
                    Toast.makeText(context, R.string.delete_message, Toast.LENGTH_SHORT).show();
                    callback.onResponse(response);
                },
                error -> Toast.makeText(context, R.string.connection_error, Toast.LENGTH_SHORT).show()
        );
    }

// Verificar si el proveedor ya existe por ID o NIT
    private void isDuplicate(int idProveedor, String nit, Response.Listener<Boolean> callback) {
        Map<String, String> params = new HashMap<>();
        params.put("idproveedor", String.valueOf(idProveedor));
        params.put("nit", nit);
        ws.post("proveedor/verificar_proveedor.php", params,
                response -> {
                    try {
                        JSONObject obj = new JSONObject(response);
                        boolean exists = obj.optBoolean("existe", false);
                        callback.onResponse(exists); // Llamamos al callback con el resultado
                    } catch (JSONException e) {
                        e.printStackTrace();
                        callback.onResponse(false); // En caso de error, devolvemos false
                    }
                },
                error -> {
                    Toast.makeText(context, R.string.connection_error, Toast.LENGTH_SHORT).show();
                    callback.onResponse(false); // En caso de error en la conexión, devolvemos false
                });
    }


    // Verificar si el NIT es duplicado
    private void isDuplicateNIT(String nit, int currentId, Response.Listener<Boolean> callback) {
        Map<String, String> params = new HashMap<>();
        params.put("nit", nit);
        params.put("idproveedor", String.valueOf(currentId));
        ws.post("proveedor/verificar_nit.php", params,
                response -> {
                    try {
                        JSONObject obj = new JSONObject(response);
                        boolean isDuplicated = obj.optBoolean("duplicado", false);
                        callback.onResponse(isDuplicated); // Llamamos al callback con el resultado
                    } catch (JSONException e) {
                        e.printStackTrace();
                        callback.onResponse(false); // En caso de error, devolvemos false
                    }
                },
                error -> {
                    Toast.makeText(context, R.string.connection_error, Toast.LENGTH_SHORT).show();
                    callback.onResponse(false); // En caso de error en la conexión, devolvemos false
                });
    }

}


