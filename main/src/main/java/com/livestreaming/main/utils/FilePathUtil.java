package com.livestreaming.main.utils;

import android.content.Context;

import java.io.File;

/**
 * 文件路径的获取和拼接
 * 
 */
public class FilePathUtil {


    public static String makeFilePath(Context context, String folderPath, String fileName) {
	File file = null;
	if (android.os.Environment.getExternalStorageState().equals(
		android.os.Environment.MEDIA_MOUNTED)) {
	    file = new File(android.os.Environment.getExternalStorageDirectory(),
		    folderPath);
	} else {
	    file = context.getApplicationContext().getCacheDir();
	}
	if (!file.exists() || !file.isDirectory()) {
	    file.mkdirs();
	}
	StringBuilder absoluteFolderPath = new StringBuilder(file.getAbsolutePath());
	if (!absoluteFolderPath.toString().endsWith("/")) {
	    absoluteFolderPath.append("/");
	}
	if (fileName != null) {
	    absoluteFolderPath.append(fileName);
	}
	return absoluteFolderPath.toString();
    }


	/**
	 * 得到文件夹目录
	 * @param context
	 * @param folderPath
	 * @return
	 */
	public static String getFolderDir(Context context, String folderPath) {
		File file = null;
		if (android.os.Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED)) {
			file = new File(android.os.Environment.getExternalStorageDirectory(),
					folderPath);
		} else {
			file = context.getApplicationContext().getCacheDir();
		}
		if (!file.exists() || !file.isDirectory()) {
			file.mkdirs();
		}

		return file.getPath();
	}

    /**
     * 清空某一路径下的文件
     * 
     * @param context
     * @param filePath
     */
    public static void clearFilePath(Context context, File filePath) {
	if (!filePath.exists()) {
	    return;
	}
	if (filePath.isFile()) {
	    filePath.delete();
	    return;
	}
	if (filePath.isDirectory()) {
	    File[] folders = filePath.listFiles();
	    for (int i = 0; i < folders.length; i++) {
		clearFilePath(context, folders[i]);
	    }
	}
    }

}
