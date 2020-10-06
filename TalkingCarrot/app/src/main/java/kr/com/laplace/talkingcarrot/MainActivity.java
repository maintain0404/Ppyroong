package kr.com.laplace.talkingcarrot;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.ParcelUuid;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Response;

import android.support.v7.app.AlertDialog;

public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {
  private final String TAG = "MAINACT";
  private final static int REQUEST_ENABLE_BT = 1;

  private final static int MODE_CHAT = 0;
  private final static int MODE_REPEAT = 1;

  //통신 관련
  private TextToSpeech tts;
  SimSimiTask task;

  //블루투스 관련
  private final static int BLUETOOTH_REQUEST_CODE = 100;
  BluetoothAdapter BAdapter;
  Set<BluetoothDevice> devices;
  BluetoothDevice bluetoothDevice;
  BluetoothSocket bluetoothSocket = null; // 블루투스 소켓
  OutputStream outputStream = null; // 블루투스에 데이터를 출력하기 위한 출력 스트림
  IntentFilter filter;
  BroadcastReceiver receiver;

  //녹음 관련
  Intent record_intent;
  SpeechRecognizer mRecognizer;

  //gui 관련
  Button record_start;
  TalkAdapter adapter;
  RecyclerView recyclerView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    recyclerView = findViewById(R.id.talkView);

    LinearLayoutManager layoutManager =
      new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
    recyclerView.setLayoutManager(layoutManager);
    adapter = new TalkAdapter();
    recyclerView.setAdapter(adapter);
    RecyclerDeco spaceDeco = new RecyclerDeco(20);


    adapter.addItem(new Talk(2, "나에게 말해줘"));

    tts = new TextToSpeech(this, this);
    tts.setPitch(2.0F);
    tts.setSpeechRate(1.2F);
    Set<String> a= new HashSet<>();
    a.add("male");
    Voice v= new Voice("en-us-x-sfg#male_2-local",new Locale("en","US"),400,200,true, a);
    tts.setVoice(v);

    record_intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
    record_intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());
    record_intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR");

    record_start = (Button) findViewById(R.id.record_button);
    record_start.setEnabled(false);
    record_start.setOnClickListener(new Button.OnClickListener() {
      @Override
      public void onClick(View view) {
        Log.d(TAG, "onclick success");
        mRecognizer = (SpeechRecognizer) SpeechRecognizer.createSpeechRecognizer(getBaseContext());
        Log.d(TAG, "contextOK");
        mRecognizer.setRecognitionListener(listener);
        mRecognizer.startListening(record_intent);
        record_start.setEnabled(false);
        record_start.setText("recording...");
      }
    });

    receiver = new BroadcastReceiver() {
      public void onReceive(Context context, Intent intent) {
        Log.d("BLUETOOTH", "intent On");
        String action = intent.getAction();
        switch (action) {
          case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
            Toast.makeText(getApplicationContext(), "블루투스 검색 시작", Toast.LENGTH_SHORT).show();
            break;
          //블루투스 디바이스 찾음
          case BluetoothDevice.ACTION_FOUND:
            //검색한 블루투스 디바이스의 객체를 구한다
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            //데이터 저장
            if (device.getName().compareTo("HEAD") == 0) {
              Toast.makeText(getApplicationContext(), "HEAD를 찾았습니다.", Toast.LENGTH_SHORT).show();
            } else {
              Toast.makeText(getApplicationContext(), "HEAD를 찾지 못했습니다.", Toast.LENGTH_SHORT).show();
            }
            break;
          //블루투스 디바이스 검색 종료
          case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
            Toast.makeText(getApplicationContext(), "블루투스 검색 종료", Toast.LENGTH_SHORT).show();
            break;

        }
      }
    };
    bluetoothSetup();
  }

  private RecognitionListener listener = new RecognitionListener() {
    @Override
    public void onRmsChanged(float rmsdB) {
      // TODO Auto-generated method stub

    }

    @Override
    public void onResults(Bundle results) {
      // TODO Auto-generated method stub
      Log.d(TAG, "onResults: failed");
      String key = "";
      key = SpeechRecognizer.RESULTS_RECOGNITION;
      ArrayList<String> mResult = results.getStringArrayList(key);
      String[] rs = new String[mResult.size()];
      mResult.toArray(rs);
      Log.d(TAG, "setText Clear");
      adapter.addItem(new Talk(1, ""+rs[0]));
      recyclerView.setAdapter(adapter);
      task = new SimSimiTask();
      task.execute("" + rs[0]);
    }

    @Override
    public void onReadyForSpeech(Bundle params) {
      // TODO Auto-generated method stub
    }

    @Override
    public void onPartialResults(Bundle partialResults) {
      // TODO Auto-generated method stub
    }

    @Override
    public void onEvent(int eventType, Bundle params) {
      // TODO Auto-generated method stub
    }

    @Override
    public void onError(int error) {
      // TODO Auto-generated method stub
      Toast.makeText(getApplicationContext(), "음성 인식 오류", Toast.LENGTH_SHORT).show();
      record_start.setText("Record");
      record_start.setEnabled(true);
    }

    @Override
    public void onEndOfSpeech() {
      // TODO Auto-generated method stub
    }

    @Override
    public void onBufferReceived(byte[] buffer) {
      // TODO Auto-generated method stub
    }

    @Override
    public void onBeginningOfSpeech() {
      // TODO Auto-generated method stub
    }
  };

  @Override
  public void onDestroy() {
    // Don't forget to shutdown tts!
    if (tts != null) {
      tts.stop();
      tts.shutdown();
    }
    super.onDestroy();
  }

  @Override
  public void onInit(int status) {

    if (status == TextToSpeech.SUCCESS) {

      int result = tts.setLanguage(Locale.KOREA);

      if (result == TextToSpeech.LANG_MISSING_DATA
        || result == TextToSpeech.LANG_NOT_SUPPORTED) {
        Log.e("TTS", "This Language is not supported");
      } else {
        Log.d("TTS", "Set OK");
      }

    } else {
      Log.e("TTS", "Initilization Failed!");
    }

  }

  private void speakOut(String speakText) {

    tts.speak(speakText, TextToSpeech.QUEUE_FLUSH, null);
  }

  public class SimSimiTask extends AsyncTask<String, String, String> {
    String inputed;
    char sentimentCode;

    protected void onPreExecute() {
      record_start.setEnabled(false);
      record_start.setText("Thinking..");
    }

    protected String doInBackground(String... utext) {
      try {
        OkHttpClient client = new OkHttpClient();

        MediaType mediaType = MediaType.parse("application/json");
        inputed = utext[0];
        RequestBody body = RequestBody.create(mediaType,
          "{\n\t\"utext\": \"" + utext[0] +
            "\", \n\t\"lang\": \"ko\", \n\t\"atext_bad_prob_max\": 0.3 \n}");
        Request request = new Request.Builder()
          .url("https://wsapi.simsimi.com/190410/talk/")
          .post(body)
          .addHeader("Content-Type", "application/json")
          .addHeader("x-api-key", "sGQNuyQ53LOuOpjDO0zTAi7sCOnNljnk7LbV0Ykf")
          .build();

        Response response = client.newCall(request).execute();
        if (response.code() != 200) {
          return "fail";
        } else {
          try {
            JSONObject result = new JSONObject(response.body().string());
            RequestBody body2 = RequestBody.create(mediaType,
              "{\n\t\"document\":{\"type\":\"PLAIN_TEXT\",\"content\":\"" + result.getString("atext") + "\"}}");
            Request request2 = new Request.Builder()
              .url("https://language.googleapis.com/v1/documents:analyzeSentiment?key=AIzaSyDi6N0WVzFJw7iuuz3xQShO0cwbS8AT0-o")
              .post(body2)
              .addHeader("Content-Type", "application/json")
              .build();

            Response response2 = client.newCall(request2).execute();
            if(response.code() != 200){
              return "fail";
            }else{
              try{
                JSONObject result2 = new JSONObject(response2.body().string());
                double score = result2.getJSONObject("documentSentiment").getDouble("score");
                double magnitude = result2.getJSONObject("documentSentiment").getDouble("magnitude");
                if(score > 0.4 && magnitude > 0.5){
                  sentimentCode = '1';
                  Log.d("SIMSIMI", "sentiment 1");
                }else if (score < 0){
                  sentimentCode = '3';
                  Log.d("SIMSIMI", "sentiment 2");
                }else{
                  sentimentCode = '2';
                  Log.d("SIMSIMI", "sentiment 3");
                }
              }catch(JSONException e){
                return "fail";
              }
            }
            return result.getString("atext");
          } catch (JSONException e) {
            return "fail";
          }
        }
      } catch (IOException e) {
        Log.d(TAG, "HTTP failed");
      }
      return "fail";
    }

    protected void onProgressUpdate(String... utext) {

    }

    protected void onPostExecute(String atext) {

      Log.d("SIMSIMI", atext);
      if(atext.compareTo("fail") == 0){
        Toast.makeText(getApplicationContext(), "네트워크 오류로 응답을 가져오지 못했습니다.", Toast.LENGTH_SHORT).show();
      }else {
        atext = deleteEmoticon(atext);
        speakOut(atext);
        record_start.setText("record");
        record_start.setEnabled(true);
        sendData(sentimentCode);
        adapter.addItem(new Talk(2, atext));
        recyclerView.setAdapter(adapter);
      }
    }
  }

  public void bluetoothSetup() {
    BAdapter = BluetoothAdapter.getDefaultAdapter();
    if (BAdapter == null) {
      // Device doesn't support Bluetooth
      Toast.makeText(getApplicationContext()
        , "장치가 블루투스를 지원하지 않습니다."
        , Toast.LENGTH_SHORT).show();
    } else {
      if (!BAdapter.isEnabled()) {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, BLUETOOTH_REQUEST_CODE);
      } else {
        connectDevice("HEAD");
      }
    }
  }

  public void connectDevice(String deviceName) {
    // 페어링 된 디바이스들을 모두 탐색
    devices = BAdapter.getBondedDevices();
    for (BluetoothDevice tempDevice : devices) {
      // 사용자가 선택한 이름과 같은 디바이스로 설정하고 반복문 종료
      if (deviceName.equals(tempDevice.getName())) {
        bluetoothDevice = tempDevice;
        Log.d("BLUETOOTH", "find device");
        break;
      }
    }
    //페어링되지 않았을 경우
    if (bluetoothDevice == null) {
      Log.d("BLUETOOTH", "discover start");
      // Register for broadcasts when a device is discovered.
      filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
      registerReceiver(receiver, filter);
      BAdapter.startDiscovery();
    } else {
      Log.d("BLUETOOTH", "pair start");
      // UUID 생성
      ParcelUuid uuid[] = bluetoothDevice.getUuids();
      UUID deviceUuid = UUID.fromString(uuid[0].toString());
      // Rfcomm 채널을 통해 블루투스 디바이스와 통신하는 소켓 생성
      try {
        bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(deviceUuid);
        bluetoothSocket.connect();
        // 데이터 송,수신 스트림을 얻어옵니다.
        outputStream = bluetoothSocket.getOutputStream();
        if (outputStream == null) {
          Log.d("BLUETOOTH", "ouputstream null");
        }
        //연결 성공했으니 버튼 ON
        Log.d("BLUETOOTH", "paired succeess");
        record_start.setEnabled(true);
      } catch (IOException e) {
        e.printStackTrace();
        Toast.makeText(getApplicationContext(), "블루투스 연결 실패, 삐룽이가 켜져 있는지 확인해주세요", Toast.LENGTH_SHORT).show();
        finish();
      }
    }
  }

  void sendData(char text) {
    // 문자열에 개행문자("\n")를 추가해줍니다.
    try {
      // 데이터 송신
      outputStream.write(text);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data){
    switch(requestCode){
      case BLUETOOTH_REQUEST_CODE:
        //블루투스 활성화 승인
        if(resultCode == Activity.RESULT_OK){

        }
        //블루투스 활성화 거절
        else{
          Toast.makeText(this, "블루투스를 활성화해야 합니다.", Toast.LENGTH_SHORT).show();
          finish();
          return;
        }
        break;
    }
  }

  public static String deleteEmoticon(String str){
    String match = "[^\uAC00-\uD7A3xfe0-9a-zA-Z\\s]";
    str =str.replaceAll(match, " ");
    return str;
  }
//  public void selectBluetoothDevice() {
//    // 이미 페어링 되어있는 블루투스 기기를 찾습니다.
//    devices = BAdapter.getBondedDevices();
//    // 페어링 된 디바이스의 크기를 저장
//    int pariedDeviceCount = devices.size();
//    // 페어링 되어있는 장치가 없는 경우
//    if(pariedDeviceCount == 0) {
//      // 페어링을 하기위한 함수 호출
//    }
//    // 페어링 되어있는 장치가 있는 경우
//    else {
//      // 디바이스를 선택하기 위한 다이얼로그 생성
//      AlertDialog.Builder builder = new AlertDialog.Builder(this);
//      builder.setTitle("페어링 되어있는 블루투스 디바이스 목록");
//      // 페어링 된 각각의 디바이스의 이름과 주소를 저장
//      List<String> list = new ArrayList<>();
//      // 모든 디바이스의 이름을 리스트에 추가
//      for(BluetoothDevice bluetoothDevice : devices) {
//        list.add(bluetoothDevice.getName());
//      }
//      list.add("취소");
//      // List를 CharSequence 배열로 변경
//      final CharSequence[] charSequences = list.toArray(new CharSequence[list.size()]);
//      list.toArray(new CharSequence[list.size()]);
//      // 해당 아이템을 눌렀을 때 호출 되는 이벤트 리스너
//      builder.setItems(charSequences, new DialogInterface.OnClickListener() {
//        @Override
//        public void onClick(DialogInterface dialog, int which) {
//          // 해당 디바이스와 연결하는 함수 호출
//          Log.d("BLUETOOTH",charSequences[which].toString());
//          connectDevice(charSequences[which].toString());
//        }
//      });
//
//      // 뒤로가기 버튼 누를 때 창이 안닫히도록 설정
//      builder.setCancelable(false);
//      // 다이얼로그 생성
//      AlertDialog alertDialog = builder.create();
//      alertDialog.show();
//    }
//  }

  // Create a BroadcastReceiver for ACTION_FOUND.
}
