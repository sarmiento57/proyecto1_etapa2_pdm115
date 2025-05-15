package sv.edu.ues.fia.controldemedicamentosyarticulosdelhogar;

import android.content.Context;

public class ViaAdministracion {
    private int idViaAdministracion;
    private String tipoAdministracion;
    private Context context;

    public ViaAdministracion(int idViaAdministracion, String tipoAdministracion, Context context) {
        this.idViaAdministracion = idViaAdministracion;
        this.tipoAdministracion = tipoAdministracion;
        this.context = context;
    }

    public int getIdViaAdministracion() {
        return idViaAdministracion;
    }

    public String getTipoAdministracion() {
        return tipoAdministracion;
    }

    public void setTipoAdministracion(String tipoAdministracion) {
        this.tipoAdministracion = tipoAdministracion;
    }

    @Override
    public String toString() {
        return context.getString(R.string.id_via_administracion) + " : " + idViaAdministracion + "\n" +
                context.getString(R.string.tipo_via_administracion) + " : " + tipoAdministracion;
    }
}
