package com.example.android.booklistingapp;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class BookActivity extends AppCompatActivity {

    // This is the tag for LOG message
    public static final String LOG_TAG = BookActivity.class.getName();
    // Key used for Saved Instance State of the app
    private static final String BOOK = "book";
    // This is the Google API URL
    private static final String BASE_URL = "https://www.googleapis.com/books/v1/volumes?&maxResults=10&q=";
    // Initialization of the bookList variable
    ArrayList<Book> bookList = new ArrayList<>();
    // Adapter for the list of books
    private BookAdapter bookAdapter;
    // Edit text field used for searching for books
    private EditText searchBook;
    // TextView visible when there is a problem with the internet connection and the list is empty
    private TextView emptyStateTextView;
    // Progress bar visible when the internet connection is delayed or slow
    private View loadingIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book);

        // Find a reference to the ListView in the layout
        ListView bookListView = (ListView) findViewById(R.id.list);

        // Find a reference to the EditText in the layout
        searchBook = (EditText) findViewById(R.id.search);

        // Find a reference to the Search Button in the layout
        ImageButton buttonSearch = (ImageButton) findViewById(R.id.button_search);

        // Find a reference to the empty state TextView
        emptyStateTextView = (TextView) findViewById(R.id.empty_view);

        // Set empty state TextView on the ListView with books when no data can be found
        bookListView.setEmptyView(emptyStateTextView);

        // Find a reference to the progress bar
        loadingIndicator = findViewById(R.id.loading_indicator);

        // Hide the loading indicator by default in the layout
        loadingIndicator.setVisibility(View.GONE);

        // Create a new adapter that takes an empty list of books as input
        bookAdapter = new BookAdapter(this, new ArrayList<Book>());

        // Set the adapter on the ListView so the list can be populated in the user interface
        bookListView.setAdapter(bookAdapter);

        // Set a click listener to the ImageButton Search which sends query to the URL based on the user input
        buttonSearch.setOnClickListener(new ImageButton.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Clear the adapter
                bookAdapter.clear();

                // If there is a network connection, fetch data
                if (checkInternetConnection()) {

                    String searchUrl = searchBook.getText().toString();
                    // Show the circle indicator
                    loadingIndicator.setVisibility(View.VISIBLE);

                    // This is called when there is an internet connection.
                    // Start the AsyncTask to fetch the books data

                    new BookAsyncTask().execute(BASE_URL + searchUrl);

                } else {
                    Log.e(LOG_TAG, "No internet connection");
                    // Otherwise, display error
                    // First, hide loading indicator so error will be visible
                    loadingIndicator.setVisibility(View.GONE);
                    // Show the empty state with no connection error message
                    emptyStateTextView.setVisibility(View.VISIBLE);
                    // Update empty state with no connection error message
                    emptyStateTextView.setText(R.string.no_internet_connection);
                }
            }
        });


        // create the book list
        if (savedInstanceState != null) {

            bookListView = (ListView) findViewById(R.id.list);
            bookList = savedInstanceState.getParcelableArrayList(BOOK);
            bookAdapter = new BookAdapter(this, new ArrayList<Book>());
            bookListView.setAdapter(bookAdapter);
            bookAdapter.addAll(bookList);

        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(BOOK, bookList);
        super.onSaveInstanceState(outState);
    }

    // Check the internet connection
    private boolean checkInternetConnection() {
        // Get a reference to the ConnectivityManager to check the state of network connectivity
        ConnectivityManager connectivityManager = (ConnectivityManager)
                getSystemService(CONNECTIVITY_SERVICE);

        // Get details on the currently active default network
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        } else {
            // Show the empty state with no connection error message
            emptyStateTextView.setVisibility(View.VISIBLE);
            // Update empty state with no connection error message
            emptyStateTextView.setText(R.string.no_internet_connection);
            return false;
        }
    }

    // {@link AsyncTask} to perform the network request on a background thread, and then
    // update the UI with the list of books in the response.
    //
    // We'll only override two of the methods of AsyncTask: doInBackground() and onPostExecute().
    // The doInBackground() method runs on a background thread, so it can run long-running code
    // (like network activity), without interfering with the responsiveness of the app.
    // Then onPostExecute() is passed the result of doInBackground() method, but runs on the
    // UI thread, so it can use the produced data to update the UI.
    //
    private class BookAsyncTask extends AsyncTask<String, Void, ArrayList<Book>> {

        // This method runs on the UI thread before doInBackground().
        // It shows the progress bar when the internet connection is delayed or slow.
        @Override
        protected void onPreExecute() {
            loadingIndicator.setVisibility(View.VISIBLE);
        }

        // This method runs on a background thread and performs the network request.
        // We should not update the UI from a background thread, so we return a list of
        // Books as the result.
        @Override
        protected ArrayList<Book> doInBackground(String... urls) {

            // Don't perform the request if there are no URLs, or the first URL is null.
            if (urls.length < 1 || urls[0] == null) {
                return null;
            }
            ArrayList<Book> result = Utils.fetchBooksData(urls[0]);

            return result;
        }

        // This method runs on the main UI thread after the background work has been
        // completed. This method receives as input, the return value from the doInBackground()
        // method. First we clear out the adapter, to get rid of books data from a previous
        // query to Google Books API. Then we update the adapter with the new list of books,
        // which will trigger the ListView to re-populate its list items.
        @Override
        protected void onPostExecute(ArrayList<Book> books) {

            // First, hide loading indicator so error will be visible
            loadingIndicator.setVisibility(View.GONE);

            // If there is a valid list of Books, then add them to the adapter's
            // data set. This will trigger the ListView to update.
            if (books != null && !books.isEmpty()) {
                bookList = books;
                bookAdapter.addAll(books);
            } else {
                // Show the empty state with no connection error message
                emptyStateTextView.setVisibility(View.VISIBLE);
                // Update empty state with no connection error message
                emptyStateTextView.setText(R.string.no_internet_connection);
            }
        }
    }
}