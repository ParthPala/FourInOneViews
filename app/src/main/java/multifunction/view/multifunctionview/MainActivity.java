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

                String[] countryName = new String[]{
                        "Afghanistan","Argentina", "Australia","Brazil","Canada","China","Denmark","United States","India","Singapore","Russia"};

        final ArrayAdapter<String> countryListadapterAutoComplete = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, countryName);

        String[] stateName = new String[]
                {"Andaman and Nicobar Islands","Andhra Pradesh", "Arunachal Pradesh", "Assam", "Bihar", "Chandigarh",
                        "Chhattisgarh","Dadra and Nagar Haveli", "Daman and Diu", "Delhi", "Goa", "Gujarat", "Haryana",
                        "Himachal Pradesh", "Jammu and Kashmir", "Jharkhand", "Karnataka", "Kerala", "Lakshadweep", "Madhya Pradesh",
                        "Maharashtra", "Manipur", "Meghalaya", "Mizoram", "Nagaland", "Orissa", "Pondicherry", "Punjab", "Rajasthan",
                        "Sikkim", "Tamil Nadu", "Telangana", "Tripura", "Uttaranchal", "Uttar Pradesh", "West Bengal"};

        final ArrayAdapter<String> countryListadapterSpinner = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, stateName);

        changeViewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fourInOneViews.getCurrentViewMode() == 0) {
                    fourInOneViews.setText(getResources().getString(R.string.txt_edittext));
                    fourInOneViews.changeView(FourInOneViews.selectview.EditText);
                    changeViewButton.setText(getResources().getString(R.string.txt_edittext));
                }

                else if (fourInOneViews.getCurrentViewMode() == 1) {
                    fourInOneViews.setText("");
                    fourInOneViews.changeView(FourInOneViews.selectview.AutoCompleteTextView);
                    fourInOneViews.setAdapter(countryListadapterAutoComplete);
                    changeViewButton.setText(getResources().getString(R.string.txt_autocompletetextview));
                }

                else if(fourInOneViews.getCurrentViewMode() == 2) {
                    fourInOneViews.setText("");
                    fourInOneViews.changeView(FourInOneViews.selectview.Spinner);
                    fourInOneViews.setAdapter(countryListadapterSpinner);
                    countryListadapterAutoComplete.notifyDataSetChanged();
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
