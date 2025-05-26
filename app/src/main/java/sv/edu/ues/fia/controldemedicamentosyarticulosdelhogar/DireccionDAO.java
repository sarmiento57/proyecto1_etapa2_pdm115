package sv.edu.ues.fia.controldemedicamentosyarticulosdelhogar;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
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

public class DireccionDAO {

    private final Context context;
    private final WebServiceHelper ws;

    public DireccionDAO(Context context) {
        this.context = context;
        this.ws = new WebServiceHelper(context);
    }

    public void addDireccion(Direccion direccion, Response.Listener<String> callback) {
        isDuplicate(direccion.getIdDireccion(), exists -> {
            if (exists) {
                Toast.makeText(context, R.string.duplicate_message, Toast.LENGTH_SHORT).show();
                return;
            }
            Map<String, String> params = new HashMap<>();
            params.put("iddireccion", String.valueOf(direccion.getIdDireccion()));
            params.put("iddistrito", String.valueOf(direccion.getIdDistrito()));
            params.put("direccionexacta", direccion.getDireccionExacta());

            ws.post("/direccion/insertar_direccion.php", params,
                    response -> {
                        Toast.makeText(context, R.string.save_message, Toast.LENGTH_SHORT).show();
                        callback.onResponse(response);
                    },
                    e -> Toast.makeText(context, R.string.connection_error, Toast.LENGTH_SHORT).show()
            );
        });
    }

    public void getDireccion(int id, Response.Listener<Direccion> callback) {
        Map<String, String> params = new HashMap<>();
        params.put("iddireccion", String.valueOf(id));
        ws.post("/direccion/obtener_direccion.php", params,
                response -> {
                    try {
                        JSONObject obj = new JSONObject(response);
                        if (obj.has("iddireccion")) {
                            Direccion direccion = new Direccion(
                                    obj.getInt("iddireccion"),
                                    obj.getInt("iddistrito"),
                                    obj.getString("direccionexacta"),
                                    context);
                            callback.onResponse(direccion);
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

    public void getAllDireccion(Response.Listener<List<Direccion>> callback) {
        ws.post("/direccion/listar_direcciones.php", new HashMap<>(),
                response -> {
                    try {
                        JSONArray array = new JSONArray(response);
                        List<Direccion> list = new ArrayList<>();
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject obj = array.getJSONObject(i);
                            list.add(new Direccion(
                                    obj.getInt("iddireccion"),
                                    obj.getInt("iddistrito"),
                                    obj.getString("direccionexacta"),
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

    public void getAllDepartamentos(Response.Listener<List<Departamento>> callback) {
        ws.post("/direccion/listar_departamentos.php", new HashMap<>(),
                response -> {
                    try {
                        JSONArray array = new JSONArray(response);
                        List<Departamento> list = new ArrayList<>();
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject obj = array.getJSONObject(i);
                            list.add(new Departamento(
                                    obj.getInt("iddepartamento"),
                                    obj.getString("nombredepartamento")));
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

    public void getDepartamento(int id, Response.Listener<Departamento> callback) {
        Map<String, String> params = new HashMap<>();
        params.put("iddepartamento", String.valueOf(id));
        ws.post("/direccion/obtener_departamento.php", params,
                response -> {
                    try {
                        JSONObject obj = new JSONObject(response);
                        if (obj.has("iddepartamento")) {
                            Departamento dep = new Departamento(
                                    obj.getInt("iddepartamento"),
                                    obj.getString("nombredepartamento"));
                            callback.onResponse(dep);
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

    public void getMunicipio(int id, Response.Listener<Municipio> callback) {
        Map<String, String> params = new HashMap<>();
        params.put("idmunicipio", String.valueOf(id));
        ws.post("/direccion/obtener_municipio.php", params,
                response -> {
                    try {
                        JSONObject obj = new JSONObject(response);
                        if (obj.has("idmunicipio")) {
                            Municipio mun = new Municipio(
                                    obj.getInt("idmunicipio"),
                                    obj.getInt("iddepartamento"),
                                    obj.getString("nombremunicipio"));
                            callback.onResponse(mun);
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

    public void getDistrito(int id, Response.Listener<Distrito> callback) {
        Map<String, String> params = new HashMap<>();
        params.put("iddistrito", String.valueOf(id));
        ws.post("/direccion/obtener_distrito.php", params,
                response -> {
                    try {
                        JSONObject obj = new JSONObject(response);
                        if (obj.has("iddistrito")) {
                            Distrito dis = new Distrito(
                                    obj.getInt("iddistrito"),
                                    obj.getInt("idmunicipio"),
                                    obj.getString("nombredistrito"));
                            callback.onResponse(dis);
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

    public void getAllMunicipios(int departamentoId, Response.Listener<List<Municipio>> callback) {
        Map<String, String> params = new HashMap<>();
        params.put("iddepartamento", String.valueOf(departamentoId));
        ws.post("/direccion/listar_municipios.php", params,
                response -> {
                    try {
                        JSONArray array = new JSONArray(response);
                        List<Municipio> list = new ArrayList<>();
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject obj = array.getJSONObject(i);
                            list.add(new Municipio(
                                    obj.getInt("idmunicipio"),
                                    obj.getInt("iddepartamento"),
                                    obj.getString("nombremunicipio")));
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

    public void getAllDistritos(int municipioId, Response.Listener<List<Distrito>> callback) {
        Map<String, String> params = new HashMap<>();
        params.put("idmunicipio", String.valueOf(municipioId));
        ws.post("/direccion/listar_distritos.php", params,
                response -> {
                    try {
                        JSONArray array = new JSONArray(response);
                        List<Distrito> list = new ArrayList<>();
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject obj = array.getJSONObject(i);
                            list.add(new Distrito(
                                    obj.getInt("iddistrito"),
                                    obj.getInt("idmunicipio"),
                                    obj.getString("nombredistrito")));
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

    public void updateDireccion(Direccion direccion, Response.Listener<String> callback) {
        Map<String, String> params = new HashMap<>();
        params.put("iddireccion", String.valueOf(direccion.getIdDireccion()));
        params.put("iddistrito", String.valueOf(direccion.getIdDistrito()));
        params.put("direccionexacta", direccion.getDireccionExacta());

        ws.post("/direccion/actualizar_direccion.php", params,
                response -> {
                    Toast.makeText(context, R.string.update_message, Toast.LENGTH_SHORT).show();
                    callback.onResponse(response);
                },
                e -> Toast.makeText(context, R.string.connection_error, Toast.LENGTH_SHORT).show()
        );
    }

    public void deleteDireccion(int id, Response.Listener<String> callback) {
        Map<String, String> params = new HashMap<>();
        params.put("iddireccion", String.valueOf(id));

        ws.post("/direccion/eliminar_direccion.php", params,
                response -> {
                    Toast.makeText(context, R.string.delete_message, Toast.LENGTH_SHORT).show();
                    callback.onResponse(response);
                },
                e -> Toast.makeText(context, R.string.connection_error, Toast.LENGTH_SHORT).show()
        );
    }

    private void isDuplicate(int id, Response.Listener<Boolean> callback) {
        Map<String, String> params = new HashMap<>();
        params.put("iddireccion", String.valueOf(id));
        ws.post("/direccion/verificar_direccion.php", params,
                response -> {
                    try {
                        JSONObject obj = new JSONObject(response);
                        boolean existe = obj.optBoolean("existe", false);
                        callback.onResponse(existe);
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




