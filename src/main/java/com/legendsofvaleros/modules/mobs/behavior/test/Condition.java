package com.legendsofvaleros.modules.mobs.behavior.test;

public class Condition {
	public static ITest and(ITest...tests) {
		return ce -> {
            for(ITest test : tests)
                if(!test.isSuccess(ce))
                    return false;
            return true;
        };
	}

	public static ITest or(ITest...tests) {
		return ce -> {
            for(ITest test : tests)
                if(test.isSuccess(ce))
                    return true;
            return false;
        };
	}

	public static ITest xor(ITest...tests) {
		return ce -> {
            boolean hasOne = false;
            for(ITest test : tests)
                if(test.isSuccess(ce)) {
                    if(hasOne)
                        return false;
                    hasOne = true;
                }
            return hasOne;
        };
	}

	public static ITest not(ITest test) {
		return ce -> !test.isSuccess(ce);
	}
}