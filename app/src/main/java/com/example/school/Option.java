package com.example.school;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.core.utilities.Tree;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.IntSupplier;
import java.util.stream.IntStream;

import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

public class Option extends AppCompatActivity {
    public static class RegisterGrades extends AppCompatActivity {
        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        protected void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.registergrades);
            setTitle("登記成績");

            final String[] choice = new String[1];
            EditText times = findViewById(R.id.times);

            String[] species = {"作業", "考試"};
            Spinner spinner = findViewById(R.id.species_spinner);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, species);
            spinner.setAdapter(adapter);
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    choice[0] = species[position];
                    Set<String> keys = dts.get(species[position]);
                    times.setText(String.valueOf(keys.stream().mapToInt(Integer::parseInt).max().orElse(0) + 1));
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });


            EditText scores_data = findViewById(R.id.scores_data);

            Button register = findViewById(R.id.grade_query);
            register.setOnClickListener(v -> {
                String stimes = times.getText().toString();

                if ("".equals(stimes)) {
                    Toast.makeText(getApplicationContext(), "次數不得為空", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (Integer.parseInt(stimes) < 1) {
                    Toast.makeText(getApplicationContext(), "次數錯誤", Toast.LENGTH_SHORT).show();
                    return;
                }
                if ("".equals(scores_data.getText().toString())) {
                    Toast.makeText(getApplicationContext(), "資料不得為空", Toast.LENGTH_SHORT).show();
                    return;
                }

                String[] scores = scores_data.getText().toString().split("\n");

                DatabaseReference reference;

                if (choice[0].equals("作業")) {
                    reference = work;
                } else
                    reference = exam;

                boolean b = true;

                if (!dts.containsKey(choice[0])) {
                    dts.put(choice[0], new TreeSet<>());
                    b = false;
                }

                ArrayList<Integer> last = new ArrayList<>();
                IntStream.rangeClosed(1, 35).forEach(last::add);

                try {
                    if (dts.get(choice[0]).contains(stimes) && b) {
                        new AlertDialog.Builder(RegisterGrades.this)
                                .setTitle("注意")
                                .setMessage("\n此筆資料已存在, 是否覆蓋")
                                .setPositiveButton("是", (dialog, which) -> {
                                    reference.child(stimes).removeValue();
                                    String[] temp;
                                    int number = 1;
                                    for (String score : scores) {
                                        temp = score.split(" ");
                                        if (temp.length == 1) {
                                            if (!"".equals(temp[0])) {
                                                if (temp[0].contains("."))
                                                    reference.child(stimes).child(String.valueOf(number)).setValue(Double.parseDouble(temp[0]));
                                                else
                                                    reference.child(stimes).child(String.valueOf(number)).setValue(Integer.parseInt(temp[0]));
                                            }
                                        } else if (temp[1].contains("."))
                                            reference.child(stimes).child(temp[0]).setValue(Double.parseDouble(temp[1]));
                                        else
                                            reference.child(stimes).child(temp[0]).setValue(Integer.parseInt(temp[1]));
                                        last.remove(Integer.valueOf(number));
                                        number++;
                                    }
                                    Toast.makeText(RegisterGrades.this, "已覆蓋", Toast.LENGTH_SHORT).show();
                                })
                                .setNegativeButton("否", (dialog, which) -> Toast.makeText(RegisterGrades.this, "取消覆蓋", Toast.LENGTH_SHORT).show())
                                .create()
                                .show();
                    } else {
                        String[] temp;
                        int number = 1;
                        for (String score : scores) {
                            temp = score.split(" ");
                            if (temp.length == 1) {
                                if (!"".equals(temp[0])) {
                                    if (temp[0].contains("."))
                                        reference.child(stimes).child(String.valueOf(number)).setValue(Double.parseDouble(temp[0]));
                                    else
                                        reference.child(stimes).child(String.valueOf(number)).setValue(Integer.parseInt(temp[0]));
                                }
                            } else if (temp[1].contains("."))
                                reference.child(stimes).child(temp[0]).setValue(Double.parseDouble(temp[1]));
                            else
                                reference.child(stimes).child(temp[0]).setValue(Integer.parseInt(temp[1]));
                            last.remove(Integer.valueOf(number));
                            number++;
                        }
                    }
                    scores_data.setText("");
                    new AlertDialog.Builder(RegisterGrades.this)
                            .setTitle("登記完成")
                            .setMessage("\n未登記到成績的:" + last.toString())
                            .setPositiveButton("ok", (dialog, which) -> {
                                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                                ClipData clip = ClipData.newPlainText("text label", scores_data.getText().toString());
                                clipboard.setPrimaryClip(clip);
                                Toast.makeText(RegisterGrades.this, "資料已複製到剪貼簿", Toast.LENGTH_SHORT).show();
                            })
                            .create()
                            .show();
                } catch (NumberFormatException nfe) {
                    Toast.makeText(getApplicationContext(), "資料有誤", Toast.LENGTH_SHORT).show();
                }
            });

            Button add = findViewById(R.id.grade_add);
            add.setOnClickListener(v -> {
                String stimes = times.getText().toString();

                if ("".equals(stimes)) {
                    Toast.makeText(getApplicationContext(), "次數不得為空", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (Integer.parseInt(stimes) < 1) {
                    Toast.makeText(getApplicationContext(), "次數錯誤", Toast.LENGTH_SHORT).show();
                    return;
                }

                if ("".equals(scores_data.getText().toString())) {
                    Toast.makeText(getApplicationContext(), "資料不得為空", Toast.LENGTH_SHORT).show();
                    return;
                }

                String[] scores = scores_data.getText().toString().split("\n");

                DatabaseReference reference;

                if (choice[0].equals("作業")) {
                    reference = work.child(stimes);
                } else
                    reference = exam.child(stimes);


                String[] temp = {};

                for (String score : scores) {
                    temp = score.split(" ");
                    reference.child(temp[0]).setValue(Integer.parseInt(temp[1]));
                }
                Toast.makeText(RegisterGrades.this, "已新增", Toast.LENGTH_SHORT).show();
                scores_data.setText("");
            });
        }
    }

    public static class QueryGrades extends AppCompatActivity {
        static class Score {
            String number;
            final ArrayList<String> scores;

            public Score(String number) {
                this.number = number;
                scores = new ArrayList<>();
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;

                Score score = (Score) o;

                return number.equals(score.number);
            }

            @Override
            public int hashCode() {
                return number != null ? number.hashCode() : 0;
            }

            @NonNull
            @Override
            public String toString() {
                StringBuilder sb = new StringBuilder();
                for (String s : scores)
                    sb.append(s).append(" | ");
                if (number.length() == 1) {
                    number = " " + number;
                }
                return number.concat("|    ").concat(sb.toString());
            }
        }

        static class All {
            ArrayList<Score> scores = new ArrayList<Score>();

            public void add(Score score) {
                scores.add(score);
            }

            public Score get(String index) {
                for (Score s : scores)
                    if (s.number.equals(index))
                        return s;
                return null;
            }

            public void clear() {
                scores.clear();
            }

            @Override
            public String toString() {
                return "All{" +
                        "scores=" + scores +
                        '}';
            }
        }

        static Map<String, Map<String, Map<String, String>>> allData = new HashMap<>();

        @Override
        protected void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.querygrade);
            setTitle("查詢成績");

            final String[] choice = new String[2];

            registerRef("考試", exam);
            registerRef("作業", work);

            ArrayList<String> arr_times = new ArrayList<>();
            arr_times.add("全部");
            Spinner times = findViewById(R.id.query_times);
            ArrayAdapter<String> adapter_times = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, arr_times);
            times.setAdapter(adapter_times);
            times.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    choice[1] = arr_times.get(position);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            String[] species = {"作業", "考試"};
            Spinner spinner = findViewById(R.id.query_species_spinner);
            ArrayAdapter<String> adapter_sp = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, species);
            spinner.setAdapter(adapter_sp);
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    choice[0] = species[position];
                    arr_times.clear();
                    arr_times.add("全部");
                    Set<Integer> temp = new TreeSet<>();
                    Set<String> arr = dts.get(choice[0]);
                    if (arr != null)
                        for (String s : arr)
                            temp.add(Integer.parseInt(s));
                    for (int i : temp)
                        arr_times.add(String.valueOf(i));
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            TextView data_show = findViewById(R.id.query_show);

            Button query = findViewById(R.id.grade_query);
            query.setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.N)
                @Override
                public void onClick(View v) {
                    String specie = choice[0];
                    String time = choice[1];
                    StringBuffer sb = new StringBuffer();

                    if (!time.equals("全部")) {
                        Map<String, String> scores = allData.get(specie).get(time);
                        String ans;
                        for (int i = 1; i <= 40; i++) {
                            if (String.valueOf(i).length() == 1)
                                sb.append(" ");
                            sb.append(i).append("          ");
                            ans = scores.getOrDefault(String.valueOf(i), "  ");
                            if (ans.length() == 2)
                                sb.append(" ");
                            sb.append(ans).append("\n");
                        }
                    } else {
                        All all = new All();
                        Map<String, Map<String, String>> scores1 = allData.get(specie);
                        if (scores1 == null) {
                            data_show.setText("查無資料");
                            return;
                        }
                        Map<String, String> scores2;
                        Iterator<String> iter = scores1.keySet().iterator();

                        for (int i = 1; i <= 40; i++) {
                            all.add(new Score(String.valueOf(i)));
                        }

                        Score temp;
                        String score;

                        while (iter.hasNext()) {
                            String key = iter.next();
                            scores2 = scores1.get(key);
                            for (int i = 1; i <= 40; i++) {
                                temp = all.get(String.valueOf(i));
                                score = scores2.getOrDefault(String.valueOf(i), "  ");
                                if (score.length() == 2)
                                    score = " " + score;
                                temp.scores.add(score);
                            }
                        }

                        for (Score s : all.scores) {
                            sb.append(s).append("\n");
                        }
                    }
                    data_show.setText(sb);
                }
            });
        }

        public static void registerRef(String key, DatabaseReference reference) {
            reference.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    Map<String, String> map;
                    if (allData.containsKey(key)) {
                        if (allData.get(key).containsKey(snapshot.getKey())) {
                            map = allData.get(key).get(snapshot.getKey());
                        } else {
                            map = new TreeMap<>();
                        }
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            map.put(ds.getKey(), ds.getValue().toString());
                        }
                        allData.get(key).put(snapshot.getKey(), map);
                    } else {
                        Map<String, Map<String, String>> at = new TreeMap<>();
                        Map<String, String> score = new TreeMap<>();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            score.put(ds.getKey(), ds.getValue().toString());
                        }
                        at.put(snapshot.getKey(), score);
                        allData.put(key, at);
                    }
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                    @SuppressLint("RestrictedApi") String path = snapshot.getRef().getPath().toString();
                    String type = "";
                    if (path.contains("作業"))
                        type = "作業";
                    else if (path.contains("考試"))
                        type = "考試";
                    allData.get(type).remove(snapshot.getKey());
                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    public static class AverageGrades extends AppCompatActivity {
        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        protected void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.averagegrades);
            setTitle("分數平均");

            final String[] choice = new String[1];

            QueryGrades.registerRef("考試", exam);
            QueryGrades.registerRef("作業", work);

            String[] species = {"作業", "考試", "全部"};
            Spinner spinner = findViewById(R.id.average_species_spinner);
            ArrayAdapter<String> adapter_sp = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, species);
            spinner.setAdapter(adapter_sp);
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    choice[0] = species[position];
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            EditText delcount = findViewById(R.id.average_delcount);

            TextView average_show = findViewById(R.id.average_show);

            Button start = findViewById(R.id.average_start);
            start.setOnClickListener(v -> {
                if ("".equals(delcount.getText().toString())) {
                    Toast.makeText(getApplicationContext(), "刪除數量不得為空", Toast.LENGTH_SHORT).show();
                    return;
                }

                int del = Integer.parseInt(delcount.getText().toString());

                if (del < 0) {
                    Toast.makeText(getApplicationContext(), "刪除數量不得小於0", Toast.LENGTH_SHORT).show();
                    return;
                }

                List<Double> scores;
                QueryGrades.All all = new QueryGrades.All();

                if (!choice[0].equals("全部")) {
                    Map<String, Map<String, String>> scores1 = QueryGrades.allData.get(choice[0]);

                    if (scores1 == null) {
                        average_show.setText("查無資料");
                        return;
                    }

                    Map<String, String> scores2;
                    Iterator<String> iter = scores1.keySet().iterator();

                    for (int i = 1; i <= 40; i++) {
                        all.add(new QueryGrades.Score(String.valueOf(i)));
                    }

                    QueryGrades.Score temp;
                    String score;

                    while (iter.hasNext()) {
                        String key = iter.next();
                        scores2 = scores1.get(key);
                        for (int i = 1; i <= 40; i++) {
                            temp = all.get(String.valueOf(i));
                            score = scores2.getOrDefault(String.valueOf(i), "0");
                            temp.scores.add(score);
                        }
                    }
                } else {
                    Map<String, Map<String, String>> score1 = QueryGrades.allData.get("考試");

                    if (score1 != null) {
                        Map<String, String> scores2;
                        Iterator<String> iter = score1.keySet().iterator();

                        for (int i = 1; i <= 40; i++) {
                            all.add(new QueryGrades.Score(String.valueOf(i)));
                        }

                        QueryGrades.Score temp;
                        String score;

                        while (iter.hasNext()) {
                            String key = iter.next();
                            scores2 = score1.get(key);
                            for (int i = 1; i <= 40; i++) {
                                temp = all.get(String.valueOf(i));
                                score = scores2.getOrDefault(String.valueOf(i), "0");
                                temp.scores.add(score);
                            }
                        }
                    }

                    score1 = QueryGrades.allData.get("作業");

                    if (score1 != null) {
                        Map<String, String> scores2;
                        Iterator<String> iter = score1.keySet().iterator();

                        if (all.scores.size() == 0) {
                            for (int i = 1; i <= 40; i++) {
                                all.add(new QueryGrades.Score(String.valueOf(i)));
                            }
                        }

                        QueryGrades.Score temp;
                        String score;

                        while (iter.hasNext()) {
                            String key = iter.next();
                            scores2 = score1.get(key);
                            for (int i = 1; i <= 40; i++) {
                                temp = all.get(String.valueOf(i));
                                score = scores2.getOrDefault(String.valueOf(i), "0");
                                temp.scores.add(score);
                            }
                        }
                    }
                }
                if (all.scores.get(0).scores.size() < del) {
                    new AlertDialog.Builder(AverageGrades.this)
                            .setTitle("錯誤")
                            .setMessage("\n刪除數量大於分數數量")
                            .setPositiveButton("ok", (dialog, which) -> delcount.setText(""))
                            .create()
                            .show();
                    return;
                }

                class Sorted {
                    String name;
                    double score;

                    public Sorted(String name, double score) {
                        this.name = name;
                        this.score = score;
                    }

                    public double getScore() {
                        return score;
                    }
                }

                List<Sorted> sets = new ArrayList<>();

                StringBuffer sb = new StringBuffer();
                AtomicReference<Double> ave = new AtomicReference<>((double) 0);
                for (QueryGrades.Score s : all.scores) {
                    scores = s.scores.parallelStream().mapToDouble(Double::parseDouble).sorted().collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
                    scores = scores.subList(del, scores.size());
                    scores.parallelStream().mapToDouble(Double::doubleValue).average().ifPresent(ave::set);
                    if (s.number.length() == 1)
                        sb.append("  ");
                    double avee = Double.parseDouble(String.format("%.1f", ave.get()));
                    sb.append(s.number).append("      ").append(avee).append("\n");
                    sets.add(new Sorted(s.number, avee));
                }
                average_show.setText(sb);

                Collections.sort(sets, Comparator.comparingDouble(Sorted::getScore).reversed());
                TextView view = new TextView(AverageGrades.this);
                view.setText("\n");
                view.setTextSize(16);
                for (int i = 0; i < 5; i++)
                    view.setText(view.getText().toString().concat(sets.get(i).name).concat("|  ").concat(String.valueOf(sets.get(i).score)).concat("\n"));

                new AlertDialog.Builder(AverageGrades.this)
                        .setTitle("最高分5個")
                        .setView(view)
                        .create()
                        .show();
            });
        }
    }

    public static class ToTableGrades extends AppCompatActivity {
        private boolean isPermissionPassed = false;

        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        protected void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            getPermission();
            if (isPermissionPassed) {
                make();
            }else
                getPermission();
        }

        private void getPermission() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
            } else {
                isPermissionPassed = true;
            }
        }

        @Override
        public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            if (requestCode == 100) {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    isPermissionPassed = true;
                } else {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this
                            , Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        Toast.makeText(this, "無權限寫入!", Toast.LENGTH_SHORT).show();
                        getPermission();
                    }
                }
            }
        }

        @RequiresApi(api = Build.VERSION_CODES.N)
        private void make() {
            Map<String, Map<String, String>> score1 = QueryGrades.allData.get("作業");
            QueryGrades.All all = new QueryGrades.All();

            if (score1 != null) {
                Map<String, String> scores2;
                Iterator<String> iter = score1.keySet().iterator();

                for (int i = 1; i <= 35; i++) {
                    all.add(new QueryGrades.Score(String.valueOf(i)));
                }

                QueryGrades.Score temp;
                String score;

                while (iter.hasNext()) {
                    String key = iter.next();
                    scores2 = score1.get(key);
                    for (int i = 1; i <= 35; i++) {
                        temp = all.get(String.valueOf(i));
                        score = scores2.getOrDefault(String.valueOf(i), "0");
                        temp.scores.add(score);
                    }
                }
            }

            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "成績.xls");

            file.delete();

            WritableWorkbook book = null;
            try {
                book = Workbook.createWorkbook(file);

                WritableSheet sheet = book.createSheet("作業", 0);

                //寫入內容
                int i = 1, j;
                for (i = 1; i <= all.scores.get(0).scores.size(); i++)  //title
                    sheet.addCell(new Label(i, 0, String.valueOf(i)));

                sheet.addCell(new Label(i+1, 0, "總分"));
                sheet.addCell(new Label(i+2, 0, "平均"));

                double sum = 0;
                for (i = 1; i <= all.scores.size(); i++, sum = 0) {
                    sheet.addCell(new Label(0, i, String.valueOf(i)));
                    for (j = 1; j <= all.scores.get(0).scores.size(); j++) {
                        sum += Double.parseDouble(all.scores.get(i - 1).scores.get(j - 1));
                        sheet.addCell(new Label(j, i, all.scores.get(i - 1).scores.get(j - 1)));
                    }
                    sheet.addCell(new Label(j+1, i, String.format("%.1f", sum)));
                    double ave = sum / j;
                    sheet.addCell(new Label(j+2, i, String.format("%.1f", ave)));
                }
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), "作業成績寫入出錯!", Toast.LENGTH_SHORT).show();
            }

//          ========================================================================================

            all = new QueryGrades.All();
            score1 = QueryGrades.allData.get("考試");

            if (score1 != null) {
                Map<String, String> scores2;
                Iterator<String> iter = score1.keySet().iterator();

                if (all.scores.size() == 0) {
                    for (int i = 1; i <= 35; i++) {
                        all.add(new QueryGrades.Score(String.valueOf(i)));
                    }
                }

                QueryGrades.Score temp;
                String score;

                while (iter.hasNext()) {
                    String key = iter.next();
                    scores2 = score1.get(key);
                    for (int i = 1; i <= 35; i++) {
                        temp = all.get(String.valueOf(i));
                        score = scores2.getOrDefault(String.valueOf(i), "0");
                        temp.scores.add(score);
                    }
                }
            }

            //操作執行
            try {
                if(book == null)
                    book = Workbook.createWorkbook(file);
                WritableSheet sheet = book.createSheet("考試", 1);

                //寫入內容
                int i, j;
                for (i = 1; i <= all.scores.get(0).scores.size(); i++)  //title
                    sheet.addCell(new Label(i, 0, String.valueOf(i)));

                sheet.addCell(new Label(i+1, 0, "總分"));
                sheet.addCell(new Label(i+2, 0, "平均"));

                double sum = 0;

                for (i = 1; i <= all.scores.size(); i++, sum = 0) {
                    sheet.addCell(new Label(0, i, String.valueOf(i)));
                    for (j = 1; j <= all.scores.get(0).scores.size(); j++) {
                        sum += Double.parseDouble(all.scores.get(i - 1).scores.get(j - 1));
                        sheet.addCell(new Label(j, i, all.scores.get(i - 1).scores.get(j - 1)));
                    }

                    sheet.addCell(new Label(j+1, i, String.format("%.1f", sum)));
                    double ave = sum / j;
                    sheet.addCell(new Label(j+2, i, String.format("%.1f", ave)));
                }
                //寫入資料
                book.write();
                //關閉檔案
                book.close();
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), "考試成績寫入出錯!", Toast.LENGTH_SHORT).show();
            }

            new AlertDialog.Builder(ToTableGrades.this)
                    .setTitle("匯出成功")
                    .setMessage("\n檔案已存於 DOCUMENTS 內")
                    .setPositiveButton("開啟檔案位置", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                            intent.putExtra(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getPath(), true);
                            intent.setType("*/*");
                            intent.addCategory(Intent.CATEGORY_OPENABLE);
                            startActivity(intent);
                        }
                    })
                    .create()
                    .show();

        }
    }

    final static Map<String, Set<String>> dts = new HashMap<>();
    static DatabaseReference exam, work;
    static String subject;
    static String user;
    static ArrayList<String> admin = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.option);
        setTitle("功能");

        String subject = getIntent().getExtras().getString("subject");

        FirebaseDatabase firebase = FirebaseDatabase.getInstance("https://school-eb60d.firebaseio.com/");
        exam = firebase.getReference(subject + "/考試");
        work = firebase.getReference(subject + "/作業");

        Option.subject = subject;

        registerRef("考試", exam);
        registerRef("作業", work);

        user = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        DatabaseReference tr = FirebaseDatabase.getInstance("https://school-eb60d.firebaseio.com/").getReference("config").child("auth").child(subject);
        tr.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren())
                    admin.add(ds.getValue().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        String[] options = {"登記成績", "查詢成績", "成績平均", "匯成Excel"};

        QueryGrades.registerRef("考試", exam);
        QueryGrades.registerRef("作業", work);

        ListView listView = findViewById(R.id.option_listview);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, options);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener((parent, view, position, id) -> {
            String s = options[position];
            Intent intent = new Intent();
            switch (s) {
                case "登記成績":
                    if (admin.size() != 0)
                        if (!admin.contains(user)) {
                            Toast.makeText(Option.this, "權限不足, 無法訪問", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    intent.setClass(Option.this, RegisterGrades.class);
                    break;
                case "查詢成績":
                    intent.setClass(Option.this, QueryGrades.class);
                    break;
                case "成績平均":
                    intent.setClass(Option.this, AverageGrades.class);
                    break;
                case "匯成Excel":
                    intent.setClass(Option.this, ToTableGrades.class);
                    break;
            }
            startActivity(intent);
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_delsub, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (admin.size() != 0) {
            if (!admin.contains(user)) {
                Toast.makeText(Option.this, "權限不足, 無法刪除", Toast.LENGTH_SHORT).show();
                return super.onOptionsItemSelected(item);
            }
        }

        DatabaseReference reference = FirebaseDatabase.getInstance("https://school-eb60d.firebaseio.com/").getReference(subject);

        new AlertDialog.Builder(Option.this)
                .setTitle("刪除科目")
                .setMessage("\n將會刪除此科目的所有資料")
                .setPositiveButton("確定", (dialog, which) -> {
                    reference.removeValue();

                    dts.remove(subject);

                    Intent intent = new Intent(Option.this, MainActivity.class);
                    startActivity(intent);
                })
                .setNegativeButton("取消", (dialog, which) -> Toast.makeText(Option.this, "取消刪除科目", Toast.LENGTH_SHORT).show())
                .create()
                .show();


        return super.onOptionsItemSelected(item);
    }

    public void registerRef(String key, DatabaseReference reference) {
        reference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Set<String> arr;
                if (dts.containsKey(key)) {
                    arr = dts.get(key);
                } else {
                    arr = new TreeSet<>();
                }
                arr.add(snapshot.getKey());
                dts.put(key, arr);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                @SuppressLint("RestrictedApi") String path = snapshot.getRef().getPath().toString();
                String type = "";
                if (path.contains("作業"))
                    type = "作業";
                else if (path.contains("考試"))
                    type = "考試";

                Set<String> arr = dts.get(type);
                arr.remove(snapshot.getKey());
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
