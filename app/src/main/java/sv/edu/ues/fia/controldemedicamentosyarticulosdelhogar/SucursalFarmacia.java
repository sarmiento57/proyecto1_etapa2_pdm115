package sv.edu.ues.fia.controldemedicamentosyarticulosdelhogar;

import android.content.Context;

public class SucursalFarmacia {
    private int idFarmacia;
    private int idDireccion;
    private String nombreFarmacia;

    private Context context ;

    public SucursalFarmacia(int idFarmacia, int idDireccion, String nombreFarmacia , Context context) {
        this.idFarmacia = idFarmacia;
        this.idDireccion = idDireccion;
        this.nombreFarmacia = nombreFarmacia;
        this.context = context;
    }

    public SucursalFarmacia(int idFarmacia, String nombreFarmacia) {
        this.idFarmacia = idFarmacia;
        this.nombreFarmacia = nombreFarmacia;
    }

    public int getIdFarmacia() {
        return idFarmacia;
    }

    public void setIdFarmacia(int idFarmacia) {
        this.idFarmacia = idFarmacia;
    }

    public int getIdDireccion() {
        return idDireccion;
    }

    public void setIdDireccion(int idDireccion) {
        this.idDireccion = idDireccion;
    }

    public String getNombreFarmacia() {
        return nombreFarmacia;
    }

    public void setNombreFarmacia(String nombreFarmacia) {
        this.nombreFarmacia = nombreFarmacia;
    }

    // esto se muestra en la factura compra en el spinner
    @Override
    public String toString() {
        return nombreFarmacia;
    }
}
