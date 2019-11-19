package multifunction.view.multifunctionview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private TextView changeViewButton;
    private FourInOneViews fourInOneViews;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        changeViewButton = findViewById(R.id.changeViewButton);
        fourInOneViews = findViewById(R.id.fourInOneViews);

                String[] countryNameArray = new String[]{
                        "Afghanistan","Argentina", "Australia","Brazil","Canada","China","Denmark","United States","India","Singapore","Russia"
        };
        final ArrayAdapter<String> countryListadapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, countryNameArray);

        changeViewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fourInOneViews.getCurrentViewMode() == 0) {
                    fourInOneViews.setText(getResources().getString(R.string.txt_edittext));
                    fourInOneViews.changeView(FourInOneViews.selectview.EditText);
                    changeViewButton.setText(getResources().getString(R.string.txt_edittext));
                }

                else if (fourInOneViews.getCurrentViewMode() == 1) {
                    fourInOneViews.setText(getResources().getString(R.string.txt_autocompletetextview));
                    fourInOneViews.changeView(FourInOneViews.selectview.AutoCompleteTextView);
                    fourInOneViews.setAdapter(countryListadapter);
                    changeViewButton.setText(getResources().getString(R.string.txt_autocompletetextview));
                }

                else if(fourInOneViews.getCurrentViewMode() == 2) {
                    fourInOneViews.setText(getResources().getString(R.string.txt_spinner));
                    fourInOneViews.changeView(FourInOneViews.selectview.Spinner);
                    fourInOneViews.setAdapter(countryListadapter);
                    countryListadapter.notifyDataSetChanged();
                    changeViewButton.setText(getResources().getString(R.string.txt_spinner));
                }

                else if(fourInOneViews.getCurrentViewMode() == 3) {
                    fourInOneViews.setText(getResources().getString(R.string.txt_textview));
                    fourInOneViews.changeView(FourInOneViews.selectview.TextView);
                    changeViewButton.setText(getResources().getString(R.string.txt_textview));

                }
            }
        });
    }
}
