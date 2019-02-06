package cz.mendelu.busitweek2019;

import cz.mendelu.busItWeek.library.StoryLineDatabaseHelper;
import cz.mendelu.busItWeek.library.builder.StoryLineBuilder;

public class BusITWeekDatabaseHelper extends StoryLineDatabaseHelper{


    public BusITWeekDatabaseHelper() {
        super(18);
    }

    @Override
    protected void onCreate(StoryLineBuilder builder) {
        builder.addCodeTask("1")
                .qr("QR")
                .location(0, 0)

                .taskDone();

        builder.addGPSTask("2")
                .location(1, 1)
                .radius(100)
                .victoryPoints(10)
                .hint("Hint")
                .simplePuzzle()
                .question("What is the best Bus IT Week?")
                .answer("Brno")
                .hint("Question hint")
                .puzzleTime(30000)
                .puzzleDone()
                .taskDone();

        builder.addGPSTask("3")
                .location(2,2).
                radius(100000)
                .choicePuzzle()
                .addChoice("Fdsfs",false)
                .addChoice("Dfsdfasf",false)
                .addChoice("Fsdfdfsd",true)
                .addChoice("Fdfasfdds",false)
                .question("Really")
                .puzzleDone()
                .taskDone();

        builder.addBeaconTask("4")
                .beacon(29028 ,54274)
                .imageSelectPuzzle()
                .addImage(R.drawable.damas, false)
                .addImage(R.drawable.damascu, false)
                .addImage(R.drawable.damascus, true)
                .addImage(R.drawable.damscu, false)
                .question("Best view ever")
                .puzzleDone()
                .location(3, 3)
                .taskDone();

    }
}
