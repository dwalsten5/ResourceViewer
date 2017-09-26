package com.alduran.doranwalsten.resourceviewer;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    @Bind(R.id.resourceListView)
    ListView resourceListView;

    boolean hasPermissions;
    private static final int PERMISSIONS_REQUEST_READ_STORAGE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        ArrayAdapter adapter = getResourceList();
        resourceListView.setAdapter(adapter);

        //Set the clicklistener
        resourceListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String filename = resourceListView.getAdapter().getItem(i).toString(); //Get the filename
                String folderPath_loader = "file:///" + Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + "/TechConnect/Resources/web/";
                String file_loader = folderPath_loader + filename;
                Intent intent = new Intent(MainActivity.this,ViewResourceActivity.class);
                intent.putExtra(ViewResourceActivity.EXTRA_HTML,file_loader);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        PERMISSIONS_REQUEST_READ_STORAGE);
            }
        }
    }


    private boolean checkPermissions() {
        return PackageManager.PERMISSION_GRANTED ==
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
    }

    public ArrayAdapter getResourceList() {
        if (checkPermissions()) {
            String folderPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + "/TechConnect/Resources/web/";
            //String folderPath = "file:///android_asset/";

            File yourDir = new File(folderPath);
            ArrayList<String> resourceList = new ArrayList<String>();
            Log.d(getClass().toString(),String.format("%d",yourDir.listFiles().length));
            for (File f : yourDir.listFiles()) {
                if (f.isFile() && f.getName().endsWith(".html")) {
                    Log.d(getClass().toString(),f.getName());

                    String file = folderPath + f.getName();
                    //Modify the HTML to ensure that images are correctly referenced
                    try {
                        Log.d(getClass().toString(),"HERE");
                        String raw_html = FileUtils.readFileToString(new File(file), "UTF-8");
                        resolveHtmlImages(raw_html,file);
                        resourceList.add(f.getName());
                    } catch (IOException e) {
                        Log.e(getClass().toString(),e.getMessage());
                    }
                }
            }

            return new ArrayAdapter(this,android.R.layout.simple_list_item_1,resourceList);
            /*
            // Get the HTML file name
            String fileName = "test_2.html";

            // Get the exact file location
            String file_loader = folderPath_loader + fileName;
            String file = folderPath + fileName;

            //Modify the HTML to ensure that images are correctly referenced
            try {
                Log.d(getClass().toString(),"HERE");
                String raw_html = FileUtils.readFileToString(new File(file), "UTF-8");
                //resolveHtmlImages(raw_html,file);
            } catch (IOException e) {
                Log.e(getClass().toString(),e.getMessage());
            }

            // Render the HTML file on WebView
            webView.loadUrl(file_loader);
            */
        } else {
            Toast.makeText(this,"Permissions Not Granted",Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    /**
     * The goal of this method is to parse an html file with images and point them to the correct directory where they will be stored
     *
     * @param html
     * @param file
     */
    private void resolveHtmlImages(String html,String file) {
        //Desired directory for all images
        String folderPath_loader = "file:///" + Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + "/TechConnect/Resources/web/";
        Document doc = Jsoup.parse(html);
        Elements imgs = doc.select("img");
        Log.d(getClass().toString(),"HERE");
        for (Element img : imgs) {
            String raw_src = img.attr("src");
            Log.d(getClass().toString(), raw_src);
            if (!raw_src.startsWith("file:///")) { //Not updated yet
                String new_src = String.format("%s/%s", folderPath_loader, raw_src);
                img.attr("src", new_src);
            }
        }
        //Write corrected result to the same file
        try {
            File f = new File(file);
            FileUtils.writeStringToFile(f, doc.outerHtml(), "UTF-8");
        } catch (IOException e) {
            Log.e(getClass().toString(),e.getMessage());
        }
    }
}
