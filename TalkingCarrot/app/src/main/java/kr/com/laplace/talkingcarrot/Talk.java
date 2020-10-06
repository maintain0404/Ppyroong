package kr.com.laplace.talkingcarrot;

public class Talk {
  int type;
  String content;

  public Talk(int type, String content){
    this.type = type;
    this.content = content;
  }

  public int getType() {
    return type;
  }

  public void setType(int type) {
    this.type = type;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

}
