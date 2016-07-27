package com.example.spreadtrummichaelwang.vollydemo;

import android.graphics.Bitmap;
import android.support.v4.util.LruCache;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

public class VollyDemoActivity extends AppCompatActivity {
    private String TAG = "michael";
    private RequestQueue requestQueue;
    private ImageView mImageView;
    private NetworkImageView mNetworImageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_volly_demo);

        mImageView = (ImageView) findViewById(R.id.im_image);
        mNetworImageView = (NetworkImageView) findViewById(R.id.niv_imgeview);
        
        requestQueue = Volley.newRequestQueue(this);

        //testStringRequest();
        //testJsonRequest();
        //testJsonArrayRequest();
        //testImageRequest();
        //testImageLoader();
        //testNetworkImageView();
        //testXMLRequest();//根据StringRequest 创建自己的XMLReuqest
        testGsonRequest();

    }

    private void testGsonRequest() {

        GsonRequest<Weather> gsonRequest = new GsonRequest<Weather>(
                "http://www.weather.com.cn/data/sk/101010100.html", Weather.class,
                new Response.Listener<Weather>() {
                    @Override
                    public void onResponse(Weather weather) {
                        WeatherInfo weatherInfo = weather.getWeatherinfo();
                        Log.d(TAG, "city is " + weatherInfo.getCity());
                        Log.d(TAG, "temp is " + weatherInfo.getTemp());
                        Log.d(TAG, "time is " + weatherInfo.getTime());
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, error.getMessage(), error);
            }
        });
        requestQueue.add(gsonRequest);

    }

    private void testXMLRequest() {
        MyXMLRequest myXMLRequest = new MyXMLRequest("http://flash.weather.com.cn/wmaps/xml/china.xml",
                new Response.Listener<XmlPullParser>() {
                    @Override
                    public void onResponse(XmlPullParser xmlPullParser) {
                        try {
                            int eventType = xmlPullParser.getEventType();
                            while (eventType != XmlPullParser.END_DOCUMENT) {
                                switch (eventType) {
                                    case XmlPullParser.START_TAG:
                                        String nodeName = xmlPullParser.getName();
                                        if ("city".equals(nodeName)) {
                                            String pName = xmlPullParser.getAttributeValue(0);
                                            Log.d(TAG, "pName is " + pName);
                                        }
                                        break;
                                }
                                eventType = xmlPullParser.next();
                            }
                        } catch (XmlPullParserException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Log.e("TAG", volleyError.getMessage(), volleyError);
            }
        });

        requestQueue.add(myXMLRequest);
    }

    /*  1. 创建一个RequestQueue对象。
        2. 创建一个ImageLoader对象。
        3. 在布局文件中添加一个NetworkImageView控件。
        4. 在代码中获取该控件的实例。
        5. 设置要加载的图片地址。*/
    private void testNetworkImageView() {
        ImageLoader imageLoader = new ImageLoader(requestQueue, new MyImageCache());

        mNetworImageView.setDefaultImageResId(R.drawable.image_init);
        mNetworImageView.setErrorImageResId(R.drawable.error);
        mNetworImageView.setImageUrl("http://www.linuxsky.cn/store/resource/img/hamigua.png",imageLoader);
    }


    /*          1. 创建一个RequestQueue对象。
                2. 创建一个ImageLoader对象。
                3. 获取一个ImageListener对象。
                4. 调用ImageLoader的get()方法加载网络上的图片。
                */
    private void testImageLoader() {

        //ImageLoader的构造函数接收两个参数，第一个参数就是RequestQueue对象，第二个参数是一个ImageCache对象
        //ImageCache起到图片缓存的作用,参考MyImageCache的实现
        ImageLoader imageLoader = new ImageLoader(requestQueue, new MyImageCache());
        //通过调用ImageLoader的getImageListener()方法能够获取到一个ImageListener对象，getImageListener()
        // 方法接收三个参数，第一个参数指定用于显示图片的ImageView控件，第二个参数指定加载图片的过程中显示的图片，
        // 第三个参数指定加载图片失败的情况下显示的图片
        ImageLoader.ImageListener imageListener = ImageLoader.getImageListener(mImageView,
                                                            R.drawable.image_loading,R.drawable.error);
        //get()方法接收两个参数，第一个参数就是图片的URL地址，第二个参数则是刚刚获取到的ImageListener对象。
        // 当然，如果你想对图片的大小进行限制，也可以使用get()方法的重载，指定图片允许的最大宽度和高度，如下所示：
        imageLoader.get("http://www.linuxsky.cn/store/resource/img/pingguo.png",imageListener);
    }

    private class MyImageCache implements ImageLoader.ImageCache{

        private LruCache<String, Bitmap> mCache;

        public MyImageCache() {
            int maxSize = 10 * 1024 * 1024; //最大缓存设置为10m
            mCache = new LruCache<String, Bitmap>(maxSize) {
                @Override
                protected int sizeOf(String key, Bitmap bitmap) {
                    return bitmap.getRowBytes() * bitmap.getHeight();
                }
            };
        }
        @Override
        public Bitmap getBitmap(String s) {
            Log.d(TAG, " getBitmap 1 ");
            return mCache.get(s);
        }

        @Override
        public void putBitmap(String s, Bitmap bitmap) {
            Log.d(TAG, " putBitmap 2 ");
            mCache.put(s, bitmap);
        }
    }

    private void testImageRequest() {

        ImageRequest imageRequest = new ImageRequest("http://www.linuxsky.cn/store/resource/img/pingguo.png",
                new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap bitmap) {
                        mImageView.setImageBitmap(bitmap);
                    }
                }, 0, 0, Bitmap.Config.RGB_565, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                mImageView.setImageResource(R.drawable.error);
            }
        });

        requestQueue.add(imageRequest);
    }

    private void testJsonArrayRequest() {

        Log.d(TAG, " testJsonArrayRequest in ");

        JsonArrayRequest jsonOR = new JsonArrayRequest("http://www.linuxsky.cn/store/interface/goodjson.txt",
                new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray jsonArray) {
                Log.d(TAG, " jsonArray = " + jsonArray.toString());
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Log.d(TAG, " VolleyError = " + volleyError.getMessage(),volleyError);
            }
        });

        requestQueue.add(jsonOR);
    }

    private void testJsonRequest() {

        Log.d(TAG, " testJsonRequest in ");

        JsonObjectRequest jsonOR = new JsonObjectRequest("http://www.linuxsky.cn/store/interface/goodjson.txt",
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {
                        Log.d(TAG, " jsonObject = " + jsonObject.toString());
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        Log.d(TAG, " VolleyError = " + volleyError.getMessage(),volleyError);
                    }
        });

        requestQueue.add(jsonOR);
    }

    private void testStringRequest() {
        StringRequest stringRequest = new StringRequest("http://www.baidu.com",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        Log.d(TAG, " Response = " + s);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        Log.d(TAG, " VolleyError = " + volleyError.getMessage());
                    }
        });

        requestQueue.add(stringRequest);
    }
}
