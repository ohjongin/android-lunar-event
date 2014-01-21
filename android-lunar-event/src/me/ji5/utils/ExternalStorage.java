/*
 * Copyright (C) 2013 Infobank. Corp. All Right Reserved.
 *
 * DO NOT COPY OR DISTRIBUTE WITHOUT PERMISSION OF THE AUTHOR
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 * Revision History
 * ----------------------------------------------------------
 * 2013/12/11  ohjongin    1.0.0    initial creation with these template
 *
 */
package me.ji5.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Created by ohjongin on 13. 6. 16.
 */
public class ExternalStorage {
    public static final String TAG = "ExternalStorage";
    public static final String SD_CARD = "sdcard";
    public static final String EXTERNAL_SD_CARD = "externalsdcard";
    public static final String CACHE_ROOT = "/Android/data";

    /**
     * @return True if the context has a write permission to external storage. False otherwise.
     */
    public static boolean isWritePermission(Context context) {
        String permission = "android.permission.WRITE_EXTERNAL_STORAGE";
        int res = context.checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }

    /**
     * @return True if the context has a permission to external storage. False otherwise.
     */
    public static boolean isReadPermission(Context context) {
        String permission = "android.permission.READ_EXTERNAL_STORAGE";
        int res = context.checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }

    /**
     * @return True if the external storage is available. False otherwise.
     */
    public static boolean isAvailable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    public static String getExternalStoragePath() {
        return Environment.getExternalStorageDirectory().getPath();
    }

    /**
     * @return True if the external storage is writable. False otherwise.
     */
    public static boolean isWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;

    }

    /**
     * @return File name list on the specific path
     */
    public static ArrayList<String> getFileList(String path) {
        ArrayList<String> apk_list = new ArrayList<String>();

        if (path == null) {
            return null;
        }

        String ext_path = ExternalStorage.getExternalStoragePath();
        String full_path = path;
        if (!path.startsWith(ext_path)) {
            String separator = path.startsWith(File.separator) ? "" : File.separator;
            full_path = ext_path + separator + path;
        }

        File dir = new File(full_path);

        if (dir != null && dir.listFiles() != null && dir.listFiles().length > 0) {
            for (File file : dir.listFiles()) {
                apk_list.add(file.getName());
            }
        } else {
            Log.e(TAG, "Directory (" + path + ") empty!!!");
        }

        return apk_list;
    }

    public static String getApplicationCacheDir(Context context) {
        boolean success = true;
        String cache_dir = getExternalStoragePath() + CACHE_ROOT + File.separator + context.getPackageName() + File.separator;

        File file = new File(cache_dir);
        if (!file.exists()) {
            success = file.mkdirs();
        }

        if (!success) {
            cache_dir = null;
        }

        return cache_dir;
    }

    /*
        http://stackoverflow.com/questions/5661418/get-apk-file-icon-version-name
     */
    public static Drawable getApplicationIcon(Context context, String filename) {
        String ext_path = ExternalStorage.getExternalStoragePath();
        String full_path = filename;
        if (!filename.startsWith(ext_path)) {
            String separator = filename.startsWith(File.separator) ? "" : File.separator;
            full_path = ext_path + separator + filename;
        }

        PackageManager pm = context.getPackageManager();
        PackageInfo pi = pm.getPackageArchiveInfo(full_path, 0);

        Drawable icon = null;
        // the secret are these two lines....
        if (pi != null && pi.applicationInfo != null) {
            pi.applicationInfo.sourceDir       = full_path;
            pi.applicationInfo.publicSourceDir = full_path;

            icon = pi.applicationInfo.loadIcon(pm);
            //String   AppName = (String)pi.applicationInfo.loadLabel(pm);
        } else {
            Log.e(TAG, "PackageInfo or ApplicationInfo is NULL!!!");
        }

        return icon;
    }

    /**
     * @return A map of all storage locations available
     */
    public static Map<String, File> getAllStorageLocations() {
        Map<String, File> map = new HashMap<String, File>(10);

        List<String> mMounts = new ArrayList<String>(10);
        List<String> mVold = new ArrayList<String>(10);
        mMounts.add("/mnt/sdcard");
        mVold.add("/mnt/sdcard");

        try {
            File mountFile = new File("/proc/mounts");
            if (mountFile.exists()) {
                Scanner scanner = new Scanner(mountFile);
                while (scanner.hasNext()) {
                    String line = scanner.nextLine();
                    if (line.startsWith("/dev/block/vold/")) {
                        String[] lineElements = line.split(" ");
                        String element = lineElements[1];

                        // don't add the default mount path
                        // it's already in the list.
                        if (!element.equals("/mnt/sdcard"))
                            mMounts.add(element);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            File voldFile = new File("/system/etc/vold.fstab");
            if (voldFile.exists()) {
                Scanner scanner = new Scanner(voldFile);
                while (scanner.hasNext()) {
                    String line = scanner.nextLine();
                    if (line.startsWith("dev_mount")) {
                        String[] lineElements = line.split(" ");
                        String element = lineElements[2];

                        if (element.contains(":"))
                            element = element.substring(0, element.indexOf(":"));
                        if (!element.equals("/mnt/sdcard"))
                            mVold.add(element);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        for (int i = 0; i < mMounts.size(); i++) {
            String mount = mMounts.get(i);
            if (!mVold.contains(mount))
                mMounts.remove(i--);
        }
        mVold.clear();

        List<String> mountHash = new ArrayList<String>(10);

        for (String mount : mMounts) {
            File root = new File(mount);
            if (root.exists() && root.isDirectory() && root.canWrite()) {
                File[] list = root.listFiles();
                String hash = "[";
                if (list != null) {
                    for (File f : list) {
                        hash += f.getName().hashCode() + ":" + f.length() + ", ";
                    }
                }
                hash += "]";
                if (!mountHash.contains(hash)) {
                    String key = SD_CARD + "_" + map.size();
                    if (map.size() == 0) {
                        key = SD_CARD;
                    } else if (map.size() == 1) {
                        key = EXTERNAL_SD_CARD;
                    }
                    mountHash.add(hash);
                    map.put(key, root);
                }
            }
        }

        mMounts.clear();

        if (map.isEmpty()) {
            map.put(SD_CARD, Environment.getExternalStorageDirectory());
        }
        return map;
    }
}
