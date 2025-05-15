package sv.edu.ues.fia.controldemedicamentosyarticulosdelhogar;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import java.util.*;

public class DoctorActivity extends AppCompatActivity {
    private final ValidarAccesoCRUD vac = new ValidarAccesoCRUD(this);
    private DoctorDAO doctorDAO;
    private ArrayAdapter<Doctor> adaptadorDoctores;
    private List<Doctor> listaDoctores = new ArrayList<>();
    private ListView listViewDoctores;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor);

        // ✅ Usar DAO con conexión web (ya no SQLite)
        doctorDAO = new DoctorDAO(this);

        TextView txtBusqueda = findViewById(R.id.searchDcctor);

        Button btnAgregarDoctor = findViewById(R.id.btnAgregarDoctor);
        btnAgregarDoctor.setVisibility(vac.validarAcceso(1) ? View.VISIBLE : View.INVISIBLE);
        btnAgregarDoctor.setOnClickListener(v -> showAddDialog());

        Button btnBuscarDoctor = findViewById(R.id.btnBuscarDoctor);
        btnBuscarDoctor.setVisibility(vac.validarAcceso(2) || vac.validarAcceso(3) || vac.validarAcceso(4) ? View.VISIBLE : View.INVISIBLE);
        btnBuscarDoctor.setOnClickListener(v -> {
            try {
                int id = Integer.parseInt(txtBusqueda.getText().toString().trim());
                buscarDoctorPorId(id); // ✅ Ahora usa callback
            } catch (NumberFormatException e) {
                e.printStackTrace();
                Toast.makeText(this, R.string.invalid_search, Toast.LENGTH_LONG).show();
            }
        });

        listViewDoctores = findViewById(R.id.lvDoctor);
        listViewDoctores.setVisibility(vac.validarAcceso(3) || vac.validarAcceso(4) ? View.VISIBLE : View.INVISIBLE);

        fillList(); // ✅ Llenado asíncrono

        listViewDoctores.setOnItemClickListener((parent, view, position, id) -> {
            Doctor doctor = (Doctor) parent.getItemAtPosition(position);
            showOptionsDialog(doctor);
        });
    }

    // ✅ getAllDoctors ahora es asíncrono
    private void fillList() {
        doctorDAO.getAllDoctors(doctores -> {
            listaDoctores = doctores;
            adaptadorDoctores = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listaDoctores);
            listViewDoctores.setAdapter(adaptadorDoctores);
        });
    }

    private void showAddDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.add);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_doctor, null);
        builder.setView(dialogView);

        EditText editTextIdDoctor = dialogView.findViewById(R.id.edtIdDoctor);
        EditText editTextNombreDoctor = dialogView.findViewById(R.id.edtNombreDoctor);
        EditText editTextEspecialidadDoctor = dialogView.findViewById(R.id.edtEspecialidadDoctor);
        EditText editTextJvpm = dialogView.findViewById(R.id.edtJVPM);
        Button btnGuardarDoctor = dialogView.findViewById(R.id.btnGuardarDoctor);
        Button btnLimpiarDoctor = dialogView.findViewById(R.id.btnLimpiarDoctor);

        List<View> vistas = Arrays.asList(editTextIdDoctor, editTextNombreDoctor, editTextEspecialidadDoctor, editTextJvpm);
        List<String> listaRegex = Arrays.asList("\\d+", "[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+", "[a-zA-ZáéíóúÁÉÍÓÚñÑ ]++", "[a-zA-ZáéíóúÁÉÍÓÚñÑ ]++");
        List<Integer> mensajesDeError = Arrays.asList(R.string.only_numbers, R.string.only_letters, R.string.only_letters, R.string.only_letters);

        ValidadorDeCampos validadorDeCampos = new ValidadorDeCampos(this, vistas, listaRegex, mensajesDeError);

        final AlertDialog dialog = builder.create();
        btnGuardarDoctor.setOnClickListener(v -> {
            if (validadorDeCampos.validarCampos()) {
                guardarDoctor(editTextIdDoctor, editTextNombreDoctor, editTextEspecialidadDoctor, editTextJvpm);
                dialog.dismiss();
            }
        });
        btnLimpiarDoctor.setOnClickListener(v -> limpiarCampos(editTextIdDoctor, editTextNombreDoctor, editTextEspecialidadDoctor, editTextJvpm));
        dialog.show();
    }

    private void guardarDoctor(EditText idView, EditText nombreView, EditText espView, EditText jvpmView) {
        int id = Integer.parseInt(idView.getText().toString());
        String nombre = nombreView.getText().toString().trim();
        String esp = espView.getText().toString().trim();
        String jvpm = jvpmView.getText().toString().trim();

        Doctor doctor = new Doctor(id, nombre, esp, jvpm, this);
        doctorDAO.addDoctor(doctor);

        new Handler(Looper.getMainLooper()).postDelayed(this::fillList, 1000); // ✅ Esperar actualización web
        limpiarCampos(idView, nombreView, espView, jvpmView);
    }

    private void buscarDoctorPorId(int id) {
        doctorDAO.getDoctor(id, doctor -> {
            if (doctor != null) viewDoctor(doctor);
            else Toast.makeText(this, R.string.not_found_message, Toast.LENGTH_SHORT).show();
        });
    }

    private void eliminarDoctor(int id) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.confirm_delete);
        builder.setMessage(getString(R.string.confirm_delete_message) + ": " + id);
        builder.setPositiveButton(R.string.yes, (dialog, which) -> {
            doctorDAO.deleteDoctor(id);
            new Handler(Looper.getMainLooper()).postDelayed(this::fillList, 1000);
        });
        builder.setNegativeButton(R.string.no, (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    private void viewDoctor(Doctor doctor) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.view);
        View view = getLayoutInflater().inflate(R.layout.dialog_doctor, null);
        builder.setView(view);

        EditText idView = view.findViewById(R.id.edtIdDoctor);
        EditText nombreView = view.findViewById(R.id.edtNombreDoctor);
        EditText espView = view.findViewById(R.id.edtEspecialidadDoctor);
        EditText jvpmView = view.findViewById(R.id.edtJVPM);

        idView.setText(String.valueOf(doctor.getIdDoctor()));
        nombreView.setText(doctor.getNombreDoctor());
        espView.setText(doctor.getEspecialidadDoctor());
        jvpmView.setText(doctor.getJvpm());

        idView.setEnabled(false);
        nombreView.setEnabled(false);
        espView.setEnabled(false);
        jvpmView.setEnabled(false);

        Button guardar = view.findViewById(R.id.btnGuardarDoctor);
        Button limpiar = view.findViewById(R.id.btnLimpiarDoctor);
        if (guardar != null) guardar.setVisibility(View.GONE);
        if (limpiar != null) limpiar.setVisibility(View.GONE);

        builder.create().show();
    }

    private void editarDoctor(Doctor doctor) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.edit);
        View view = getLayoutInflater().inflate(R.layout.dialog_doctor, null);
        builder.setView(view);

        EditText idView = view.findViewById(R.id.edtIdDoctor);
        EditText nombreView = view.findViewById(R.id.edtNombreDoctor);
        EditText espView = view.findViewById(R.id.edtEspecialidadDoctor);
        EditText jvpmView = view.findViewById(R.id.edtJVPM);

        idView.setText(String.valueOf(doctor.getIdDoctor()));
        nombreView.setText(doctor.getNombreDoctor());
        espView.setText(doctor.getEspecialidadDoctor());
        jvpmView.setText(doctor.getJvpm());

        idView.setEnabled(false);

        Button guardar = view.findViewById(R.id.btnGuardarDoctor);
        Button limpiar = view.findViewById(R.id.btnLimpiarDoctor);
        limpiar.setEnabled(false);

        List<View> vistas = Arrays.asList(nombreView, espView, jvpmView);
        List<String> listaRegex = Arrays.asList("[a-zA-Z]+", "[a-zA-Z]+", "[a-zA-Z]+");
        List<Integer> mensajesDeError = Arrays.asList(R.string.only_letters, R.string.only_letters, R.string.only_letters);

        ValidadorDeCampos validador = new ValidadorDeCampos(this, vistas, listaRegex, mensajesDeError);

        final AlertDialog dialog = builder.create();
        guardar.setOnClickListener(v -> {
            if (validador.validarCampos()) {
                doctor.setNombreDoctor(nombreView.getText().toString().trim());
                doctor.setEspecialidadDoctor(espView.getText().toString().trim());
                doctor.setJvpm(jvpmView.getText().toString().trim());
                doctorDAO.updateDoctor(doctor);
                new Handler(Looper.getMainLooper()).postDelayed(this::fillList, 1000);
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void showOptionsDialog(final Doctor doctor) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.options);
        View view = getLayoutInflater().inflate(R.layout.dialog_options, null);
        builder.setView(view);

        final AlertDialog dialog = builder.create();

        view.findViewById(R.id.buttonView).setOnClickListener(v -> {
            if (vac.validarAcceso(2)) viewDoctor(doctor);
            else Toast.makeText(getApplicationContext(), R.string.action_block, Toast.LENGTH_LONG).show();
            dialog.dismiss();
        });

        view.findViewById(R.id.buttonEdit).setOnClickListener(v -> {
            if (vac.validarAcceso(3)) editarDoctor(doctor);
            else Toast.makeText(getApplicationContext(), R.string.action_block, Toast.LENGTH_LONG).show();
            dialog.dismiss();
        });

        view.findViewById(R.id.buttonDelete).setOnClickListener(v -> {
            if (vac.validarAcceso(4)) eliminarDoctor(doctor.getIdDoctor());
            else Toast.makeText(getApplicationContext(), R.string.action_block, Toast.LENGTH_LONG).show();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void limpiarCampos(EditText... fields) {
        for (EditText field : fields) field.setText("");
    }
}