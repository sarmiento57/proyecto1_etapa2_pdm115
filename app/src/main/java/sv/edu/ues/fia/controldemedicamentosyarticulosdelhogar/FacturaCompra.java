package sv.edu.ues.fia.controldemedicamentosyarticulosdelhogar;

import android.content.Context;

public class FacturaCompra {
    private int idCompra;
    private int idFarmacia;
    private int idProveedor;
    private String fechaCompra;
    private double totalCompra;

    private Context context;

    public FacturaCompra(int idCompra, int idFarmacia, int idProveedor, String fechaCompra, double totalCompra, Context context) {
        this.idCompra = idCompra;
        this.idFarmacia = idFarmacia;
        this.idProveedor = idProveedor;
        this.fechaCompra = fechaCompra;
        this.totalCompra = totalCompra;
        this.context = context;
    }

    public FacturaCompra(int idCompra, int idProveedor, String fechaCompra, Context context) {
        this.idCompra = idCompra;
        this.idProveedor = idProveedor;
        this.fechaCompra = fechaCompra;
        this.context = context;
    }

    public int getIdCompra() {
        return idCompra;
    }

    public void setIdCompra(int idCompra) {
        this.idCompra = idCompra;
    }

    public int getIdFarmacia() {
        return idFarmacia;
    }

    public void setIdFarmacia(int idFarmacia) {
        this.idFarmacia = idFarmacia;
    }

    public int getIdProveedor() {
        return idProveedor;
    }

    public void setIdProveedor(int idProveedor) {
        this.idProveedor = idProveedor;
    }

    public String getFechaCompra() {
        return fechaCompra;
    }

    public void setFechaCompra(String fechaCompra) {
        this.fechaCompra = fechaCompra;
    }

    public double getTotalCompra() {
        return totalCompra;
    }

    public void setTotalCompra(double totalCompra) {
        this.totalCompra = totalCompra;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    @Override
    public String toString() {
        if (getIdCompra() == -1) {
            return context.getString(R.string.select_factura);
        }
        return context.getString(R.string.invoice_id) +": " + getIdCompra() + "\n" + context.getString(R.string.purchase_date) +":" + getFechaCompra();
    }
}
