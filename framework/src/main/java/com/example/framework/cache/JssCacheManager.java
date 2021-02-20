package com.example.framework.cache;

import android.annotation.SuppressLint;

import androidx.arch.core.executor.ArchTaskExecutor;
import androidx.collection.ArrayMap;

import org.litepal.LitePal;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.Map;

public class JssCacheManager {

    //反序列,把二进制数据转换成java object对象
    private static Object toObject(byte[] data) {
        ByteArrayInputStream bais = null;
        ObjectInputStream ois = null;
        try {
            bais = new ByteArrayInputStream(data);
            ois = new ObjectInputStream(bais);
            return ois.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (bais != null) {
                    bais.close();
                }
                if (ois != null) {
                    ois.close();
                }
            } catch (Exception ignore) {
                ignore.printStackTrace();
            }
        }
        return null;
    }

    //序列化存储数据需要转换成二进制
    private static <T> byte[] toByteArray(T body) {
        ByteArrayOutputStream baos = null;
        ObjectOutputStream oos = null;
        try {
            baos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(baos);
            oos.writeObject(body);
            oos.flush();
            return baos.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (baos != null) {
                    baos.close();
                }
                if (oos != null) {
                    oos.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return new byte[0];
    }

    @SuppressLint("RestrictedApi")
    public static <T> void delete(final String key, final T body) {

        ArchTaskExecutor.getIOThreadExecutor().execute(() -> {
//            JssDataCache cache = new JssDataCache();
//            cache.key = key;
//            cache.data = toByteArray(body);
//            cache.save();
            LitePal.deleteAll(JssDataCache.class, "key=?", key);
//                JssCacheDatabase.get().getCache().delete(cache);
        });

    }

    @SuppressLint("RestrictedApi")
    public static <T> void deleteAll() {
        ArchTaskExecutor.getIOThreadExecutor().execute(() -> {
            LitePal.deleteAll(JssDataCache.class);
//                JssCacheDatabase.get().getCache().deleteAll();
        });

    }

    /**
     * 保存单个数据
     */
    @SuppressLint("RestrictedApi")
    public static <T> void save(final String key, final T body) {
        ArchTaskExecutor.getIOThreadExecutor().execute(() -> {
            JssDataCache cache = new JssDataCache();
            cache.key = key;
            cache.data = toByteArray(body);
            cache.saveOrUpdate("key=?",key);
//                JssCacheDatabase.get().getCache().save(cache);
        });

    }

    /**
     * 保存多个数据
     */
    @SuppressLint("RestrictedApi")
    public static <T> void saves(final Map<String, T> map) {
        ArchTaskExecutor.getIOThreadExecutor().execute(() -> {

            for (Map.Entry<String, T> pair : map.entrySet()) {
                JssDataCache cache = new JssDataCache();
                cache.key = pair.getKey();
                cache.data = toByteArray(pair.getValue());
                cache.save();
//                    JssCacheDatabase.get().getCache().save(cache);
            }

        });

    }

    /**
     * 从数据库中获取单个数据
     */
    @SuppressLint("RestrictedApi")
    public static void getCache(final String key, final QueryListener listener) {

        ArchTaskExecutor.getIOThreadExecutor()
                .execute(() -> {

//                        final JssDataCache cache = JssCacheDatabase.get().getCache().getCache(key);
                    List<JssDataCache> jssDataCaches = LitePal.where("key= ? ", key)
                            .find(JssDataCache.class);
                    if (jssDataCaches.isEmpty()) return;
                    final JssDataCache cache = jssDataCaches.get(0);
                    if (cache == null || cache.data == null) return;

                    final Object o = toObject(cache.data);

                    ArchTaskExecutor.getMainThreadExecutor()
                            .execute(() -> {
                                if (listener != null) {
                                    listener.onQuerySucceed(o);
                                }

                            });

                });


//        JssDataCache cache = JssCacheDatabase.get().getCache().getCache(key);
//        if (cache != null && cache.data != null) {
//            return toObject(cache.data);
//        }
//        return null;
    }

    /**
     * 从数据库中获取多个数据
     */
    @SuppressLint("RestrictedApi")
    public static void getCaches(final QueryListeners listeners, final String... keys) {
        ArchTaskExecutor.getIOThreadExecutor()
                .execute(() -> {

                    final Map<String, Object> map = new ArrayMap<>();

                    for (String key : keys) {
                        List<JssDataCache> jssDataCaches = LitePal.where("key=?", key)
                                .find(JssDataCache.class);
//                        final JssDataCache cache = JssCacheDatabase.get().getCache().getCache(key);
                        if (jssDataCaches.isEmpty()) continue;
                        final JssDataCache cache =jssDataCaches.get(0);
                        final Object o = toObject(cache.data);

                        map.put(key, o);
                    }

                    ArchTaskExecutor.getMainThreadExecutor()
                            .execute(() -> {
                                if (listeners != null) {
                                    listeners.onQuerySucceeds(map);
                                }
                            });
                });
    }


    public interface QueryListener {
        void onQuerySucceed(Object o);
    }

    public interface QueryListeners {
        void onQuerySucceeds(Map<String, Object> map);
    }

}