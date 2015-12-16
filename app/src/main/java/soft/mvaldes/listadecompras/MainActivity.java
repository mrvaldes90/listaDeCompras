package soft.mvaldes.listadecompras;

import android.app.ActionBar;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Toast;


import com.mobeta.android.dslv.DragSortController;
import com.mobeta.android.dslv.DragSortListView;
import com.mobeta.android.dslv.SimpleDragSortCursorAdapter;

import soft.mvaldes.datos.ToDo;

public class MainActivity extends AppCompatActivity {
    DragSortListView listView;
    SimpleDragSortCursorAdapter adapter;
    ToDo listaNueva; //lista a crear...
    ToDo listaABorrar; //lista a borrar...
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //toolbar.

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                inputToDo(null, view);
            }
        });

        listView = (DragSortListView) findViewById(R.id.listview);
        Cursor todoCursor = ToDo.fetchResultCursor();
        adapter = new SimpleDragSortCursorAdapter(this,
                R.layout.mainlist_item_row,
                todoCursor,
                new String[]{"Name"},
                new int[]{R.id.mainListName},
                R.id.mainListName);

        listView.setAdapter(adapter);
        //listView.setDropListener(onDrop);
        //listView.setRemoveListener(onRemove);
        listView.setRemoveListener(new DragSortListView.RemoveListener() {
            @Override
            public void remove(int which) {
                borrarLista(which);
            }
        });
        DragSortController controller = new DragSortController(listView);
        controller.setDragHandleId(R.id.mainListHandler);
        //controller.setClickRemoveId(R.id.);
        controller.setRemoveEnabled(true);
        controller.setSortEnabled(true);
        listView.setLongClickable(true);
        controller.setDragInitMode(1);
        //controller.setRemoveMode(removeMode);

        listView.setFloatViewManager(controller);
        listView.setOnTouchListener(controller);
        listView.setDragEnabled(true);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(view.getContext(), DetailsActivity.class);
                Log.d("Info", "idEnviadoToDo" +  String.valueOf(id));
                i.putExtra("idTodo", id);
                startActivity(i);
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                final View vista = view;
                Context context = view.getContext();
                final int which = position;
                final long ToDoId = id;
                final CharSequence[] items = {getResources().getString(R.string.action_edit_item), getResources().getString(R.string.action_delete_item)};

                new AlertDialog.Builder(context).setTitle("Listas...")
                        .setItems(items, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int item) {
                                if (item == 0)
                                    inputToDo(ToDo.load(ToDo.class,ToDoId),vista);
                                else if (item == 1)
                                    borrarLista(which);
                                dialog.dismiss();
                            }
                        }).show();
                return true;
            }
        });
    }

    private void borrarLista(long position) {
        Cursor c = (Cursor)listView.getItemAtPosition((int)(position));
        int _id = c.getInt(0);
        if (listaABorrar != null)
            listaABorrar.save();
        listaABorrar = ToDo.load(ToDo.class,_id);
        listaABorrar.status=false;
        Log.d("Info", "Se borro el item..." + String.valueOf(position) + " :id " + String.valueOf(_id));
        Snackbar.make(listView, "Se borró la lista y todas sus actividades... Puedo deshacerlo!", Snackbar.LENGTH_LONG)
                .setCallback(new Snackbar.Callback() {
                    @Override
                    public void onDismissed(Snackbar snackbar, int event) {
                        super.onDismissed(snackbar, event);
                        if (event != DISMISS_EVENT_ACTION) {
                            if (listaABorrar != null) {
                                listaABorrar.save();
                                listaABorrar = null;
                            }
                        }

                        actualizarMainList();
                    }
                })
                .setAction("Deshacer", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        listaABorrar = null;
                        Toast.makeText(MainActivity.this, "Deshecho...", Toast.LENGTH_SHORT).show();
                    }
                }).show();
    }

    private void inputToDo(ToDo lst, View v){
        boolean modificar = false;
        if (listaNueva != null && listaNueva.name != null) {
            if (!listaNueva.name.trim().equals("")) {
                Log.d("Info", "Existía una lista con Name: " + listaNueva.name + " position: " + String.valueOf(listaNueva.position));
                listaNueva.save();
            }
        }
        if (lst == null)
            lst = new ToDo();

        listaNueva = lst;
        String titulo = getString(R.string.input_todo_title);
        if (listaNueva.getId() != null && listaNueva.getId()>0) {
            modificar = true;
            titulo = String.format(titulo,getString(R.string.Modify));
        }
        else
            titulo = String.format(titulo, getString(R.string.Add));
        final Context context = v.getContext();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View formElementsView = inflater.inflate(R.layout.todo_input_form, null, false);
        final EditText todoInputName = (EditText) formElementsView.findViewById(R.id.todo_input_name);
        if (modificar)
            todoInputName.setText(listaNueva.name);
        new AlertDialog.Builder(context)
                .setView(formElementsView)
                .setTitle(titulo)
                .setPositiveButton(modificar ? R.string.Modify : R.string.Add,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                listaNueva.name = todoInputName.getText().toString();
                                if (listaNueva.name.trim().equals("")) {
                                    Toast.makeText(MainActivity.this, "El nombre de la lista no debe estar en blanco...", Toast.LENGTH_SHORT).show();
                                } else {
                                    Log.d("Info", "Se va a guardar el item con Name: " + listaNueva.name + " position: " + String.valueOf(listaNueva.position));
                                    listaNueva.save();
                                    listaNueva = null;
                                }
                                actualizarMainList();
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

    private void actualizarMainList(){
        Cursor oldCursor = adapter.getCursor();
        if (oldCursor!=null || !oldCursor.isClosed())
        {
            Cursor newCursor = ToDo.fetchResultCursor();
            adapter.swapCursor(newCursor);
            oldCursor.close();
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id){
            case R.id.action_add_item:
                inputToDo(null, this.findViewById(R.id.listview));
                break;
            case R.id.action_exit:
                finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}
