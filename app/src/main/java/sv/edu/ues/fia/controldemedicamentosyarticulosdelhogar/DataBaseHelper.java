package sv.edu.ues.fia.controldemedicamentosyarticulosdelhogar;

import android.content.Context;
import android.content.res.AssetManager;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;

public class DataBaseHelper extends SQLiteOpenHelper {
    private static final String NOMBRE_BASE_DATOS = "control_medicamentos.s3db";
    private static final String [] SCRIPTS = {
            "creation_db_script.sql",
            "user_table_filling_script.sql",
            "districts_filling_script.sql",
            "triggers.sql",
    };
    private static final int VERSION = 1;
    private final Context context;

    public DataBaseHelper(Context context) {
        super(context, NOMBRE_BASE_DATOS, null, VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            for (String script : SCRIPTS) {
                Log.d("DB", "Ejecutando script: " + script);
                String sqlScript = getSqlScript(script);

                BufferedReader reader = new BufferedReader(new StringReader(sqlScript));
                StringBuilder statementBuilder = new StringBuilder();
                String line;
                boolean insideTrigger = false;

                while ((line = reader.readLine()) != null) {
                    line = line.trim();

                    // Ignorar comentarios y líneas vacías
                    if (line.isEmpty() || line.startsWith("--")) continue;

                    // Detectar comienzo de trigger
                    if (line.toUpperCase().startsWith("CREATE TRIGGER")) {
                        insideTrigger = true;
                    }

                    statementBuilder.append(line).append(" ");

                    // Si es un trigger, solo ejecutar cuando llega a END;
                    if (insideTrigger) {
                        if (line.toUpperCase().endsWith("END;")) {
                            insideTrigger = false;
                            String fullStatement = statementBuilder.toString().trim();
                            statementBuilder.setLength(0);
                            ejecutarSQL(db, fullStatement, script);
                        }
                        continue;
                    }

                    // Para sentencias normales
                    if (line.endsWith(";")) {
                        String fullStatement = statementBuilder.toString().trim();
                        statementBuilder.setLength(0);
                        ejecutarSQL(db, fullStatement, script);
                    }
                }
            }
        } catch (SQLiteException | IOException e) {
            e.printStackTrace();
            Log.e("DB ERROR", "Error en onCreate", e);
        }
    }

    private void ejecutarSQL(SQLiteDatabase db, String statement, String script) {
        try {
            db.execSQL(statement);
            Log.d("DB", "Ejecutado: " + statement);
        } catch (SQLException e) {
            Log.e("DB ERROR", "Fallo al ejecutar: " + statement + " - SCRIPT: " + script, e);
        }
    }

    private String getSqlScript(String SCRIPT_NAME) throws IOException {
        AssetManager assetManager = context.getAssets();
        InputStream inputStream = assetManager.open(SCRIPT_NAME);

        int size = inputStream.available();
        byte[] buffer = new byte[size];
        inputStream.read(buffer);
        inputStream.close();

        return new String(buffer, StandardCharsets.UTF_8);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void llenarDB(SQLiteDatabase db) {
        String script = "test_data.sql";

        try {
            Log.d("DB", "Ejecutando script: " + script);
            String sqlScript = getSqlScript(script);

            BufferedReader reader = new BufferedReader(new StringReader(sqlScript));
            StringBuilder statementBuilder = new StringBuilder();
            String line;
            boolean insideTrigger = false;

            while ((line = reader.readLine()) != null) {
                line = line.trim();

                // Ignorar comentarios y líneas vacías
                if (line.isEmpty() || line.startsWith("--")) continue;

                // Detectar comienzo de trigger
                if (line.toUpperCase().startsWith("CREATE TRIGGER")) {
                    insideTrigger = true;
                }

                statementBuilder.append(line).append(" ");

                // Si es un trigger, solo ejecutar cuando llega a END;
                if (insideTrigger) {
                    if (line.toUpperCase().endsWith("END;")) {
                        insideTrigger = false;
                        String fullStatement = statementBuilder.toString().trim();
                        statementBuilder.setLength(0);
                        ejecutarSQL(db, fullStatement, script);
                    }
                    continue;
                }

                // Para sentencias normales
                if (line.endsWith(";")) {
                    String fullStatement = statementBuilder.toString().trim();
                    statementBuilder.setLength(0);
                    ejecutarSQL(db, fullStatement, script);
                }
            }
        } catch (SQLiteException | IOException e) {
            e.printStackTrace();
            Log.e("DB ERROR", "Error en onCreate", e);
        }
    }
}
