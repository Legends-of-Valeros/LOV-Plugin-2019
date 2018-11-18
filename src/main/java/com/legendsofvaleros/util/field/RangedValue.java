package com.legendsofvaleros.util.field;

import com.google.gson.JsonDeserializer;

import java.util.Random;

public class RangedValue {
	public static final JsonDeserializer<RangedValue> JSON = (json, typeOfT, context) -> {
        RangedValue value = new RangedValue();

        String val = json.getAsString().replace(",", ".");

        // Sub off any potential negative signs
        if(val.substring(1).contains("-")) {
            String left, right;
            if(val.startsWith("-"))
                left = "-" + val.substring(1).split("-", 2)[0];
            else
                left = val.split("-", 2)[0];

            if(val.startsWith("-"))
                right = val.substring(1).split("-", 2)[1];
            else
                right = val.split("-", 2)[1];

            value.min = Double.parseDouble(left);
            value.max = Double.parseDouble(right);
        }else
            value.min = value.max = Double.parseDouble(val);

        return value;
    };
	
	private static final Random RAND = new Random();
	
	double min, max;

	RangedValue() { }
	public RangedValue(double min, double max) {
		this.min = min;
		this.max = max;
	}
	
	public int intValue() {
		if(min == max) return (int)min;
		if(min > max) return (int)min;
		return RAND.nextInt((int)(max - min)) + (int)min;
	}

	public double doubleValue() {
		if(min == max) return min;
		if(min > max) return min;
		return RAND.nextDouble() * (max - min) + min;
	}

	public float floatValue() {
		if(min == max) return (float)min;
		if(min > max) return (float)min;
		return RAND.nextFloat() * (float)(max - min) + (float)min;
	}
	
	public long longValue() {
		return (long)intValue();
	}
}