/*
 * Created by Angel Leon (@gubatron)
 * Copyright (c) 2011-2020, FrostWire(R). All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.frostwire.android.core;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.frostwire.android.util.SystemUtils;
import com.frostwire.search.telluride.TellurideSearchPerformer;
import com.frostwire.search.telluride.TellurideSearchResult;
import com.frostwire.util.Logger;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;

public final class TellurideCourier {
    private static final Logger LOG = Logger.getLogger(TellurideCourier.class);
    private static Gson gson = null;

    private static final Queue<TellurideCourierCallback> knownCallbacks = new LinkedList<>();

    public static void abortQueries() {
        while (!knownCallbacks.isEmpty()) {
            TellurideCourierCallback courierCallback = knownCallbacks.poll();
            courierCallback.abort();
        }
    }

    public abstract class TellurideCourierCallback {
        private boolean hasAborted = false;
        private String url = null;

        abstract void onResults(List<TellurideSearchResult> results, boolean errored);

        final void abort() {
            hasAborted = true;
        }

        final boolean aborted() {
            return hasAborted;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TellurideCourierCallback that = (TellurideCourierCallback) o;
            return Objects.equals(url, that.url);
        }

        @Override
        public int hashCode() {
            return Objects.hash(url);
        }

        private void setUrl(String url) {
            this.url = url;
        }
    }

    // runs on SEARCH_PERFORMER HandlerThread
    public static void queryPage(String url, TellurideCourierCallback callback) {
        if (SystemUtils.isUIThread()) {
            SystemUtils.postToHandler(SystemUtils.HandlerThreadName.SEARCH_PERFORMER, () -> queryPage(url, callback));
            return;
        }
        if (callback != null) {
            callback.setUrl(url);
            knownCallbacks.add(callback);
        }
        SystemUtils.ensureBackgroundThreadOrCrash("TellurideCourier::queryPage");
        boolean error = false;
        long a = System.currentTimeMillis();
        Python python = Python.getInstance();
        long b = System.currentTimeMillis();
        long pythonInstanceFetchTime = b - a;
        if (callback != null && callback.aborted()) {
            knownCallbacks.remove(callback);
            LOG.info("TellurideCourier::queryPage aborted by TellurideCourierCallback (stage 1)");
            return;
        }
        LOG.info("TellurideCourier::queryPage - Got Python instance in " + pythonInstanceFetchTime + " ms");
        PyObject telluride_module = python.getModule("telluride");
        if (callback != null && callback.aborted()) {
            knownCallbacks.remove(callback);
            LOG.info("TellurideCourier::queryPage aborted by TellurideCourierCallback (stage 2)");
            return;
        }
        PyObject query_video_result = telluride_module.callAttr("query_video", url);
        if (query_video_result == null && callback != null) {
            knownCallbacks.remove(callback);
            callback.onResults(null, true);
            return;
        }
        if (query_video_result == null) {
            return;
        }
        String json_query_video_result = query_video_result.toString();
        if (callback != null && callback.aborted()) {
            knownCallbacks.remove(callback);
            LOG.info("TellurideCourier::queryPage aborted by TellurideCourierCallback (stage 3)");
            return;
        }
        synchronized (knownCallbacks) {
            if (gson == null) {
                gson = new GsonBuilder().create();
            }
        }

        List<TellurideSearchResult> validResults = TellurideSearchPerformer.getValidResults(json_query_video_result, gson, null, -1, url);
        LOG.info("TellurideCourier::queryPage: TellurideSearchPerformer.getValidResults() -> " + validResults.size());
        validResults.forEach(r -> {
            LOG.info("TellurideCourier::queryPage: displayName " + r.getDisplayName());
            LOG.info("TellurideCourier::queryPage: fileName " + r.getFilename());
            LOG.info("TellurideCourier::queryPage: download url: " + r.getDownloadUrl() + "\n");
        });

        if (callback != null && !callback.aborted()) {
            knownCallbacks.remove(callback);
            callback.onResults(validResults, error);
        }
    }
}
