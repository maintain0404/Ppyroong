package kr.com.laplace.talkingcarrot;

import android.Manifest;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import android.os.Handler;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.util.List;

public class StartActivity extends AppCompatActivity {
  ImageView logo_carrot;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_start);
    logo_carrot = findViewById(R.id.logo_carrot);
    checkPermission();


    Handler hd = new Handler();
    Log.d("START", "start main");
    hd.postDelayed(new splashhandler(), 3000);
  }

  protected void checkPermission(){
    PermissionListener permissionlistener = new PermissionListener() {

      @Override
      public void onPermissionGranted() {
        Toast.makeText(StartActivity.this, "권한 허가", Toast.LENGTH_SHORT).show();
      }

      @Override
      public void onPermissionDenied(List<String> deniedPermissions) {
        Toast.makeText(StartActivity.this, "권한 거부\n" + deniedPermissions.toString(), Toast.LENGTH_SHORT).show();

        //어플 완전 종료
        moveTaskToBack(true);
        finish();
        android.os.Process.killProcess(android.os.Process.myPid());
      }
    };

    TedPermission.with(this)
      .setPermissionListener(permissionlistener)
      .setRationaleMessage("앵무새랑 놀기 위해서는 녹음 권한이 필요해요")
      .setDeniedMessage("왜 거부하셨어요...\n하지만 [설정] > [권한] 에서 권한을 허용할 수 있어요.")
      .setPermissions(Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN,
        Manifest.permission.INTERNET, Manifest.permission.RECORD_AUDIO)
      .check();

  }

  private class splashhandler implements Runnable{
    public void run(){
      startActivity(new Intent(getApplication(), MainActivity.class)); //로딩이 끝난 후, ChoiceFunction 이동
      StartActivity.this.finish(); // 로딩페이지 Activity stack에서 제거
    }
  }
}
