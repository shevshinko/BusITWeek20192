package cz.mendelu.busitweek2019;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cz.mendelu.busItWeek.library.ChoicePuzzle;
import cz.mendelu.busItWeek.library.ImageSelectPuzzle;
import cz.mendelu.busItWeek.library.StoryLine;
import cz.mendelu.busItWeek.library.Task;

public class ImagePuzzleActivity extends AppCompatActivity {

    private TextView questionTextView;
    private TextView hintTextView;
    private RecyclerView recyclerView;

    private StoryLine storyLine;
    private Task currentTask;
    private ImageSelectPuzzle puzzle;

    private AnswersAdapter answersAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choice_puzzle);
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
        recyclerView=findViewById(R.id.answers);
        storyLine = StoryLine.open(this, BusITWeekDatabaseHelper.class);

    }

    @Override
    public void onBackPressed() {
        skipCurrentTask();
    }
    private class AnswersAdapter extends RecyclerView.Adapter<AnswersAdapter.MyViewHolder>{
        private List<Integer> answerList;

        public AnswersAdapter(List<Integer> answerList) {
            this.answerList = answerList;
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.row_image_puzzle,viewGroup,false);
            return new MyViewHolder(view);

        }

        @Override
        public void onBindViewHolder(@NonNull final MyViewHolder myViewHolder, int position) {
            Integer answer = answerList.get(position);

            Picasso.get().load(answer).into(myViewHolder.answer);

            myViewHolder.itemView.setOnClickListener (new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(puzzle.getAnswerForImage(myViewHolder.getAdapterPosition())){
                        //correct
                        currentTask.finish(true);
                        finish();
                    }else {
                        //not Correct
                        Toast.makeText(ImagePuzzleActivity.this,
                                "Wrong answer",Toast.LENGTH_SHORT).show();

                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return answerList.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder{
            public ImageView answer;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);
                answer = itemView.findViewById(R.id.image);
            }
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

    @Override
    protected void onResume() {
        super.onResume();
        currentTask = storyLine.currentTask();
        if (currentTask != null){
            puzzle = (ImageSelectPuzzle) currentTask.getPuzzle();
            questionTextView.setText(puzzle.getQuestion());
            hintTextView.setText(currentTask.getHint());
            //hintTextView.setText(puzzle.getHint());
            initilaizeTheList();
        }

    }

    private void initilaizeTheList(){
        List<Integer> List = new ArrayList<>();
        for(Map.Entry<Integer, Boolean> entry: puzzle.getImages().entrySet()){
            List.add(entry.getKey());

        }
        answersAdapter= new AnswersAdapter(List);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(answersAdapter);
    }
}
