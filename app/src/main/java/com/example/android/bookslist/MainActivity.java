package com.example.android.bookslist;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NetworkStateBroadcastReceiver.OnNetworkStateReadyListener,LoaderManager.LoaderCallbacks<List<Book>> {

    //书籍loader的ID
    private static final int BOOK_LOADER_ID = 0;
    //Google图书的API（URL末端待追加书名关键字）
    private String BOOK_REQUEST_URL =
            "https://www.googleapis.com/books/v1/volumes?q=";
    private ViewHolder holder = new ViewHolder();
    // 书籍列表适配器
    private BookAdapter mAdapter;
    // 获取LoaderManager对象, 管理各个loader实例.
    private LoaderManager loaderManager;
    private NetworkStateBroadcastReceiver mReceiver;
    static Boolean flag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loaderManager = getLoaderManager();

        holder.booksListView = (ListView) findViewById(R.id.list);
        holder.mEmptyStateView = (TextView) findViewById(R.id.empty_view);
        holder.enterTitle = (EditText) findViewById(R.id.enter_title);
        holder.searchButton = (Button) findViewById(R.id.search_button);
        holder.mLoading = findViewById(R.id.in_loading);

        holder.mLoading.setVisibility(View.GONE);
        holder.booksListView.setEmptyView(holder.mEmptyStateView);
        //将loader加载完毕获得的结果以动态滚动形式加载到用户界面上
        viewTheResult();
        //检查设备联网情况并进行UI更新。
        checkNetworkState();


    }

    @Override
    protected void onStart() {
        super.onStart();
        //this.onRestoreInstanceState();
    }

    @Override
    protected void onResume(){
        super.onResume();
        mReceiver = new NetworkStateBroadcastReceiver(this);
        IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mReceiver, intentFilter);
    }

    @Override
    protected void onPause(){
        super.onPause();
        unregisterReceiver(mReceiver);
        //Bundle bundle = new Bundle();
        //bundle.
        //this.onSaveInstanceState();
    }

    @Override
    public void onNetworkStateReady() {
        checkNetworkState();
        if (flag){
            holder.mEmptyStateView.setText("");
        }else{
            holder.mEmptyStateView.setText(R.string.no_internet_connection);
            Toast.makeText(this, R.string.no_internet_connection, Toast.LENGTH_SHORT).show();
        }
    }

    private void checkNetworkState(){
        ConnectivityManager cm =
                (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        if (activeNetwork != null && activeNetwork.isConnectedOrConnecting()) {
            flag = true;
            holder.searchButton.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (hasFocus){
                        closeKeyBoard();
                        updateUi();
                    }
                }
            });
            /*
            holder.searchButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    closeKeyBoard();
                    updateUi();
                }
            });
            */
        } else {
            flag = false;
            holder.mLoading.setVisibility(View.GONE);
            holder.mEmptyStateView.setText(R.string.no_internet_connection);
        }
    }

    private void updateUi() {

        if (flag == false){
            Toast.makeText(this, R.string.no_internet_connection, Toast.LENGTH_SHORT).show();
            return;
        }
        String newBookTitle = holder.enterTitle.getText().toString().trim();
        if (newBookTitle.equals("")) {
            Toast.makeText(this, R.string.enter_incorrect, Toast.LENGTH_SHORT).show();
            holder.enterTitle.setText("");
            return;
        }
        /*
        if (flag == false){
            Toast.makeText(this, R.string.no_internet_connection, Toast.LENGTH_SHORT).show();
            return;
        }
        */
        holder.mEmptyStateView.setText("");
        holder.mLoading.setVisibility(View.VISIBLE);

        //定义变量，存储转义后的书名参数，因为如果输入的书名中有空格，如the book,请求会因为URL不正确
        //而发生错误，此处对其进行编码转义，用%20代替空格。
        String param = null;

        try {
            // 转码为 UTF-8格式
            param = URLEncoder.encode(newBookTitle, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        BOOK_REQUEST_URL += param;

        if (loaderManager.getLoader(0) != null) {
            loaderManager.restartLoader(0, null, this);
        }

        Log.i("MainAcitivity", "initLoader() is called.");

        // 初始化loader，传递参数分别为(1)目标loader的ID;(2)Bundle;(3)MainActivity的引用。
        loaderManager.initLoader(BOOK_LOADER_ID, null, this);

    }

    private void viewTheResult(){
        // 实例化书籍列表适配器
        mAdapter = new BookAdapter(this, new ArrayList<Book>());

        // 为listView绑定适配器，实现书籍列表在用户界面上的动态滚动
        holder.booksListView.setAdapter(mAdapter);

        // 为列表的每一项设置监听器，当某一项被选中就向浏览器发送intent打开包含对应书籍详细信息的页面
        holder.booksListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                // 找到被选中的具体item项
                Book currentBook = mAdapter.getItem(position);

                // 将字符串类型的URL转换成Uri对象 (作为intent的传递参数)
                Uri newsUri = Uri.parse(currentBook.getmUrl());
                Intent websiteIntent = new Intent(Intent.ACTION_VIEW, newsUri);

                // 发送intent打开新activity
                startActivity(websiteIntent);
            }
        });
    }

    /**
     * 调用以隐藏软键盘
     */
    public void closeKeyBoard(){

        InputMethodManager methodManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        methodManager.hideSoftInputFromWindow(holder.enterTitle.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    @Override
    public Loader<List<Book>> onCreateLoader(int i, Bundle bundle) {
        // 首次创建loader时使用提供的URL创建loader实例
        return new BookLoader(this, BOOK_REQUEST_URL);
    }

    @Override
    public void onLoadFinished(Loader<List<Book>> loader, List<Book> books) {

        if (flag == false){
            return;
        }
        Log.i("MainAcitivity", "onLoadFinish() is called.");
        holder.mLoading.setVisibility(View.GONE);
        // 清理包含旧版本的书籍数据的适配器
        mAdapter.clear();
        holder.mEmptyStateView.setText(R.string.empty_view_text);
        // 如果有有效的列表，则将它们添加到适配器的数据集中。 这将触发ListView进行更新。
        if (books != null && !books.isEmpty()) {
            mAdapter.addAll(books);
            BOOK_REQUEST_URL =
                    "https://www.googleapis.com/books/v1/volumes?q=";
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Book>> loader) {
        // 重置loader,清理旧的数据
        mAdapter.clear();
    }

    //静态内部类，只分配一次内存，用来管理View实例，避免进行多次findViewById。
    static class ViewHolder {
        ListView booksListView;
        TextView mEmptyStateView;
        EditText enterTitle;
        Button searchButton;
        View mLoading;
    }


}
