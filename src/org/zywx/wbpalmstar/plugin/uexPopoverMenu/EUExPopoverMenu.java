/*
 * Copyright (c) 2015.  The AppCan Open Source Project.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */
package org.zywx.wbpalmstar.plugin.uexPopoverMenu;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.zywx.wbpalmstar.base.BUtility;
import org.zywx.wbpalmstar.base.ResoureFinder;
import org.zywx.wbpalmstar.engine.EBrowserView;
import org.zywx.wbpalmstar.engine.universalex.EUExBase;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class EUExPopoverMenu extends EUExBase {
    private static final String TAG = "EUExPopoverMenu";
    private ResoureFinder finder;
    private String bgColorStr = "#393A3F";
    private String dividerColorStr = "#191B1D";
    private String textColorStr = "#FFFFFF";
    private Integer textSize = 13;
    private int directionDef = 0;

    private boolean hasIcon = false;
    public static final String CALLBACK_ITEM_SELECTED = "uexPopoverMenu.cbItemSelected";

    public EUExPopoverMenu(Context context, EBrowserView eBrowserView) {
        super(context, eBrowserView);
        finder = ResoureFinder.getInstance(context);
    }

    /**
     * 打开popoverMenu, 弹出框的宽和高自适应
     * @param params
     */
    public void openPopoverMenu(String [] params) {
        if (params == null || params.length < 1) {
            errorCallback(0, 0, "error params !");
            return;
        }
        String json = params[0];
        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(json);
            if (!jsonObject.has("x") || !jsonObject.has("y")) {
                errorCallback(0, 0, "x and y can not be null !");
                Log.i(TAG, "Invalid Params. x and y can not be null !");
                return;
            }
            if (!jsonObject.has("direction")) {
                errorCallback(0, 0, "direction can not be null !");
                Log.i(TAG, "Invalid Params. direction can not be null!");
                return;
            }
            if (!jsonObject.has("data")) {
                errorCallback(0, 0, "data can not be null !");
                Log.i(TAG, "Invalid Params. data can not be null!");
                return;
            }
            double x = jsonObject.optDouble("x", 0);
            double y = jsonObject.optDouble("y", 0);
            int direction = jsonObject.optInt("direction", directionDef);
            String bgColor = jsonObject.optString("bgColor", bgColorStr);
            String dividerColor = jsonObject.optString("dividerColor", dividerColorStr);
            String textColor = jsonObject.optString("textColor", textColorStr);
            int textSizeVal = jsonObject.optInt("textSize", textSize);
            textSizeVal = DensityUtil.px2sp(mContext, textSizeVal);
            JSONArray  dataArray = jsonObject.getJSONArray("data");

            showPopoverMenu(x, y, direction, bgColor, dividerColor, textColor, textSizeVal, dataArray);

        } catch (JSONException e) {
            Log.i(TAG, e.getMessage());
        }
    }

    private void showPopoverMenu(double x, double y, int direction, String bgColorStr, String dividerColor, String textColor, int textSize, JSONArray data) {
        LinearLayout rootView = (LinearLayout) LayoutInflater.from(mContext).inflate(finder.getLayoutId("plugin_uex_popovermenu"),null);

        final PopupWindow popupWindow = new PopupWindow(rootView,
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, true);

        popupWindow.setContentView(rootView);
        //设置背景色
        rootView.setBackgroundColor(Color.parseColor(bgColorStr));
        popupWindow.setContentView(rootView);
        //为popupWindow设置背景，这样点击外部可以消失。
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        popupWindow.setOutsideTouchable(true);

        int length = data.length();
        List<ItemData> itemDataList = new ArrayList<ItemData>();
        try {
            for (int i = 0; i < length; i++) {
                ItemData item = new ItemData();
                String icon = data.getJSONObject(i).optString("icon", "");
                if (!hasIcon && !TextUtils.isEmpty(icon)) {
                    hasIcon =  true;
                }
                if (hasIcon) {
                    item.setIcon(data.getJSONObject(i).getString("icon"));
                }
                item.setText(data.getJSONObject(i).getString("text"));
                itemDataList.add(item);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < length; i ++) {
            LinearLayout linearLayout = new LinearLayout(mContext);
            linearLayout.setOrientation(LinearLayout.HORIZONTAL);
            linearLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));

            if (hasIcon) {
                ImageView imageView = new ImageView(mContext);
                imageView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                String imagePath = itemDataList.get(i).getIcon();
                imageView.setImageBitmap(getBitmapFromPath(imagePath));
                imageView.setPadding(DensityUtil.dip2px(mContext, 5), 0, 0, 0);
                linearLayout.addView(imageView);
            }
            TextView tvText= new TextView(mContext);
            LinearLayout.LayoutParams tvLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            tvLayoutParams.gravity = Gravity.CENTER_VERTICAL;
            tvText.setLayoutParams(tvLayoutParams);

            tvText.setPadding(DensityUtil.dip2px(mContext, 5), 0, DensityUtil.dip2px(mContext, 8), 0);
            tvText.setText(itemDataList.get(i).getText());
            tvText.setTextColor(Color.parseColor(textColor));
            tvText.setTextSize(textSize);
            linearLayout.addView(tvText);
            linearLayout.setTag(i);
            linearLayout.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    callBackPluginJs(CALLBACK_ITEM_SELECTED, String.valueOf(v.getTag()));
                    if (popupWindow != null && popupWindow.isShowing()) {
                        popupWindow.dismiss();
                    }
                }
            });
            rootView.addView(linearLayout);

            //添加分割线
            View dividerLine = new View(mContext);
            dividerLine.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, DensityUtil.dip2px(mContext, 1)));
            dividerLine.setBackgroundColor(Color.parseColor(dividerColor));
            rootView.addView(dividerLine);
        }
        int gravityParam;
        switch (direction) {
            case 0:
                gravityParam = Gravity.LEFT | Gravity.TOP;
                break;
            case 1:
                gravityParam = Gravity.RIGHT | Gravity.TOP;
                break;
            case 2:
                gravityParam = Gravity.LEFT | Gravity.BOTTOM;
                break;
            case 3:
                gravityParam = Gravity.RIGHT | Gravity.BOTTOM;
                break;
            default:
                gravityParam = Gravity.LEFT | Gravity.TOP;
                break;
        }
        popupWindow.showAtLocation(mBrwView.getRootView(), gravityParam, (int)x, (int)y);
    }

    private Bitmap getBitmapFromPath(String path) {
        path = BUtility.makeRealPath(path, mBrwView.getCurrentWidget().getWidgetPath(), mBrwView.getCurrentWidget().m_wgtType);


        Bitmap bitmap = null;
        InputStream is = null;
        try {
            if (path.startsWith(BUtility.F_Widget_RES_path)) {
                try {
                    is = mContext.getAssets().open(path);
                    if (is != null) {
                        bitmap = BitmapFactory.decodeStream(is);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (path.startsWith("/")) {
                bitmap = BitmapFactory.decodeFile(path);
            }
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return bitmap;
    }

    @Override
    protected boolean clean() {
        return false;
    }

    private void callBackPluginJs(String methodName, String jsonData){
        String js = SCRIPT_HEADER + "if(" + methodName + "){"
                + methodName + "('" + jsonData + "');}";
        onCallback(js);
    }

}
