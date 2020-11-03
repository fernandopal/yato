package es.fernandopal.yato.files;

import java.util.List;

public interface IConfigFile {
    List<?> getList(String key);
    String getString(String key);
    Integer getInt(String key);
}
