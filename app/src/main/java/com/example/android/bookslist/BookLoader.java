package com.example.android.bookslist;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.util.Log;

import java.util.List;

public class BookLoader extends AsyncTaskLoader<List<Book>> {

    private String mUrl;

    public BookLoader(Context context, String url) {
        super(context);
        mUrl = url;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Override
    public List<Book> loadInBackground() {
        if (mUrl == null || MainActivity.flag == false) {
            return null;
        }

        Log.i("MainAcitivity", "loadInBackground() is called.");
        return QueryUtils.fetchNewsData(mUrl);
    }
}
