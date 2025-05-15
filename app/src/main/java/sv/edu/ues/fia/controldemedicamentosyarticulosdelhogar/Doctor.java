package sv.edu.ues.fia.controldemedicamentosyarticulosdelhogar;

import android.content.Context;

public class Doctor {
    private int idDoctor;
    private String nombreDoctor;
    private String especialidadDoctor;
    private String jvpm;
    private Context context;

    public Doctor(int idDoctor, String nombreDoctor, String especialidadDoctor, String jvpm, Context context) {
        this.idDoctor = idDoctor;
        this.nombreDoctor = nombreDoctor;
        this.especialidadDoctor = especialidadDoctor;
        this.jvpm = jvpm;
        this.context = context;
    }

    public Doctor(int idDoctor, String nombreDoctor, Context context) {
        this.idDoctor = idDoctor;
        this.nombreDoctor = nombreDoctor;
        this.context = context;
    }

    public int getIdDoctor() {
        return idDoctor;
    }

    public void setIdDoctor(int idDoctor) {
        this.idDoctor = idDoctor;
    }

    public String getNombreDoctor() {
        return nombreDoctor;
    }

    public void setNombreDoctor(String nombreDoctor) {
        this.nombreDoctor = nombreDoctor;
    }

    public String getEspecialidadDoctor() {
        return especialidadDoctor;
    }

    public void setEspecialidadDoctor(String especialidadDoctor) {
        this.especialidadDoctor = especialidadDoctor;
    }

    public String getJvpm() {
        return jvpm;
    }

    public void setJvpm(String jvpm) {
        this.jvpm = jvpm;
    }

    @Override
    public String toString() {
        return context.getString(R.string.id_doctor) + ": " + getIdDoctor() + "\n"
                + context.getString(R.string.nombre_doctor) + ": " + getNombreDoctor();
    }
}
