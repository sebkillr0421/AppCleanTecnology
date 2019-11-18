package h2h.iot.panic;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class GraphActivity extends AppCompatActivity {

    private String clientId = MqttClient.generateClientId();
    private MqttAndroidClient client;
    private String topic ;
    private int qos = 1;
    private int count = 0, n = 0;
    private float flow = 0,Tamb=0,TFilter=0;
    private Boolean plotDate = true;
    private Thread thread;
    private LineChart lineChart;
    private float[] datasetFlujo = new float[20];
    private float[] datasetTamb = new float[20];
    private float[] datasetTFil = new float[20];
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);
        requestTopic();
        for(int i=0; i<20;i++){
            datasetFlujo[i] = 0;
            datasetTamb[i] = 0;
            datasetTFil[i] = 0;
        }

        drawLineChartFlow();
        drawLineChartTamb();
        drawLineChartTfilter();
        client = new MqttAndroidClient(this.getApplicationContext(),"tcp://m16.cloudmqtt.com:17872",clientId);
        try {
            MqttConnectOptions options = new MqttConnectOptions();
            options.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1);
            options.setCleanSession(false);
            options.setUserName("bjqlvjrp");
            options.setPassword("FXAW5K6WJH8G".toCharArray());
            IMqttToken token = client.connect(options);
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Toast.makeText(GraphActivity.this,"onSuccess",Toast.LENGTH_LONG).show();
                    try{
                        client.subscribe(topic,qos);
                    }catch (MqttException ex){
                        ex.printStackTrace();
                    }

                    client.setCallback(new MqttCallback() {
                        @Override
                        public void connectionLost(Throwable cause) {

                        }

                        @Override
                        public void messageArrived(String topic, MqttMessage message) throws Exception {

                            Toast.makeText(GraphActivity.this, message.toString(), Toast.LENGTH_LONG).show();
                            flow = Float.parseFloat(message.toString().substring(message.toString().indexOf("\"flow\":")+7,message.toString().indexOf("\"tamb\":")));
                            Tamb = Float.parseFloat(message.toString().substring(message.toString().indexOf("\"tamb\":")+7,message.toString().indexOf("\"Tfilter\":")));
                            TFilter = Float.parseFloat(message.toString().substring(message.toString().indexOf("\"Tfilter\":")+10,message.toString().indexOf("\"pressbar\":")));
                            if(n < 20) {
                                datasetFlujo[n] = flow;
                                datasetTamb[n] = Tamb;
                                datasetTFil[n] = TFilter;
                                n++;
                            }else{
                                Toast.makeText(GraphActivity.this, "hey!!",Toast.LENGTH_LONG).show();
                                for(int j=0; j<19; j++){
                                    datasetFlujo[j] = datasetFlujo[j+1];
                                    datasetTamb[j] = datasetTamb[j+1];
                                    datasetTFil[j] = datasetTFil[j+1];
                                }
                                datasetFlujo[19] = flow;
                                datasetTamb[19] = Tamb;
                                datasetTFil[19] = TFilter;
                            }
                            drawLineChartFlow();
                            drawLineChartTamb();
                            drawLineChartTfilter();

                        }

                        @Override
                        public void deliveryComplete(IMqttDeliveryToken token) {

                        }
                    });
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Toast.makeText(GraphActivity.this,"onFailure",Toast.LENGTH_LONG).show();
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }

    }



    private void requestTopic(){
        Bundle extras = getIntent().getExtras();
        topic = extras.getString("topic");
    }

    private void drawLineChartFlow() {
        lineChart = findViewById(R.id.flujoGra);
        List<Entry> lineEntries = getDataSetFlow();
        LineDataSet lineDataSet = new LineDataSet(lineEntries, "sss");
        lineDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
        lineDataSet.setHighlightEnabled(true);
        lineDataSet.setLineWidth(2);
        lineDataSet.setColor(Color.RED);
        lineDataSet.setCircleColor(Color.YELLOW);
        lineDataSet.setCircleRadius(1);
        lineDataSet.setCircleHoleRadius(1);
        lineDataSet.setDrawHighlightIndicators(true);
        lineDataSet.setHighLightColor(Color.RED);
        lineDataSet.setValueTextSize(12);
        lineDataSet.setValueTextColor(Color.DKGRAY);

        LineData lineData = new LineData(lineDataSet);
        lineChart.getDescription().setText("Flujo L/m");
        lineChart.getDescription().setTextSize(14);
        lineChart.setDrawMarkers(true);
        lineChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTH_SIDED);
        lineChart.animateY(1);
        lineChart.getXAxis().setGranularityEnabled(true);
        lineChart.getXAxis().setGranularity(1.0f);
        lineChart.getXAxis().setLabelCount(lineDataSet.getEntryCount());
        lineChart.setData(lineData);
    }

    private void drawLineChartTamb() {
        lineChart = findViewById(R.id.temGra);
        List<Entry> lineEntries = getDataSetTamb();
        LineDataSet lineDataSet = new LineDataSet(lineEntries, "Temperatura Ambiente [C]");
        lineDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
        lineDataSet.setHighlightEnabled(true);
        lineDataSet.setLineWidth(2);
        lineDataSet.setColor(Color.GREEN);
        lineDataSet.setCircleColor(Color.YELLOW);
        lineDataSet.setCircleRadius(1);
        lineDataSet.setCircleHoleRadius(1);
        lineDataSet.setDrawHighlightIndicators(true);
        lineDataSet.setHighLightColor(Color.RED);
        lineDataSet.setValueTextSize(12);
        lineDataSet.setValueTextColor(Color.DKGRAY);

        LineData lineData = new LineData(lineDataSet);
        lineChart.getDescription().setText("Grafica Temperatura Ambiente");
        lineChart.getDescription().setTextSize(14);
        lineChart.setDrawMarkers(true);
        lineChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTH_SIDED);
        lineChart.animateY(1);
        lineChart.getXAxis().setGranularityEnabled(true);
        lineChart.getXAxis().setGranularity(1.0f);
        lineChart.getXAxis().setLabelCount(lineDataSet.getEntryCount());
        lineChart.setData(lineData);
    }

    private void drawLineChartTfilter() {
        lineChart = findViewById(R.id.temFGra);
        List<Entry> lineEntries = getDataSetTfilter();
        LineDataSet lineDataSet = new LineDataSet(lineEntries, "Temperatura Filtro [C]");
        lineDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
        lineDataSet.setHighlightEnabled(true);
        lineDataSet.setLineWidth(2);
        lineDataSet.setColor(Color.YELLOW);
        lineDataSet.setCircleColor(Color.YELLOW);
        lineDataSet.setCircleRadius(1);
        lineDataSet.setCircleHoleRadius(1);
        lineDataSet.setDrawHighlightIndicators(true);
        lineDataSet.setHighLightColor(Color.RED);
        lineDataSet.setValueTextSize(10);
        lineDataSet.setValueTextColor(Color.DKGRAY);

        LineData lineData = new LineData(lineDataSet);
        lineChart.getDescription().setText("Grafica Temperatura Filtro");
        lineChart.getDescription().setTextSize(14);
        lineChart.setDrawMarkers(true);
        lineChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTH_SIDED);
        lineChart.animateY(1);
        lineChart.getXAxis().setGranularityEnabled(true);
        lineChart.getXAxis().setGranularity(1.0f);
        lineChart.getXAxis().setLabelCount(lineDataSet.getEntryCount());
        lineChart.setData(lineData);
    }

    private List<Entry> getDataSetFlow() {
        List<Entry> lineEntries = new ArrayList<Entry>();
        lineEntries.add(new Entry(0, datasetFlujo[0]));
        lineEntries.add(new Entry(1, datasetFlujo[1]));
        lineEntries.add(new Entry(2, datasetFlujo[2]));
        lineEntries.add(new Entry(3, datasetFlujo[3]));
        lineEntries.add(new Entry(4, datasetFlujo[4]));
        lineEntries.add(new Entry(5, datasetFlujo[5]));
        lineEntries.add(new Entry(6, datasetFlujo[6]));
        lineEntries.add(new Entry(7, datasetFlujo[7]));
        lineEntries.add(new Entry(8, datasetFlujo[8]));
        lineEntries.add(new Entry(9, datasetFlujo[9]));
        lineEntries.add(new Entry(10, datasetFlujo[10]));
        lineEntries.add(new Entry(11, datasetFlujo[11]));
        lineEntries.add(new Entry(12, datasetFlujo[12]));
        lineEntries.add(new Entry(13, datasetFlujo[13]));
        lineEntries.add(new Entry(14, datasetFlujo[14]));
        lineEntries.add(new Entry(15, datasetFlujo[15]));
        lineEntries.add(new Entry(16, datasetFlujo[16]));
        lineEntries.add(new Entry(17, datasetFlujo[17]));
        lineEntries.add(new Entry(18, datasetFlujo[18]));
        lineEntries.add(new Entry(19, datasetFlujo[19]));


        return lineEntries;
    }

    private List<Entry> getDataSetTamb() {
        List<Entry> lineEntries = new ArrayList<Entry>();
        lineEntries.add(new Entry(0,  datasetTamb[0]));
        lineEntries.add(new Entry(1,  datasetTamb[1]));
        lineEntries.add(new Entry(2,  datasetTamb[2]));
        lineEntries.add(new Entry(3,  datasetTamb[3]));
        lineEntries.add(new Entry(4,  datasetTamb[4]));
        lineEntries.add(new Entry(5,  datasetTamb[5]));
        lineEntries.add(new Entry(6,  datasetTamb[6]));
        lineEntries.add(new Entry(7,  datasetTamb[7]));
        lineEntries.add(new Entry(8,  datasetTamb[8]));
        lineEntries.add(new Entry(9,  datasetTamb[9]));
        lineEntries.add(new Entry(10, datasetTamb[10]));
        lineEntries.add(new Entry(11, datasetTamb[11]));
        lineEntries.add(new Entry(12, datasetTamb[12]));
        lineEntries.add(new Entry(13, datasetTamb[13]));
        lineEntries.add(new Entry(14, datasetTamb[14]));
        lineEntries.add(new Entry(15, datasetTamb[15]));
        lineEntries.add(new Entry(16, datasetTamb[16]));
        lineEntries.add(new Entry(17, datasetTamb[17]));
        lineEntries.add(new Entry(18, datasetTamb[18]));
        lineEntries.add(new Entry(19, datasetTamb[19]));


        return lineEntries;
    }

    private List<Entry> getDataSetTfilter() {
        List<Entry> lineEntries = new ArrayList<Entry>();
        lineEntries.add(new Entry(0,  datasetTFil[0]));
        lineEntries.add(new Entry(1,  datasetTFil[1]));
        lineEntries.add(new Entry(2,  datasetTFil[2]));
        lineEntries.add(new Entry(3,  datasetTFil[3]));
        lineEntries.add(new Entry(4,  datasetTFil[4]));
        lineEntries.add(new Entry(5,  datasetTFil[5]));
        lineEntries.add(new Entry(6,  datasetTFil[6]));
        lineEntries.add(new Entry(7,  datasetTFil[7]));
        lineEntries.add(new Entry(8,  datasetTFil[8]));
        lineEntries.add(new Entry(9,  datasetTFil[9]));
        lineEntries.add(new Entry(10, datasetTFil[10]));
        lineEntries.add(new Entry(11, datasetTFil[11]));
        lineEntries.add(new Entry(12, datasetTFil[12]));
        lineEntries.add(new Entry(13, datasetTFil[13]));
        lineEntries.add(new Entry(14, datasetTFil[14]));
        lineEntries.add(new Entry(15, datasetTFil[15]));
        lineEntries.add(new Entry(16, datasetTFil[16]));
        lineEntries.add(new Entry(17, datasetTFil[17]));
        lineEntries.add(new Entry(18, datasetTFil[18]));
        lineEntries.add(new Entry(19, datasetTFil[19]));


        return lineEntries;
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


}
