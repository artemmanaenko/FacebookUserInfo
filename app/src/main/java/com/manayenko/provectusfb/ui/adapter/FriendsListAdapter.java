package com.manayenko.provectusfb.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.manayenko.provectusfb.R;
import com.manayenko.provectusfb.model.Friend;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by Artem on 29.11.2014.
 */
public class FriendsListAdapter extends BaseAdapter {
    private Context context;

    private List<Friend> friendsList;

    public FriendsListAdapter(Context context) {
        this.context = context;
    }

    @Override
    public int getCount() {
        return friendsList != null ? friendsList.size() : 0;
    }

    @Override
    public Friend getItem(int position) {
        return friendsList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.list_item_friend, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.photo = (ImageView) convertView.findViewById(R.id.friend_photo);
            viewHolder.name = (TextView) convertView.findViewById(R.id.friend_name);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Friend user = getItem(position);

        Picasso.with(context).load(user.getPhotoUrl()).into(viewHolder.photo);

        viewHolder.name.setText(user.getName());

        return convertView;
    }

    public void setFriendsList(List<Friend> friendsList) {
        this.friendsList = friendsList;
        notifyDataSetChanged();
    }

    private class ViewHolder {
        ImageView photo;
        TextView name;
    }
}
