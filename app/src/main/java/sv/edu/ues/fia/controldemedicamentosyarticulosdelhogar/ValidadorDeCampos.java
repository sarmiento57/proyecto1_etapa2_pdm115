package sv.edu.ues.fia.controldemedicamentosyarticulosdelhogar;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.List;
import java.util.regex.Pattern;

public class ValidadorDeCampos {

    private List<View> vistas;
    private List<String> listaRegex;
    private List<Integer> mensajesDeError;
    private Context contexto;

    public ValidadorDeCampos(Context contexto, List<View> vistas, List<String> listaRegex, List<Integer> mensajesDeError) {
        this.contexto = contexto;
        this.vistas = vistas;
        this.listaRegex = listaRegex;
        this.mensajesDeError = mensajesDeError;
    }

    public boolean validarCampos() {
        boolean todosLosCamposValidos = true;

        for (int i = 0; i < vistas.size(); i++) {
            View vista = vistas.get(i);
            String regex = listaRegex.get(i);
            int mensajeDeErrorId = mensajesDeError.get(i);

            if (vista instanceof EditText) {
                EditText editText = (EditText) vista;
                String texto = editText.getText().toString();
                if (!Pattern.matches(regex, texto)) {
                    editText.setError(contexto.getString(mensajeDeErrorId));
                    todosLosCamposValidos = false; // Validación fallida
                } else {
                    editText.setError(null); // Limpiar error si la validación pasa
                }
            }
            if (vista instanceof Spinner) {
                Spinner spinner = (Spinner) vista;
                if (spinner.getSelectedItemPosition() == 0) {
                    TextView errorText = (TextView) spinner.getSelectedView();
                    if (errorText != null) {
                        errorText.setError(""); // icono
                        errorText.setTextColor(Color.RED);
                        errorText.setText(contexto.getString(mensajeDeErrorId));
                    }
                    todosLosCamposValidos = false;
                }
            }
        }
        return todosLosCamposValidos; // Devolver resultado general de la validación
    }
}