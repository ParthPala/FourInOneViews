# FourInOneViews
Do you need a single view which can morph into a desired view at run-time but currently have four views stacked over one-another  with long if-else conditions to show/hide one/some of them??  

Then worry no more, because FourInOneViews is here to the rescue!! 

- This is a view where you can change it at run time with single line of code 
- This view can behave as any one of the below 

1. Textview 
2. EditText 
3. Auto-CompleteTextview 
4. Spinner

Following is the sample code-
### activity.xml
```
<multifunction.view.multifunctionview.FourInOneViews
      android:layout_width="match_parent"
      android:layout_height="wrap_content"
       app1:selectview="TextView"/>
   ``` 
      
 ### MainActivity.class
 ```
 FourInOneViews fourInOneViews;
 fourInOneViews = findViewById(R.id.fourInOneViews);
 
   String[] countryNameArray = new String[]{
                        "Afghanistan","Argentina", "Australia","Brazil","Canada","China","Denmark","United States","India","Singapore","Russia"
        };
        
   final ArrayAdapter<String> CountryListadapter = new ArrayAdapter<String>(this,
    android.R.layout.simple_spinner_item, countryNameArray);
  ```
 ##### For TextView 
 ```
   fourInOneViews.changeView(FourInOneViews.selectview.TextView);
   ```
 ##### For EditText 
 ```
   fourInOneViews.changeView(FourInOneViews.selectview.EditText);
   ```
 ##### For AutoCompleteTextView 
 ```
   fourInOneViews.changeView(FourInOneViews.selectview.AutoCompleteTextView);
   fourInOneViews.setAdapter(CountryListadapter);
   CountryListadapter.notifyDataSetChanged();
   ```
 ##### For Spinner 
 ```
   fourInOneViews.changeView(FourInOneViews.selectview.Spinner);
   fourInOneViews.setAdapter(CountryListadapter);
   CountryListadapter.notifyDataSetChanged();
  ``` 
  ## Preview
   #### For TextView & EditText
  ![Alt Text](https://github.com/ParthPala/FourInOneViews/blob/master/app/src/main/res/Gifs/example1.gif)
  #### For AutoCompleteTextView & Spinner
  ![Alt Text](https://github.com/ParthPala/FourInOneViews/blob/master/app/src/main/res/Gifs/example2.gif)
  
  ## For Customization 
  ```
  // to set drawable/icon for spinner
 setDropDownDrawable(Drawable drawable)
   ```
   ```
   // to set number of characters after which the search should begin for AutoCompleteTextView
 setThreshold(int numOfChar)
   ```
   ```
   //to set drawable/icon with custom height and width
   setDropDownDrawable(Drawable drawable, int height, int width);
   ```
   ```
   // returns int for current view state
  getCurrentViewMode()
   ```
   ```
   //View to anchor the auto-complete dropdown.
  setDropDownAnchor()
   ```
  
