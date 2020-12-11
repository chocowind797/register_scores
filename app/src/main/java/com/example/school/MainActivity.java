package com.example.school;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.DownloadListener;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    public static class Relay extends AppCompatActivity {
        @Override
        protected void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.relay);

            setTitle("科目");

            ArrayList<String> types = getIntent().getStringArrayListExtra("types");

            if(types != null) {
                if (!types.contains("新增科目"))
                    types.add("新增科目");
                ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, types);

                ListView listView = findViewById(R.id.main_listview);
                listView.setAdapter(adapter);

                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        String subject = types.get(position);
                        if (subject.equals("新增科目")) {
                            EditText et = new EditText(Relay.this);

                            new AlertDialog.Builder(Relay.this).setTitle("新增科目")
                                    .setIcon(android.R.drawable.ic_input_add)
                                    .setView(et)
                                    .setPositiveButton("確定", (dialog, which) -> {
                                        String input = et.getText().toString();
                                        if ("".equals(input)) {
                                            Toast.makeText(Relay.this, "新增内容不能為空！", Toast.LENGTH_SHORT).show();
                                        } else {
                                            if (input.trim().equals("")) {
                                                Toast.makeText(Relay.this, "新增内容不能為空！", Toast.LENGTH_SHORT).show();
                                            } else {
                                                if (types.contains(input)) {
                                                    Toast.makeText(Relay.this, "科目已存在！", Toast.LENGTH_SHORT).show();
                                                } else {
                                                    types.add(0, input);
                                                    listView.setAdapter(adapter);
                                                    Toast.makeText(Relay.this, "已新增項目", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        }
                                    })
                                    .setNegativeButton("取消", (dialog, which) -> Toast.makeText(Relay.this, "取消新增", Toast.LENGTH_SHORT).show())
                                    .show();
                        } else {
                            Intent intent = new Intent(Relay.this, Option.class);
                            Bundle bundle = new Bundle();
                            bundle.putString("subject", types.get(position));
                            intent.putExtras(bundle);
                            startActivity(intent);
                        }
                    }
                });
            }
        }
    }

    public static final String version = "1.0.0";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        ArrayList<String> types = new ArrayList<>();

        final boolean[] check = {false};

        DatabaseReference reference = FirebaseDatabase.getInstance("https://school-eb60d.firebaseio.com/").getReference();
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds : snapshot.getChildren()) {
                    if(ds.getKey().equals("config")){
                        String v = ds.child("version").getValue().toString();
                        if(version.equals(v)){
                            check[0] = true;
                        }
                    }else {
                        types.add(ds.getKey());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        Button button = findViewById(R.id.relay);
        button.setOnClickListener(v -> {
            if(types.size() == 0) {
                Toast.makeText(MainActivity.this, "請重新點擊", Toast.LENGTH_SHORT).show();
                return;
            }
            if(check[0]) {
                Intent intent = new Intent(MainActivity.this, Relay.class);
                Bundle bundle = new Bundle();
                bundle.putStringArrayList("types", types);
                intent.putExtras(bundle);
                startActivity(intent);
            }else{
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("更新")
                        .setMessage("\n版本過舊,請更新")
                        .setPositiveButton("下載更新", (dialog, which) -> {

                            WebView webView = new WebView(MainActivity.this);
                            String url = "https://github.com/chocowind797/register_scores/blob/master/app/release/app-release.apk";
                            webView.loadUrl(url);
                            webView.setDownloadListener(new DownloadListener() {
                                @Override
                                public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                                    Uri uri = Uri.parse(url);
                                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                    startActivity(intent);
                                }
                            });
                        })
                        .create()
                        .show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_setting, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        FirebaseAuth.getInstance().signOut();

        Toast.makeText(this, "已登出", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(this, Activities.HomeActivity.class);
        startActivity(intent);

        return super.onOptionsItemSelected(item);
    }
}
