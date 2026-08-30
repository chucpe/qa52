package lesson2_8.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EchoResponse {
    private Map<String, Object> args;
    private Map<String, Object> headers;
    private String url;
    private Map<String, Object> data;
    private Map<String, Object> json;
    private Map<String, Object> form;

    private Map<String, Object> files;

    // Геттеры и сеттеры
    public Map<String, Object> getArgs() { return args; }
    public void setArgs(Map<String, Object> args) { this.args = args; }

    public Map<String, Object> getHeaders() { return headers; }
    public void setHeaders(Map<String, Object> headers) { this.headers = headers; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public Map<String, Object> getData() { return data; }
    public void setData(Map<String, Object> data) { this.data = data; }

    public Map<String, Object> getJson() { return json; }
    public void setJson(Map<String, Object> json) { this.json = json; }

    public Map<String, Object> getForm() { return form; }
    public void setForm(Map<String, Object> form) { this.form = form; }

    public Map<String, Object> getFiles() { return files; }
    public void setFiles(Map<String, Object> files) { this.files = files; }

    @Override
    public String toString() {
        return "EchoResponse{" +
                "args=" + args +
                ", headers=" + headers +
                ", url='" + url + '\'' +
                ", data=" + data +
                ", json=" + json +
                ", form=" + form +
                ", files=" + files +
                '}';
    }
}