package sv.edu.ues.fia.controldemedicamentosyarticulosdelhogar;

import android.content.Context;

public class Receta {
    private int idDoctor;
    private int idCliente;
    private int idReceta;
    private String fechaExpedida;
    private String descripcion;
    private Context context;

    public Receta(int idDoctor, int idCliente, int idReceta, String fechaExpedida, String descripcion, Context context) {
        this.idDoctor = idDoctor;
        this.idCliente = idCliente;
        this.idReceta = idReceta;
        this.fechaExpedida = fechaExpedida;
        this.descripcion = descripcion;
        this.context = context;
    }

    public int getIdDoctor() { return idDoctor; }
    public int getIdCliente() { return idCliente; }
    public int getIdReceta() { return idReceta; }
    public String getFechaExpedida() { return fechaExpedida; }
    public String getDescripcion() { return descripcion; }

    public void setFechaExpedida(String fechaExpedida) {
        this.fechaExpedida = fechaExpedida;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    @Override
    public String toString() {
        return context.getString(R.string.id_doctor) + " : " + idDoctor + "\n" +
                context.getString(R.string.id_receta) + " : " + idReceta + "\n" +
                context.getString(R.string.fecha_receta) + " : " + fechaExpedida;
    }
}
