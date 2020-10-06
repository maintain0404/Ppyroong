package kr.com.laplace.talkingcarrot;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;



public class TalkAdapter extends RecyclerView.Adapter<TalkAdapter.ViewHolder>{
  ArrayList<Talk> items = new ArrayList<Talk>();

  public class ViewHolder extends RecyclerView.ViewHolder{
    TextView tv2;
    ImageView talker;
    Talk talkItem;

    public ViewHolder(@NonNull View itemView) {
      super(itemView);

      tv2 = itemView.findViewById(R.id.textView2);
      talker = itemView.findViewById(R.id.imageView);
      if(talkItem != null) {
        if (talkItem.getType() == 1) {
          talker.setImageResource(R.drawable.ic_launcher_foreground);
        } else {
          talker.setImageResource(R.drawable.carrot);
        }
      }
    }

    public void setItem(Talk item){
      //tv1.setText(item.getType());
      talkItem = item;
      if(talkItem.getType() == 1){
        talker.setImageResource(R.drawable.ic_launcher_foreground);
      }
      tv2.setText(item.getContent());
    }
  }

  @NonNull
  @Override
  public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType){
    LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
    View itemView = inflater.inflate(R.layout.talk_item, viewGroup, false);
    Log.d("RV", "recyclerview set");


    return new ViewHolder(itemView);
  }

  @Override
  public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position){
    Talk item = items.get(position);
    viewHolder.setItem(item);
  }

  @Override
  public int getItemCount(){
    return items.size();
  }

  public void addItem(Talk item){
    Log.d("RV", "ADDITEM/Now " + Integer.toString(getItemCount()));
    items.add(item);
  }


  public void setItems(ArrayList<Talk> items){
    this.items = items;
  }

  public Talk getItem(int position){
    return items.get(position);
  }

  public Talk setItem(int position, Talk item){
    return items.set(position, item);
  }


}


