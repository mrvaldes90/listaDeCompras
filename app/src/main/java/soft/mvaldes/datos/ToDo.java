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
    // This is a regular field
    @Column(name = "Position")
    public long position;

    // Make sure to have a default constructor for every ActiveAndroid model
    public ToDo(){
        super();
        this.status = true;
        //this.position = ToDo.count();
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
                from(ToDo.class)
                .where("Status=1")
                .orderBy("Name ASC").toSql();
        // Execute query on the underlying ActiveAndroid SQLite database
        Cursor resultCursor = Cache.openDatabase().rawQuery(resultRecords, null);
        return resultCursor;
    }

    public static List<ToDo> getAll() {
        return new Select()
                .from(ToDo.class)
                .where("status=1")
                .orderBy("Name ASC")
                .execute();
    }

    public static long count(){
        return new Select()
                .from(ToDo.class)
                .where("status=1")
                .execute().size();
    }

}