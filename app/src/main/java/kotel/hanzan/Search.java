package kotel.hanzan;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.lcodecore.tkrefreshlayout.TwinklingRefreshLayout;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import kotel.hanzan.Data.PubInfo;
import kotel.hanzan.Data.StaticData;
import kotel.hanzan.listener.JRecyclerViewListener;
import kotel.hanzan.view.JRecyclerView;

import static kotel.hanzan.R.drawable.favorite;

public class Search extends AppCompatActivity {
    private ImageView back, searchIcon, deleteText, searchText;
    private EditText searchEditText;
    private LinearLayout initialPanel;
    private JRecyclerView recyclerView;

    private InputMethodManager inputMethodManager;

    private ArrayList<PubInfo> pubInfoArray;

    private class SearchRecyclerViewAdapter extends RecyclerView.Adapter<SearchRecyclerViewAdapter.ViewHolder> {
        RecyclerView.LayoutParams imageParams;
        private ViewHolder lastClickedViewHolder = null;
        private int lastClickedNumber;

        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView image, favorite;
            TextView text1, text2, text3;

            public ViewHolder(View itemView) {
                super(itemView);
                image = (ImageView) itemView.findViewById(R.id.pubitem_image);
                favorite = (ImageView) itemView.findViewById(R.id.pubitem_favorite);
                text1 = (TextView) itemView.findViewById(R.id.pubitem_text1);
                text2 = (TextView) itemView.findViewById(R.id.pubitem_text2);
                text3 = (TextView) itemView.findViewById(R.id.pubitem_text3);
            }
        }

        public void setFavoriteButton(boolean clicked) {
            try {
                pubInfoArray.get(lastClickedNumber).setFavorite(clicked);
                if (clicked) {
                    lastClickedViewHolder.favorite.setImageResource(R.drawable.favorite_clicked);
                } else {
                    lastClickedViewHolder.favorite.setImageResource(favorite);
                }
            } catch (Exception e) {
            }
        }

        public SearchRecyclerViewAdapter() {
            imageParams = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, StaticData.displayWidth / 2);
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.pubitem, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            PubInfo pubInfo = pubInfoArray.get(position);

            Picasso.with(getApplicationContext()).load(pubInfo.imageAddress[0]).into(holder.image);
            if (pubInfo.getFavorite()) {
                holder.favorite.setImageResource(R.drawable.favorite_clicked);
            } else {
                holder.favorite.setImageResource(favorite);
            }
            holder.text1.setText(pubInfo.name);
            holder.text2.setText(pubInfo.businessType);
            holder.text3.setText(pubInfo.address);


            holder.itemView.setOnClickListener(view -> {
                lastClickedViewHolder = holder;
                lastClickedNumber = position;
                Intent intent = new Intent(getApplicationContext(), PubPage.class);
                intent.putExtra("info", pubInfo);
                startActivityForResult(intent, PubPage.REQUEST_OPENPUBPAGE);
            });

        }

        @Override
        public int getItemCount() {
            return pubInfoArray.size();
        }
    }

    SearchRecyclerViewAdapter adapter;

    private String searchedWord = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        adapter = new SearchRecyclerViewAdapter();

        back = (ImageView) findViewById(R.id.search_back);
        searchIcon = (ImageView) findViewById(R.id.search_searchIcon);
        searchText = (ImageView) findViewById(R.id.search_search);
        deleteText = (ImageView) findViewById(R.id.search_searchDeleteText);
        searchEditText = (EditText) findViewById(R.id.search_searchEditText);
        initialPanel = (LinearLayout) findViewById(R.id.search_initialPanel);
        recyclerView = (JRecyclerView) findViewById(R.id.search_recycler);


        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.toString().length() != 0) {
                    searchText.setVisibility(View.VISIBLE);
                } else {
                    searchText.setVisibility(View.GONE);
                }
            }
        });

        searchIcon.setOnClickListener(view -> search(searchEditText.getText().toString()));

        back.setOnClickListener(view -> finish());

        searchText.setOnClickListener(view -> search(searchEditText.getText().toString()));

        deleteText.setOnClickListener(view -> {
            pubInfoArray = new ArrayList<>();
            adapter.notifyDataSetChanged();
            searchEditText.setText("");
            initialPanel.setVisibility(View.VISIBLE);
        });

        searchEditText.setOnEditorActionListener((textView, i, keyEvent) -> {
            search(searchEditText.getText().toString());
            return false;
        });

        recyclerView.setOnJRecyclerViewListener(new JRecyclerViewListener() {
            @Override
            public void onRefresh(TwinklingRefreshLayout refreshLayout) {
                search(searchedWord);
            }

            @Override
            public void onLoadMore() {

            }
        });
    }


    private void search(String word) {
        if (word.length() == 0) {
            Toast.makeText(this, "검색어를 입력해주세요", Toast.LENGTH_SHORT).show();
            return;
        }

        searchedWord = word;

        inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

        initialPanel.setVisibility(View.INVISIBLE);

        pubInfoArray = new ArrayList<>();

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }
}
