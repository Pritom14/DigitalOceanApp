package in.tosc.doandroidlib;

import android.app.Activity;
import android.content.Intent;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.util.List;

import in.tosc.doandroidlib.api.DOLoginActivity;
import in.tosc.doandroidlib.api.DigitalOceanClient;
import in.tosc.doandroidlib.api.DigitalOceanStatisticsClient;
import in.tosc.doandroidlib.objects.Account;
import in.tosc.doandroidlib.objects.Action;
import in.tosc.doandroidlib.objects.Droplet;
import in.tosc.doandroidlib.objects.Image;
import in.tosc.doandroidlib.objects.Size;
import in.tosc.doandroidlib.serializer.ListDeserializer;
import in.tosc.doandroidlib.serializer.ObjectDeserializer;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import timber.log.Timber;

/**
 * Created by championswimmer on 26/11/16.
 */

public class DigitalOcean {

    public static final String TAG = "DO";
    public static final int LOGIN_ACT_INTENT_CODE = 3245;

    public static final int LOGIN_SUCCESS = 901;
    public static final int LOGIN_FAIL = 902;

    public static final String BASE_URL = "https://api.digitalocean.com/v2/";
    public static final String BASE_URL_V1 = "https://cloud.digitalocean.com/api/v1/";

    public static String getClientId() {
        return clientId;
    }

    static String clientId;

    public static String getCallbackUrl() {
        return callbackUrl;
    }

    static String callbackUrl;
    static String authToken = "";
    static Retrofit r, r2;

    public static void init (String clientId, String callbackUrl) {
        if (Timber.treeCount() == 0 && BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
        Timber.tag(TAG).d("init: clientId=%s callbackUrl=%s", clientId, callbackUrl);
        DigitalOcean.clientId = clientId;
        DigitalOcean.callbackUrl = callbackUrl;
    }

    public static void doLogin(Activity act) {
        Timber.tag(TAG).d("doLogin: launching DOLoginActivity");
        Intent loginIntent = new Intent(act, DOLoginActivity.class);
        act.startActivityForResult(loginIntent, 3245);
    }

    private static HttpLoggingInterceptor newLoggingInterceptor() {
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
            @Override
            public void log(String message) {
                Timber.tag(TAG).d(message);
            }
        });
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BASIC);
        return loggingInterceptor;
    }

    public static void onLoggedIn (String token) {
        Timber.tag(TAG).d("onLoggedIn: received auth token, building API clients");
        DigitalOcean.authToken = token;

        OkHttpClient httpClient = new OkHttpClient.Builder()
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Request request = chain.request().newBuilder()
                                .addHeader("Content-Type", "application/json")
                                .addHeader("Authorization", "Bearer " + authToken)
                                .build();
                        return chain.proceed(request);
                    }
                })
                .addInterceptor(newLoggingInterceptor())
                .build();


        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Account.class, new ObjectDeserializer<Account>("account"))
                .registerTypeAdapter(Action.class, new ObjectDeserializer<Action>("action"))
                .registerTypeAdapter(new TypeToken<List<Droplet>>(){}.getType(), new ListDeserializer<Droplet>("droplets"))
                .registerTypeAdapter(new TypeToken<List<Image>>(){}.getType(), new ListDeserializer<Image>("images"))
                .registerTypeAdapter(new TypeToken<List<Size>>(){}.getType(), new ListDeserializer<Size>("sizes"))
                .create();

        r = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(httpClient)
                .build();
        r2 = new Retrofit.Builder()
                .baseUrl(BASE_URL_V1)
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient)
                .build();
    }

    public static DigitalOceanClient getDOClient (final String authToken) {
        Timber.tag(TAG).d("getDOClient: requesting DigitalOceanClient instance");

        if(r == null){
            Timber.tag(TAG).d("getDOClient: no cached Retrofit instance, building a new one");
            OkHttpClient httpClient = new OkHttpClient.Builder()
                    .addInterceptor(new Interceptor() {
                        @Override
                        public Response intercept(Chain chain) throws IOException {
                            Request request = chain.request().newBuilder()
                                    .addHeader("Content-Type", "application/json")
                                    .addHeader("Authorization", "Bearer " + authToken)
                                    .build();
                            return chain.proceed(request);
                        }
                    })
                    .addInterceptor(newLoggingInterceptor())
                    .build();


            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(Account.class, new ObjectDeserializer<Account>("account"))
                    .registerTypeAdapter(Action.class, new ObjectDeserializer<Action>("action"))
                    .registerTypeAdapter(new TypeToken<List<Droplet>>(){}.getType(), new ListDeserializer<Droplet>("droplets"))
                    .registerTypeAdapter(new TypeToken<List<Image>>(){}.getType(), new ListDeserializer<Image>("images"))
                    .registerTypeAdapter(new TypeToken<List<Size>>(){}.getType(), new ListDeserializer<Size>("sizes"))
                    .create();

            r = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .client(httpClient)
                    .build();
        }
        return r.create(DigitalOceanClient.class);
    }

    public static DigitalOceanStatisticsClient getDOStatsClient (final String authToken) {
        Timber.tag(TAG).d("getDOStatsClient: requesting DigitalOceanStatisticsClient instance");
        if(r2 == null){
            Timber.tag(TAG).d("getDOStatsClient: no cached Retrofit instance, building a new one");
            OkHttpClient httpClient = new OkHttpClient.Builder()
                    .addInterceptor(new Interceptor() {
                        @Override
                        public Response intercept(Chain chain) throws IOException {
                            Request request = chain.request().newBuilder()
                                    .addHeader("Content-Type", "application/json")
                                    .addHeader("Authorization", "Bearer " + authToken)
                                    .build();
                            return chain.proceed(request);
                        }
                    })
                    .addInterceptor(newLoggingInterceptor())
                    .build();


            r2 = new Retrofit.Builder()
                    .baseUrl(BASE_URL_V1)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(httpClient)
                    .build();
        }
        return r2.create(DigitalOceanStatisticsClient.class);
    }




}
