package com.study.camera2.view;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import com.study.camera2.MyApplication;

import java.io.OutputStream;
import java.util.concurrent.TimeUnit;

/****
 * Android10 以上使用MediaStore 获取Uri 通过uri 进行读写操纵；
 * 媒体库类进行存储；
 */
public class MediaStoreSave {

    /***
     *  通过媒体库获取要插入的图片Uri
     * @param fileName  要创建的文件名称带后缀（如xx.txt）
     * @param mineType  文件的格式类型（如图片格式image/jpeg）
     * @param relativePath  包含某个公共目录下子目录的路径（如公共目录DCIM下面创建一个test文件夹下"DCIM/test"）
     * @param mContext      上下文；
     * @return
     */
    public  static Uri insertPicUri(String fileName, String mineType, String relativePath, Context mContext){

        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME,fileName);
        //文件的格式类型；
        values.put(MediaStore.Images.Media.MIME_TYPE, mineType);
        //文件标题；
        values.put(MediaStore.Images.Media.TITLE, fileName);
        //设置最后修改时间
        values.put(MediaStore.Images.ImageColumns.DATE_MODIFIED,
                TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()));
        //注意RELATIVE_PATH需要targetVersion=29
        //故该方法只可在Android10的手机上执行
        //再Download 公共媒体路径下创建子目录relativePath；
        values.put(MediaStore.Images.Media.RELATIVE_PATH, "DCIM/"+relativePath);//公共目录下目录名
        //根据手机NAND闪存这个叫做“主要存储”(primary storage)的库名生成URI// 其实是将数据保存到external_primary 目录下面；
        Uri contentUri = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
        Uri  mInsert = null;
        if (contentUri!=null){
            mInsert = mContext.getContentResolver().insert(contentUri, values);
        }
        return mInsert;
    }


    /***
     *
     * @param uri  写入图片数据的路径地址Uri
     * @param bytes 图片的字节数组；
     */
    public static void  SavePic(Uri uri, byte[] bytes) {

        if (uri != null) {
            //使用流将内容写入该uri中即可
            OutputStream outputStream = null;
            try {
                outputStream = MyApplication.getContext().getContentResolver().openOutputStream(uri);
                outputStream.write(bytes);
                outputStream.flush();
                outputStream.close();

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }


    /***
     * 查询指定的URI 文件；
     * @param context
     * @param name 文件名字加上后缀；
     * @return
     *
     * Uri uri,                //数据资源的路径
     * String[] projection,    //查询的列
     * String selection,       //查询的条件
     * String[] selectionArgs, //条件的填充值
     * String sortOrder){}     //排序依据；
     */
    //name是文件名称，是MediaStore查找文件的条件之一

    public static  Uri  getImageIns(Context context, String name)  {
        Uri  itemUri = null;
        ContentResolver resolver = context.getContentResolver();
        //根据日期降序查询
        String sortOrder = MediaStore.Images.Media.DATE_MODIFIED + " DESC";
        //查询条件 根据名字进行查询；
        String selection = MediaStore.Images.Media.DISPLAY_NAME + "='" + name + "'";
        //查询获取cursor;
        Cursor cursor =  resolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, selection, null, sortOrder);

        if (cursor != null && cursor.moveToFirst()) {
            //媒体数据库中查询到的id字段的数据；
            int columnId = cursor.getColumnIndex(MediaStore.Images.Media._ID);

            do {
                //通过mediaId获取它的uri
                int mediaId = cursor.getInt(columnId);
                //拼接URI;
                itemUri=ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, mediaId);

            } while (cursor.moveToNext());
        }
        cursor.close();

        return itemUri;

    }


}
