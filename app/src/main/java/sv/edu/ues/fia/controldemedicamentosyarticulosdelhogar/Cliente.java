package sv.edu.ues.fia.controldemedicamentosyarticulosdelhogar;

import android.content.Context;

public class Cliente {
    private int idCliente;
    private String nombreCliente;
    private String telefonoCliente;
    private String correoCliente;
    private Context context;

    public Cliente(int idCliente, String nombreCliente, String telefonoCliente, String correoCliente, Context context) {
        this.idCliente = idCliente;
        this.nombreCliente = nombreCliente;
        this.telefonoCliente = telefonoCliente;
        this.correoCliente = correoCliente;
        this.context = context;
    }



    public Cliente(int idCliente, String nombreCliente, Context context) {
        this.idCliente = idCliente;
        this.nombreCliente = nombreCliente;
        this.context = context;

        this.telefonoCliente = "";
        this.correoCliente = "";
    }


    public int getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(int idCliente) {
        this.idCliente = idCliente;
    }

    public String getNombreCliente() {
        return nombreCliente;
    }

    public void setNombreCliente(String nombreCliente) {
        this.nombreCliente = nombreCliente;
    }

    public String getTelefonoCliente() {
        return telefonoCliente;
    }

    public void setTelefonoCliente(String telefonoCliente) {
        this.telefonoCliente = telefonoCliente;
    }

    public String getCorreoCliente() {
        return correoCliente;
    }

    public void setCorreoCliente(String correoCliente) {
        this.correoCliente = correoCliente;
    }

    @Override
    public String toString() {

        String phoneDisplay = (telefonoCliente != null && !telefonoCliente.isEmpty()) ? telefonoCliente : "N/A";
        return (context != null ? context.getString(R.string.client_name_label) : "Nombre") + ": " + getNombreCliente() + "\n"
                + (context != null ? context.getString(R.string.client_phone_label) : "Teléfono") + ": " + phoneDisplay;
    }
}

