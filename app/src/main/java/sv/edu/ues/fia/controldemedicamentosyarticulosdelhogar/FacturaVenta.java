package sv.edu.ues.fia.controldemedicamentosyarticulosdelhogar;

import android.content.Context;

public class FacturaVenta {
    private int idCliente;
    private int idVenta;
    private int idFarmacia;
    private String fechaVenta;
    private double totalVenta;
    private Context context;

    public FacturaVenta(int idVenta, int idCliente, int idFarmacia, String fechaVenta, double totalVenta, Context context) {
        this.idVenta = idVenta;
        this.idCliente = idCliente;
        this.idFarmacia = idFarmacia;
        this.fechaVenta = fechaVenta;
        this.totalVenta = totalVenta;
        this.context = context;
    }

    public FacturaVenta(int idVenta, Context context) {
        this.idVenta = idVenta;
        this.context = context;
        this.idCliente = 0;
        this.idFarmacia = 0;
        this.fechaVenta = "";
        this.totalVenta = 0.0;
    }

    public FacturaVenta() {
    }

    public int getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(int idCliente) {
        this.idCliente = idCliente;
    }

    public int getIdVenta() {
        return idVenta;
    }

    public void setIdVenta(int idVenta) {
        this.idVenta = idVenta;
    }

    public int getIdFarmacia() {
        return idFarmacia;
    }

    public void setIdFarmacia(int idFarmacia) {
        this.idFarmacia = idFarmacia;
    }

    public String getFechaVenta() {
        return fechaVenta;
    }

    public void setFechaVenta(String fechaVenta) {
        this.fechaVenta = fechaVenta;
    }

    public double getTotalVenta() {
        return totalVenta;
    }

    public void setTotalVenta(double totalVenta) {
        this.totalVenta = totalVenta;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    @Override
    public String toString() {
        return (context != null ? context.getString(R.string.sale_invoice_id_label) : "ID Venta") + ": " + getIdVenta() + "\n"
                + (context != null ? context.getString(R.string.sale_date_label) : "Fecha Venta") + ": " + getFechaVenta();
    }
}


