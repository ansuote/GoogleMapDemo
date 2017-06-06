package com.lkl.ansuote.demo.googlemapdemo.nearbysearch;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.lkl.ansuote.demo.googlemapdemo.R;
import com.lkl.ansuote.demo.googlemapdemo.base.mode.PlaceBean;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by huangdongqiang on 23/05/2017.
 */
public class PlacesAdapter extends BaseAdapter {
    private List<PlaceBean> mList;
    private Context mContext;
    private LayoutInflater mLayoutInflater;

    public PlacesAdapter(Context context) {
        mContext = context;
        if (null != mContext) {
            mLayoutInflater = LayoutInflater.from(mContext);
        }
    }

    public void setData(List<PlaceBean> list) {
        mList = list;
    }

    @Override
    public int getCount() {
        if (null != mList) {
            return mList.size();
        }
        return 0;
    }

    @Override
    public Object getItem(int position) {
        if (null != mList) {
            return mList.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;

        if (null == convertView) {
            convertView = mLayoutInflater.inflate(R.layout.item_nearby_search, null);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        setDataToView(viewHolder, position);

        return convertView;
    }

    private void setDataToView(ViewHolder viewHolder, int position) {
        if (null == mList || mList.size() <= 0) return;

        PlaceBean bean = mList.get(position);
        if (null != bean) {
            String name = bean.getPlaceName();
            String address = bean.getAddress();
            boolean isSelected = bean.isSelected();

            if (TextUtils.isEmpty(name)) {
                name = "";
            }

            if (TextUtils.isEmpty(address)) {
                address = "";
            }

            viewHolder.nameText.setText(name);
            viewHolder.addressText.setText(address);
            viewHolder.checkImage.setVisibility(isSelected ? View.VISIBLE : View.GONE);
        }
    }

    static final class ViewHolder {
        @BindView(R.id.text_place_name) public TextView nameText;
        @BindView(R.id.text_address) public TextView addressText;
        @BindView(R.id.image_icon) public ImageView checkImage;

        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
