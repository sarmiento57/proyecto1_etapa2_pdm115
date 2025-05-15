package sv.edu.ues.fia.controldemedicamentosyarticulosdelhogar;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.widget.Toast;

import java.sql.SQLException;

public class ControlBD {

    private final Context context;
    private final DataBaseHelper DBHelper;

    public ControlBD(Context context) {
        this.context = context;
        DBHelper = new DataBaseHelper(context);
    }

    public SQLiteDatabase getConnection() throws SQLiteException {
        return DBHelper.getWritableDatabase();
    }

    public void closeConnection() throws SQLiteException {
        DBHelper.close();
    }

    public Context getContext() {
        return context;
    }


    public void llenarDB() {
        SQLiteDatabase db = DBHelper.getWritableDatabase();
        DBHelper.llenarDB(db);
        Toast.makeText(context, R.string.fill_database, Toast.LENGTH_LONG).show();
    }
}
