package com.example.school;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicReference;

public class Option extends AppCompatActivity {
    public static class RegisterGrades extends AppCompatActivity {
        @Override
        protected void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.registergrades);
            setTitle("登記成績");

            final String[] choice = new String[1];

            String[] species = {"作業", "考試"};
            Spinner spinner = findViewById(R.id.species_spinner);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, species);
            spinner.setAdapter(adapter);
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    choice[0] = species[position];
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            EditText times = findViewById(R.id.times);

            EditText scores_data = findViewById(R.id.scores_data);

            Button register = findViewById(R.id.grade_query);
            register.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String stimes = times.getText().toString();

                    if("".equals(stimes)){
                        Toast.makeText(getApplicationContext(), "次數不得為空", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if(Integer.parseInt(stimes) < 1) {
                        Toast.makeText(getApplicationContext(), "次數錯誤", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if("".equals(scores_data.getText().toString())){
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

                    if(!dts.containsKey(choice[0])) {
                        dts.put(choice[0], new TreeSet<>());
                        b = false;
                    }

                    try {
                        if (dts.get(choice[0]).contains(stimes) && b) {
                            new AlertDialog.Builder(RegisterGrades.this)
                                    .setTitle("注意")
                                    .setMessage("\n此筆資料已存在, 是否覆蓋")
                                    .setPositiveButton("是", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
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
                                                number++;
                                            }
                                            Toast.makeText(RegisterGrades.this, "已覆蓋", Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .setNegativeButton("否", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            Toast.makeText(RegisterGrades.this, "取消覆蓋", Toast.LENGTH_SHORT).show();
                                        }
                                    })
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
                                number++;
                            }
                            Toast.makeText(getApplicationContext(), "已登記", Toast.LENGTH_SHORT).show();
                        }
                        scores_data.setText("");
                    }catch (NumberFormatException nfe){
                        Toast.makeText(getApplicationContext(), "資料有誤", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            Button add = findViewById(R.id.grade_add);
            add.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String stimes = times.getText().toString();

                    if(Integer.parseInt(stimes) < 1) {
                        Toast.makeText(getApplicationContext(), "次數錯誤", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if("".equals(scores_data.getText().toString())){
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
                }
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

            @Override
            public String toString() {
                StringBuffer sb = new StringBuffer();
                for(String s : scores)
                    sb.append(s).append(" | ");
                if(number.length() == 1){
                    number = " " + number;
                }
                return number.concat("|    ").concat(sb.toString());
            }
        }
        static class All {
            ArrayList<Score> scores = new ArrayList<Score>();
            public void add(Score score){
                scores.add(score);
            }
            public Score get(String index){
                for(Score s : scores)
                    if(s.number.equals(index))
                        return s;
                return null;
            }
            public void clear(){
                scores.clear();
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
                    Set<String> arr = dts.get(choice[0]);
                    Log.e("Tag", dts.toString());
                    if(arr != null)
                        arr_times.addAll(arr);
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

                    if(!time.equals("全部")) {
                        Map<String, String> scores = allData.get(specie).get(time);
                        String ans;
                        for(int i = 1;i <= 40;i++){
                            if(String.valueOf(i).length() == 1)
                                sb.append(" ");
                            sb.append(i).append("          ");
                            ans = scores.getOrDefault(String.valueOf(i), "  ");
                            if(ans.length() == 2)
                                sb.append(" ");
                            sb.append(ans).append("\n");
                        }
                    }else{
                        All all = new All();
                        Map<String, Map<String, String>> scores1 = allData.get(specie);
                        if(scores1 == null){
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
                    if(allData.containsKey(key)){
                        if(allData.get(key).containsKey(snapshot.getKey())){
                            map = allData.get(key).get(snapshot.getKey());
                        }else{
                            map = new TreeMap<>();
                        }
                        for(DataSnapshot ds : snapshot.getChildren()){
                            map.put(ds.getKey(), ds.getValue().toString());
                        }
                        allData.get(key).put(snapshot.getKey(), map);
                    }else{
                        Map<String, Map<String, String>> at = new TreeMap<>();
                        Map<String, String> score = new TreeMap<>();
                        for(DataSnapshot ds : snapshot.getChildren()){
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
        @Override
        protected void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.averagegrades);
            setTitle("分數平均");

            final String choice[] = new String[1];

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
            start.setOnClickListener(new View.OnClickListener() {
                @SuppressLint("DefaultLocale")
                @RequiresApi(api = Build.VERSION_CODES.N)
                @Override
                public void onClick(View v) {
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
                    }else{
                        Map<String, Map<String, String>> score1 = QueryGrades.allData.get("考試");

                        if(score1 != null) {
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

                        if(score1 != null) {
                            Map<String, String> scores2;
                            Iterator<String> iter = score1.keySet().iterator();

                            if(all.scores.size() == 0){
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
                                .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        delcount.setText("");
                                    }
                                })
                                .create()
                                .show();
                        return;
                    }

                    StringBuffer sb = new StringBuffer();
                    AtomicReference<Double> ave = new AtomicReference<>((double) 0);
                    for (QueryGrades.Score s : all.scores) {
                        scores = s.scores.parallelStream().mapToDouble(Double::parseDouble).sorted().collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
                        scores = scores.subList(del, scores.size());
                        scores.parallelStream().mapToDouble(Double::doubleValue).average().ifPresent(ave::set);
                        if (s.number.length() == 1)
                            sb.append("  ");
                        sb.append(s.number).append("      ").append(String.format("%.1f", ave.get())).append("\n");
                    }
                    average_show.setText(sb);
                }
            });
        }
    }

    final static Map<String, Set<String>> dts = new HashMap<>();
    static DatabaseReference exam, work;
    static String subject;
    static String[] user;

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

        user = new String[2];
        user[0] = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        DatabaseReference tr = FirebaseDatabase.getInstance("https://school-eb60d.firebaseio.com/").getReference("config").child("auth").child(subject);
        tr.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                user[1] = snapshot.getValue().toString();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        String[] options = {"登記成績", "查詢成績", "成績平均"};

        ListView listView = findViewById(R.id.option_listview);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, options);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String s = options[position];
                Intent intent = new Intent();
                switch (s){
                    case "登記成績":
                        if(user[1] != null)
                            if(!user[0].equals(user[1])) {
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
                }
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_delsub, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if(user[1] != null){
            if(!user[0].equals(user[1])){
                Toast.makeText(Option.this, "權限不足, 無法刪除", Toast.LENGTH_SHORT).show();
                return super.onOptionsItemSelected(item);
            }
        }

        String subject = getIntent().getExtras().getString("subject");

        DatabaseReference reference = FirebaseDatabase.getInstance("https://school-eb60d.firebaseio.com/").getReference(subject);
        reference.removeValue();

        dts.remove(subject);

        Intent intent = new Intent(Option.this, MainActivity.class);
        startActivity(intent);

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
