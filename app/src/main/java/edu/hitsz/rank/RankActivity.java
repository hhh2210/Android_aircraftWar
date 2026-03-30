package edu.hitsz.rank;

import android.content.Context;
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
import edu.hitsz.R;

public class RankActivity extends AppCompatActivity {

    private RankDbHelper rankDbHelper;
    private ListView rankListView;
    private TextView emptyView;
    private RankAdapter rankAdapter;

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
        refreshRankList();
    }

    @Override
    protected void onDestroy() {
        rankDbHelper.close();
        super.onDestroy();
    }

    private void refreshRankList() {
        List<RankRecord> records = rankDbHelper.queryAllOrderByScoreDesc();
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
            holder.metaText.setText(String.format(
                    Locale.getDefault(),
                    "难度：%s\n时间：%s",
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
