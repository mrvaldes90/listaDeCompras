package soft.mvaldes.listadecompras;

import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.mobeta.android.dslv.DragSortController;
import com.mobeta.android.dslv.DragSortListView;
import com.mobeta.android.dslv.SimpleDragSortCursorAdapter;

import soft.mvaldes.datos.Item;
import soft.mvaldes.datos.ToDo;

public class DetailsActivity extends AppCompatActivity {
    private Item item;
    private Item itemBorrar;
    DragSortListView listView;
    CheckBox chk;
    SimpleDragSortCursorAdapter adapter;
    ToDo padre;
    long idPadre;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        Bundle bundle = getIntent().getExtras();
        idPadre = bundle.getLong("idTodo");
        padre = ToDo.load(ToDo.class, idPadre);
        Log.d("Info", "idPadre: " + String.valueOf(idPadre));
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fabDetails);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                inputItem(null, view);
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                Log.d("info", "back");
            }
        });
        listView = (DragSortListView) findViewById(R.id.listviewDetails);
        Cursor itemCursor = Item.fetchResultCursor(idPadre);
        adapter = new SimpleDragSortCursorAdapter(this,
                R.layout.detaillist_item_row,
                itemCursor,
                new String[]{"Name","addedOn","Done"},
                new int[]{R.id.detailListName,R.id.detailListAddedOn, R.id.detailListDone},
                R.id.detailListName);

        listView.setAdapter(adapter);
        listView.setRemoveListener(new DragSortListView.RemoveListener() {
            @Override
            public void remove(int which) {
                borrarItem(which);
            }
        });
        DragSortController controller = new DragSortController(listView);
        controller.setDragHandleId(R.id.detailListHandler);
        //controller.setClickRemoveId(R.id.);
        controller.setRemoveEnabled(true);
        controller.setSortEnabled(true);
        listView.setLongClickable(true);
        controller.setDragInitMode(1);
        //controller.setRemoveMode(removeMode);

        listView.setFloatViewManager(controller);
        listView.setOnTouchListener(controller);
        listView.setDragEnabled(true);
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                final View vista = view;
                Context context = view.getContext();
                final int which = position;
                final long ItemId = id;
                final CharSequence[] items = {getResources().getString(R.string.action_edit_item), getResources().getString(R.string.action_delete_item)};

                new AlertDialog.Builder(context).setTitle("Tareas...")
                        .setItems(items, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int item) {
                                if (item == 0)
                                    inputItem(Item.load(Item.class, ItemId), vista);
                                else if (item == 1)
                                    borrarItem(which);
                                dialog.dismiss();
                            }
                        }).show();
                return true;
            }
        });
        //Agregar header

        LayoutInflater inflater = getLayoutInflater();
        ViewGroup header = (ViewGroup)inflater.inflate(R.layout.detaillist_header_row, listView, false);
        TextView txtHeader = (TextView) header.findViewById(R.id.detailListHeader);
        txtHeader.setText(padre.name);
        listView.addHeaderView(header, null, false);
    }

    private void inputItem(Item itm, View v) {
        boolean modificar = false;
        if (item != null) {
            if (!item.name.trim().equals("")) {
                Log.d("Info", "Existía una lista con Name: " + item.name + " position: " + String.valueOf(item.position));
                item.save();
            }
        }
        if (itm == null)
            itm = new Item();

        item = itm;
        String titulo = getString(R.string.input_item_title);
        if (item.getId() != null && item.getId()>0) {
            modificar = true;
            titulo = String.format(titulo,getString(R.string.Modify));
        }
        else
            titulo = String.format(titulo, getString(R.string.Add));
        final Context context = v.getContext();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View formElementsView = inflater.inflate(R.layout.todo_input_form, null, false);
        final EditText itemInputName = (EditText) formElementsView.findViewById(R.id.todo_input_name);
        if (modificar)
            itemInputName.setText(item.name);
        final View vista = v;
        new AlertDialog.Builder(context)
                .setView(formElementsView)
                .setTitle(titulo)
                .setPositiveButton(modificar ? R.string.Modify : R.string.Add,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                item.name = itemInputName.getText().toString();
                                item.todo = padre;
                                if (item.name.trim().equals("")) {
                                    Toast.makeText(DetailsActivity.this, "El nombre de la tarea no debe estar en blanco...", Toast.LENGTH_SHORT).show();
                                } else {
                                    Log.d("Info", "Se va a guardar el item con Name: " + item.name + " position: " + String.valueOf(item.position));
                                    item.save();
                                    Log.d("Info", "Se guardó el item... Name: " + item.name + " position: " + String.valueOf(item.position));
                                    item = null;
                                }
                                actualizarDetailList();
                            }
                        })
                .setNegativeButton(R.string.Cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    private void actualizarDetailList(){
        Cursor oldCursor = adapter.getCursor();
        if (oldCursor!=null || !oldCursor.isClosed())
        {
            Cursor newCursor = Item.fetchResultCursor(idPadre);
            adapter.swapCursor(newCursor);
            oldCursor.close();
        }
    }

    private void borrarItem(int which) {
        Cursor c = (Cursor)listView.getItemAtPosition((int)(which));
        int _id = c.getInt(0);
        if (itemBorrar != null)
            itemBorrar.save();
        itemBorrar = Item.load(Item.class,_id);
        itemBorrar.status=false;
        Log.d("Info", "Se borro el item..." + String.valueOf(which) + " :id " + String.valueOf(_id));
        Snackbar.make(listView,"Se borró la tarea... Puedo deshacerlo!",Snackbar.LENGTH_LONG)
                .setCallback(new Snackbar.Callback() {
                    @Override
                    public void onDismissed(Snackbar snackbar, int event) {
                        super.onDismissed(snackbar, event);
                        if (event != DISMISS_EVENT_ACTION) {
                            if (itemBorrar != null) {
                                itemBorrar.save();
                                itemBorrar = null;
                            }
                        }
                        actualizarDetailList();
                    }
                })
                .setAction("Deshacer", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        itemBorrar = null;
                        Toast.makeText(DetailsActivity.this, "Deshecho...", Toast.LENGTH_SHORT).show();
                    }
                }).show();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        Log.d("Info","id selected " + id + " " + getResources().getResourceEntryName(id));
        finish();

        return super.onOptionsItemSelected(item);
    }
}
