package cz.mendelu.busitweek2019;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import cz.mendelu.busItWeek.library.SimplePuzzle;
import cz.mendelu.busItWeek.library.StoryLine;
import cz.mendelu.busItWeek.library.Task;

public class SimplePuzzleActivity extends AppCompatActivity {

    private TextView questionTextView;
    private TextView hintTextView;
    private EditText answerEditText;
    private Button doneButton;

    private StoryLine storyLine;
    private Task currentTask;
    private SimplePuzzle puzzle;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_puzzle);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                skipCurrentTask();
            }
        });


        questionTextView = findViewById(R.id.question);
        hintTextView = findViewById(R.id.hint);
        answerEditText = findViewById(R.id.answer);
        doneButton = findViewById(R.id.done_button);

        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAnswer();
            }
        });

        storyLine = StoryLine.open(this, BusITWeekDatabaseHelper.class);



    }

    @Override
    public void onBackPressed() {
        skipCurrentTask();
    }

    @Override
    protected void onResume() {
        super.onResume();
        currentTask = storyLine.currentTask();
        if (currentTask != null){
            puzzle = (SimplePuzzle) currentTask.getPuzzle();
            questionTextView.setText(puzzle.getQuestion());
            hintTextView.setText(currentTask.getHint());
            //hintTextView.setText(puzzle.getHint());
        }

    }

    private void checkAnswer(){
        String userAnswer = answerEditText.getText().toString();
        String correctAnswer = puzzle.getAnswer();

        if (userAnswer.equalsIgnoreCase(correctAnswer)){
            // correct answer
            storyLine.currentTask().finish(true);
            finish();
        } else {
            // false answer
            Toast.makeText(this, "Wrong answer", Toast.LENGTH_LONG).show();
            answerEditText.setText("");
        }
    }

    private void skipCurrentTask(){
        DialogUtility.skipTask(this, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                storyLine.currentTask().skip();
                finish();
            }
        });


    }
}











