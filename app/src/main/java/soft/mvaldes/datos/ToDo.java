package soft.mvaldes.datos;

import android.database.Cursor;

import com.activeandroid.Cache;
import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

import java.util.List;

/**
 * Created by mvaldez on 02/12/2015.
 */

@Table(name = "ToDo")
public class ToDo extends Model {
    // This is the unique id given by the server
    // This is a regular field
    @Column(name = "Name")
    public String name;
    // This is a regular field
    @Column(name = "Status")
    public boolean status;

    // Make sure to have a default constructor for every ActiveAndroid model
    public ToDo(){
        super();
        status = true;
    }

    // Used to return items from another table based on the foreign key
    public List<Item> items() {
        return getMany(Item.class, "ToDo");
    }

    // Return cursor for result set for all todo items
    public static Cursor fetchResultCursor() {
        String tableName = Cache.getTableInfo(ToDo.class).getTableName();
        // Query all items without any conditions
        String resultRecords = new Select(tableName + ".*, " + tableName + ".Id as _id").
                from(ToDo.class).toSql();
        // Execute query on the underlying ActiveAndroid SQLite database
        Cursor resultCursor = Cache.openDatabase().rawQuery(resultRecords, null);
        return resultCursor;
    }

}