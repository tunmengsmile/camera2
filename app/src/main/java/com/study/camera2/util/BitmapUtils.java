package com.study.camera2.util;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;

import com.study.camera2.MyApplication;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class BitmapUtils {


    private static final String TAG = BitmapUtils.class.getSimpleName();


    /**
     * Saves a Bitmap object to disk for analysis.
     *
     * @param bitmap   The bitmap to save.
     * @param filename The location to save the bitmap to.
     */
    public static void saveBitmap(final Bitmap bitmap, final String filename) {
        final String root = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "Download";
        final File myDir = new File(root);

        if (!myDir.mkdirs()) {
            Log.i(TAG, "Make dir failed");
        }

        final String fname = filename;
        final File file = new File(myDir, fname);
        if (file.exists()) {
            file.delete();
        }
        try {
            final FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
        } catch (final Exception e) {
            Log.e(TAG, "Exception!");
        }
    }


    public static String getPic(Uri uri, byte[] bytes, final String filename) {


        final String root = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "picture";


        try {
            final File myDir = new File(root);
            if (!myDir.exists()) {
                Log.e("main", "onImageAvailable: 路径不存在");
                myDir.mkdirs();
            } else {
                Log.e("main", "onImageAvailable: 路径存在");
            }
            file = new File(myDir, filename);
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(bytes);
            fileOutputStream.flush();
            fileOutputStream.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file.getAbsolutePath();
    }


    private static File file;

    public static String getPicfile(byte[] bytes, final String filename) {


        final String root = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "picture";


        try {
            final File myDir = new File(root);
            if (!myDir.exists()) {
                Log.e("main", "onImageAvailable: 路径不存在");
                myDir.mkdirs();
            } else {
                Log.e("main", "onImageAvailable: 路径存在");
            }
            file = new File(myDir, filename);
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(bytes);
            fileOutputStream.flush();
            fileOutputStream.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file.getAbsolutePath();
    }


    /**
     * 读取图片属性：旋转的角度
     *
     * @param path 图片绝对路径
     * @return degree旋转的角度
     */
    public static int readPictureDegree(String path) {
        int degree = 0;
        ExifInterface exifInterface = null;
        try {
            exifInterface = new ExifInterface(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        int orientation = exifInterface.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL);
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                degree = 90;
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                degree = 180;
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                degree = 270;
                break;
        }
        System.out.println("图片旋转了：" + degree + " 度");
        return degree;
    }


    /**
     * 将Bitmap转换成Base64字符串
     *
     * @param bit
     * @return
     */
    public static String Bitmap2StrByBase64(Bitmap bit) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bit.compress(Bitmap.CompressFormat.JPEG, 100, bos);//参数100表示不压缩
        byte[] bytes = bos.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }


    /***
     * 删除文件
     * @param path
     * @return
     */
    public static boolean deleteFile(String path) {
        File file = new File(path);
        if (file.exists() && file.isFile()) {
            if (!file.delete()) {
                Log.e(TAG, "delete " + path + " failed");
                return false;
            }
            return true;
        }
        Log.d(TAG, "file is no exist");
        return true;
    }


    /***
     * 将图片进行旋转；
     * @param angle
     * @param bitmap
     * @return
     */
    public static Bitmap rotaingBitamp(int angle, Bitmap bitmap) {
        // 旋转图片 动作
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        // 创建新的图片
        Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        return resizedBitmap;
    }


    public static Bitmap mirror(Bitmap rotaingBitamp) {

        Matrix m = new Matrix();
        m.postScale(-1, 1); // 镜像水平翻转
        return Bitmap.createBitmap(rotaingBitamp, 0, 0, rotaingBitamp.getWidth(), rotaingBitamp.getHeight(), m, true);

    }



}