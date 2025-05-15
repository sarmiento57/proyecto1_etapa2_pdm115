package sv.edu.ues.fia.controldemedicamentosyarticulosdelhogar;

import android.content.Context;

public class FormaFarmaceutica {
    private int idFormaFarmaceutica;
    private String tipoFormaFarmaceutica;
    private Context context;

    public FormaFarmaceutica(int idFormaFarmaceutica, String tipoFormaFarmaceutica, Context context) {
        this.idFormaFarmaceutica = idFormaFarmaceutica;
        this.tipoFormaFarmaceutica = tipoFormaFarmaceutica;
        this.context = context;
    }

    public int getIdFormaFarmaceutica() {
        return idFormaFarmaceutica;
    }

    public void setIdFormaFarmaceutica(int idFormaFarmaceutica) {
        this.idFormaFarmaceutica = idFormaFarmaceutica;
    }

    public String getTipoFormaFarmaceutica() {
        return tipoFormaFarmaceutica;
    }

    public void setTipoFormaFarmaceutica(String tipoFormaFarmaceutica) {
        this.tipoFormaFarmaceutica = tipoFormaFarmaceutica;
    }

    @Override
    public String toString() {
        return context.getString(R.string.id_forma_farmaceutica) + ": " + getIdFormaFarmaceutica() + "\n"
           + context.getString(R.string.tipo_forma_farmaceutica) + ": " + getTipoFormaFarmaceutica();
    }
}