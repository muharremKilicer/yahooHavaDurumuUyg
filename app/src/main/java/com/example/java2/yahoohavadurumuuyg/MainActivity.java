package com.example.java2.yahoohavadurumuuyg;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ImageView havaResim;
    private TextView sicaklik,durum,yer;
    private EditText sehir;

    WebView webres;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sicaklik=(TextView) findViewById(R.id.txtSicaklik);
        durum=(TextView) findViewById(R.id.txtDurum);
        yer=(TextView) findViewById(R.id.txtYer);
        sehir=(EditText) findViewById(R.id.txtSehir);
        webres=(WebView) findViewById(R.id.webView);
    }

    public void getir(View v){
        //SQL sorgusunu ve sonunda json kodunu yolluyoruz
        String gelenSehir=sehir.getText().toString().trim();
        String sql="select * from weather.forecast where woeid in (select woeid from geo.places(1) where text=\""+gelenSehir+",türkiye\") and u='c'";
        String url = "https://query.yahooapis.com/v1/public/yql?q="+sql+"&format=json";
        new jsonOku(url, this).execute();
        //Butona tıklandığında klavye gizlensin
        InputMethodManager inputManager = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);

        inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);
    }

    class jsonOku extends AsyncTask<Void, Void, Void> {

        private ProgressDialog pr;

        String url = "";
        String data = "";

        public jsonOku(String url, Activity ac) {
            this.url = url;
            pr = new ProgressDialog(ac);
            pr.setMessage("Yükleniyor, Lütfen Bekleyiniz...");
            pr.show();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute(); // bekleme durumu
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            try {
                //Bunu uzun uzun yazmak gerekiyor. Yan yana yazınca hata veiyor.
                JSONObject obj = new JSONObject(data);
                JSONObject q=obj.getJSONObject("query");
                int count=q.getInt("count");
                //Toast.makeText(getApplicationContext(), "varmı "+count, Toast.LENGTH_SHORT).show();
                if (count>0){
                    JSONObject r=q.getJSONObject("results");
                    JSONObject c=r.getJSONObject("channel");
                    JSONObject loc=c.getJSONObject("location");
                    JSONObject item=c.getJSONObject("item");
                    JSONObject con=item.getJSONObject("condition");

                    //Burada ileri günlere ait veriler var.
                    //Son 5 güne ait verileri getiriyoruz.
                    JSONArray fore=item.getJSONArray("forecast");
                    for (int i = 1; i < fore.length()-4 ; i++) {
                        JSONObject gun=fore.getJSONObject(i);
                        String date=gun.getString("date");
                        String lowHigh=gun.getString("low")+"-"+gun.getString("high");
                        String durum=gun.getString("text");
                        //Tarih parçalanıyor.
                        String[] parts = date.split(" ");
                        String part1 = parts[0]; // Sayısı gelse yeter
                        String part2 = parts[1]; // Ay da gelse iyi olur.
                        //İleri 5 güne ait olan verileri istersek ekrana yazıdırbilir.
                    }

                    //Resim burada saklı, almamız gerekiyor.
                    String des=item.getString("description");
                    //Resim'i bulmaya çalışalım.
                    String[] parts = des.split("img src=\"");
                    String part1 = parts[1]; // Baştaki yazıyı silelim.
                    String[] parts2 = part1.split("\"/>\n<BR");
                    String resurl = parts2[0]; // Sondaki yazıları silelim. Geriye resim url kalsın
                    //Resimurl yüklensin.
                    webres.loadUrl(resurl);

                    //temperature de alınablir F olan result channel
                    //Şehir ve ülke isimleri alındı.
                    String city=loc.getString("city");
                    String country=loc.getString("country");
                    String temp=con.getString("temp");
                    String text=con.getString("text");
                    yer.setText(city+" "+country);
                    sicaklik.setText(temp + " C ");
                    durum.setText(text);
                }else{
                    Toast.makeText(getApplicationContext(), "HATA !!!" +"\n"+"Bu şehir bulunamadı", Toast.LENGTH_SHORT).show();
                }

            } catch (JSONException e) {
                Toast.makeText(getApplicationContext(), "Json veri çekme hatası", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            //burada toast gibi buton gibi grafiksel yapıları çalıştıramayız.
            try {
                data = Jsoup.connect(url).timeout(30000).ignoreContentType(true).execute().body();
                //Log.d("Gelen Data : ", data);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                pr.dismiss(); // yükleniyor yazısını durdurur.
            }
            return null;
        }
    }

}
