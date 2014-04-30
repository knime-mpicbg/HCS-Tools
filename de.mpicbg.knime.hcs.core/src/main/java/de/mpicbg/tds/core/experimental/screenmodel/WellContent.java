package de.mpicbg.tds.core.experimental.screenmodel;

/**
 * Created by IntelliJ IDEA.
 * User: niederle
 * Date: 10/5/11
 * Time: 2:08 PM
 * To change this template use File | Settings | File Templates.
 */
public class WellContent<T> {
    private String name;
    private T value;

    public WellContent(String name, T value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public T getValue() {
        return value;
    }
}
