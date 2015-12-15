package soft.mvaldes.datos;
import android.database.Cursor;

import com.activeandroid.Cache;
import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by mvaldez on 02/12/2015.
 */
@Table(name = "Item")
public class Item extends Model {
    // This is a regular field
    @Column(name = "Name")
    public String name;

    @Column(name = "Done")
    public boolean done;

    @Column(name = "addedOn")
    public Date addedOn;

    // This is a regular field
    @Column(name = "Status")
    public boolean status;

    @Column(name = "Position")
    public long position;

    // This is an association to another activeandroid model
    @Column(name = "ToDo", onUpdate = Column.ForeignKeyAction.CASCADE, onDelete = Column.ForeignKeyAction.CASCADE)
    public ToDo todo;

    public boolean listoGuardar;

    // Make sure to have a default constructor for every ActiveAndroid model
    public Item(){
        super();
        String date = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());;
        SimpleDateFormat sf = new SimpleDateFormat("EEE MMM dd HH:mm:ss ZZZZZ yyyy");
        sf.setLenient(true);
        try{
            this.addedOn = sf.parse(date);
        }
        catch (ParseException e){
            this.addedOn = new Date();
        }
        this.status = true;
        this.listoGuardar = true;
        //this.position = Item.count();
    }

    public Item(String name, boolean done, Date addedOn, ToDo todo){
        super();
        this.name = name;
        this.done = done;
        this.addedOn = addedOn;
        this.todo = todo;
        this.position = this.count();
    }
    // Return cursor for result set for all items
    public static Cursor fetchResultCursor() {
        String tableName = Cache.getTableInfo(Item.class).getTableName();
        // Query all items without any conditions
        String resultRecords = new Select(tableName + ".*, " + tableName + ".Id as _id").
                from(Item.class)
                .where("Status=1")
                .orderBy("Position ASC").toSql();
        // Execute query on the underlying ActiveAndroid SQLite database
        Cursor resultCursor = Cache.openDatabase().rawQuery(resultRecords, null);
        return resultCursor;
    }

    // Return cursor for result set for all items of todo
    public static Cursor fetchResultCursor(long todoId) {
        String tableName = Cache.getTableInfo(Item.class).getTableName();
        // Query all items without any conditions
        String resultRecords = new Select(tableName + ".*, " + tableName + ".Id as _id").
                from(Item.class)
                .where("Status=1 and ToDo=" + String.valueOf(todoId))
                .orderBy("Position ASC").toSql();
        // Execute query on the underlying ActiveAndroid SQLite database
        Cursor resultCursor = Cache.openDatabase().rawQuery(resultRecords, null);
        return resultCursor;
    }

    public static List<Item> getAll() {
        return new Select()
                .from(Item.class)
                .where("status=1")
                .orderBy("Name ASC")
                .execute();
    }

    public static long count(){
        return new Select()
                .from(Item.class)
                .where("status=1")
                .execute().size();
    }
}
