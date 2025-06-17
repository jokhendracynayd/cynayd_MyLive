package com.livestreaming.live.bean;

public class ChangeRoomBackBean  {
          int id=0;
          String name="";
          String thumb="";
          String swf="";
          String swftime="" ;
          int needcoin =0;
          int score=0;
          int list_order=0;
          long addtime=0;
          String words="'";
          int uptime=0;
          String name_en="";
          String words_en="";
          String type="";
          public ChangeRoomBackBean(){

          }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getThumb() {
        return thumb;
    }

    public void setThumb(String thumb) {
        this.thumb = thumb;
    }

    public String getSwf() {
        return swf;
    }

    public void setSwf(String swf) {
        this.swf = swf;
    }
}