package com.kai_jan_57.opendsbmobile.network;

import android.os.AsyncTask;
import android.util.Base64;

import com.google.common.io.CountingInputStream;
import com.kai_jan_57.opendsbmobile.utils.GzipUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

import javax.net.ssl.HttpsURLConnection;

import static java.net.HttpURLConnection.HTTP_OK;

public abstract class RequestSenderTask extends AsyncTask<Object, Integer, Object> {

    private static final String DSBCONTROL_HOST = "https://app.dsbcontrol.de/";
    private static final String DSBCONTROL_JSON_API_PATH = "JsonHandler.ashx/GetData";

    public static String getHost() {
        return DSBCONTROL_HOST;
    }

    static String getJsonApiPath() {
        return getHost() + DSBCONTROL_JSON_API_PATH;
    }

    void ExecuteRequestSenderTask(String url, RequestType requestType) {
        try {
            execute(url, onSetupPacket().toString().getBytes(), requestType);
        } catch (JSONException e) {
            onException(e);
        }
    }

    JSONObject onSetupPacket() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("AppId", "");
        jsonObject.put("PushId", "");
        jsonObject.put("AppVersion", "1.0");
        jsonObject.put("Device", "Generic");
        jsonObject.put("OsVersion", "9 API Level: 28");
        jsonObject.put("Language", Locale.getDefault().getLanguage());
        return jsonObject;
    }

    abstract void onProgress(int progress);

    abstract void onJsonParsed(JSONObject jsonObject);

    abstract void onEmptyResponse();

    abstract void onException(Exception exception);


    @Override
    protected Object doInBackground(Object... objects) {
        /*
        if (false) {
            try {
                for (int i = 0; i < 10; i++) {
                    Thread.sleep(1);
                    publishProgress(i * 10);
                }
            } catch (Exception e) {

            }
            try {
                return new JSONObject("{\"Resultcode\":0,\"ResultStatusInfo\":\"\",\"StartIndex\":-1,\"ResultMenuItems\":[{\"Index\":0,\"IconLink\":\"\",\"Title\":\"Inhalte\",\"Childs\":[{\"Index\":1,\"IconLink\":\"https:\\/\\/app.dsbcontrol.de\\/static\\/androidicons\\/Tiles.png\",\"Title\":\"Aushänge\",\"Root\":{\"Id\":\"\",\"Date\":\"\",\"Title\":\"\",\"Detail\":\"\",\"Tags\":\"\",\"ConType\":0,\"Prio\":0,\"Index\":0,\"Childs\":[{\"Id\":\"8a2e9a43-736a-49a4-92e7-ccad4e94576c\",\"Date\":\"06.03.2019 14:18\",\"Title\":\"Elternsprechtag\",\"Detail\":\"\",\"Tags\":\"\",\"ConType\":2,\"Prio\":0,\"Index\":0,\"Childs\":[{\"Id\":\"8a2e9a43-736a-49a4-92e7-ccad4e94576c\",\"Date\":\"06.03.2019 14:18\",\"Title\":\"Elternsprechtag.doc\",\"Detail\":\"https:\\/\\/app.dsbcontrol.de\\/data\\/f84c9896-d45d-4781-a9dc-cd730ae5f918\\/8a2e9a43-736a-49a4-92e7-ccad4e94576c\\/8a2e9a43-736a-49a4-92e7-ccad4e94576c_000.png\",\"Tags\":\"\",\"ConType\":4,\"Prio\":0,\"Index\":0,\"Childs\":[],\"Preview\":\"f84c9896-d45d-4781-a9dc-cd730ae5f918\\/8a2e9a43-736a-49a4-92e7-ccad4e94576c\\/8a2e9a43-736a-49a4-92e7-ccad4e94576c_000.png\"}],\"Preview\":\"\"},{\"Id\":\"23a2b060-9a18-4a5a-82cf-2234e336fe08\",\"Date\":\"06.03.2019 14:18\",\"Title\":\"Computer-AG\",\"Detail\":\"\",\"Tags\":\"\",\"ConType\":2,\"Prio\":0,\"Index\":0,\"Childs\":[{\"Id\":\"23a2b060-9a18-4a5a-82cf-2234e336fe08\",\"Date\":\"06.03.2019 14:18\",\"Title\":\"Computer-AG.doc\",\"Detail\":\"https:\\/\\/app.dsbcontrol.de\\/data\\/f84c9896-d45d-4781-a9dc-cd730ae5f918\\/23a2b060-9a18-4a5a-82cf-2234e336fe08\\/23a2b060-9a18-4a5a-82cf-2234e336fe08_000.png\",\"Tags\":\"\",\"ConType\":4,\"Prio\":0,\"Index\":0,\"Childs\":[],\"Preview\":\"f84c9896-d45d-4781-a9dc-cd730ae5f918\\/23a2b060-9a18-4a5a-82cf-2234e336fe08\\/23a2b060-9a18-4a5a-82cf-2234e336fe08_000.png\"}],\"Preview\":\"\"},{\"Id\":\"8da975d1-5e6b-4843-8170-0b507afc1c7f\",\"Date\":\"06.03.2019 14:18\",\"Title\":\"Elternsprechtag\",\"Detail\":\"\",\"Tags\":\"\",\"ConType\":2,\"Prio\":0,\"Index\":0,\"Childs\":[{\"Id\":\"8da975d1-5e6b-4843-8170-0b507afc1c7f\",\"Date\":\"06.03.2019 14:18\",\"Title\":\"Elternsprechtag.doc\",\"Detail\":\"https:\\/\\/app.dsbcontrol.de\\/data\\/f84c9896-d45d-4781-a9dc-cd730ae5f918\\/8da975d1-5e6b-4843-8170-0b507afc1c7f\\/8da975d1-5e6b-4843-8170-0b507afc1c7f_000.png\",\"Tags\":\"\",\"ConType\":4,\"Prio\":0,\"Index\":0,\"Childs\":[],\"Preview\":\"f84c9896-d45d-4781-a9dc-cd730ae5f918\\/8da975d1-5e6b-4843-8170-0b507afc1c7f\\/8da975d1-5e6b-4843-8170-0b507afc1c7f_000.png\"}],\"Preview\":\"\"},{\"Id\":\"f02f719e-7e21-4070-92bd-cf82a8ff4de3\",\"Date\":\"06.03.2019 14:18\",\"Title\":\"Computer-AG\",\"Detail\":\"\",\"Tags\":\"\",\"ConType\":2,\"Prio\":0,\"Index\":0,\"Childs\":[{\"Id\":\"f02f719e-7e21-4070-92bd-cf82a8ff4de3\",\"Date\":\"06.03.2019 14:18\",\"Title\":\"Computer-AG.doc\",\"Detail\":\"https:\\/\\/app.dsbcontrol.de\\/data\\/f84c9896-d45d-4781-a9dc-cd730ae5f918\\/f02f719e-7e21-4070-92bd-cf82a8ff4de3\\/f02f719e-7e21-4070-92bd-cf82a8ff4de3_000.png\",\"Tags\":\"\",\"ConType\":4,\"Prio\":0,\"Index\":0,\"Childs\":[],\"Preview\":\"f84c9896-d45d-4781-a9dc-cd730ae5f918\\/f02f719e-7e21-4070-92bd-cf82a8ff4de3\\/f02f719e-7e21-4070-92bd-cf82a8ff4de3_000.png\"}],\"Preview\":\"\"},{\"Id\":\"e1f44c12-da19-4f9f-8643-40b95e508adc\",\"Date\":\"06.03.2019 14:18\",\"Title\":\"Elternsprechtag\",\"Detail\":\"\",\"Tags\":\"\",\"ConType\":2,\"Prio\":0,\"Index\":0,\"Childs\":[{\"Id\":\"e1f44c12-da19-4f9f-8643-40b95e508adc\",\"Date\":\"06.03.2019 14:18\",\"Title\":\"Elternsprechtag.doc\",\"Detail\":\"https:\\/\\/app.dsbcontrol.de\\/data\\/f84c9896-d45d-4781-a9dc-cd730ae5f918\\/e1f44c12-da19-4f9f-8643-40b95e508adc\\/e1f44c12-da19-4f9f-8643-40b95e508adc_000.png\",\"Tags\":\"\",\"ConType\":4,\"Prio\":0,\"Index\":0,\"Childs\":[],\"Preview\":\"f84c9896-d45d-4781-a9dc-cd730ae5f918\\/e1f44c12-da19-4f9f-8643-40b95e508adc\\/e1f44c12-da19-4f9f-8643-40b95e508adc_000.png\"}],\"Preview\":\"\"},{\"Id\":\"27545ed9-287b-4d39-894a-a25118b3721a\",\"Date\":\"16.08.2018 13:12\",\"Title\":\"ipad\",\"Detail\":\"\",\"Tags\":\"\",\"ConType\":2,\"Prio\":0,\"Index\":0,\"Childs\":[{\"Id\":\"27545ed9-287b-4d39-894a-a25118b3721a\",\"Date\":\"16.08.2018 13:12\",\"Title\":\"ipad\",\"Detail\":\"https:\\/\\/app.dsbcontrol.de\\/data\\/f84c9896-d45d-4781-a9dc-cd730ae5f918\\/27545ed9-287b-4d39-894a-a25118b3721a\\/27545ed9-287b-4d39-894a-a25118b3721a_000.png\",\"Tags\":\"\",\"ConType\":4,\"Prio\":0,\"Index\":0,\"Childs\":[],\"Preview\":\"f84c9896-d45d-4781-a9dc-cd730ae5f918\\/27545ed9-287b-4d39-894a-a25118b3721a\\/27545ed9-287b-4d39-894a-a25118b3721a_000.png\"}],\"Preview\":\"\"},{\"Id\":\"44068978-dd21-43e5-9c47-901b89314259\",\"Date\":\"06.03.2019 14:18\",\"Title\":\"bitte ins Sekretariat\",\"Detail\":\"\",\"Tags\":\"\",\"ConType\":2,\"Prio\":0,\"Index\":0,\"Childs\":[{\"Id\":\"44068978-dd21-43e5-9c47-901b89314259\",\"Date\":\"06.03.2019 14:18\",\"Title\":\"bitte ins Sekretariat.doc\",\"Detail\":\"https:\\/\\/app.dsbcontrol.de\\/data\\/f84c9896-d45d-4781-a9dc-cd730ae5f918\\/44068978-dd21-43e5-9c47-901b89314259\\/44068978-dd21-43e5-9c47-901b89314259_000.png\",\"Tags\":\"\",\"ConType\":4,\"Prio\":0,\"Index\":0,\"Childs\":[],\"Preview\":\"f84c9896-d45d-4781-a9dc-cd730ae5f918\\/44068978-dd21-43e5-9c47-901b89314259\\/44068978-dd21-43e5-9c47-901b89314259_000.png\"}],\"Preview\":\"\"},{\"Id\":\"a10bed8b-5007-4dfe-9cf1-e02daba36588\",\"Date\":\"06.03.2019 14:18\",\"Title\":\"Elternsprechtag\",\"Detail\":\"\",\"Tags\":\"\",\"ConType\":2,\"Prio\":0,\"Index\":0,\"Childs\":[{\"Id\":\"a10bed8b-5007-4dfe-9cf1-e02daba36588\",\"Date\":\"06.03.2019 14:18\",\"Title\":\"Elternsprechtag.doc\",\"Detail\":\"https:\\/\\/app.dsbcontrol.de\\/data\\/f84c9896-d45d-4781-a9dc-cd730ae5f918\\/a10bed8b-5007-4dfe-9cf1-e02daba36588\\/a10bed8b-5007-4dfe-9cf1-e02daba36588_000.png\",\"Tags\":\"\",\"ConType\":4,\"Prio\":0,\"Index\":0,\"Childs\":[],\"Preview\":\"f84c9896-d45d-4781-a9dc-cd730ae5f918\\/a10bed8b-5007-4dfe-9cf1-e02daba36588\\/a10bed8b-5007-4dfe-9cf1-e02daba36588_000.png\"}],\"Preview\":\"\"},{\"Id\":\"f7206462-0c6c-41c3-8452-5da6cd0c7cc8\",\"Date\":\"06.03.2019 14:18\",\"Title\":\"Sprechprüfungen\",\"Detail\":\"\",\"Tags\":\"\",\"ConType\":2,\"Prio\":0,\"Index\":0,\"Childs\":[{\"Id\":\"f7206462-0c6c-41c3-8452-5da6cd0c7cc8\",\"Date\":\"06.03.2019 14:18\",\"Title\":\"Sprechprüfungen.doc\",\"Detail\":\"https:\\/\\/app.dsbcontrol.de\\/data\\/f84c9896-d45d-4781-a9dc-cd730ae5f918\\/f7206462-0c6c-41c3-8452-5da6cd0c7cc8\\/f7206462-0c6c-41c3-8452-5da6cd0c7cc8_000.png\",\"Tags\":\"\",\"ConType\":4,\"Prio\":0,\"Index\":0,\"Childs\":[],\"Preview\":\"f84c9896-d45d-4781-a9dc-cd730ae5f918\\/f7206462-0c6c-41c3-8452-5da6cd0c7cc8\\/f7206462-0c6c-41c3-8452-5da6cd0c7cc8_000.png\"}],\"Preview\":\"\"},{\"Id\":\"bde48469-f76f-4971-9a8e-0addc55acda3\",\"Date\":\"06.03.2019 14:18\",\"Title\":\"Elternsprechtag\",\"Detail\":\"\",\"Tags\":\"\",\"ConType\":2,\"Prio\":0,\"Index\":0,\"Childs\":[{\"Id\":\"bde48469-f76f-4971-9a8e-0addc55acda3\",\"Date\":\"06.03.2019 14:18\",\"Title\":\"Elternsprechtag.doc\",\"Detail\":\"https:\\/\\/app.dsbcontrol.de\\/data\\/f84c9896-d45d-4781-a9dc-cd730ae5f918\\/bde48469-f76f-4971-9a8e-0addc55acda3\\/bde48469-f76f-4971-9a8e-0addc55acda3_000.png\",\"Tags\":\"\",\"ConType\":4,\"Prio\":0,\"Index\":0,\"Childs\":[],\"Preview\":\"f84c9896-d45d-4781-a9dc-cd730ae5f918\\/bde48469-f76f-4971-9a8e-0addc55acda3\\/bde48469-f76f-4971-9a8e-0addc55acda3_000.png\"}],\"Preview\":\"\"}],\"Preview\":\"\"},\"Childs\":[],\"MethodName\":\"tiles\",\"NewCount\":0,\"SaveLastState\":true},{\"Index\":0,\"IconLink\":\"https:\\/\\/app.dsbcontrol.de\\/static\\/androidicons\\/Timetable.png\",\"Title\":\"Pläne\",\"Root\":{\"Id\":\"\",\"Date\":\"\",\"Title\":\"\",\"Detail\":\"\",\"Tags\":\"\",\"ConType\":0,\"Prio\":0,\"Index\":0,\"Childs\":[{\"Id\":\"df712f61-7e46-4dd9-a08d-7d35f5d00f64\",\"Date\":\"06.03.2019 14:18\",\"Title\":\"app Schüler heute\",\"Detail\":\"\",\"Tags\":\"\",\"ConType\":2,\"Prio\":0,\"Index\":0,\"Childs\":[{\"Id\":\"df712f61-7e46-4dd9-a08d-7d35f5d00f64\",\"Date\":\"06.03.2019 14:18\",\"Title\":\"subst_001.htm\",\"Detail\":\"https:\\/\\/app.dsbcontrol.de\\/data\\/f84c9896-d45d-4781-a9dc-cd730ae5f918\\/df712f61-7e46-4dd9-a08d-7d35f5d00f64\\/df712f61-7e46-4dd9-a08d-7d35f5d00f64.htm\",\"Tags\":\"\",\"ConType\":3,\"Prio\":0,\"Index\":0,\"Childs\":[],\"Preview\":\"f84c9896-d45d-4781-a9dc-cd730ae5f918\\/df712f61-7e46-4dd9-a08d-7d35f5d00f64\\/preview.png\"}],\"Preview\":\"\"},{\"Id\":\"179dc822-9a77-4320-a786-b44bbaaef931\",\"Date\":\"06.03.2019 14:18\",\"Title\":\"app Schüler morgen\",\"Detail\":\"\",\"Tags\":\"\",\"ConType\":2,\"Prio\":0,\"Index\":0,\"Childs\":[{\"Id\":\"179dc822-9a77-4320-a786-b44bbaaef931\",\"Date\":\"06.03.2019 14:18\",\"Title\":\"subst_001.htm\",\"Detail\":\"https:\\/\\/app.dsbcontrol.de\\/data\\/f84c9896-d45d-4781-a9dc-cd730ae5f918\\/179dc822-9a77-4320-a786-b44bbaaef931\\/subst_001.htm\",\"Tags\":\"\",\"ConType\":3,\"Prio\":0,\"Index\":0,\"Childs\":[],\"Preview\":\"f84c9896-d45d-4781-a9dc-cd730ae5f918\\/179dc822-9a77-4320-a786-b44bbaaef931\\/preview.png\"}],\"Preview\":\"\"}],\"Preview\":\"\"},\"Childs\":[],\"MethodName\":\"timetable\",\"NewCount\":0,\"SaveLastState\":true}],\"MethodName\":\"\",\"NewCount\":0,\"SaveLastState\":true},{\"Index\":1,\"IconLink\":\"\",\"Title\":\"Sonstiges\",\"Childs\":[{\"Index\":0,\"IconLink\":\"https:\\/\\/app.dsbcontrol.de\\/static\\/androidicons\\/Settings.png\",\"Title\":\"Einstellungen\",\"Childs\":[],\"MethodName\":\"settings\",\"NewCount\":0,\"SaveLastState\":false},{\"Index\":1,\"IconLink\":\"https:\\/\\/app.dsbcontrol.de\\/static\\/androidicons\\/Feedback.png\",\"Title\":\"Feedback\",\"Childs\":[],\"MethodName\":\"feedback\",\"NewCount\":0,\"SaveLastState\":false},{\"Index\":2,\"IconLink\":\"https:\\/\\/app.dsbcontrol.de\\/static\\/androidicons\\/About.png\",\"Title\":\"Info\",\"Childs\":[],\"MethodName\":\"about\",\"NewCount\":0,\"SaveLastState\":false},{\"Index\":3,\"IconLink\":\"https:\\/\\/app.dsbcontrol.de\\/static\\/androidicons\\/Logout.png\",\"Title\":\"Logout\",\"Childs\":[],\"MethodName\":\"logout\",\"NewCount\":0,\"SaveLastState\":false}],\"MethodName\":\"\",\"NewCount\":0,\"SaveLastState\":true}],\"ChannelType\":0,\"MandantId\":\"8746a16c-d8f9-4091-bfec-b45a9b61c3ac\"}");
            } catch (JSONException e) {
                return e;
            }
        }*/
        try {
            HttpsURLConnection httpsURLConnection = (HttpsURLConnection) new URL(((String) objects[0])).openConnection();
            httpsURLConnection.setRequestMethod("POST");
            httpsURLConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            httpsURLConnection.setDoInput(true);
            httpsURLConnection.setDoOutput(true);
            httpsURLConnection.setReadTimeout(10000);

            byte[] compressed = GzipUtils.encodeGzip((byte[]) objects[1]);
            JSONObject request = new JSONObject();
            request.put("Data", new String(Base64.encode(compressed, Base64.DEFAULT)));
            request.put("DataType", ((RequestType) objects[2]).intValue);
            request = new JSONObject().put("req", request);

            OutputStream outputStream = httpsURLConnection.getOutputStream();
            outputStream.write(request.toString().getBytes(StandardCharsets.UTF_8));
            outputStream.close();
            httpsURLConnection.connect();
            int responseCode = httpsURLConnection.getResponseCode();
            if (responseCode != HTTP_OK) {
                return new IOException("HTTP error: " + responseCode);
            }

            int contentLength = httpsURLConnection.getContentLength();
            if (contentLength <= 0) {
                return null;
            }

            @SuppressWarnings("UnstableApiUsage") CountingInputStream countingInputStream = new CountingInputStream(httpsURLConnection.getInputStream());
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(countingInputStream, StandardCharsets.UTF_8));

            String line;
            StringBuilder responseString = new StringBuilder();
            while ((line = bufferedReader.readLine()) != null) {
                responseString.append(line);
                publishProgress((int) (countingInputStream.getCount() * 100 / contentLength));
            }
            JSONObject jsonObject = new JSONObject(responseString.toString());
            return new JSONObject(new String(GzipUtils.decodeGzip(Base64.decode(jsonObject.getString("d").getBytes(), Base64.DEFAULT))));
            // May throw: ProtocolException, IOException, JSONException
        } catch (Exception exception) {
            return exception;
        }
    }

    @Override
    protected void onProgressUpdate(Integer[] values) {
        onProgress(values[0]);
    }

    @Override
    protected void onPostExecute(Object object) {
        if (object instanceof JSONObject) {
            onJsonParsed((JSONObject) object);
        } else if (object != null) {
            onException((Exception) object);
        } else {
            onEmptyResponse();
        }
    }

    enum RequestType {
        DataUnknown(0),
        GetData(1),
        MailType(2),
        FeedbackType(3),
        SubjectsType(4),
        ErrorType(5),
        PushSettings(6);

        final int intValue;

        RequestType(int intValue) {
            this.intValue = intValue;
        }
    }

}
