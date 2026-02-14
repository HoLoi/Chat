package com.example.chatrealtime.network;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Đơn giản hóa upload multipart (ảnh/video) qua Volley.
 */
public abstract class VolleyMultipartRequest extends Request<NetworkResponse> {

    private final Response.Listener<NetworkResponse> mListener;
    private final Response.ErrorListener mErrorListener;
    private final String boundary;
    private final String mimeType;

    public VolleyMultipartRequest(int method, String url,
                                  Response.Listener<NetworkResponse> listener,
                                  Response.ErrorListener errorListener) {
        super(method, url, errorListener);
        this.mListener = listener;
        this.mErrorListener = errorListener;
        this.boundary = "apiclient-" + System.currentTimeMillis();
        this.mimeType = "multipart/form-data;boundary=" + boundary;
    }

    @Override
    public String getBodyContentType() {
        return mimeType;
    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            // Text params
            Map<String, String> params = getParams();
            if (params != null && !params.isEmpty()) {
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    buildTextPart(bos, entry.getKey(), entry.getValue());
                }
            }

            // File params
            Map<String, DataPart> data = getByteData();
            if (data != null && !data.isEmpty()) {
                for (Map.Entry<String, DataPart> entry : data.entrySet()) {
                    buildDataPart(bos, entry.getValue(), entry.getKey());
                }
            }

            // end boundary
            bos.write(("--" + boundary + "--\r\n").getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bos.toByteArray();
    }

    @Override
    protected Response<NetworkResponse> parseNetworkResponse(NetworkResponse response) {
        return Response.success(response, HttpHeaderParser.parseCacheHeaders(response));
    }

    @Override
    protected void deliverResponse(NetworkResponse response) {
        mListener.onResponse(response);
    }

    @Override
    public void deliverError(com.android.volley.VolleyError error) {
        mErrorListener.onErrorResponse(error);
    }

    protected Map<String, DataPart> getByteData() throws AuthFailureError {
        return Collections.emptyMap();
    }

    private void buildTextPart(ByteArrayOutputStream bos, String name, String value) throws IOException {
        bos.write(("--" + boundary + "\r\n").getBytes());
        bos.write(("Content-Disposition: form-data; name=\"" + name + "\"\r\n\r\n").getBytes());
        bos.write(value.getBytes());
        bos.write("\r\n".getBytes());
    }

    private void buildDataPart(ByteArrayOutputStream bos, DataPart dataFile, String inputName) throws IOException {
        bos.write(("--" + boundary + "\r\n").getBytes());
        bos.write(("Content-Disposition: form-data; name=\"" + inputName + "\"; filename=\"" + dataFile.getFileName() + "\"\r\n").getBytes());
        if (dataFile.getType() != null) {
            bos.write(("Content-Type: " + dataFile.getType() + "\r\n\r\n").getBytes());
        } else {
            bos.write(("Content-Type: application/octet-stream\r\n\r\n").getBytes());
        }

        InputStream inputStream = new ByteArrayInputStream(dataFile.getContent());
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            bos.write(buffer, 0, bytesRead);
        }
        bos.write("\r\n".getBytes());
    }

    public static class DataPart {
        private final String fileName;
        private final byte[] content;
        private final String type;

        public DataPart(String fileName, byte[] content, String type) {
            this.fileName = fileName;
            this.content = content;
            this.type = type;
        }

        public String getFileName() {
            return fileName;
        }

        public byte[] getContent() {
            return content;
        }

        public String getType() {
            return type;
        }
    }
}
