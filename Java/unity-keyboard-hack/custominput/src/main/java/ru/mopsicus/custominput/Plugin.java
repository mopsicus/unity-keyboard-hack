package ru.mopsicus.custominput;


import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Build;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;

import com.unity3d.player.UnityPlayer;

import org.json.JSONException;
import org.json.JSONObject;

public class Plugin {

    JSONObject data;
    InputMethodManager inputMethodManager;
    View view = null;
    Activity context;
    boolean multiLinesMode = false;
    boolean checkHeight = true;
    ViewTreeObserver.OnGlobalLayoutListener listener;
    TextWatcher textWatcher;

    public Plugin () {
        view = UnityPlayer.currentActivity.getWindow().getCurrentFocus();
        data = new JSONObject();
        context =  UnityPlayer.currentActivity;
        inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        listener = new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Rect rect = new Rect();
                view.getWindowVisibleDisplayFrame(rect);
                int screenHeight = view.getRootView().getHeight();
                int keypadHeight = screenHeight - rect.bottom;
                SendData(1, keypadHeight);
                if (checkHeight) {
                    checkHeight = false;
                    return;
                }
                if (keypadHeight == 0)
                    close();

            }
        };
        textWatcher = new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!multiLinesMode) {
                    if (s.length() == 0) {
                        SendData(2, "");
                        return;
                    }
                    char lastchar = s.toString().charAt(s.length()-1);
                    if (lastchar == '\n')
                        close();
                    else
                        SendData(2, s.toString());
                } else
                    SendData(2, s.toString());
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void afterTextChanged(Editable s) {
            }
        };
    }

    public void show (final String text, final boolean mode) {
        checkHeight = true;
        multiLinesMode = mode;
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LayoutInflater inflater = context.getLayoutInflater();
                Resources resources = context.getResources();
                String packageName = context.getPackageName();
                int id = resources.getIdentifier("input", "layout", packageName);
                View view = inflater.inflate(id, null);
                FrameLayout.LayoutParams param = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
                param.setMargins(2000, 0, 0, 0); // move from screen
                context.addContentView(view, param);
                initText(text);
            }
        });
    }

    void initText (final String text) {
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Resources resources = context.getResources();
                String packageName = context.getPackageName();
                final EditText textArea = (EditText)context.findViewById(resources.getIdentifier("textArea", "id", packageName));
                textArea.setText(text);
                textArea.setBackgroundColor(0x00000000);
                textArea.setTextColor(0x00000000);
                textArea.addTextChangedListener(textWatcher);
                textArea.setFocusableInTouchMode(true);
                textArea.requestFocus();
                textArea.setCursorVisible(false);
                if (!multiLinesMode)
                    textArea.setMaxLines(1);
//                textArea.setOnTouchListener(new View.OnTouchListener() {
//                    public boolean onTouch(View view, MotionEvent motionEvent) {
//                        close();
//                        return false;
//                    }
//                });
                inputMethodManager.showSoftInput(textArea, InputMethodManager.SHOW_IMPLICIT);
                view.getViewTreeObserver().addOnGlobalLayoutListener(listener);
            }
        });
    }

    public void close () {
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        SendData(0, null);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            view.getViewTreeObserver().removeOnGlobalLayoutListener(listener);
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Resources resources = context.getResources();
                String packageName = context.getPackageName();
                EditText textArea = (EditText) context.findViewById(resources.getIdentifier("textArea", "id", packageName));
                if (textArea != null) {
                    ViewGroup viewGroup = (ViewGroup) textArea.getParent();
                    viewGroup.removeView(textArea);
                }
            }
        });
    }

    void SendData (int code, Object info) {
        try {
            data.remove("code");
            data.remove("data");
            data.put("code", code);
            if (info != null)
                data.put("data", info);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        UnityPlayer.UnitySendMessage("Plugins", "OnCustomInputAction", data.toString());
    }


}
