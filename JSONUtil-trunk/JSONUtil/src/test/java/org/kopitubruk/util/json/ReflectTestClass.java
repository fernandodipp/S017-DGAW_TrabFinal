package org.kopitubruk.util.json;

import java.util.ArrayList;
import java.util.List;

/**
 * Class used by reflection tests.
 *
 * @author Bill Davidson
 */
class ReflectTestClass
{
    static final String SOME_STATIC_DATA = "some static data";
    transient int someTransientData = 4;

    private int a = 1;
    private String b = "something";
    private ArrayList<Long> c = new ArrayList<>();
    @SuppressWarnings("unused")
    private List<Short> d = null;
    private boolean f = true;

    /**
     * Get a
     *
     * @return a
     */
    public Integer getA()
    {
        return a;
    }

    /**
     * Get b
     *
     * @return b
     */
    protected String getB()
    {
        return b;
    }

    List<Long> getC()
    {
        return c;
    }

    @SuppressWarnings("unused")
    private Double getE()
    {
        return new Double(25);
    }

    public boolean isF()
    {
        return f;
    }
}
