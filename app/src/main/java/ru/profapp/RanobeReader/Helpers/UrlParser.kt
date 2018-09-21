package ru.profapp.RanobeReader.Helpers

import android.os.AsyncTask
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.URL
import java.util.*

/**
 * Created by Ruslan on 08.02.2018.
 */

class UrlParser : AsyncTask<String, Void, String> {

    private val Cookies: Map<String, String> = HashMap()
    private val Data: Map<String, String> = HashMap()
    private var Header: Map<String, String> = HashMap()

    constructor()

    constructor(header: Map<String, String>) {
        Header = header
    }

    override fun doInBackground(vararg params: String): String? {

        val result = StringBuilder()
        try {

            val url = URL(params[0])

            val `in` = BufferedReader(InputStreamReader(url.openStream()))

            var line: String?

            do {

                line = `in`.readLine()

                if (line == null)

                    break

                result.append(line)

            } while (true)


            `in`.close()

        } catch (e: IOException) {
            return null
        }

        return result.toString()

    }

    /*
*
        StringBuilder result = isNew StringBuilder();
        try {
            String data ="";

            HttpURLConnection urlConnection;

            URL url = isNew URL(params[0]);
            urlConnection = (HttpURLConnection) ((url.openConnection()));

            //urlConnection.setUseCaches(false);
           // urlConnection.setChunkedStreamingMode(1024);


            urlConnection.setDoOutput(true);
            for(Map.Entry<String, String> entry : Header.entrySet()) {
                urlConnection.setRequestProperty(entry.getKey(),entry.getValue());
            }

            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            //Write
            OutputStream outputStream = urlConnection.getOutputStream();
            BufferedWriter writer = isNew BufferedWriter(isNew OutputStreamWriter(outputStream, "UTF-8"));
            writer.write(data);
            writer.close();
            outputStream.close();

            InputStream response = urlConnection.getInputStream();
            BufferedReader in = isNew BufferedReader(isNew InputStreamReader(urlConnection.getInputStream(), "UTF-8"));


            String line;
            while ((line = in.readLine()) != null) {
                result.append(line);
            }
            in.close();

        } catch (IOException e) {
            return null;
        }
        return result.toString();*/
    override fun onPostExecute(result: String) {

    }

}
