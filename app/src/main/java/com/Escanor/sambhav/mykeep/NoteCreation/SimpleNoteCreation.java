package com.Escanor.sambhav.mykeep.NoteCreation;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TableLayout;
import android.widget.TextView;

import com.Escanor.sambhav.mykeep.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SimpleNoteCreation extends AppCompatActivity {

    EditText                            titleEditText;
    EditText                            contentEditText;
    TextView                            lastUpdateDateTextView;
    RadioGroup                          colorPickerRadioGroup;

    LinearLayout                        noteLayout, noteActionsLayout;
    TableLayout                         bottomToolbar;
    android.support.v7.app.ActionBar    actionBar;
    ImageButton                         noteActionsButton;

    String                              noteColor;
    String                              lastUpdateDateString, creationDateString;

    String                              lastTitle, lastContent;
    int                                 notePosition;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_note_creation);

        actionBar = getSupportActionBar();
        titleEditText = findViewById(R.id.title_edit_text);
        contentEditText = findViewById(R.id.content_edit_text);
        colorPickerRadioGroup = findViewById(R.id.color_picker_radio_group);
        noteLayout = findViewById(R.id.simple_note_creation_linear_layout);
        noteActionsLayout = findViewById(R.id.note_actions_layout);
        bottomToolbar = findViewById(R.id.bottom_toolbar);
        lastUpdateDateTextView = findViewById(R.id.last_modification_date);
        noteActionsButton = findViewById(R.id.note_actions_button);

        Intent editionIntent = getIntent();
        lastTitle = editionIntent.getStringExtra("title");
        lastContent = editionIntent.getStringExtra("content");
        String color = editionIntent.getStringExtra("color");
        creationDateString = editionIntent.getStringExtra("creationDate");
        notePosition = editionIntent.getIntExtra("position", -1);

        // Set activity default color
        noteLayout.setBackgroundColor(Color.parseColor(color));
        noteActionsLayout.setBackgroundColor(Color.parseColor(color));
        bottomToolbar.setBackgroundColor(Color.parseColor(color));
        noteColor = color;
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor(color)));
        getWindow().setStatusBarColor(darkenNoteColor(Color.parseColor(noteColor), 0.7f));

        // Set title and content if edit
        titleEditText.setText(lastTitle);
        contentEditText.setText(lastContent);

        // Get date
        if (creationDateString.isEmpty())
            creationDateString = new SimpleDateFormat("ddMMyyyyhhmmss", Locale.getDefault()).format(new Date());
        lastUpdateDateString = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());

        // Set "Last Update" field content
        lastUpdateDateTextView.setText("Last update : " + lastUpdateDateString);

        // Open keyboard and put focus on the content edit text
        if (lastTitle.isEmpty() && lastContent.isEmpty()) {
            contentEditText.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            assert imm != null;
            imm.showSoftInput(contentEditText, InputMethodManager.SHOW_IMPLICIT);
        }

        // Hide note actions by default
        noteActionsLayout.setVisibility(View.GONE);

        // Hide/Show note actions
        noteActionsButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (noteActionsLayout.getVisibility() == View.GONE) {
                    noteActionsLayout.setVisibility(View.VISIBLE);
                    noteActionsButton.setBackgroundColor(darkenNoteColor(Color.parseColor(noteColor), 0.9f));
                }
                else if (noteActionsLayout.getVisibility() == View.VISIBLE) {
                    noteActionsButton.setBackgroundColor(Color.parseColor(noteColor));
                    noteActionsLayout.setVisibility(View.GONE);
                }
            }
        });

        // Check the color picker
        colorPickerRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @SuppressLint("ResourceType")
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                if (checkedId == R.id.default_color_checkbox) {
                    noteColor = getResources().getString(R.color.colorNoteDefault);

                }
                else if (checkedId == R.id.red_color_checkbox) {
                    noteColor = getResources().getString(R.color.colorNoteRed);
                }
                else if (checkedId == R.id.orange_color_checkbox) {
                    noteColor = getResources().getString(R.color.colorNoteOrange);
                }
                else if (checkedId == R.id.yellow_color_checkbox) {
                    noteColor = getResources().getString(R.color.colorNoteYellow);
                }
                else if (checkedId == R.id.green_color_checkbox) {
                    noteColor = getResources().getString(R.color.colorNoteGreen);
                }
                else if (checkedId == R.id.cyan_color_checkbox) {
                    noteColor = getResources().getString(R.color.colorNoteCyan);
                }
                else if (checkedId == R.id.light_blue_color_checkbox) {
                    noteColor = getResources().getString(R.color.colorNoteLightBlue);
                }
                else if (checkedId == R.id.dark_blue_color_checkbox) {
                    noteColor = getResources().getString(R.color.colorNoteDarkBlue);
                }
                else if (checkedId == R.id.purple_color_checkbox) {
                    noteColor = getResources().getString(R.color.colorNotePurple);
                }
                else if (checkedId == R.id.pink_color_checkbox) {
                    noteColor = getResources().getString(R.color.colorNotePink);
                }
                else if (checkedId == R.id.brown_color_checkbox) {
                    noteColor = getResources().getString(R.color.colorNoteBrow);
                }
                else if (checkedId == R.id.grey_color_checkbox) {
                    noteColor = getResources().getString(R.color.colorNoteGrey);
                }
                noteLayout.setBackgroundColor(Color.parseColor(noteColor));
                noteActionsLayout.setBackgroundColor(Color.parseColor(noteColor));
                bottomToolbar.setBackgroundColor(Color.parseColor(noteColor));
                actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor(noteColor)));
                getWindow().setStatusBarColor(darkenNoteColor(Color.parseColor(noteColor), 0.7f));

                noteActionsButton.setBackgroundColor(darkenNoteColor(Color.parseColor(noteColor), 0.9f));
            }
        });
    }

    public static int darkenNoteColor(int color, float factor) {
        int a = Color.alpha(color);
        int r = Math.round(Color.red(color) * factor);
        int g = Math.round(Color.green(color) * factor);
        int b = Math.round(Color.blue(color) * factor);
        return Color.argb(a,
                Math.min(r,255),
                Math.min(g,255),
                Math.min(b,255));
    }

    // Save note content on back pressed
    @Override
    public void onBackPressed() {

        Boolean changed = false;
        String titleText = titleEditText.getText().toString();
        String contentText = contentEditText.getText().toString();

        if (!titleText.equals(lastTitle) || !contentText.equals(lastContent))
            changed = true;

        // Check if fields are not empty
        if ((!TextUtils.isEmpty(titleText) || !TextUtils.isEmpty(contentText)) && changed) {


            JSONObject noteJSON = new JSONObject();
            try {
                noteJSON.put("noteTitle", titleEditText.getText().toString());
                noteJSON.put("noteContent", contentEditText.getText().toString());
                noteJSON.put("noteColor", noteColor);
                noteJSON.put("noteCreationDate", creationDateString);
                noteJSON.put("noteLastUpdateDate", lastUpdateDateString);
                noteJSON.put("notePosition", notePosition);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            // Return note JSON to MainActivity
            Intent resultIntent = new Intent();
            resultIntent.putExtra("noteJSON", noteJSON.toString());
            setResult(Activity.RESULT_OK, resultIntent);
        }
        this.finish();
    }
}
