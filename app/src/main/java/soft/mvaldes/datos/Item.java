package soft.mvaldes.datos;
import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by mvaldez on 02/12/2015.
 */
@Table(name = "Items")
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

    // This is an association to another activeandroid model
    @Column(name = "ToDo", onUpdate = Column.ForeignKeyAction.CASCADE, onDelete = Column.ForeignKeyAction.CASCADE)
    public ToDo todo;

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
    }

    public Item(String name, boolean done, Date addedOn, ToDo todo){
        super();
        this.name = name;
        this.done = done;
        this.addedOn = addedOn;
        this.todo = todo;
    }
}
