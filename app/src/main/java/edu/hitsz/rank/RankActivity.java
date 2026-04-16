package edu.hitsz.rank;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import edu.hitsz.GameDifficulty;
import edu.hitsz.MainActivity;
import edu.hitsz.R;

public class RankActivity extends AppCompatActivity {

    private RankDbHelper rankDbHelper;
    private ListView rankListView;
    private TextView emptyView;
    private RankAdapter rankAdapter;

    private Button filterAll;
    private Button filterEasy;
    private Button filterNormal;
    private Button filterHard;
    private Button activeFilter;

    private String currentDifficultyFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rank);
        setTitle(R.string.rank_title);

        rankDbHelper = new RankDbHelper(getApplicationContext());
        rankListView = findViewById(R.id.list_rank);
        emptyView = findViewById(R.id.text_rank_empty);
        rankAdapter = new RankAdapter(this);
        rankListView.setAdapter(rankAdapter);

        filterAll = findViewById(R.id.button_filter_all);
        filterEasy = findViewById(R.id.button_filter_easy);
        filterNormal = findViewById(R.id.button_filter_normal);
        filterHard = findViewById(R.id.button_filter_hard);

        filterAll.setOnClickListener(v -> applyFilter(null, filterAll));
        filterEasy.setOnClickListener(v -> applyFilter(GameDifficulty.EASY, filterEasy));
        filterNormal.setOnClickListener(v -> applyFilter(GameDifficulty.NORMAL, filterNormal));
        filterHard.setOnClickListener(v -> applyFilter(GameDifficulty.HARD, filterHard));

        Button backToMenu = findViewById(R.id.button_back_to_menu);
        backToMenu.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        applyFilter(null, filterAll);
    }

    @Override
    protected void onDestroy() {
        rankDbHelper.close();
        super.onDestroy();
    }

    private void applyFilter(String difficulty, Button selected) {
        currentDifficultyFilter = difficulty;
        highlightFilter(selected);
        refreshRankList();
    }

    private void highlightFilter(Button selected) {
        if (activeFilter != null) {
            activeFilter.setEnabled(true);
        }
        selected.setEnabled(false);
        activeFilter = selected;
    }

    private void refreshRankList() {
        List<RankRecord> records;
        if (currentDifficultyFilter == null) {
            records = rankDbHelper.queryAllOrderByScoreDesc();
        } else {
            records = rankDbHelper.queryByDifficulty(currentDifficultyFilter);
        }
        rankAdapter.setRecords(records);
        boolean hasRecords = !records.isEmpty();
        rankListView.setVisibility(hasRecords ? View.VISIBLE : View.GONE);
        emptyView.setVisibility(hasRecords ? View.GONE : View.VISIBLE);
    }

    private final class RankAdapter extends BaseAdapter {
        private final LayoutInflater inflater;
        private List<RankRecord> records = new ArrayList<>();

        private RankAdapter(Context context) {
            inflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return records.size();
        }

        @Override
        public RankRecord getItem(int position) {
            return records.get(position);
        }

        @Override
        public long getItemId(int position) {
            return getItem(position).getId();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.item_rank_record, parent, false);
                holder = new ViewHolder();
                holder.rankText = convertView.findViewById(R.id.text_rank_index);
                holder.scoreText = convertView.findViewById(R.id.text_rank_score);
                holder.metaText = convertView.findViewById(R.id.text_rank_meta);
                holder.deleteButton = convertView.findViewById(R.id.button_rank_delete);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            RankRecord record = getItem(position);
            holder.rankText.setText(String.format(Locale.getDefault(), "#%d", position + 1));
            holder.scoreText.setText(String.format(Locale.getDefault(), "分数：%d", record.getScore()));
            String usernameDisplay = record.getUsername().isEmpty() ? "" : "玩家：" + record.getUsername() + "\n";
            holder.metaText.setText(String.format(
                    Locale.getDefault(),
                    "%s难度：%s\n时间：%s",
                    usernameDisplay,
                    GameDifficulty.toDisplayName(record.getDifficulty()),
                    record.getPlayedAt()
            ));
            holder.deleteButton.setFocusable(false);
            holder.deleteButton.setOnClickListener(view -> {
                if (rankDbHelper.deleteById(record.getId()) > 0) {
                    refreshRankList();
                }
            });
            return convertView;
        }

        private void setRecords(List<RankRecord> records) {
            this.records = new ArrayList<>(records);
            notifyDataSetChanged();
        }
    }

    private static final class ViewHolder {
        private TextView rankText;
        private TextView scoreText;
        private TextView metaText;
        private Button deleteButton;
    }
}
