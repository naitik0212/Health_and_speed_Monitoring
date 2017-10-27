package com.example.mc.assignment2;

//Basic Android Imports
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.StaticLabelsFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

//Graph import
//Imports to connect to Server

public class MainActivity extends AppCompatActivity{

    private TextView patientID;
    private TextView patientName;
    private TextView patientAge;
    private RadioGroup patientSex;

    private GraphView graphView;

    private boolean isTableMade = false;

    private PatientData patientData;

    private int count = 0;


    private Handler handler1 = new Handler();

    private List<AccelerometerData> accelerometerDataList = new ArrayList<AccelerometerData>();

    LineGraphSeries<DataPoint> xplot;
    LineGraphSeries<DataPoint> yplot;
    LineGraphSeries<DataPoint> zplot;

    String tableName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        this.graphView = new GraphView(this);
        ViewGroup layout = (ViewGroup) findViewById(R.id.graph);
        this.graphView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        layout.addView(this.graphView);

        this.patientID = (TextView) findViewById(R.id.patientID);
        this.patientName = (TextView) findViewById(R.id.patientName);
        this.patientAge = (TextView) findViewById(R.id.patientAge);
        this.patientSex = (RadioGroup) findViewById(R.id.radioGroup);

        xplot = new LineGraphSeries<DataPoint>();
        yplot = new LineGraphSeries<DataPoint>();
        zplot = new LineGraphSeries<DataPoint>();

        xplot.setColor(Color.RED); //X - Accelerometer
        yplot.setColor(Color.BLUE); //Y - Accelerometer
        zplot.setColor(Color.GREEN); //Z- Accelerometer

        graphView.addSeries(xplot);
        graphView.addSeries(yplot);
        graphView.addSeries(zplot);


        StaticLabelsFormatter staticLabelsFormatter = new StaticLabelsFormatter(graphView);
        staticLabelsFormatter.setHorizontalLabels(new String[]{ "1", "2", "3", "4", "5", "6", "7", "8", "9", "10"});
        graphView.getGridLabelRenderer().setLabelFormatter(staticLabelsFormatter);


        Intent accelerometerSensorService = new Intent(MainActivity.this, AccelerometerService.class);
        startService(accelerometerSensorService);

    }


    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    public void onRunButtonClick(View view) {
        this.isTableMade = false;

        View v = this.getCurrentFocus();
        // to hide the keyboard when we click the run button
        if (v != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(this.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
        //if db has no table create one
        if (isTableMade == false) {
            getTableName();
            DBManager.commonInstance().createTable(tableName, this);
            this.isTableMade = true;
            this.handler1.postDelayed(updateGraph, 2);
        }
        else {
            graphPlot();
        }

    }

    // resets on clicking stop button
    public void onStopButtonClick(View view) {
        accelerometerDataList.clear();
        this.isTableMade = false;
        count = 0;
        xplot.resetData(new DataPoint[]{new DataPoint(0, 0)});
        yplot.resetData(new DataPoint[]{new DataPoint(0, 0)});
        zplot.resetData(new DataPoint[]{new DataPoint(0, 0)});
    }


    public void onUploadButtonClick(View view) {


        new UploadFile().execute(DBManager.commonInstance().databasePath());
    }
    //download database file from the server
    public void onDownloadButtonClick(View view) {
        String fileName = DBManager.commonInstance().databaseName();
        if (fileName == null) {
            getTableName();
            DBManager.commonInstance().createTable(tableName, this);
        }
        new DownloadFile().execute();
    }

    public void getTableName () {
        // boolean isMale is set here based on what is selected by the user, if not, default taken as Male

        int age=0;

        boolean isMale = true;
        if (this.patientSex.getCheckedRadioButtonId() == R.id.female) {
            isMale = false;
        }
        String id = this.patientID.getText().toString();
        String name = this.patientName.getText().toString();
        String ageValue = this.patientAge.getText().toString();
        if(ageValue!=null && !ageValue.isEmpty()){
            age = Integer.parseInt(ageValue);

        }else{
            tableName = null;
            Toast.makeText(this, "enter all values", Toast.LENGTH_SHORT).show();
       }

        this.patientData = new PatientData(id,name,age,isMale);

     if (age > 0){
         if(id != null && !id.isEmpty()){
             if(name != null && !name.isEmpty()){

                     String gender = "Female";
                     if (isMale) {
                         gender = "Male";
                     }
                     tableName = "Group17";
                 } else {
                 tableName = null;
                 Toast.makeText(this, "enter all values", Toast.LENGTH_SHORT).show();
             }
         }else {
             tableName = null;
             Toast.makeText(this, "enter all values", Toast.LENGTH_SHORT).show();
         }
     }else {
        tableName = null;
        Toast.makeText(this, "enter all values", Toast.LENGTH_SHORT).show();
    }}

    //used code
    private Runnable updateGraph = new Runnable() {
        public void run() {
            graphPlot();
            if (isTableMade) {
                handler1.postDelayed(updateGraph, 1000);
            }
        }
    };

    private void graphPlot() {

        if (this.isTableMade == true) {
            accelerometerDataList.clear();
            accelerometerDataList.addAll(0, DBManager.commonInstance().getAccelerometerData("",10));
        }
        DataPoint[] xValues = new DataPoint[accelerometerDataList.size()];
        DataPoint[] yValues = new DataPoint[accelerometerDataList.size()];
        DataPoint[] zValues = new DataPoint[accelerometerDataList.size()];

        for(int i=0; i< accelerometerDataList.size(); i++){
            xValues[i] = new DataPoint(i,accelerometerDataList.get(i).getX());
            yValues[i] = new DataPoint(i,accelerometerDataList.get(i).getY());
            zValues[i] = new DataPoint(i,accelerometerDataList.get(i).getZ());
        }
        if(accelerometerDataList.size() > 0) {
            xplot.resetData(xValues);
            yplot.resetData(yValues);
            zplot.resetData(zValues);
        }
    }


    private class UploadFile extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {
            int count = params.length;

            int bytesRead, bytesAvailable, bufferSize;
            byte[] buffer;
            int maxBufferSize = 1024;

            String urlOfServer = "http://10.218.110.136/CSE535Fall17Folder/UploadToServer.php";
            String lineEnd = "\r\n";
            String twoHyphens = "--";
            String boundary = "*****";

            InputStream input = null;
            DataOutputStream outputStream = null;

            HttpURLConnection con = null;

            //Code has been used from the website
            TrustManager[] tManager = new TrustManager[] { new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
                @Override
                public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                }
            } };

            try {
                SSLContext sslContext = SSLContext.getInstance("TLS");

                sslContext.init(null, tManager, new java.security.SecureRandom());

                //HttpURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                //URL to upload the file
                URL url = new URL(urlOfServer);
                String fileName = DBManager.commonInstance().databaseName();


                String filePathName = params[0];
                String fileUploadPathName = params[0] + "_upload";
                Log.d("fileName", fileName);



                //Making two directories to store Uploaded and Downloaded data
                File directory = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+File.separator+"/Android/Data/CSE535_ASSIGNMENT2");
                directory.mkdirs();
                copy(new File(filePathName),new File(directory,fileName));

                File directory1 = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+File.separator+"/Android/Data/CSE535_ASSIGNMENT2_EXTRA/");
                directory1.mkdirs();


                copy(new File(filePathName), new File(fileUploadPathName));

                //to upload the file we need to open the HTTP connection to the URL
                con = (HttpURLConnection) url.openConnection();

                con.setUseCaches(false);
                con.setDoOutput(true);
                con.setDoInput(true);

                con.setRequestMethod("POST");
                con.setRequestProperty("Connection", "Keep-Alive");
                con.setRequestProperty("ENCTYPE", "multipart/form-data");
                con.setRequestProperty("Content-Type", "multipart/form-data;boundary="+boundary);
                con.setRequestProperty("uploaded_file", fileName);

                outputStream = new DataOutputStream(con.getOutputStream());

                outputStream.writeBytes(twoHyphens + boundary + lineEnd);
                outputStream.writeBytes("Content-Disposition: form-data; name='uploaded_file';fileName='" +fileName+"'" + lineEnd);
                outputStream.writeBytes(lineEnd);

                FileInputStream fileInputStream = new FileInputStream(fileUploadPathName);

                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];

                bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                while (bytesRead > 0) {
                    try {
                        outputStream.write(buffer);
                    } catch (OutOfMemoryError e) {
                        e.printStackTrace();
                    }
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                }

                outputStream.writeBytes(lineEnd);
                outputStream.writeBytes(twoHyphens + boundary + twoHyphens
                        + lineEnd);

                //Closing all the streams
                outputStream.flush();
                outputStream.close();
                fileInputStream.close();

                //response code 200 corresponds to server status OK
                if (con.getResponseCode() == 200) {
                    return true;
                } else {
                    return false;
                }

            }
            catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }

        //Code to copy the file has been used from a website
        private void copy(File src, File dst) throws IOException {
            InputStream inputStream = new FileInputStream(src);
            OutputStream outputStream = new FileOutputStream(dst);

            // To Transfer bytes from inputStream to outputStream
            byte[] buf = new byte[1024];
            int len;
            while ((len = inputStream.read(buf)) > 0) {
                outputStream.write(buf, 0, len);
            }
            inputStream.close();
            outputStream.close();
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if (result) {
                Toast.makeText(MainActivity.this,"Upload Successful", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(MainActivity.this,"Upload Failed", Toast.LENGTH_LONG).show();
            }
        }
    }



    private class DownloadFile extends AsyncTask<Void, Void, Boolean> {

        String path = Environment.getExternalStorageDirectory().getAbsolutePath()+File.separator+"/Android/Data/CSE535_ASSIGNMENT2_EXTRA/"+tableName+"_downloaded";



        @Override
        protected Boolean doInBackground(Void... params) {

            Log.d(path,"path");

            String urlOfServer = "http://10.218.110.136/CSE535Fall17Folder/";
            InputStream inputStream = null;
            OutputStream outputStream = null;

            HttpURLConnection con = null;

            //Code to set trust Manager, used from website
            TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
                @Override
                public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                }
                @Override
                public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                }
            } };

            try {
                SSLContext sc = SSLContext.getInstance("TLS");

                sc.init(null, trustAllCerts, new java.security.SecureRandom());

                //HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            } catch (KeyManagementException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }

            try {
                URL url = new URL( urlOfServer + DBManager.commonInstance().databaseName());
                con = (HttpURLConnection) url.openConnection();
                con.connect();

                if (con.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    return false;
                }

                inputStream = con.getInputStream();
                outputStream = new FileOutputStream(path);

                byte data[] = new byte[4096];
                long total = 0;
                int count;
                while ((count = inputStream.read(data)) != -1) {
                    total += count;
                    outputStream.write(data, 0, count);
                }
                if (total > 0) {
                    return true;
                }
            } catch (Exception e) {
                return false;
            }
            finally
            {
                try {
                    if (outputStream != null)
                        outputStream.close();
                    if (inputStream != null)
                        inputStream.close();
                } catch (IOException ex) {
                    return false;
                }
                if (con != null)
                    con.disconnect();
            }
            return false;
        }

        @Override
        protected void onPreExecute() {

            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);

            if (result) {
                Toast.makeText(MainActivity.this,"Download Successful", Toast.LENGTH_LONG).show();
                List<AccelerometerData> acc = DBManager.commonInstance().getAccelerometerData(path, 10);
                MainActivity.this.isTableMade = false;
                accelerometerDataList.clear();
                accelerometerDataList.addAll(0, acc);
                graphPlot();
            } else {
                Toast.makeText(MainActivity.this,"Download Failed", Toast.LENGTH_LONG).show();
            }
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }
    }

}

class PatientData {

    private String name;
    private int age;
    private String id;
    private boolean isMale;


    public String getName() {
        return this.name;
    }

    public int getAge() {
        return this.age;
    }

    public String getId() {
        return this.id;
    }

    public boolean isMale() {
        return this.isMale;
    }


    public PatientData(String patientId, String patientName, int age, boolean isMale) {
        this.name = patientName;
        this.id = patientId;
        this.age = age;
        this.isMale = isMale;
    }
}

class AccelerometerData {

    private long timestamp;
    private float x;
    private float y;
    private float z;

    public void setX(float x) {
        this.x = x;
    }

    public void setZ(float z) {
        this.z = z;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }


    public float getZ() {

        return z;
    }



    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {

        this.timestamp = timestamp;
    }


}