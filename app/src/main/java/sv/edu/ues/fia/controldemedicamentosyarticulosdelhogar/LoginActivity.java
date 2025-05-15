package sv.edu.ues.fia.controldemedicamentosyarticulosdelhogar;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

public class LoginActivity extends AppCompatActivity {

    private EditText usernameEditText, passwordEditText;
    private Button loginButton;
    private WebServiceHelper webServiceHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        usernameEditText = findViewById(R.id.login_username);
        passwordEditText = findViewById(R.id.login_password);
        loginButton = findViewById(R.id.login_button);
        webServiceHelper = new WebServiceHelper(this);

        loginButton.setOnClickListener(v -> {
            String username = usernameEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, R.string.invalid_credentials, Toast.LENGTH_SHORT).show();
                return;
            }

            validarUsuarioMySQL(username, password);
        });
    }

    private void validarUsuarioMySQL(String username, String password) {
        Map<String, String> params = new HashMap<>();
        params.put("usuario", username);
        params.put("clave", password);

        webServiceHelper.post("login_usuario.php", params,
                response -> {
                    try {
                        JSONObject json = new JSONObject(response);
                        boolean success = json.getBoolean("success");

                        if (success) {
                            String idUsuario = json.getString("id_usuario");
                            JSONArray permisos = json.getJSONArray("permisos");

                            SharedPreferences prefs = getSharedPreferences("PERMISOS_APP", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.clear();

                            for (int i = 0; i < permisos.length(); i++) {
                                editor.putBoolean(permisos.getString(i), true);
                                Log.d("LoginActivity", "Permiso guardado: " + permisos.getString(i));
                            }

                            editor.putString("id_usuario", idUsuario);
                            editor.putString("user_name", username);
                            editor.apply();

                            Toast.makeText(this, getString(R.string.welcome_message) + " " + username, Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(this, MenuActivity.class));
                            finish();
                        } else {
                            Toast.makeText(this, R.string.invalid_credentials, Toast.LENGTH_SHORT).show();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, R.string.connection_error, Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(this, "Error de conexión: " + error.getMessage(), Toast.LENGTH_LONG).show();
                }
        );
    }
}
