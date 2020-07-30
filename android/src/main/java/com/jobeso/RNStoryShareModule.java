package com.jobeso;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;

import com.facebook.react.bridge.JSApplicationIllegalArgumentException;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class RNStoryShareModule extends ReactContextBaseJavaModule {
  private static final String FILE = "file";
  private static final String BASE64 = "base64";
  private static final String INTERNAL_DIR_NAME = "rnstoryshare";

  private static final String SUCCESS = "success";
  private static final String UNKNOWN_ERROR = "An unknown error occured in RNStoryShare";
  private static final String ERROR_TYPE_NOT_SUPPORTED = "Type not supported by RNStoryShare";
  private static final String ERROR_NO_PERMISSIONS = "Permissions Missing";
  private static final String TYPE_ERROR = "Type Error";
  private static final String MEDIA_TYPE_IMAGE = "image/*";
  private static final String MEDIA_TYPE_VIDEO = "video/*";
  private static final String PHOTO = "photo";
  private static final String VIDEO = "video";

  private static final String instagramScheme = "com.instagram.android";

  public RNStoryShareModule(ReactApplicationContext reactContext) {
    super(reactContext);
  }

  @Override
  public String getName() {
    return "RNStoryShare";
  }

  @Override
  public Map < String, Object > getConstants() {
    final Map < String, Object > constants = new HashMap < > ();
    constants.put("BASE64", BASE64);
    constants.put("FILE", FILE);
    constants.put("SUCCESS", SUCCESS);
    constants.put("UNKNOWN_ERROR", UNKNOWN_ERROR);
    constants.put("TYPE_ERROR", TYPE_ERROR);
    return constants;
  }

  private String generateFileName() {
    Random r = new Random();
    int hash = r.nextInt(999999);

    return "video-" + hash + ".mp4";
  }

  private String getFilePath() {
    String externalDir = this.getReactApplicationContext().getExternalCacheDir() + "/";
    String namespaceDir = externalDir + INTERNAL_DIR_NAME + "/";
    String fileName = generateFileName();

    File folder = new File(namespaceDir);

    if (!folder.exists()) {
      boolean isCreated = folder.mkdir();

      if (!isCreated) {
        return externalDir + fileName;
      }
    }

    return namespaceDir + fileName;
  }

  private static File createFile(final String path) {
    final File file = new File(path);

    if (!file.exists()) {
      try {
        file.createNewFile();
      } catch (IOException e) {
        e.printStackTrace();
        return null;
      }
    }

    return file;
  }

  private static File getSavedImageFileForBase64(final String path, final String data) {
    final byte[] imgBytesData = android.util.Base64.decode(data, android.util.Base64.DEFAULT);
    final File file = createFile(path);
    final FileOutputStream fileOutputStream;

    try {
      fileOutputStream = new FileOutputStream(file);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
      return null;
    }

    final BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
    try {
      bufferedOutputStream.write(imgBytesData);
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    } finally {
      try {
        bufferedOutputStream.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    return file;
  }

  private File getFileFromBase64String(String base64ImageData) {
    String backgroundAssetPath = getFilePath();
    String data = base64ImageData.substring(base64ImageData.indexOf(",") + 1);

    return getSavedImageFileForBase64(backgroundAssetPath, data);
  }

  private static void copyFile(File src, File dst) throws IOException {
    try (InputStream in = new FileInputStream(src)) {
      try (OutputStream out = new FileOutputStream(dst)) {
        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in .read(buf)) > 0) {
          out.write(buf, 0, len);
        }
      }
    }
  }

  private void _shareToInstagram(
          @Nullable File backgroundFile,
          @Nullable File stickerFile,
          @Nullable String attributionLink,
          @Nullable String backgroundBottomColor,
          @Nullable String backgroundTopColor,
          String media,
          Promise promise
  ) {
    try {
      Intent intent = new Intent("com.instagram.share.ADD_TO_STORY");
      String providerName = this.getReactApplicationContext().getPackageName() + ".fileprovider";
      Activity activity = getCurrentActivity();

      if (backgroundFile != null) {
        Uri backgroundImageUri = FileProvider.getUriForFile(activity, providerName, backgroundFile);

        intent.setDataAndType(backgroundImageUri, media.equals(VIDEO) ? MEDIA_TYPE_VIDEO : MEDIA_TYPE_IMAGE);
      } else {
        intent.setType(MEDIA_TYPE_IMAGE);
      }

      if (stickerFile != null) {
        Uri stickerAssetUri = FileProvider.getUriForFile(activity, providerName, stickerFile);

        intent.putExtra("interactive_asset_uri", stickerAssetUri);
        assert activity != null;
        activity.grantUriPermission(
                "com.instagram.android", stickerAssetUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
      }

      intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

      if (backgroundBottomColor != null) {
        intent.putExtra("bottom_background_color", backgroundBottomColor);
      }

      if (backgroundTopColor != null) {
        intent.putExtra("top_background_color", backgroundTopColor);
      }

      if (attributionLink != null) {
        intent.putExtra("content_url", attributionLink);
      }

      assert activity != null;
      if (activity.getPackageManager().resolveActivity(intent, 0) != null) {
        activity.startActivityForResult(intent, 0);
        promise.resolve(SUCCESS);
      } else {
        throw new Exception("Couldn't open intent");
      }
    } catch (Exception e) {
      promise.reject(UNKNOWN_ERROR, e);
    }
  }

  @ReactMethod
  public void shareToInstagram(String base64, Promise promise) {
      try {
          Activity activity = getCurrentActivity();
          File media = getFileFromBase64String(base64);
          String providerName = getReactApplicationContext().getPackageName() + ".fileprovider";
          Uri uri = FileProvider.getUriForFile(activity, providerName, media);

          Intent share = new Intent(Intent.ACTION_SEND);
          share.setPackage("com.instagram.android");
          share.setType(MEDIA_TYPE_VIDEO);
          share.putExtra(Intent.EXTRA_STREAM, uri);
          share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

          activity.startActivity(share);

          promise.resolve(true);
      } catch (Exception e) {
          promise.reject(e);
      }
  }

  private void canOpenUrl(Promise promise) {
    try {
      Activity activity = getCurrentActivity();
      assert activity != null;
      activity.getPackageManager().getPackageInfo(RNStoryShareModule.instagramScheme, PackageManager.GET_ACTIVITIES);
      promise.resolve(true);
    } catch (PackageManager.NameNotFoundException e) {
      promise.resolve(false);
    } catch (Exception e) {
      promise.reject(new JSApplicationIllegalArgumentException(
              "Could not check if URL '" + RNStoryShareModule.instagramScheme + "' can be opened: " + e.getMessage()));
    }
  }

  @ReactMethod
  public void isInstagramAvailable(Promise promise) {
    canOpenUrl(promise);
  }
}
