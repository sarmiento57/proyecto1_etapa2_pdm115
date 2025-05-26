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

public class RecetaDAO {

    private final Context context;
    private final WebServiceHelper ws;

    public RecetaDAO(Context context) {
        this.context = context;
        this.ws = new WebServiceHelper(context);
    }

    public void addReceta(Receta receta, Response.Listener<String> callback) {
        isDuplicate(receta.getIdReceta(), exists -> {
            if (exists) {
                Toast.makeText(context, R.string.duplicate_message, Toast.LENGTH_SHORT).show();
                return;
            }

            Map<String, String> params = new HashMap<>();
            params.put("idreceta", String.valueOf(receta.getIdReceta()));
            params.put("iddoctor", String.valueOf(receta.getIdDoctor()));
            params.put("idcliente", String.valueOf(receta.getIdCliente()));
            params.put("fechaexpedida", receta.getFechaExpedida());
            params.put("descripcion", receta.getDescripcion());

            ws.post("receta/insertar_receta.php", params,
                    response -> {
                        Toast.makeText(context, R.string.save_message, Toast.LENGTH_SHORT).show();
                        callback.onResponse(response);
                    },
                    error -> Toast.makeText(context, R.string.connection_error, Toast.LENGTH_SHORT).show()
            );
        });
    }

    public void updateReceta(Receta receta, Response.Listener<String> callback) {
        Map<String, String> params = new HashMap<>();
        params.put("idreceta", String.valueOf(receta.getIdReceta()));
        params.put("fechaexpedida", receta.getFechaExpedida());
        params.put("descripcion", receta.getDescripcion());

        ws.post("receta/actualizar_receta.php", params,
                response -> {
                    Toast.makeText(context, R.string.update_message, Toast.LENGTH_SHORT).show();
                    callback.onResponse(response);
                },
                error -> Toast.makeText(context, R.string.connection_error, Toast.LENGTH_SHORT).show()
        );
    }


    public void deleteReceta(int idReceta, Response.Listener<String> callback) {
        Map<String, String> params = new HashMap<>();
        params.put("idreceta", String.valueOf(idReceta));

        ws.post("receta/eliminar_receta.php", params,
                response -> {
                    Toast.makeText(context, R.string.delete_message, Toast.LENGTH_SHORT).show();
                    callback.onResponse(response);
                },
                error -> Toast.makeText(context, R.string.connection_error, Toast.LENGTH_SHORT).show()
        );
    }

    public void getAllRecetas(Response.Listener<List<Receta>> callback) {
        ws.post("receta/listar_recetas.php", new HashMap<>(),
                response -> {
                    try {
                        JSONArray array = new JSONArray(response);
                        List<Receta> list = new ArrayList<>();
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject obj = array.getJSONObject(i);
                            list.add(new Receta(
                                    obj.getInt("idreceta"),
                                    obj.getInt("iddoctor"),
                                    obj.getInt("idcliente"),
                                    obj.getString("fechaexpedida"),
                                    obj.getString("descripcion"),
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

    public void getReceta(int id, Response.Listener<Receta> callback) {
        Map<String, String> params = new HashMap<>();
        params.put("idreceta", String.valueOf(id));

        ws.post("receta/obtener_receta.php", params,
                response -> {
                    try {
                        JSONObject obj = new JSONObject(response);
                        if (obj.has("idreceta")) {
                            Receta receta = new Receta(
                                    obj.getInt("idreceta"),
                                    obj.getInt("iddoctor"),
                                    obj.getInt("idcliente"),
                                    obj.getString("fechaexpedida"),
                                    obj.getString("descripcion"),
                                    context
                            );
                            callback.onResponse(receta);
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

    // Verificar si la receta es duplicada
    private void isDuplicate(int id, Response.Listener<Boolean> callback) {
        Map<String, String> params = new HashMap<>();
        params.put("idreceta", String.valueOf(id));

        ws.post("receta/verificar_receta.php", params,
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

    // Obtener todos los doctores
    public void getAllDoctores(Response.Listener<List<Doctor>> callback) {
        ws.post("receta/listar_doctores.php", new HashMap<>(),
                response -> {
                    try {
                        JSONArray array = new JSONArray(response);
                        List<Doctor> doctores = new ArrayList<>();
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject obj = array.getJSONObject(i);
                            doctores.add(new Doctor(
                                    obj.getInt("iddoctor"),
                                    obj.getString("nombredoctor"),
                                    context
                            ));
                        }
                        callback.onResponse(doctores);
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

    // Obtener todos los clientes
    public void getAllClientes(Response.Listener<List<Cliente>> callback) {
        ws.post("receta/listar_clientes.php", new HashMap<>(),
                response -> {
                    try {
                        JSONArray array = new JSONArray(response);
                        List<Cliente> clientes = new ArrayList<>();
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject obj = array.getJSONObject(i);
                            clientes.add(new Cliente(
                                    obj.getInt("idcliente"),
                                    obj.getString("nombrecliente"),
                                    context
                            ));
                        }
                        callback.onResponse(clientes);
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
}
