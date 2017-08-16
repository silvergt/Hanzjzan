package kotel.hanzan;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import kotel.hanzan.Data.PubInfo;
import kotel.hanzan.Data.StaticData;
import kotel.hanzan.view.JRecyclerView;

public class Search extends AppCompatActivity {
    private ImageView back, searchIcon, deleteText;
    private EditText searchEditText;
    private LinearLayout initialPanel;
    private JRecyclerView recyclerView;

    private InputMethodManager inputMethodManager;

    private ArrayList<PubInfo> pubInfoArray;
    private class SearchRecyclerViewAdapter extends RecyclerView.Adapter<SearchRecyclerViewAdapter.ViewHolder>{
        RecyclerView.LayoutParams imageParams;
        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView image,favorite;
            TextView text1,text2,text3;
            public ViewHolder(View itemView) {
                super(itemView);
                image = (ImageView)itemView.findViewById(R.id.pubitem_image);
                favorite = (ImageView)itemView.findViewById(R.id.pubitem_favorite);
                text1 = (TextView)itemView.findViewById(R.id.pubitem_text1);
                text2 = (TextView)itemView.findViewById(R.id.pubitem_text2);
                text3 = (TextView)itemView.findViewById(R.id.pubitem_text3);
                itemView.setOnClickListener(view -> {
                    Intent intent = new Intent(Search.this,PubPage.class);
                    startActivity(intent);
                });
            }
        }

        public SearchRecyclerViewAdapter() {
            imageParams = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, StaticData.displayWidth/2);
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.pubitem,parent,false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {

        }

        @Override
        public int getItemCount() {
            return pubInfoArray.size();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);

        back = (ImageView)findViewById(R.id.search_back);
        searchIcon = (ImageView)findViewById(R.id.search_searchIcon);
        deleteText = (ImageView)findViewById(R.id.search_searchDeleteText);
        searchEditText = (EditText)findViewById(R.id.search_searchEditText);
        initialPanel = (LinearLayout)findViewById(R.id.search_initialPanel);
        recyclerView = (JRecyclerView)findViewById(R.id.search_recycler);


        searchIcon.setOnClickListener(view -> search(searchEditText.getText().toString()));

        back.setOnClickListener(view -> finish());

        deleteText.setOnClickListener(view -> searchEditText.setText(""));

        searchEditText.setOnEditorActionListener((textView, i, keyEvent) -> {
            search(searchEditText.getText().toString());
            return false;
        });
    }


    private void search(String word){
        if(word.length()<2){
            Toast.makeText(this,"검색어는 최소 2글자 이상이어야 합니다",Toast.LENGTH_SHORT).show();
            return;
        }

        inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

        initialPanel.setVisibility(View.INVISIBLE);

        pubInfoArray = new ArrayList<>();

        pubInfoArray.add(new PubInfo()); //TEST

        SearchRecyclerViewAdapter adapter = new SearchRecyclerViewAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

    }
}
