package luoyi.android.com.wsfzzs;

import android.webkit.JavascriptInterface;

public class JavaAndJS {
    private OnResultListener mOnResultListener;

    @JavascriptInterface
    public void getHtml(String html) {
        if (mOnResultListener != null) mOnResultListener.onResult(html);

    }

    public void setOnResultListener(OnResultListener onResultListener) {
        mOnResultListener = onResultListener;
    }

    public interface OnResultListener {
        void onResult(String html);
    }
}
