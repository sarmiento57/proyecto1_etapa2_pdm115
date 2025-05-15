package sv.edu.ues.fia.controldemedicamentosyarticulosdelhogar;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

public class MenuAdapter extends ArrayAdapter<String> {

    private final Context contexto;
    private final String[] opciones;
    private final int[] iconos;

    public MenuAdapter(@NonNull Context contexto, String[] opciones, int[] iconos) {
        super(contexto, R.layout.menu_opciones, opciones);
        this.contexto = contexto;
        this.opciones = opciones;
        this.iconos = iconos;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if(convertView == null)
            convertView = LayoutInflater.from(contexto).inflate(R.layout.menu_opciones, parent, false);

        ImageView icono_opcion = convertView.findViewById(R.id.icono_opcion);
        TextView texto_opcion = convertView.findViewById(R.id.texto_opcion);
        icono_opcion.setImageDrawable(ContextCompat.getDrawable(contexto, iconos[position]));
        texto_opcion.setText(opciones[position]);

        return convertView;
    }
}
