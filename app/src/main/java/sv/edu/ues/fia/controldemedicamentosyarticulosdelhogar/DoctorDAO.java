package sv.edu.ues.fia.controldemedicamentosyarticulosdelhogar;

import android.content.Context;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// DAO adaptado para trabajar con WebService en lugar de SQLite
public class DoctorDAO {
    private final Context context;
    private final WebServiceHelper ws;

    public DoctorDAO(Context context) {
        this.context = context;
        this.ws = new WebServiceHelper(context);
    }

    public void addDoctor(Doctor doctor) {
        isDuplicate(doctor.getIdDoctor(), exists -> {
            if (exists) {
                Toast.makeText(context, R.string.duplicate_message, Toast.LENGTH_SHORT).show();
                return;
            }

            Map<String, String> params = new HashMap<>();
            params.put("IDDOCTOR", String.valueOf(doctor.getIdDoctor()));
            params.put("NOMBREDOCTOR", doctor.getNombreDoctor());
            params.put("ESPECIALIDADDOCTOR", doctor.getEspecialidadDoctor());
            params.put("JVPM", doctor.getJvpm());

            ws.post("insertar_doctor.php", params,
                    r -> Toast.makeText(context, R.string.save_message, Toast.LENGTH_SHORT).show(),
                    e -> Toast.makeText(context, R.string.connection_error, Toast.LENGTH_SHORT).show());
        });
    }


    public void updateDoctor(Doctor doctor) {
        Map<String, String> params = new HashMap<>();
        params.put("IDDOCTOR", String.valueOf(doctor.getIdDoctor()));
        params.put("NOMBREDOCTOR", doctor.getNombreDoctor());
        params.put("ESPECIALIDADDOCTOR", doctor.getEspecialidadDoctor());
        params.put("JVPM", doctor.getJvpm());

        ws.post("actualizar_doctor.php", params,
                r -> Toast.makeText(context, R.string.update_message, Toast.LENGTH_SHORT).show(),
                e -> Toast.makeText(context, R.string.connection_error, Toast.LENGTH_SHORT).show());
    }

    public void deleteDoctor(int id) {
        Map<String, String> params = new HashMap<>();
        params.put("IDDOCTOR", String.valueOf(id));

        ws.post("eliminar_doctor.php", params,
                r -> Toast.makeText(context, R.string.delete_message, Toast.LENGTH_SHORT).show(),
                e -> Toast.makeText(context, R.string.connection_error, Toast.LENGTH_SHORT).show());
    }

    public void getDoctor(int id, Response.Listener<Doctor> callback) {
        Map<String, String> params = new HashMap<>();
        params.put("IDDOCTOR", String.valueOf(id));

        ws.post("obtener_doctor.php", params,
                response -> {
                    try {
                        JSONObject obj = new JSONObject(response);
                        Doctor doctor = new Doctor(
                                obj.getInt("IDDOCTOR"),
                                obj.getString("NOMBREDOCTOR"),
                                obj.getString("ESPECIALIDADDOCTOR"),
                                obj.getString("JVPM"),
                                context
                        );
                        callback.onResponse(doctor);
                    } catch (Exception e) {
                        Toast.makeText(context, R.string.no, Toast.LENGTH_SHORT).show();
                    }
                },
                e -> Toast.makeText(context, R.string.connection_error, Toast.LENGTH_SHORT).show());
    }

    public void getAllDoctors(Response.Listener<List<Doctor>> callback) {
        ws.post("listar_doctores.php", new HashMap<>(),
                response -> {
                    try {
                        JSONArray array = new JSONArray(response);
                        List<Doctor> list = new ArrayList<>();
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject obj = array.getJSONObject(i);
                            list.add(new Doctor(
                                    obj.getInt("IDDOCTOR"),
                                    obj.getString("NOMBREDOCTOR"),
                                    obj.getString("ESPECIALIDADDOCTOR"),
                                    obj.getString("JVPM"),
                                    context
                            ));
                        }
                        callback.onResponse(list);
                    } catch (Exception e) {
                        Toast.makeText(context, R.string.no, Toast.LENGTH_SHORT).show();
                    }
                },
                e -> Toast.makeText(context, R.string.connection_error, Toast.LENGTH_SHORT).show());
    }
    public void isDuplicate(int idDoctor, Response.Listener<Boolean> callback) {
        Map<String, String> params = new HashMap<>();
        params.put("IDDOCTOR", String.valueOf(idDoctor));

        ws.post("verificar_doctor.php", params,
                response -> {
                    // Esperamos algo como {"existe": true}
                    try {
                        JSONObject obj = new JSONObject(response);
                        boolean existe = obj.getBoolean("existe");
                        callback.onResponse(existe);
                    } catch (Exception e) {
                        Toast.makeText(context, R.string.connection_error, Toast.LENGTH_SHORT).show();
                        callback.onResponse(false); // Por defecto no duplicado si falla
                    }
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(context, R.string.connection_error, Toast.LENGTH_SHORT).show();
                    callback.onResponse(false); // Por defecto no duplicado si falla
                });
    }

}

