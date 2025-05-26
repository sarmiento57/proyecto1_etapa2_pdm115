package sv.edu.ues.fia.controldemedicamentosyarticulosdelhogar;

import android.content.Context;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

public class WebServiceHelper {
    public final Context context;
    private final String BASE_URL = "http://192.168.1.51/api_farmacia/";


    public WebServiceHelper(Context context) {
        this.context = context;
    }

    public void post(String endpoint, Map<String, String> params,
                     Response.Listener<String> onSuccess,
                     Response.ErrorListener onError) {
        StringRequest req = new StringRequest(Request.Method.POST, BASE_URL + endpoint, onSuccess, onError) {
            @Override
            protected Map<String, String> getParams() {
                return params;
            }
        };
        Volley.newRequestQueue(context).add(req);
    }

    public void ejecutarScript(String script) {
        Map<String, String> p = new HashMap<>();
        p.put("script", script);
        post("ejecutar_script.php", p,
                r -> Toast.makeText(context, r, Toast.LENGTH_SHORT).show(),
                e -> Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    // Método para ejecutar el script 'test_data.sql' en el servidor
    public void ejecutarTestDataScript(Response.Listener<String> onSuccess, Response.ErrorListener onError) {
        Map<String, String> p = new HashMap<>();
        p.put("script", "test_data");  // Especificamos que queremos ejecutar el script 'test_data.sql'
        post("ejecutar_test_data.php", p, onSuccess, onError);
    }

}

