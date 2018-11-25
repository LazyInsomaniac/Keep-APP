package com.Escanor.sambhav.mykeep;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.Escanor.sambhav.mykeep.NoteCreation.SimpleNoteCreation;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    List<ItemObjects> staggeredList;
    RecyclerViewAdapter rcAdapter;

    List<ItemObjects> listViewItems;
    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Delete all notes
        /*
        String[] allNotes = fileList();
        for (String allNote : allNotes) {
            deleteFile(allNote);
        }
        */

        // Check and get storage permissions
        if (Build.VERSION.SDK_INT >= 23) {
            if (!checkPermissionForReadExternalStorage())
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            if (!checkPermissionForWriteExternalStorage())
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);

        // Check orientation to put the good amount of columns
        int column = 2;
        if (getResources().getConfiguration().orientation == 2)
            column = 3;

        StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(column, 1);
        recyclerView.setLayoutManager(staggeredGridLayoutManager);
        // Prevent the loss of items
        recyclerView.getRecycledViewPool().setMaxRecycledViews(0, 0);


        final float scale = getResources().getDisplayMetrics().density;
        int spacing = (int) (1 * scale + 0.5f);
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(spacing));

        // Load data
        //staggeredList = getListItemData();

        // Load notes from internal storage
        staggeredList = loadNotes();

        rcAdapter = new RecyclerViewAdapter(staggeredList);
        recyclerView.setAdapter(rcAdapter);

        // Drag and drop
        ItemTouchHelper ith = new ItemTouchHelper(_ithCallback);
        ith.attachToRecyclerView(recyclerView);

        // Create a simple note button click listener
        Button createSimpleNoteButton = findViewById(R.id.create_new_note);
        createSimpleNoteButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent simpleNoteIntent = new Intent(getApplicationContext(), SimpleNoteCreation.class);
                simpleNoteIntent.putExtra("title", "");
                simpleNoteIntent.putExtra("content", "");
                simpleNoteIntent.putExtra("color", getResources().getString(R.color.colorNoteDefault));
                simpleNoteIntent.putExtra("creationDate", "");
                simpleNoteIntent.putExtra("position", -1);
                // TODO
                startActivityForResult(simpleNoteIntent, 1);
            }
        });
    }

    // Drag and drop
    ItemTouchHelper.Callback _ithCallback = new ItemTouchHelper.Callback() {
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {

            Collections.swap(staggeredList, viewHolder.getAdapterPosition(), target.getAdapterPosition());
            rcAdapter.notifyItemMoved(viewHolder.getAdapterPosition(), target.getAdapterPosition());
            return true;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            //TODO
        }

        // Defines the enabled move directions in each state (idle, swiping, dragging).
        @Override
        public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {

            return makeFlag(ItemTouchHelper.ACTION_STATE_DRAG,
                    ItemTouchHelper.DOWN | ItemTouchHelper.UP | ItemTouchHelper.START | ItemTouchHelper.END);
        }
    };

    // Get the data from the note creation
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {

            String noteJSON = data.getStringExtra("noteJSON");

            try {
                JSONObject json = new JSONObject(noteJSON);

                String noteTitle = json.getString("noteTitle");
                String noteContent = json.getString("noteContent");
                String noteColor = json.getString("noteColor");
                String noteLastUpdateDate = json.getString("noteLastUpdateDate");
                String noteCreationDate = json.getString("noteCreationDate");
                int    notePosition = json.getInt("notePosition");

                saveNote(noteJSON, noteCreationDate);

                if (notePosition > -1) {
                    listViewItems.remove(notePosition);
                    listViewItems.add(notePosition, new ItemObjects(noteTitle, noteContent, noteColor, noteLastUpdateDate, noteCreationDate));
                    rcAdapter.notifyItemChanged(notePosition);
                }
                else {
                    listViewItems.add(new ItemObjects(noteTitle, noteContent, noteColor, noteLastUpdateDate, noteCreationDate));
                    rcAdapter.notifyDataSetChanged();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // Load notes from internal storage
    private List<ItemObjects> loadNotes() {

        listViewItems = new ArrayList<>();
        String[] allNotes = fileList();

        Boolean secure = false;

        for (String allNote : allNotes) {

            FileInputStream fis = null;
            try {
                fis = getBaseContext().openFileInput(allNote);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            InputStreamReader isr = null;
            if (fis != null) {
                isr = new InputStreamReader(fis);
            }
            BufferedReader bufferedReader = null;
            if (isr != null) {
                bufferedReader = new BufferedReader(isr);
            }
            StringBuilder sb = new StringBuilder();
            String line;
            try {
                if (bufferedReader != null) {
                    while ((line = bufferedReader.readLine()) != null) {
                        sb.append(line);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            JSONObject json = null;
            try {
                if (sb.toString().length() > 0)
                    secure = true;
                json = new JSONObject(sb.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }

            String noteTitle = null;
            try {
                if (json != null) {
                    noteTitle = json.getString("noteTitle");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            String noteContent = null;
            try {
                if (json != null) {
                    noteContent = json.getString("noteContent");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            String noteColor = null;
            try {
                if (json != null) {
                    noteColor = json.getString("noteColor");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            String noteLastUpdateDate = null;
            try {
                if (json != null) {
                    noteLastUpdateDate = json.getString("noteLastUpdateDate");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            String noteCreationDate = null;
            try {
                if (json != null) {
                    noteCreationDate = json.getString("noteCreationDate");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (secure)
                listViewItems.add(new ItemObjects(noteTitle, noteContent, noteColor, noteLastUpdateDate, noteCreationDate));
        }
        return listViewItems;
    }

    // Save notes to internal storage
    public void saveNote(String note, String noteCreationDate) {

        // Name file with current date
        FileOutputStream outputStream;

        try {
            outputStream = openFileOutput(noteCreationDate, Context.MODE_PRIVATE);
            outputStream.write(note.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
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
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // Temporary solution to add notes
    @SuppressLint("ResourceType")
    private List<ItemObjects> getListItemData(){

        listViewItems = new ArrayList<>();

        listViewItems.add(new ItemObjects("", "Cat cat cat cat", getResources().getString(R.color.colorNoteRed)));
        listViewItems.add(new ItemObjects("Lorem", "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nullapariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.", getResources().getString(R.color.colorNoteOrange)));
        listViewItems.add(new ItemObjects("","Length length length length", getResources().getString(R.color.colorNoteYellow)));
        listViewItems.add(new ItemObjects("", "String string string string", getResources().getString(R.color.colorNoteGreen)));
        listViewItems.add(new ItemObjects("", "Content content content content", getResources().getString(R.color.colorNoteCyan)));
        listViewItems.add(new ItemObjects("", "Size size size size size size size size size size size", getResources().getString(R.color.colorNoteLightBlue)));
        listViewItems.add(new ItemObjects("", "Char char char char char char char", getResources().getString(R.color.colorNoteDarkBlue)));
        listViewItems.add(new ItemObjects("", "Dog dog dog dog dog dog", getResources().getString(R.color.colorNotePurple)));
        listViewItems.add(new ItemObjects("", "Int hold dog cat hold hold hold cat hold hold dog hold int hold hold hold hold hold", getResources().getString(R.color.colorNotePink)));

        listViewItems.add(new ItemObjects("", "test test test test test test", getResources().getString(R.color.colorNoteRed)));
        listViewItems.add(new ItemObjects("Lorem", "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nullapariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.", getResources().getString(R.color.colorNoteOrange)));
        listViewItems.add(new ItemObjects("Alkane","Hello Markdown", R.drawable.one, getResources().getString(R.color.colorNoteYellow)));
        listViewItems.add(new ItemObjects("Ethane", "Hello Markdown", getResources().getString(R.color.colorNoteGreen)));
        listViewItems.add(new ItemObjects("Alkyne", "Hello Markdown", R.drawable.three, getResources().getString(R.color.colorNoteCyan)));
        listViewItems.add(new ItemObjects("Benzene", "Hello Markdown", R.drawable.four, getResources().getString(R.color.colorNoteLightBlue)));
        listViewItems.add(new ItemObjects("Amide", "Hello Markdown", R.drawable.one, getResources().getString(R.color.colorNoteDarkBlue)));
        listViewItems.add(new ItemObjects("Amino Acid", "Hello Markdown", R.drawable.two, getResources().getString(R.color.colorNotePurple)));
        listViewItems.add(new ItemObjects("Phenol", "Hello Markdown", getResources().getString(R.color.colorNotePink)));
        listViewItems.add(new ItemObjects("Carbonxylic", "Hello Markdown", R.drawable.four, getResources().getString(R.color.colorNoteRed)));
        listViewItems.add(new ItemObjects("Nitril", "Hello Markdown", R.drawable.one, getResources().getString(R.color.colorNoteOrange)));
        listViewItems.add(new ItemObjects("Ether", "Hello Markdown", R.drawable.two, getResources().getString(R.color.colorNoteYellow)));
        listViewItems.add(new ItemObjects("Ester", "Hello Markdown", R.drawable.three, getResources().getString(R.color.colorNoteGreen)));
        listViewItems.add(new ItemObjects("Alcohol", "Hello Markdown", getResources().getString(R.color.colorNoteCyan)));
        listViewItems.add(new ItemObjects("Title only", "", getResources().getString(R.color.colorNoteLightBlue)));
        listViewItems.add(new ItemObjects("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "", getResources().getString(R.color.colorNoteDarkBlue)));
        listViewItems.add(new ItemObjects("", "Content only", getResources().getString(R.color.colorNotePurple)));
        listViewItems.add(new ItemObjects("", "Kkk", getResources().getString(R.color.colorNotePink)));
        listViewItems.add(new ItemObjects("", "Kkkkkk", getResources().getString(R.color.colorNoteRed)));
        listViewItems.add(new ItemObjects("", "Kkkkkkkkkk", getResources().getString(R.color.colorNoteOrange)));
        listViewItems.add(new ItemObjects("", "Kkkkkkkkkkkkk", getResources().getString(R.color.colorNoteYellow)));
        listViewItems.add(new ItemObjects("", "Kkkkkkkkkkkkkkkkkkk", getResources().getString(R.color.colorNoteGreen)));
        listViewItems.add(new ItemObjects("", "Kkkkkkkkkkkkkkkkkkkkkkkk", getResources().getString(R.color.colorNoteCyan)));

        return listViewItems;
    }

    // Check for read in external storage
    public boolean checkPermissionForReadExternalStorage() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            int result = getApplicationContext().checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
            return result == PackageManager.PERMISSION_GRANTED;
        }
        return false;
    }

    // Check for write in external storage
    public boolean checkPermissionForWriteExternalStorage() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            int result = getApplicationContext().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            return result == PackageManager.PERMISSION_GRANTED;
        }
        return false;
    }
}
