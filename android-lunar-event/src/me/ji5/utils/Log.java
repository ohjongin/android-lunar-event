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
 * 2013/12/11  ohjongin    1.0.0    Initial creation with these template
 * 2014/01/03  ohjongin    1.1.0    Add log methods to use variable arguments
 * 2014/01/03  kiljae lee  1.2.0    Add method for sending an email with log
 *
 */
package me.ji5.utils;

import android.content.Context;
import android.os.Environment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;


public class Log {
    public static boolean LOG_VERBOSE = true;
    public static boolean LOG_INFO = true;
    public static boolean LOG_DEBUG = true;
    public static boolean LOG_WARNING = true;
    public static boolean LOG_ERROR = true;

    protected static boolean mFlushFile = false;
    protected static String TAG = "IB_LOG"; // should be replaced with package name or app name
    protected static String mFilename = null;
    protected static boolean mEnabled = true;
    
    public static boolean isDebugMode() {
        return mEnabled;
    }
    
    public static void setDebugMode(boolean enable) {
        mEnabled = enable;
    }

    public static void setLogTag(String tag) {
        TAG = tag;
    }
    
    // only for compatibility with android.util.Log class
    public static void v(String tag, String log){
        if (mEnabled && LOG_VERBOSE) {
            String realLog = getClassNameAndLineNumber(4) + log;
            android.util.Log.v(TAG, realLog);
        }
    }
    
    public static void i(String tag, String log){
        if (mEnabled && LOG_INFO) {
            String realLog = getClassNameAndLineNumber(4) + log;
            android.util.Log.i(TAG, realLog);
        }
    }

    public static void d(String tag, String log){
        if (mEnabled && LOG_DEBUG) {
            String realLog = getClassNameAndLineNumber(4) + log;
            android.util.Log.d(TAG, realLog);
        }
    }

    public static void w(String tag, String log){
        if (mEnabled && LOG_WARNING) {
            String realLog = getClassNameAndLineNumber(4) + log;
            android.util.Log.w(TAG, realLog);
        }
    }

    public static void e(String tag, String log){
        if (mEnabled && LOG_ERROR) {
            String realLog = getClassNameAndLineNumber(4) + log;
            android.util.Log.e(TAG, realLog);
        }
    }
    
    public static void v(String log){
        if (mEnabled && LOG_VERBOSE) {
            String realLog = getClassNameAndLineNumber(4) + log;
            android.util.Log.v(TAG, realLog);
        }
    }
    
    public static void i(String log){
        if (mEnabled && LOG_INFO) {
            String realLog = getClassNameAndLineNumber(4) + log;
            android.util.Log.i(TAG, realLog);
        }
    }
    
    public static void d(String log){
        if (mEnabled && LOG_DEBUG) {
            String realLog = getClassNameAndLineNumber(4) + log;
            android.util.Log.d(TAG, realLog);
        }
    }
    
    public static void w(String log){
        if (mEnabled && LOG_WARNING) {
            String realLog = getClassNameAndLineNumber(4) + log;
            android.util.Log.w(TAG, realLog);
        }
    }
    
    public static void e(String log){
        if (mEnabled && LOG_ERROR) {
            String realLog = getClassNameAndLineNumber(4) + log;
            android.util.Log.e(TAG, realLog);
        }
    }

    public static void v(Object... args) {
        if (mEnabled && LOG_VERBOSE) {
            String realLog = getClassNameAndLineNumber(4) + buildVariableArguments(args);
            android.util.Log.v(TAG, realLog);
        }
    }

    public static void i(Object... args) {
        if (mEnabled && LOG_INFO) {
            String realLog = getClassNameAndLineNumber(4) + buildVariableArguments(args);
            android.util.Log.i(TAG, realLog);
        }
    }

    public static void d(Object... args) {
        if (mEnabled && LOG_DEBUG) {
            String realLog = getClassNameAndLineNumber(4) + buildVariableArguments(args);
            android.util.Log.d(TAG, realLog);
        }
    }

    public static void w(Object... args) {
        if (mEnabled && LOG_WARNING) {
            String realLog = getClassNameAndLineNumber(4) + buildVariableArguments(args);
            android.util.Log.w(TAG, realLog);
        }
    }

    public static void e(Object... args) {
        if (mEnabled && LOG_ERROR) {
            String realLog = getClassNameAndLineNumber(4) + buildVariableArguments(args);
            android.util.Log.e(TAG, realLog);
        }
    }

    public static String buildVariableArguments(Object... agrs) {
        StringBuilder sb = new StringBuilder();
        /*sb.append(" [")
                .append(Thread.currentThread().getId())
                .append("] ");*/
        for (Object item : agrs) {
            sb.append(item);
        }
        return sb.toString();
    }

    public static void fileLog(String log) {
        Date now = new Date();

        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd,HH:mm:ss.SSS");
        String temp_log = format.format(now) + "    " + log + "\n";

        format = new SimpleDateFormat("yyyyMMdd");
        String fileName = TAG + format.format(now) + ".txt";
        FileOutputStream fos = null;
        try {
            File root = Environment.getExternalStorageDirectory();

            if (root.canWrite()) {
                fos = new FileOutputStream(new File(root, fileName), true);
                fos.write(temp_log.getBytes());
                fos.close();
                fos.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
            StackTraceElement[] elem = e.getStackTrace();
            for (int i = 0; i < elem.length; i++) {
                android.util.Log.e(TAG, "fileLog() -> " + elem[i]);
            }
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static String getClassNameAndLineNumber(int depth) {
        final StackTraceElement[] ste = Thread.currentThread().getStackTrace();
        StringBuffer tempBuf= new StringBuffer();
        
        String[] temp = ste[depth].getClassName().split("\\.");
        tempBuf.append("[" +temp[temp.length - 1]);
        tempBuf.append(":" +ste[depth].getLineNumber() + "] ");
        
        return tempBuf.toString();
    }
    
    public static String getAppDefaultStoragePath(String packageName) {
        return Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/data/" + packageName;
    }

    public static void saveLogcatMessage(Context context) {
        saveLogcatMessage(getAppDefaultStoragePath(context.getPackageName()));
    }
    
    public static void saveLogcatMessage(String path) {
        try {
            Process process = Runtime.getRuntime().exec("logcat -v time -d");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream(), "UTF-8"));

            StringBuilder log = new StringBuilder();
            String line = "";
            // String separator = System.getProperty("line.separator");
            while ((line = bufferedReader.readLine()) != null) {
                log.append(line);
                log.append("\r\n");
            }
            
            //to create a Text file name "logcat.txt" in SDCard
            if (log != null && log.toString().length() > 0) {
                String ext_path = Environment.getExternalStorageDirectory().getAbsolutePath();
                if (path.startsWith(ext_path)) {
                    ext_path = path;
                } else {
                    ext_path = Environment.getExternalStorageDirectory().getAbsolutePath() + path;
                }
                File dir = new File (ext_path);
                boolean success = dir.mkdirs();
                if (!dir.exists() && !success) {
                    Log.e("Can't create base directory: " + dir.getPath());
                }
                String filename = getFilenameByDate(null, null, ".txt");
                File file = new File(dir, filename);
                // to write logcat in text file
                FileOutputStream fOut = new FileOutputStream(file);
                OutputStreamWriter osw = new OutputStreamWriter(fOut);

                // Write the string to the file
                osw.write(log.toString());
                osw.flush();
                osw.close();
            } else {
                Log.e("Could not capture logcat text.. Check 'android.permission.READ_LOGS' in your AndroidManifest.xml");
            }
            // i.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(Environment.getExternalStorageDirectory() + DATA_STORAGE_PATH_BASE + "/logcat","file name")));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }       
    }

    public static String getFilenameByDate(String prefix, String postfix, String ext) {
        if (prefix == null) prefix = "";
        if (postfix == null) postfix = "";

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH.mm.ss.SSS");
        String filename = prefix + sdf.format(new Date()) + postfix + ext;
        filename = filename.replace(" ", "_");
        filename = filename.replace(":", "-");

        return filename;
    }
}