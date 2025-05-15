package sv.edu.ues.fia.controldemedicamentosyarticulosdelhogar;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

public class ValidarAccesoCRUD {

    private final ControlBD controlBD;
    private final Context context;
    public ValidarAccesoCRUD(Context context) {
        this.controlBD = new ControlBD(context);
        this.context = context;
    }

    public boolean validarAcceso(int opcionCRUD) {
        SharedPreferences permisosApp = context.getSharedPreferences("PERMISOS_APP", Context.MODE_PRIVATE);
        String idOpcion = permisosApp.getString("id_opcion", "-1");
        String idUsuario = permisosApp.getString("id_usuario", "-1");

        Log.d("ValidarAccesoCRUD", "ID DE USUARIO: " + idUsuario);
        Log.d("ValidarAccesoCRUD", "PERMISOS SOLICITADOS: " + idOpcion);
        Log.d("ValidarAccesoCRUD", "OPCION DE CRUD: " + opcionCRUD);

        if ("-1".equals(idOpcion) || "-1".equals(idUsuario)) {
            return false;
        }

        try {
            SQLiteDatabase conexionDB = controlBD.getConnection();
            Cursor cursor = conexionDB.rawQuery("SELECT * FROM OPCIONCRUD", null);
            int position = encontrarPosicion(idOpcion);

            if (cursor.moveToPosition(position)) {
                switch (opcionCRUD) {
                    case 1:
                        cursor.moveToPosition(position + 1);
                        return permisosApp.getBoolean(cursor.getString(
                                cursor.getColumnIndexOrThrow("IDOPCION")),false);
                    case 2:
                        cursor.moveToPosition(position + 2);
                        return permisosApp.getBoolean(cursor.getString(
                                cursor.getColumnIndexOrThrow("IDOPCION")),false);
                    case 3:
                        cursor.moveToPosition(position + 3);
                        return permisosApp.getBoolean(cursor.getString(
                                cursor.getColumnIndexOrThrow("IDOPCION")),false);
                    case 4:
                        cursor.moveToPosition(position + 4);
                        return permisosApp.getBoolean(cursor.getString(
                                cursor.getColumnIndexOrThrow("IDOPCION")),false);
                    default:
                        return false;
                }
            }
            cursor.close();
            conexionDB.close();
        } catch (SQLiteException e) {
            e.printStackTrace();
        }
        return false;
    }

    public int encontrarPosicion(String idOpcion) {
        SQLiteDatabase conexionDB = controlBD.getConnection();
        Cursor cursor = conexionDB.rawQuery("SELECT * FROM OPCIONCRUD", null);
        int position = -1;
        if (cursor.moveToFirst()) {
            do {
                String currentIdOpcion = cursor.getString(cursor.getColumnIndexOrThrow("IDOPCION"));
                if (currentIdOpcion.equals(idOpcion)) {
                    position = cursor.getPosition();
                    break;
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        return position;
    }
}
