package com.livestreaming.common.bean;

public class SelectionModel {
    public int value;
    public int afterValue;

   public boolean isSelected=false;
   public SelectionModel(int v,int afterValue){
       this.value=v;
       this.afterValue=afterValue;
   }
}
