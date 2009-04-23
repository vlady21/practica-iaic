package jess;

import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.reflect.Method;

/**
 * We use this in deftemplates so that we can serialize an engine containing
 * defclasses.
 */

class SerializablePD implements Serializable {
    private String m_class, m_property;
    private transient Method m_get, m_set;

    SerializablePD(Class c, PropertyDescriptor pd) {
        m_class = c.getName();
        m_property = pd.getName();
        m_get = pd.getReadMethod();
        m_set = pd.getWriteMethod();
    }

    private void reload(Rete engine) throws JessException {
        try {
            Class c = engine.findClass(m_class);
            PropertyDescriptor[] pd = ReflectFunctions.getPropertyDescriptors(c);
            for (int i=0; i<pd.length; i++)
                if (pd[i].getName().equals(m_property)) {
                    m_get = pd[i].getReadMethod();
                    m_set = pd[i].getWriteMethod();
                    return;
                }

        } catch (Exception e) {
            throw new JessException("SerializablePD.reload",
                                    "Can't recreate property", e);
        }
    }

    String getName() { return m_property; }


    Method getReadMethod(Rete engine) throws JessException {
        if (m_get == null)
            reload(engine);
        return m_get;
    }

    Method getWriteMethod(Rete engine) throws JessException {
        if (m_set == null)
            reload(engine);
        return m_set;
    }

    public boolean equals(Object o) {
        if (!(o instanceof SerializablePD))
            return false;

        SerializablePD pd = (SerializablePD) o;

        return m_class.equals(pd.m_class) &&
            m_property.equals(pd.m_property);
    }
}

