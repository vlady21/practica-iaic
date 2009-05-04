package jess;

import java.io.Serializable;
import java.util.HashMap;

/** **********************************************************************
 * Mathematical functions for Jess. The original versions of many of these
 * were contributed by user Win Carus.
 *
 *
 * @author Win Carus (C)1997
 * @author Ernest J. Friedman-Hill
 * */

class MathFunctions implements IntrinsicPackage, Serializable {
    private void addFunction(Userfunction uf, HashMap ht) {
        ht.put(uf.getName(), uf);
    }

    public void add(HashMap table) {
        // abs added by Win Carus (9.19.97)
        addFunction(new Abs(), table);
        // div added by Win Carus (9.19.97)
        addFunction(new Div(), table);
        // float added by Win Carus (9.19.97)
        addFunction(new JessFloat(), table);
        // integer added by Win Carus (9.19.97)
        addFunction(new JessInteger(), table);
        // max added by Win Carus (9.19.97)
        addFunction(new Max(), table);
        // min added by Win Carus (9.19.97)
        addFunction(new Min(), table);
        // ** added by Win Carus (9.19.97)
        addFunction(new Expt(), table);
        // exp added by Win Carus (9.19.97)
        addFunction(new Exp(), table);
        // log added by Win Carus (9.19.97)
        addFunction(new Log(), table);
        // log10 added by Win Carus (9.19.97)
        addFunction(new Log10(), table);
        addFunction(new Constant("pi", Math.PI), table);
        addFunction(new Constant("e", Math.E), table);
        // round added by Win Carus (9.19.97)
        addFunction(new Round(), table);
        // sqrt added by Win Carus (9.19.97)
        addFunction(new Sqrt(), table);
        // random added by Win Carus (9.19.97)
        addFunction(new JessRandom(), table);
    }
}

class Abs implements Userfunction, Serializable {
    public String getName() {
        return "abs";
    }

    public Value call(ValueVector vv, Context context) throws JessException {
        Value v = vv.get(1).resolveValue(context);
        return new Value(Math.abs(v.numericValue(context)), v.type());
    }
}

class Div implements Userfunction, Serializable {
    public String getName() {
        return "div";
    }

    public Value call(ValueVector vv, Context context) throws JessException {
        int first = (int) vv.get(1).numericValue(context);
        int second = (int) vv.get(2).numericValue(context);

        return new Value((first / second), RU.INTEGER);
    }
}

class JessFloat implements Userfunction, Serializable {
    public String getName() {
        return "float";
    }

    public Value call(ValueVector vv, Context context) throws JessException {
        return new Value(vv.get(1).numericValue(context), RU.FLOAT);
    }
}

class JessInteger implements Userfunction, Serializable {
    public String getName() {
        return "integer";
    }

    public Value call(ValueVector vv, Context context) throws JessException {
        return new Value((int) vv.get(1).numericValue(context), RU.INTEGER);
    }
}

class Max implements Userfunction, Serializable {
    public String getName() {
        return "max";
    }

    public Value call(ValueVector vv, Context context) throws JessException {
        Value v1 = vv.get(1).resolveValue(context);
        int type = v1.type();
        double max = v1.numericValue(context);

        for (int i = 2; i < vv.size(); i++) {
            Value v2 = vv.get(i).resolveValue(context);
            if (v2.type() == RU.FLOAT) type = RU.FLOAT;
            double n = v2.numericValue(context);
            if (n > max)
                max = n;
        }

        return new Value(max, type);
    }
}

class Min implements Userfunction, Serializable {
    public String getName() {
        return "min";
    }

    public Value call(ValueVector vv, Context context) throws JessException {
        Value v1 = vv.get(1).resolveValue(context);
        int type = v1.type();
        double min = v1.numericValue(context);

        for (int i = 2; i < vv.size(); i++) {
            Value v2 = vv.get(i).resolveValue(context);
            if (v2.type() == RU.FLOAT) type = RU.FLOAT;
            double n = v2.numericValue(context);
            if (n < min)
                min = n;
        }

        return new Value(min, type);
    }
}

class Expt implements Userfunction, Serializable {
    public String getName() {
        return "**";
    }

    public Value call(ValueVector vv, Context context) throws JessException {
        return new Value(Math.pow(vv.get(1).numericValue(context), vv.get(2).numericValue(context)), RU.FLOAT);
    }
}

class Exp implements Userfunction, Serializable {
    public String getName() {
        return "exp";
    }

    public Value call(ValueVector vv, Context context) throws JessException {
        return new Value(Math.pow(Math.E, vv.get(1).numericValue(context)), RU.FLOAT);
    }
}

class Log implements Userfunction, Serializable {
    public String getName() {
        return "log";
    }

    public Value call(ValueVector vv, Context context) throws JessException {
        return new Value(Math.log(vv.get(1).numericValue(context)), RU.FLOAT);
    }
}

class Log10 implements Userfunction, Serializable {

    private static final double log10 = Math.log(10.0);

    public String getName() {
        return "log10";
    }

    public Value call(ValueVector vv, Context context) throws JessException {
        return new Value((Math.log(vv.get(1).numericValue(context)) / log10), RU.FLOAT);
    }
}

class Constant implements Userfunction, Serializable {
    private Value m_val;
    private String m_name;

    Constant(String name, double value) {
        m_name = name;
        try {
            m_val = new Value(value, RU.FLOAT);
        } catch (JessException re) {
        }
    }

    public String getName() {
        return m_name;
    }

    public Value call(ValueVector vv, Context context) throws JessException {
        return m_val;
    }
}

class Round implements Userfunction, Serializable {
    public String getName() {
        return "round";
    }

    public Value call(ValueVector vv, Context context) throws JessException {
        return new Value(Math.round(vv.get(1).numericValue(context)), RU.INTEGER);
    }
}

class Sqrt implements Userfunction, Serializable {
    public String getName() {
        return "sqrt";
    }

    public Value call(ValueVector vv, Context context) throws JessException {
        return new Value(Math.sqrt(vv.get(1).numericValue(context)), RU.FLOAT);
    }
}

class JessRandom implements Userfunction, Serializable {
    public String getName() {
        return "random";
    }

    public Value call(ValueVector vv, Context context) throws JessException {
        return new Value((int) (Math.random() * 65536), RU.INTEGER);
    }
}

