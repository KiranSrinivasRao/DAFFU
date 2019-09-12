package com.fireflies.daffu;

import android.net.NetworkInfo;

public interface DownloadCallback {

    /**
     * Indicates the call handler needs to update its appearance or information of the
     * task . To be called in main Thread.
     *
     * @param result endResult
     */
    void updateFromDownload(String result);

    /**
     * get device's active network status in the form of NetworkInfo object
     *
     * @return return
     */
    NetworkInfo getActiveNetworkInfo();

    /**
     * Indicate to call back handler any progress update
     *
     * @param progressCode    must be one of the constants defined above
     * @param percentComplete should be updated in terms of minutes
     */
    void onProgressUpdate(int progressCode, int percentComplete);

    /**
     * Indicates the download task is finished - either successfully or not
     */
    void finishDownloading();

    interface Progress {
        int ERROR = -1;
        int CONNECTION_SUCCESS = 0;
        int GET_INPUT_STREAM_SUCCESS = 1;
        int PROCESS_INPUT_STREAM_IN_PROGRESS = 2;
        int PROCESS_INPUT_STREAM_SUCCESS = 3;
    }

}
