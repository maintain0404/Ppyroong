//Motion of bird fluffing

#include <Servo.h>
#include <SoftwareSerial.h>

SoftwareSerial BluetoothSerial(2, 3);


//Left and Right wing
Servo left, right;
int lvalue = 0;
int rvalue = 0;
//lvalue and rvalue is each left and right wings degree

//Head movement
//Servo head;
Servo Hnod, Hside;

int Hsidevalue = 0;

String StrPersonSpeak;

//Eye LED
//#define EYE 11

unsigned long timer1_millis = 0;

void setup() {
  left.attach(9);
  right.attach(10);
  //pinMode(EYE, OUTPUT);
  Hside.attach(12);

  Hside.write(60);
  right.write(0);
  left.write(120);
  WingMoveOriginal();
  WingMoveOriginal();
  HeadLeftRight();
  Hside.write(60);
  right.write(0);
  left.write(120);
  //120~30 senter65
  
  ExcitedMotion();
  BluetoothSerial.begin(9600);
  Serial.begin(9600);

  
}

void WingMoveOriginal()
{
  for(lvalue = 100; lvalue > 0 ; lvalue--)
  {
    left.write(lvalue);
    right.write(100-lvalue);
    delay(3);
  }
  for(lvalue = 0; lvalue < 100; lvalue++){
  
    left.write(lvalue);
    right.write(100-lvalue);
    delay(3);
  }
  
}

void WingMoveLowerHalf()
{
  for(lvalue = 100; lvalue > 50 ; lvalue--)
  {
    left.write(lvalue);
    right.write(100-lvalue);
    delay(3);
  }
  for(lvalue = 50; lvalue < 100; lvalue++){
  
    left.write(lvalue);
    right.write(100-lvalue);
    delay(3);
  }
  
}

void WingMoveUpperHalf()
{
  for(lvalue = 50; lvalue > 0 ; lvalue--)
  {
    left.write(lvalue);
    right.write(100-lvalue);
    delay(3);
  }
  for(lvalue = 0; lvalue < 50; lvalue++){
  
    left.write(lvalue);
    right.write(100-lvalue);
    delay(3);
  }
}

//Head Movement Basic
void HeadLeftRight()
{
  for(Hsidevalue = 65; Hsidevalue > 30 ; Hsidevalue--)
  {
    Hside.write(Hsidevalue);
    
    delay(5);
  }
  for(Hsidevalue = 30; Hsidevalue < 120; Hsidevalue++){
    Hside.write(Hsidevalue);
    
    delay(5);
  }
  for(Hsidevalue = 120; Hsidevalue>65;Hsidevalue--){
    Hside.write(Hsidevalue);

    delay(5);
  }
}


//Func Motion
//In-Process


void ProcessMotion()
{
  //While in Process or Unavailable to understand the words
  //Flapping wings twice and head will be bent
  for (Hsidevalue = 65; Hsidevalue > 30; Hsidevalue--){
    Hside.write(Hsidevalue);
    delay(5);
  }
  WingMoveLowerHalf();
  WingMoveLowerHalf();
  //have to set it back to original by manual on main
}

//Excited Motion
void ExcitedMotion()
{
  //For some reason if the Parrot got excited
  //Flaps upper half and rocks it's head
  for(lvalue = 50; lvalue > 0 ; lvalue--)
  {
    left.write(lvalue);
    right.write(140-lvalue);
    Hsidevalue = map(lvalue,0,50,30,120);
    Hside.write(Hsidevalue);
    delay(3);
  }
  for(lvalue = 0; lvalue < 50; lvalue++){
  
    left.write(lvalue);
    right.write(140-lvalue);
    Hsidevalue = map(lvalue,0,50,30,120);
    Hside.write(Hsidevalue);
    delay(3);
  }
  for(lvalue = 50; lvalue > 0 ; lvalue--)
  {
    left.write(lvalue);
    right.write(140-lvalue);
    Hsidevalue = map(lvalue,0,50,30,120);
    Hside.write(Hsidevalue);
    delay(3);
  }
  for(lvalue = 0; lvalue < 50; lvalue++){
  
    left.write(lvalue);
    right.write(140-lvalue);
    Hsidevalue = map(lvalue,0,50,30,120);
    Hside.write(Hsidevalue);
    delay(3);
  }
  Hside.write(60);
  right.write(0);
  left.write(120);
}

//기본 답변시 행동
//그냥 퍼득이는게 좋을거 같은데 아이디어 받기 위해 주석처리 한글로함 ^__________^
void RegularAnswer()
{
  WingMoveOriginal();
  WingMoveOriginal();
}
/*
String readBluetooth()
{
  String str = "";
  char ch;

  while(BluetoothSerial.available()){
    ch = BluetoothSerial.read();
    str.concat(ch);
    delay(10);
    
  }
  Serial.println(str);
  return str;
}
*/


void loop() {
  //wing movement start
  //wings move identically on 0~50 angle
  //have to add condition
  //Temp condition == 90sec
  Hside.write(60);
  right.write(0);
  left.write(120);
  

  if(BluetoothSerial.available()){
    int IntPersonSpeaktype = int(BluetoothSerial.read());
    Serial.println(int(IntPersonSpeaktype));
 
    if(IntPersonSpeaktype==49){
      ProcessMotion();
      Hside.write(60);
      right.write(0);
      left.write(120);
    }
    
    else if(IntPersonSpeaktype==50)
    {
      ExcitedMotion();
      Hside.write(60);
      right.write(0);
      left.write(120);
     }
    else if(IntPersonSpeaktype==51)
    {
      RegularAnswer();
      Hside.write(60);
      right.write(0);
      left.write(120);
    }
      
     
    else{
      //RegularAnswer();
      Serial.println("ELSE");
      //답변 넣기
    }
    
  }

  
  
  

/*
  WingMoveOriginal();
  delay(1000);
  WingMoveLowerHalf();
  delay(1000);
  WingMoveUpperHalf();
  delay(1000);
  HeadLeftRight();
  delay(1000);
  ProcessMotion();
  delay(1000);
  ExcitedMotion();
  delay(1000);
  RegularAnswer();
  delay(1000);
  */
  
  //1. 음성 인식시 움직임
  //2. 날개와 같이 움직임
  //3. 랜덤으로 움직임
}
