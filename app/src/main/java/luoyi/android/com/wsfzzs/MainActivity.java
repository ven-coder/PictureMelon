package luoyi.android.com.wsfzzs;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.blankj.utilcode.constant.PermissionConstants;
import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.PathUtils;
import com.blankj.utilcode.util.PermissionUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.FileCallback;
import com.lzy.okgo.model.Progress;
import com.lzy.okgo.model.Response;
import com.lzy.okgo.request.base.Request;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements JavaAndJS.OnResultListener {

    private Button mBtn1;
    private TextView mTvText;
    private StringBuilder mLogStringBuilder = new StringBuilder();
    private X5WebView mWebView;
    private int downloadPosition = 0;
    private List<String> mUrls;
    private EditText mEtUrl;
    private String mPath;
    private NestedScrollView mScrollView;
    private Button mBtn2;
    private boolean isRunningParse = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initWebView();
        mBtn1.setOnClickListener(v -> parseData());
        mBtn2.setOnClickListener(v -> openFileManage());


        boolean granted = PermissionUtils.isGranted(PermissionConstants.STORAGE);
        if (!granted) {
            showPermissionRequest();
        }
    }

    private void showPermissionRequest() {
        new AlertDialog.Builder(this)
                .setCancelable(false)
                .setMessage("请先授予读写文件权限后使用\n(若一直出现此窗口请清除本APP所有数据后重试)")
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                }).setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                permission();
            }
        }).show();

    }

    private void permission() {
        PermissionUtils.permission(PermissionConstants.STORAGE).callback(new PermissionUtils.SimpleCallback() {
            @Override
            public void onGranted() {

            }

            @Override
            public void onDenied() {
                showPermissionRequest();
            }
        }).request();
    }

    private void openFileManage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mWebView.postDelayed(this::parseClipDataUrl, 1000);
    }

    private void parseClipDataUrl() {
        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData primaryClip = clipboardManager.getPrimaryClip();
        if (primaryClip != null) {
            ClipData.Item item = clipboardManager.getPrimaryClip().getItemAt(0);
            String url = item.getText().toString();
            if (!url.startsWith("http")) {
                return;
            }
            mEtUrl.setText(url);
            ToastUtils.showShort("链接已自动粘贴");
        }
    }

    private void parseData() {
        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData primaryClip = clipboardManager.getPrimaryClip();
        if (primaryClip != null) {
            ClipData.Item item = clipboardManager.getPrimaryClip().getItemAt(0);
            String url = item.getText().toString();
            String inputUrl = mEtUrl.getText().toString();
            if (!TextUtils.isEmpty(inputUrl)) {
                url = inputUrl;
            }
            if (!url.startsWith("http")) {
                ToastUtils.showShort("输入的链接或剪贴板的链接无效");
                return;
            }
            mEtUrl.setText(url);
            mWebView.loadUrl(url);
        } else {
            ToastUtils.showShort("剪贴板中无链接");
        }
    }

    private void initWebView() {
        mUrls = new ArrayList<>();
        mLogStringBuilder = new StringBuilder();
        JavaAndJS javaAndJS = new JavaAndJS();
        javaAndJS.setOnResultListener(this);
        mWebView.addJavascriptInterface(javaAndJS, "java_js");
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView webView, String s) {
                super.onPageFinished(webView, s);
                if (isRunningParse) return;
                isRunningParse = true;
                log("\n解析数据...");
                LogUtils.d("解析数据", s);
                mWebView.postDelayed(() -> mWebView.loadUrl("javascript:window.java_js.getHtml('<head>'+" +
                        "document.getElementsByTagName('html')[0].innerHTML+'</head>');"), 5000);
            }

            @Override
            public void onPageStarted(WebView webView, String s, Bitmap bitmap) {
                super.onPageStarted(webView, s, bitmap);
                mLogStringBuilder.delete(0, mLogStringBuilder.length());
                log("加载数据...");
            }
        });
    }

    private void log(String txt) {
        mLogStringBuilder.append(txt);
        mTvText.setText(mLogStringBuilder.toString());
        mScrollView.fullScroll(NestedScrollView.FOCUS_DOWN);
    }

    @Override
    public void onResult(String html) {
        Document document = Jsoup.parse(html);

        //检查是否是商品图
        Elements goodsElements = document.getElementsByClass("min-header-image");
        if (goodsElements.isEmpty()) {
            //检查是否是视频
            Elements videoElements = document.getElementsByClass("video___16lLY");
            if (!videoElements.isEmpty()) {
                //解析视频
                parseVideo(document);
            } else {
                //解析图片
                parseImage(document);
            }
        } else {
            //解析商品图片
            parseGoodsImage(document);
        }


    }

    /**
     * 解析视频
     */
    private void parseVideo(Document document) {
        Elements dirs = document.getElementsByClass("productName___3-D-Z");
        if (!dirs.isEmpty()) {
            Element element = dirs.get(0);
            mPath = PathUtils.getExternalDownloadsPath() + "/dewu/" + element.text()
                    .replace(" ", "_")
                    .replace("\"", "_") + "/";
        }
        mUrls.clear();
        String videoUrl = document.getElementsByClass("video___16lLY").get(0).attr("src");
        mUrls.add(videoUrl);
        if (mUrls.isEmpty()) {
            log("\n暂未解析到视频");
            isRunningParse = false;
            return;
        }
        downloadPosition = 0;
        download();
    }

    /**
     * 解析商品图片
     */
    private void parseGoodsImage(Document document) {

        Elements titles = document.getElementsByClass("product-title");
        if (!titles.isEmpty()) {
            Element element = titles.get(0);
            mPath = PathUtils.getExternalDownloadsPath() + "/dewu/" + element.text()
                    .replace(" ", "_")
                    .replace("\"", "_") + "/";
        }

        Elements elements = document.getElementsByClass("min-header-image");
        if (elements.isEmpty()) {
            log("\n暂未解析到图片");
            isRunningParse = false;
            return;
        }
        mUrls.clear();
        for (Element element : elements) {
            String img = element.getElementsByTag("img").get(0).attr("src").replace("?x-oss-process=image/format,webp", "");
            mUrls.add(img);

        }
        if (mUrls.isEmpty()) {
            log("\n暂未解析到图片");
            isRunningParse = false;
            return;
        }
        downloadPosition = 0;
        download();
    }

    /**
     * 解析普通图片
     */
    private void parseImage(Document document) {
        Elements dirs = document.getElementsByClass("productName___3-D-Z");
        if (!dirs.isEmpty()) {
            Element element = dirs.get(0);
            mPath = PathUtils.getExternalDownloadsPath() + "/dewu/" + element.text()
                    .replace(" ", "_")
                    .replace("\"", "_") + "/";
        }
        Elements elements = document.getElementsByClass("imgItem imgBlock___25ly1");
        if (elements.isEmpty()) {
            elements = document.getElementsByClass("swiper-wrapper");
        }
        mUrls.clear();
        for (Element element : elements) {
            String src = element.getElementsByTag("img")
                    .get(0).attr("src")
                    .replace("?x-oss-process=image/format,webp/resize,w_174/crop,w_174,h_174,g_center", "");
            mUrls.add(src);
//            runOnUiThread(() -> log("\n\n" + src));
        }
        if (mUrls.isEmpty()) {
            log("\n暂未解析到图片");
            isRunningParse = false;
            return;
        }
        downloadPosition = 0;
        download();
    }

    private void download() {
        FileUtils.createOrExistsDir(mPath);
        Uri uri = Uri.parse(mUrls.get(downloadPosition));
        LogUtils.d(uri, uri.getLastPathSegment());
        OkGo.<File>get(uri.toString()).execute(new FileCallback(mPath, uri.getLastPathSegment()) {

            private String mPreProgressStr = "";

            @Override
            public void downloadProgress(Progress progress) {
                super.downloadProgress(progress);
                String progressStr = new BigDecimal((double) progress.currentSize / progress.totalSize).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue() * 100 + "%";
                progressStr = "下载：" + (downloadPosition + 1) + "/" + mUrls.size() + "（" + progressStr + "）";
                StringBuilder replace = mLogStringBuilder.replace(
                        mLogStringBuilder.length() - mPreProgressStr.length(),
                        mLogStringBuilder.length(),
                        progressStr);
                mTvText.setText(replace.toString());
                mPreProgressStr = progressStr;
                Log.d("123456", progressStr);
            }

            @Override
            public void onStart(Request<File, ? extends Request> request) {
                super.onStart(request);
                mPreProgressStr = "下载：" + (downloadPosition + 1) + "/" + mUrls.size() + "（0%）";
                log("\n" + mPreProgressStr);
            }

            @Override
            public void onSuccess(Response<File> response) {
                String absolutePath = response.body().getAbsolutePath();
//                log("\n下载成功：" + (downloadPosition + 1) + "/" + mUrls.size());
                downloadPosition++;
                if (downloadPosition >= mUrls.size()) {
                    MediaScannerConnection.scanFile(MainActivity.this, new String[]{PathUtils.getExternalDownloadsPath() + "/dewu/"}, null, null);
                    isRunningParse = false;
                    return;
                }
                download();
            }

            @Override
            public void onError(Response<File> response) {
                super.onError(response);
                isRunningParse = false;
                log("\n下载失败：" + (downloadPosition + 1) + "/" + mUrls.size());
            }
        });
    }

    private void initView() {
        mBtn1 = (Button) findViewById(R.id.btn_1);
        mTvText = (TextView) findViewById(R.id.tv_text);
        mWebView = (X5WebView) findViewById(R.id.web_view);
        mEtUrl = (EditText) findViewById(R.id.et_url);
        mScrollView = (NestedScrollView) findViewById(R.id.scrollView);
        mBtn2 = (Button) findViewById(R.id.btn_2);
    }
}
